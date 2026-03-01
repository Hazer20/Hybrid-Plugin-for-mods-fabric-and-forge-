"""Utility helpers for Minecraft pack migration.

This module intentionally centralizes lower-level reusable operations used by
both datapack and resourcepack update workflows.
"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
import json
import shutil
from pathlib import Path
from typing import Dict, Iterator, List, Mapping, Optional, Sequence, Tuple
from zipfile import ZIP_DEFLATED, ZipFile

from .logger import UpdateLogger


DATAPACK_FORMAT_1_21_8 = 61
RESOURCEPACK_FORMAT_1_21_8 = 46


@dataclass
class ProjectPaths:
    root: Path
    datapacks: Path
    old_datapack: Path
    new_datapack: Path
    resourcepacks: Path
    old_resourcepack: Path
    new_resourcepack: Path
    logs: Path
    config: Path


@dataclass
class ValidationIssue:
    severity: str
    message: str
    path: Optional[Path] = None


def ensure_project_structure(root: Path, logger: UpdateLogger, dry_run: bool = False) -> ProjectPaths:
    datapacks = root / "datapacks"
    old_datapack = datapacks / "old_datapack"
    new_datapack = datapacks / "new_datapack"

    resourcepacks = root / "resourcepacks"
    old_resourcepack = resourcepacks / "old_resourcepack"
    new_resourcepack = resourcepacks / "new_resourcepack"

    logs = root / "logs"
    config = root / "config"

    dirs = [
        root,
        datapacks,
        old_datapack,
        new_datapack,
        resourcepacks,
        old_resourcepack,
        new_resourcepack,
        logs,
        config,
    ]

    for directory in dirs:
        if dry_run:
            logger.info(f"[dry-run] Would ensure directory exists: {directory}")
        else:
            directory.mkdir(parents=True, exist_ok=True)
            logger.info(f"Ensured directory exists: {directory}")

    write_pack_mcmeta(
        pack_dir=new_datapack,
        pack_format=DATAPACK_FORMAT_1_21_8,
        description="Converted datapack for Minecraft 1.21.8",
        logger=logger,
        dry_run=dry_run,
    )
    write_pack_mcmeta(
        pack_dir=new_resourcepack,
        pack_format=RESOURCEPACK_FORMAT_1_21_8,
        description="Converted resource pack for Minecraft 1.21.8",
        logger=logger,
        dry_run=dry_run,
    )

    return ProjectPaths(
        root=root,
        datapacks=datapacks,
        old_datapack=old_datapack,
        new_datapack=new_datapack,
        resourcepacks=resourcepacks,
        old_resourcepack=old_resourcepack,
        new_resourcepack=new_resourcepack,
        logs=logs,
        config=config,
    )


def write_pack_mcmeta(
    pack_dir: Path,
    pack_format: int,
    description: str,
    logger: UpdateLogger,
    dry_run: bool = False,
) -> None:
    mcmeta = {"pack": {"pack_format": pack_format, "description": description}}
    path = pack_dir / "pack.mcmeta"
    if dry_run:
        logger.info(f"[dry-run] Would write pack metadata file: {path}")
        return
    pack_dir.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(mcmeta, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    logger.info(f"Wrote pack metadata: {path}")


def timestamp_for_backup() -> str:
    return datetime.now().strftime("%Y%m%d_%H%M%S")


def create_backup(source: Path, backup_root: Path, logger: UpdateLogger, dry_run: bool = False) -> Optional[Path]:
    if not source.exists():
        logger.warning(f"Backup skipped because source path does not exist: {source}")
        return None

    backup_root = backup_root / "backups"
    target = backup_root / f"{source.stem}_{timestamp_for_backup()}{source.suffix if source.is_file() else ''}"

    if not dry_run:
        counter = 1
        while target.exists():
            target = backup_root / f"{source.stem}_{timestamp_for_backup()}_{counter}{source.suffix if source.is_file() else ''}"
            counter += 1

    if dry_run:
        logger.info(f"[dry-run] Would create backup: {source} -> {target}")
        return target

    backup_root.mkdir(parents=True, exist_ok=True)
    if source.is_dir():
        shutil.copytree(source, target)
    else:
        shutil.copy2(source, target)
    logger.info(f"Created backup: {source} -> {target}")
    return target


def extract_zip_to_dir(zip_path: Path, destination: Path, logger: UpdateLogger, dry_run: bool = False) -> Path:
    """Extract zip archive to destination folder."""
    if dry_run:
        logger.info(f"[dry-run] Would extract zip: {zip_path} -> {destination}")
        return destination
    destination.mkdir(parents=True, exist_ok=True)
    with ZipFile(zip_path, "r") as zf:
        zf.extractall(destination)
    logger.info(f"Extracted zip: {zip_path} -> {destination}")
    return destination


def zip_dir(source_dir: Path, zip_path: Path, logger: UpdateLogger, dry_run: bool = False) -> Path:
    """Pack a directory into zip archive preserving relative paths."""
    if dry_run:
        logger.info(f"[dry-run] Would create zip: {source_dir} -> {zip_path}")
        return zip_path
    zip_path.parent.mkdir(parents=True, exist_ok=True)
    with ZipFile(zip_path, "w", compression=ZIP_DEFLATED) as zf:
        for file_path in source_dir.rglob("*"):
            if file_path.is_file():
                zf.write(file_path, arcname=file_path.relative_to(source_dir))
    logger.info(f"Created zip: {source_dir} -> {zip_path}")
    return zip_path


def iter_files_by_extension(root: Path, extensions: Sequence[str]) -> Iterator[Path]:
    normalized = {ext.lower() for ext in extensions}
    for path in root.rglob("*"):
        if path.is_file() and path.suffix.lower() in normalized:
            yield path


def read_json_file(path: Path, logger: UpdateLogger) -> Optional[dict]:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001
        logger.error(f"Failed to parse JSON file {path}: {exc}")
        return None


def write_json_file(path: Path, payload: Mapping, logger: UpdateLogger, dry_run: bool = False) -> None:
    if dry_run:
        logger.info(f"[dry-run] Would write JSON file: {path}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    logger.info(f"Wrote JSON file: {path}")


def copy_tree(src: Path, dst: Path, logger: UpdateLogger, dry_run: bool = False) -> None:
    if dry_run:
        logger.info(f"[dry-run] Would copy tree {src} -> {dst}")
        return
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(src, dst)
    logger.info(f"Copied tree {src} -> {dst}")


def copy_file(src: Path, dst: Path, logger: UpdateLogger, dry_run: bool = False) -> None:
    if dry_run:
        logger.info(f"[dry-run] Would copy file {src} -> {dst}")
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    logger.info(f"Copied file {src} -> {dst}")


def write_text_file(path: Path, text: str, logger: UpdateLogger, dry_run: bool = False) -> None:
    if dry_run:
        logger.info(f"[dry-run] Would write text file: {path}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    logger.info(f"Wrote text file: {path}")


def apply_text_replacements(text: str, replacements: Mapping[str, str]) -> Tuple[str, int]:
    count = 0
    out = text
    for old, new in replacements.items():
        if old in out:
            before = out
            out = out.replace(old, new)
            if out != before:
                count += before.count(old)
    return out, count


def validate_datapack_structure(root: Path) -> List[ValidationIssue]:
    issues: List[ValidationIssue] = []
    if not root.exists():
        issues.append(ValidationIssue("error", "Datapack root does not exist", root))
        return issues
    if not (root / "pack.mcmeta").exists():
        issues.append(ValidationIssue("warning", "Missing pack.mcmeta", root / "pack.mcmeta"))
    data_dir = root / "data"
    if not data_dir.exists():
        issues.append(ValidationIssue("error", "Missing data directory", data_dir))
        return issues
    namespaces = [p for p in data_dir.iterdir() if p.is_dir()]
    if not namespaces:
        issues.append(ValidationIssue("warning", "No namespaces found in data/", data_dir))
    required_common = ["advancements", "loot_tables", "recipes", "tags", "functions"]
    for namespace in namespaces:
        for name in required_common:
            candidate = namespace / name
            if not candidate.exists():
                issues.append(ValidationIssue("info", f"Namespace '{namespace.name}' missing optional folder '{name}'", candidate))
    return issues


def validate_resourcepack_structure(root: Path) -> List[ValidationIssue]:
    issues: List[ValidationIssue] = []
    if not root.exists():
        issues.append(ValidationIssue("error", "Resource pack root does not exist", root))
        return issues
    if not (root / "pack.mcmeta").exists():
        issues.append(ValidationIssue("warning", "Missing pack.mcmeta", root / "pack.mcmeta"))
    assets_dir = root / "assets"
    if not assets_dir.exists():
        issues.append(ValidationIssue("error", "Missing assets directory", assets_dir))
        return issues
    minecraft_assets = assets_dir / "minecraft"
    if not minecraft_assets.exists():
        issues.append(ValidationIssue("warning", "Missing assets/minecraft namespace (pack may be modded namespace only)", minecraft_assets))
    return issues


def relative_to(path: Path, root: Path) -> str:
    try:
        return str(path.relative_to(root))
    except ValueError:
        return str(path)


def split_multi_values(items: Sequence[str]) -> List[str]:
    output: List[str] = []
    for item in items:
        for piece in item.split(","):
            piece = piece.strip()
            if piece:
                output.append(piece)
    return output


__all__ = [
    "DATAPACK_FORMAT_1_21_8",
    "RESOURCEPACK_FORMAT_1_21_8",
    "ProjectPaths",
    "ValidationIssue",
    "ensure_project_structure",
    "write_pack_mcmeta",
    "timestamp_for_backup",
    "create_backup",
    "extract_zip_to_dir",
    "zip_dir",
    "iter_files_by_extension",
    "read_json_file",
    "write_json_file",
    "copy_tree",
    "copy_file",
    "write_text_file",
    "apply_text_replacements",
    "validate_datapack_structure",
    "validate_resourcepack_structure",
    "relative_to",
    "split_multi_values",
]
