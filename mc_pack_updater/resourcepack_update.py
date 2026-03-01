"""Resourcepack conversion workflow for Minecraft 1.20.4 -> 1.21.8.

The updater performs pragmatic filesystem and data transforms:
- copy source pack to destination;
- rewrite `pack.mcmeta` to target format;
- migrate legacy `.lang` files to `.json` language format;
- update paths and file names known to have changed;
- rewrite JSON metadata and model files to updated identifiers.
"""

from __future__ import annotations

from dataclasses import dataclass, field
import json
from pathlib import Path
from typing import Dict, List, MutableMapping, Optional, Sequence, Tuple

from .logger import UpdateLogger
from .utils import (
    RESOURCEPACK_FORMAT_1_21_8,
    apply_text_replacements,
    copy_file,
    copy_tree,
    iter_files_by_extension,
    read_json_file,
    relative_to,
    validate_resourcepack_structure,
    write_json_file,
    write_pack_mcmeta,
    write_text_file,
)


RESOURCE_TEXT_REPLACEMENTS: Dict[str, str] = {
    "textures/blocks/": "textures/block/",
    "textures/items/": "textures/item/",
    "minecraft:grass_path": "minecraft:dirt_path",
}

RESOURCE_PATH_RENAMES: Dict[str, str] = {
    "assets/minecraft/textures/blocks": "assets/minecraft/textures/block",
    "assets/minecraft/textures/items": "assets/minecraft/textures/item",
    "assets/minecraft/models/block/grass_path.json": "assets/minecraft/models/block/dirt_path.json",
    "assets/minecraft/models/item/grass_path.json": "assets/minecraft/models/item/dirt_path.json",
}


@dataclass
class ResourcepackStats:
    """Aggregated counters for resourcepack conversion."""

    files_scanned: int = 0
    json_files_rewritten: int = 0
    lang_files_converted: int = 0
    text_files_rewritten: int = 0
    renamed_paths: int = 0
    warnings: int = 0


@dataclass
class ResourcepackUpdateResult:
    """Result object returned by resourcepack updater."""

    source: Path
    destination: Path
    stats: ResourcepackStats = field(default_factory=ResourcepackStats)
    validation_issues: List[str] = field(default_factory=list)


class ResourcepackUpdater:
    """Convert resourcepack content to modernized structure."""

    def __init__(self, logger: UpdateLogger, dry_run: bool = False) -> None:
        self.logger = logger
        self.dry_run = dry_run

    def update(self, source: Path, destination: Path) -> ResourcepackUpdateResult:
        """Run complete resourcepack conversion pipeline."""
        result = ResourcepackUpdateResult(source=source, destination=destination)

        issues = validate_resourcepack_structure(source)
        for issue in issues:
            message = f"[{issue.severity}] {issue.message} ({issue.path})"
            result.validation_issues.append(message)
            if issue.severity == "error":
                self.logger.error(message)
                result.stats.warnings += 1
            elif issue.severity == "warning":
                self.logger.warning(message)
                result.stats.warnings += 1
            else:
                self.logger.info(message)

        copy_tree(source, destination, logger=self.logger, dry_run=self.dry_run)

        write_pack_mcmeta(
            pack_dir=destination,
            pack_format=RESOURCEPACK_FORMAT_1_21_8,
            description="Updated resource pack for Minecraft 1.21.8",
            logger=self.logger,
            dry_run=self.dry_run,
        )

        self._rename_legacy_paths(destination, result)
        self._convert_language_files(destination, result)
        self._rewrite_json_and_text(destination, result)

        self.logger.info(
            "Resourcepack conversion finished: "
            f"files_scanned={result.stats.files_scanned}, "
            f"json_rewritten={result.stats.json_files_rewritten}, "
            f"lang_converted={result.stats.lang_files_converted}, "
            f"text_rewritten={result.stats.text_files_rewritten}, "
            f"renamed_paths={result.stats.renamed_paths}"
        )

        return result

    def _rename_legacy_paths(self, root: Path, result: ResourcepackUpdateResult) -> None:
        """Rename known legacy directories/files to current layout."""
        for old_rel, new_rel in RESOURCE_PATH_RENAMES.items():
            old_path = root / old_rel
            new_path = root / new_rel
            if not old_path.exists():
                continue

            if self.dry_run:
                self.logger.info(f"[dry-run] Would rename {old_path} -> {new_path}")
                result.stats.renamed_paths += 1
                continue

            new_path.parent.mkdir(parents=True, exist_ok=True)
            old_path.rename(new_path)
            self.logger.info(f"Renamed path {old_path} -> {new_path}")
            result.stats.renamed_paths += 1

    def _convert_language_files(self, root: Path, result: ResourcepackUpdateResult) -> None:
        """Convert `.lang` language files to `.json` modern equivalent.

        Legacy format:
            key=value

        New format:
            {
              "key": "value"
            }
        """
        for lang_path in iter_files_by_extension(root, [".lang"]):
            result.stats.files_scanned += 1
            payload = self._parse_lang_file(lang_path)
            if payload is None:
                result.stats.warnings += 1
                continue

            json_path = lang_path.with_suffix(".json")
            if self.dry_run:
                self.logger.info(f"[dry-run] Would convert lang file {lang_path} -> {json_path}")
                result.stats.lang_files_converted += 1
                continue

            write_json_file(json_path, payload, logger=self.logger, dry_run=False)
            try:
                lang_path.unlink()
            except Exception as exc:  # noqa: BLE001
                self.logger.warning(f"Failed to delete legacy lang file {lang_path}: {exc}")
                result.stats.warnings += 1
            else:
                self.logger.info(f"Converted lang file {lang_path} -> {json_path}")
                result.stats.lang_files_converted += 1

    def _parse_lang_file(self, path: Path) -> Optional[Dict[str, str]]:
        """Parse Minecraft legacy .lang file."""
        try:
            lines = path.read_text(encoding="utf-8").splitlines()
        except Exception as exc:  # noqa: BLE001
            self.logger.error(f"Failed to read lang file {path}: {exc}")
            return None

        output: Dict[str, str] = {}
        for idx, line in enumerate(lines, start=1):
            stripped = line.strip()
            if not stripped or stripped.startswith("#"):
                continue
            if "=" not in stripped:
                self.logger.warning(f"Malformed lang line {path}:{idx} (missing '='): {line}")
                continue
            key, value = stripped.split("=", 1)
            output[key.strip()] = value.strip()

        return output

    def _rewrite_json_and_text(self, root: Path, result: ResourcepackUpdateResult) -> None:
        """Apply replacements in JSON and supported text resources."""
        for file_path in root.rglob("*"):
            if not file_path.is_file():
                continue
            result.stats.files_scanned += 1

            suffix = file_path.suffix.lower()
            if suffix == ".json":
                payload = read_json_file(file_path, self.logger)
                if payload is None:
                    result.stats.warnings += 1
                    continue
                changed = self._rewrite_json_payload(payload)
                if changed:
                    write_json_file(file_path, payload, logger=self.logger, dry_run=self.dry_run)
                    result.stats.json_files_rewritten += 1
            elif suffix in {".mcmeta", ".txt", ".properties"}:
                original = file_path.read_text(encoding="utf-8")
                updated, count = apply_text_replacements(original, RESOURCE_TEXT_REPLACEMENTS)
                if count > 0:
                    write_text_file(file_path, updated, logger=self.logger, dry_run=self.dry_run)
                    result.stats.text_files_rewritten += 1

    def _rewrite_json_payload(self, payload: MutableMapping) -> bool:
        """Recursively update JSON identifiers and paths."""
        changed = False

        def walk(node):
            nonlocal changed
            if isinstance(node, dict):
                for key, value in list(node.items()):
                    if isinstance(value, str):
                        new_value, local_changes = apply_text_replacements(value, RESOURCE_TEXT_REPLACEMENTS)
                        if local_changes > 0:
                            node[key] = new_value
                            changed = True
                    elif isinstance(value, (dict, list)):
                        walk(value)
            elif isinstance(node, list):
                for idx, value in enumerate(node):
                    if isinstance(value, str):
                        new_value, local_changes = apply_text_replacements(value, RESOURCE_TEXT_REPLACEMENTS)
                        if local_changes > 0:
                            node[idx] = new_value
                            changed = True
                    elif isinstance(value, (dict, list)):
                        walk(value)

        walk(payload)
        return changed


def update_multiple_resourcepacks(
    sources: Sequence[Path],
    destination_root: Path,
    logger: UpdateLogger,
    dry_run: bool = False,
) -> List[ResourcepackUpdateResult]:
    """Batch conversion for multiple resource packs."""
    updater = ResourcepackUpdater(logger=logger, dry_run=dry_run)
    results: List[ResourcepackUpdateResult] = []
    for src in sources:
        dst = destination_root / src.name
        logger.info(f"Starting resourcepack conversion for {src} -> {dst}")
        results.append(updater.update(src, dst))
    return results


__all__ = [
    "ResourcepackStats",
    "ResourcepackUpdateResult",
    "ResourcepackUpdater",
    "update_multiple_resourcepacks",
]
