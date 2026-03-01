"""Command line interface for Minecraft pack updater.

Usage example:
    python update_mc_pack.py \
        --datapack path/to/old_datapack \
        --resourcepack path/to/old_resourcepack \
        --output ./root \
        --backup --log

The CLI supports multi-value options by repeating arguments or using comma
separated lists.
"""

from __future__ import annotations

import argparse
from pathlib import Path
from typing import List, Sequence

from .datapack_update import update_multiple_datapacks
from .logger import UpdateLogger, create_logger
from .resourcepack_update import update_multiple_resourcepacks
from .utils import (
    create_backup,
    ensure_project_structure,
    split_multi_values,
)


class CLIExitCodes:
    """Simple constants for command exit status."""

    OK = 0
    INVALID_ARGUMENTS = 2
    RUNTIME_ERROR = 3


def build_parser() -> argparse.ArgumentParser:
    """Build argument parser for CLI entrypoint."""
    parser = argparse.ArgumentParser(
        prog="update_mc_pack.py",
        description=(
            "Automatic converter of Minecraft datapacks/resourcepacks "
            "from 1.20.4 to 1.21.8"
        ),
    )

    parser.add_argument(
        "--datapack",
        action="append",
        default=[],
        help=(
            "Path(s) to old datapack folders. "
            "Repeat argument or provide comma separated list for multiple packs."
        ),
    )

    parser.add_argument(
        "--resourcepack",
        action="append",
        default=[],
        help=(
            "Path(s) to old resource pack folders. "
            "Repeat argument or provide comma separated list for multiple packs."
        ),
    )

    parser.add_argument(
        "--output",
        required=True,
        help="Output root path where project structure and converted packs are stored.",
    )

    parser.add_argument(
        "--backup",
        action="store_true",
        help="Create timestamped backups of source datapack/resourcepack before conversion.",
    )

    parser.add_argument(
        "--log",
        action="store_true",
        help="Enable console log output in addition to log files.",
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Simulate all actions without modifying files.",
    )

    parser.add_argument(
        "--debug",
        action="store_true",
        help="Enable debug-level logging for diagnostics.",
    )

    return parser


def run_cli(argv: Sequence[str] | None = None) -> int:
    """Run CLI workflow.

    Returns:
        Exit code integer.
    """

    parser = build_parser()
    args = parser.parse_args(argv)

    datapack_values = split_multi_values(args.datapack)
    resourcepack_values = split_multi_values(args.resourcepack)

    if not datapack_values and not resourcepack_values:
        parser.error("At least one --datapack or --resourcepack path must be provided.")
        return CLIExitCodes.INVALID_ARGUMENTS

    output_root = Path(args.output).resolve()

    bootstrap_logger = create_logger(
        logs_dir=output_root / "logs",
        enable_console=args.log,
        debug=args.debug,
    )

    try:
        paths = ensure_project_structure(output_root, logger=bootstrap_logger, dry_run=args.dry_run)

        datapack_sources = [Path(p).resolve() for p in datapack_values]
        resourcepack_sources = [Path(p).resolve() for p in resourcepack_values]

        if args.backup:
            _backup_sources(
                datapack_sources=datapack_sources,
                resourcepack_sources=resourcepack_sources,
                output_root=output_root,
                logger=bootstrap_logger,
                dry_run=args.dry_run,
            )

        if datapack_sources:
            dp_results = update_multiple_datapacks(
                sources=datapack_sources,
                destination_root=paths.new_datapack,
                logger=bootstrap_logger,
                dry_run=args.dry_run,
            )
            bootstrap_logger.info(f"Converted datapacks: {len(dp_results)}")

        if resourcepack_sources:
            rp_results = update_multiple_resourcepacks(
                sources=resourcepack_sources,
                destination_root=paths.new_resourcepack,
                logger=bootstrap_logger,
                dry_run=args.dry_run,
            )
            bootstrap_logger.info(f"Converted resourcepacks: {len(rp_results)}")

        summary = bootstrap_logger.summary()
        bootstrap_logger.info(
            "Run completed with summary: "
            f"debug={summary['DEBUG']} info={summary['INFO']} "
            f"warning={summary['WARNING']} error={summary['ERROR']}"
        )

        return CLIExitCodes.OK

    except Exception as exc:  # noqa: BLE001
        bootstrap_logger.exception(f"Unexpected runtime error: {exc}")
        return CLIExitCodes.RUNTIME_ERROR


def _backup_sources(
    datapack_sources: List[Path],
    resourcepack_sources: List[Path],
    output_root: Path,
    logger: UpdateLogger,
    dry_run: bool,
) -> None:
    """Create backups for all source packs."""
    for source in datapack_sources:
        create_backup(source=source, backup_root=output_root, logger=logger, dry_run=dry_run)
    for source in resourcepack_sources:
        create_backup(source=source, backup_root=output_root, logger=logger, dry_run=dry_run)


__all__ = ["build_parser", "run_cli", "CLIExitCodes"]
