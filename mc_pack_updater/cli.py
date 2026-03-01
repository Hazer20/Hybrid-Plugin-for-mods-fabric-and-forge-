"""Command line interface for Minecraft pack updater."""

from __future__ import annotations

import argparse
from pathlib import Path
from typing import List, Sequence, Tuple

from .ai_assistant import AIMigrationAssistant
from .datapack_update import update_multiple_datapacks
from .logger import UpdateLogger, create_logger
from .resourcepack_update import update_multiple_resourcepacks
from .utils import (
    create_backup,
    ensure_project_structure,
    extract_zip_to_dir,
    split_multi_values,
    timestamp_for_backup,
    zip_dir,
)


class CLIExitCodes:
    OK = 0
    INVALID_ARGUMENTS = 2
    RUNTIME_ERROR = 3


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="update_mc_pack.py",
        description="Automatic converter of Minecraft datapacks/resourcepacks from 1.20.4 to 1.21.8",
    )
    parser.add_argument("--datapack", action="append", default=[], help="Path(s) to old datapack folders or .zip archives.")
    parser.add_argument("--resourcepack", action="append", default=[], help="Path(s) to old resource pack folders or .zip archives.")
    parser.add_argument("--output", required=True, help="Output root path for converted packs.")
    parser.add_argument("--backup", action="store_true", help="Create timestamped backups before conversion.")
    parser.add_argument("--log", action="store_true", help="Enable console logging in addition to log files.")
    parser.add_argument("--dry-run", action="store_true", help="Simulate actions without file writes.")
    parser.add_argument("--debug", action="store_true", help="Enable debug logging.")
    parser.add_argument("--ai-assist", action="store_true", help="Enable AI-assisted post-conversion quality pass and report.")
    parser.add_argument("--ai-min-score", type=float, default=85.0, help="Minimum target score for AI assurance (default: 85).")
    return parser


def run_cli(argv: Sequence[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)

    datapack_values = split_multi_values(args.datapack)
    resourcepack_values = split_multi_values(args.resourcepack)

    if not datapack_values and not resourcepack_values:
        parser.error("At least one --datapack or --resourcepack path must be provided.")
        return CLIExitCodes.INVALID_ARGUMENTS

    output_root = Path(args.output).resolve()
    logger = create_logger(logs_dir=output_root / "logs", enable_console=args.log, debug=args.debug)

    try:
        paths = ensure_project_structure(output_root, logger=logger, dry_run=args.dry_run)

        raw_dp_sources = [Path(p).resolve() for p in datapack_values]
        raw_rp_sources = [Path(p).resolve() for p in resourcepack_values]

        dp_sources, dp_zip_origins = _resolve_sources_with_zip(
            raw_dp_sources,
            output_root=output_root,
            source_kind="datapack",
            logger=logger,
            dry_run=args.dry_run,
        )
        rp_sources, rp_zip_origins = _resolve_sources_with_zip(
            raw_rp_sources,
            output_root=output_root,
            source_kind="resourcepack",
            logger=logger,
            dry_run=args.dry_run,
        )

        if args.backup:
            _backup_sources(raw_dp_sources, raw_rp_sources, output_root, logger, args.dry_run)

        converted_dp_dirs: List[Path] = []
        converted_rp_dirs: List[Path] = []

        if dp_sources:
            dp_results = update_multiple_datapacks(dp_sources, paths.new_datapack, logger, args.dry_run)
            converted_dp_dirs = [item.destination for item in dp_results]
            logger.info(f"Converted datapacks: {len(dp_results)}")
            _zip_back_if_needed(dp_results=[(src, dst) for src, dst in zip(dp_sources, converted_dp_dirs)], zip_origins=dp_zip_origins, output_root=output_root, pack_kind="datapack", logger=logger, dry_run=args.dry_run)

        if rp_sources:
            rp_results = update_multiple_resourcepacks(rp_sources, paths.new_resourcepack, logger, args.dry_run)
            converted_rp_dirs = [item.destination for item in rp_results]
            logger.info(f"Converted resourcepacks: {len(rp_results)}")
            _zip_back_if_needed(dp_results=[(src, dst) for src, dst in zip(rp_sources, converted_rp_dirs)], zip_origins=rp_zip_origins, output_root=output_root, pack_kind="resourcepack", logger=logger, dry_run=args.dry_run)

        if args.ai_assist:
            logger.info("AI assist enabled: running post-conversion assurance pass")
            ai = AIMigrationAssistant(logger=logger, dry_run=args.dry_run)
            ai_result = ai.run(converted_dp_dirs, converted_rp_dirs, output_root, args.ai_min_score)
            logger.info(
                "AI assurance completed: "
                f"score={ai_result.confidence_score:.1f}, "
                f"fixed_files={ai_result.fixed_files}, replacements={ai_result.replacements}"
            )

        summary = logger.summary()
        logger.info(
            "Run completed with summary: "
            f"debug={summary['DEBUG']} info={summary['INFO']} "
            f"warning={summary['WARNING']} error={summary['ERROR']}"
        )
        return CLIExitCodes.OK

    except Exception as exc:  # noqa: BLE001
        logger.exception(f"Unexpected runtime error: {exc}")
        return CLIExitCodes.RUNTIME_ERROR


def _resolve_sources_with_zip(
    raw_sources: List[Path],
    output_root: Path,
    source_kind: str,
    logger: UpdateLogger,
    dry_run: bool,
) -> Tuple[List[Path], dict[Path, Path]]:
    """Return normalized sources; if source is .zip it gets extracted first.

    Returns:
        (resolved_sources, zip_origin_by_extracted_path)
    """
    resolved: List[Path] = []
    zip_origins: dict[Path, Path] = {}
    work_root = output_root / "_work" / "extracted" / source_kind

    for source in raw_sources:
        if source.suffix.lower() == ".zip":
            extract_dir = work_root / f"{source.stem}_{timestamp_for_backup()}"
            extract_zip_to_dir(source, extract_dir, logger=logger, dry_run=dry_run)
            resolved.append(extract_dir)
            zip_origins[extract_dir] = source
            logger.info(f"Using extracted {source_kind} source: {extract_dir}")
        else:
            resolved.append(source)
    return resolved, zip_origins


def _zip_back_if_needed(
    dp_results: List[Tuple[Path, Path]],
    zip_origins: dict[Path, Path],
    output_root: Path,
    pack_kind: str,
    logger: UpdateLogger,
    dry_run: bool,
) -> None:
    """For sources that came from zip, create converted zip outputs."""
    zip_output_dir = output_root / "zipped_results" / pack_kind
    for original_source, converted_dir in dp_results:
        if original_source not in zip_origins:
            continue
        source_zip = zip_origins[original_source]
        zip_name = f"{source_zip.stem}_updated_1_21_8.zip"
        zip_target = zip_output_dir / zip_name
        zip_dir(converted_dir, zip_target, logger=logger, dry_run=dry_run)


def _backup_sources(
    datapack_sources: List[Path],
    resourcepack_sources: List[Path],
    output_root: Path,
    logger: UpdateLogger,
    dry_run: bool,
) -> None:
    for source in datapack_sources:
        create_backup(source=source, backup_root=output_root, logger=logger, dry_run=dry_run)
    for source in resourcepack_sources:
        create_backup(source=source, backup_root=output_root, logger=logger, dry_run=dry_run)


__all__ = ["build_parser", "run_cli", "CLIExitCodes"]
