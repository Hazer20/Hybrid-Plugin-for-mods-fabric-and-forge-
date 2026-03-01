"""Datapack conversion workflow.

This module provides a structured conversion pipeline for Minecraft datapacks.
It is designed to transform datapack content authored for Minecraft 1.20.4 into
format expected by Minecraft 1.21.8.

Disclaimer
----------
Minecraft snapshots and releases can include subtle format changes that are
hard to infer automatically. The conversion implemented here uses a curated set
of deterministic rewrites and extensive logging so maintainers can review and
extend behavior over time.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Mapping, MutableMapping, Optional, Sequence, Tuple

from .logger import UpdateLogger
from .utils import (
    DATAPACK_FORMAT_1_21_8,
    apply_text_replacements,
    copy_tree,
    iter_files_by_extension,
    read_json_file,
    relative_to,
    validate_datapack_structure,
    write_json_file,
    write_pack_mcmeta,
    write_text_file,
)


MCFUNCTION_REPLACEMENTS: Dict[str, str] = {
    "execute if block ~ ~-1 ~ minecraft:grass_path": "execute if block ~ ~-1 ~ minecraft:dirt_path",
    "setblock ~ ~ ~ minecraft:grass_path": "setblock ~ ~ ~ minecraft:dirt_path",
    "replaceitem entity": "item replace entity",
    "replaceitem block": "item replace block",
    "/function": "function",
    "minecraft:flowing_water": "minecraft:water",
    "minecraft:flowing_lava": "minecraft:lava",
}

ITEM_BLOCK_ID_REPLACEMENTS: Dict[str, str] = {
    "minecraft:grass_path": "minecraft:dirt_path",
    "minecraft:scute": "minecraft:turtle_scute",
}

TAG_NAME_REPLACEMENTS: Dict[str, str] = {
    "minecraft:tools": "minecraft:breaks_decorated_pots",
    "minecraft:music_discs": "minecraft:creeper_drop_music_discs",
}

REMOVED_COMMAND_KEYWORDS = {
    "replaceitem",
}


@dataclass
class DatapackStats:
    """Aggregated counters for datapack conversion output."""

    json_files_scanned: int = 0
    function_files_scanned: int = 0
    text_files_scanned: int = 0
    json_files_rewritten: int = 0
    function_files_rewritten: int = 0
    tags_rewritten: int = 0
    recipes_rewritten: int = 0
    advancements_rewritten: int = 0
    loot_tables_rewritten: int = 0
    total_text_replacements: int = 0
    warnings: int = 0


@dataclass
class DatapackUpdateResult:
    """Result container for a datapack conversion run."""

    source: Path
    destination: Path
    stats: DatapackStats = field(default_factory=DatapackStats)
    validation_issues: List[str] = field(default_factory=list)


class DatapackUpdater:
    """Convert datapack content from old to new target format."""

    def __init__(self, logger: UpdateLogger, dry_run: bool = False) -> None:
        self.logger = logger
        self.dry_run = dry_run

    def update(self, source: Path, destination: Path) -> DatapackUpdateResult:
        """Execute full datapack conversion workflow.

        Steps:
            1) Validate source structure.
            2) Copy source into destination workspace.
            3) Rewrite pack metadata.
            4) Rewrite JSON categories and function files.
            5) Produce summary stats.
        """

        result = DatapackUpdateResult(source=source, destination=destination)

        issues = validate_datapack_structure(source)
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
            pack_format=DATAPACK_FORMAT_1_21_8,
            description="Updated datapack for Minecraft 1.21.8",
            logger=self.logger,
            dry_run=self.dry_run,
        )

        self._rewrite_json_files(destination, result)
        self._rewrite_function_files(destination, result)

        self.logger.info(
            "Datapack conversion finished: "
            f"json_scanned={result.stats.json_files_scanned}, "
            f"json_rewritten={result.stats.json_files_rewritten}, "
            f"functions_scanned={result.stats.function_files_scanned}, "
            f"functions_rewritten={result.stats.function_files_rewritten}, "
            f"replacements={result.stats.total_text_replacements}"
        )
        return result

    def _rewrite_json_files(self, root: Path, result: DatapackUpdateResult) -> None:
        """Scan and rewrite JSON files in datapack categories."""
        for json_path in iter_files_by_extension(root, [".json"]):
            result.stats.json_files_scanned += 1
            payload = read_json_file(json_path, self.logger)
            if payload is None:
                result.stats.warnings += 1
                continue

            relative = relative_to(json_path, root)
            rewritten, changed, category = self._rewrite_json_payload(payload, relative)
            if changed:
                write_json_file(json_path, rewritten, logger=self.logger, dry_run=self.dry_run)
                result.stats.json_files_rewritten += 1
                if category == "tag":
                    result.stats.tags_rewritten += 1
                elif category == "recipe":
                    result.stats.recipes_rewritten += 1
                elif category == "advancement":
                    result.stats.advancements_rewritten += 1
                elif category == "loot_table":
                    result.stats.loot_tables_rewritten += 1
                self.logger.info(f"Rewrote JSON: {json_path}")

    def _rewrite_json_payload(self, payload: dict, relative_path: str) -> Tuple[dict, bool, str]:
        """Rewrite JSON payload according to detected category."""
        category = self._detect_json_category(relative_path)
        changed = False

        if category == "recipe":
            changed = self._rewrite_recipe_payload(payload)
        elif category == "tag":
            changed = self._rewrite_tag_payload(payload)
        elif category == "advancement":
            changed = self._rewrite_advancement_payload(payload)
        elif category == "loot_table":
            changed = self._rewrite_loot_table_payload(payload)
        else:
            changed = self._rewrite_generic_payload(payload)

        return payload, changed, category

    @staticmethod
    def _detect_json_category(relative_path: str) -> str:
        path = relative_path.replace("\\", "/")
        if "/recipes/" in path:
            return "recipe"
        if "/tags/" in path:
            return "tag"
        if "/advancements/" in path:
            return "advancement"
        if "/loot_tables/" in path:
            return "loot_table"
        return "generic"

    def _rewrite_generic_payload(self, payload: MutableMapping) -> bool:
        """Apply recursive id/syntax rewrites to arbitrary JSON payload."""
        changed = False

        def visit(node):
            nonlocal changed
            if isinstance(node, dict):
                for key, value in list(node.items()):
                    # rename legacy key variants
                    if key == "item" and isinstance(value, str):
                        new_value = ITEM_BLOCK_ID_REPLACEMENTS.get(value, value)
                        if new_value != value:
                            node[key] = new_value
                            changed = True
                    if key == "id" and isinstance(value, str):
                        new_value = ITEM_BLOCK_ID_REPLACEMENTS.get(value, value)
                        if new_value != value:
                            node[key] = new_value
                            changed = True
                    if isinstance(value, (dict, list)):
                        visit(value)
                    elif isinstance(value, str):
                        new_value = ITEM_BLOCK_ID_REPLACEMENTS.get(value, value)
                        if new_value != value:
                            node[key] = new_value
                            changed = True
            elif isinstance(node, list):
                for index, value in enumerate(node):
                    if isinstance(value, (dict, list)):
                        visit(value)
                    elif isinstance(value, str):
                        new_value = ITEM_BLOCK_ID_REPLACEMENTS.get(value, value)
                        if new_value != value:
                            node[index] = new_value
                            changed = True

        visit(payload)
        return changed

    def _rewrite_tag_payload(self, payload: MutableMapping) -> bool:
        """Rewrite block/item/fluid tags for renamed identifiers."""
        changed = False

        values = payload.get("values")
        if isinstance(values, list):
            for idx, entry in enumerate(values):
                if isinstance(entry, str):
                    replaced = TAG_NAME_REPLACEMENTS.get(entry, entry)
                    replaced = ITEM_BLOCK_ID_REPLACEMENTS.get(replaced, replaced)
                    if replaced != entry:
                        values[idx] = replaced
                        changed = True

        # Handle nested references if present
        if self._rewrite_generic_payload(payload):
            changed = True

        return changed

    def _rewrite_recipe_payload(self, payload: MutableMapping) -> bool:
        """Rewrite recipe payload to modernized style.

        Notes:
        - Rewrites legacy item IDs.
        - Rewrites optional `result` object to explicit modern dict form.
        """
        changed = False

        if "result" in payload:
            result_node = payload["result"]
            if isinstance(result_node, str):
                payload["result"] = {"id": ITEM_BLOCK_ID_REPLACEMENTS.get(result_node, result_node), "count": 1}
                changed = True
            elif isinstance(result_node, dict):
                if "item" in result_node and "id" not in result_node:
                    result_node["id"] = result_node.pop("item")
                    changed = True
                rid = result_node.get("id")
                if isinstance(rid, str):
                    replaced = ITEM_BLOCK_ID_REPLACEMENTS.get(rid, rid)
                    if replaced != rid:
                        result_node["id"] = replaced
                        changed = True

        for key in ("ingredient", "ingredients", "key"):
            if key in payload and self._rewrite_generic_payload(payload[key]):
                changed = True

        if self._rewrite_generic_payload(payload):
            changed = True

        return changed

    def _rewrite_advancement_payload(self, payload: MutableMapping) -> bool:
        """Rewrite advancement trigger/reward syntax where known."""
        changed = False

        criteria = payload.get("criteria")
        if isinstance(criteria, dict):
            for criterion in criteria.values():
                if isinstance(criterion, dict):
                    trigger = criterion.get("trigger")
                    if trigger == "minecraft:impossible":
                        # Keep as-is, no rewrite.
                        pass
                    conditions = criterion.get("conditions")
                    if isinstance(conditions, dict):
                        if self._rewrite_generic_payload(conditions):
                            changed = True

        rewards = payload.get("rewards")
        if isinstance(rewards, dict):
            function_id = rewards.get("function")
            if isinstance(function_id, str) and function_id.startswith("#"):
                rewards["function"] = function_id[1:]
                changed = True

        if self._rewrite_generic_payload(payload):
            changed = True

        return changed

    def _rewrite_loot_table_payload(self, payload: MutableMapping) -> bool:
        """Rewrite loot table entries and functions."""
        changed = False
        if payload.get("type") == "minecraft:chest":
            # Example of policy update hook; keep type but recurse.
            pass
        if self._rewrite_generic_payload(payload):
            changed = True
        return changed

    def _rewrite_function_files(self, root: Path, result: DatapackUpdateResult) -> None:
        """Rewrite `.mcfunction` command files."""
        for function_file in iter_files_by_extension(root, [".mcfunction"]):
            result.stats.function_files_scanned += 1
            original = function_file.read_text(encoding="utf-8")
            updated, count = self._rewrite_function_text(original, function_file)
            if count > 0:
                write_text_file(function_file, updated, logger=self.logger, dry_run=self.dry_run)
                result.stats.function_files_rewritten += 1
                result.stats.total_text_replacements += count

    def _rewrite_function_text(self, text: str, source: Path) -> Tuple[str, int]:
        """Rewrite command text while logging potentially removed commands."""
        rewritten, count = apply_text_replacements(text, MCFUNCTION_REPLACEMENTS)

        lines = rewritten.splitlines()
        for idx, line in enumerate(lines, start=1):
            stripped = line.strip()
            if not stripped or stripped.startswith("#"):
                continue
            keyword = stripped.split()[0]
            if keyword in REMOVED_COMMAND_KEYWORDS:
                self.logger.warning(
                    f"Potentially removed command still present in {source}:{idx}: '{keyword}'"
                )

        return rewritten, count


def update_multiple_datapacks(
    sources: Sequence[Path],
    destination_root: Path,
    logger: UpdateLogger,
    dry_run: bool = False,
) -> List[DatapackUpdateResult]:
    """Batch convert multiple datapacks into destination root.

    Each source datapack gets its own directory under destination root with
    original folder name.
    """
    updater = DatapackUpdater(logger=logger, dry_run=dry_run)
    results: List[DatapackUpdateResult] = []
    for src in sources:
        dst = destination_root / src.name
        logger.info(f"Starting datapack conversion for {src} -> {dst}")
        results.append(updater.update(src, dst))
    return results


__all__ = [
    "DatapackStats",
    "DatapackUpdateResult",
    "DatapackUpdater",
    "update_multiple_datapacks",
]
