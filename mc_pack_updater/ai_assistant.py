"""AI-assisted post-conversion assurance.

This module adds an "AI-like" quality layer over deterministic converters.
It does not depend on external APIs and works offline. The assistant:

1. Scans converted datapacks/resourcepacks.
2. Detects suspicious legacy tokens and structural issues.
3. Applies safe auto-fixes for known issues.
4. Calculates a confidence score and writes a readable report.

The goal is to improve final conversion quality without requiring network
services or paid model integrations.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Iterable, List, Sequence, Tuple

from .logger import UpdateLogger
from .utils import apply_text_replacements, iter_files_by_extension, write_pack_mcmeta, write_text_file


AI_TOKEN_REPLACEMENTS: Dict[str, str] = {
    "minecraft:grass_path": "minecraft:dirt_path",
    "replaceitem entity": "item replace entity",
    "replaceitem block": "item replace block",
    "textures/blocks/": "textures/block/",
    "textures/items/": "textures/item/",
}

SUSPICIOUS_TOKENS: Tuple[str, ...] = (
    "minecraft:grass_path",
    "replaceitem ",
    "textures/blocks/",
    "textures/items/",
)


@dataclass
class AIAssuranceResult:
    """Summary of assistant checks and fixes."""

    scanned_files: int = 0
    fixed_files: int = 0
    replacements: int = 0
    warnings: List[str] = field(default_factory=list)
    confidence_score: float = 0.0
    report_path: Path | None = None


class AIMigrationAssistant:
    """Offline AI-style quality assistant for converted packs."""

    def __init__(self, logger: UpdateLogger, dry_run: bool = False) -> None:
        self.logger = logger
        self.dry_run = dry_run

    def run(
        self,
        datapack_dirs: Sequence[Path],
        resourcepack_dirs: Sequence[Path],
        output_root: Path,
        min_score: float = 85.0,
    ) -> AIAssuranceResult:
        """Run full assurance pass and produce report."""
        result = AIAssuranceResult()

        for dp in datapack_dirs:
            self._ensure_pack_metadata(dp, datapack=True)
            self._scan_and_fix_pack(dp, result)

        for rp in resourcepack_dirs:
            self._ensure_pack_metadata(rp, datapack=False)
            self._scan_and_fix_pack(rp, result)

        result.confidence_score = self._score(result)

        if result.confidence_score < min_score:
            warning = (
                f"AI assurance score is below threshold: "
                f"{result.confidence_score:.1f} < {min_score:.1f}"
            )
            result.warnings.append(warning)
            self.logger.warning(warning)
        else:
            self.logger.info(
                f"AI assurance score reached target: {result.confidence_score:.1f} / 100"
            )

        result.report_path = self._write_report(result, output_root)
        return result

    def _ensure_pack_metadata(self, pack_dir: Path, datapack: bool) -> None:
        description = "AI checked datapack for Minecraft 1.21.8" if datapack else "AI checked resourcepack for Minecraft 1.21.8"
        pack_format = 61 if datapack else 46
        if (pack_dir / "pack.mcmeta").exists():
            return
        self.logger.warning(f"AI assistant: missing pack.mcmeta, auto-create: {pack_dir}")
        write_pack_mcmeta(
            pack_dir=pack_dir,
            pack_format=pack_format,
            description=description,
            logger=self.logger,
            dry_run=self.dry_run,
        )

    def _scan_and_fix_pack(self, root: Path, result: AIAssuranceResult) -> None:
        for file_path in root.rglob("*"):
            if not file_path.is_file():
                continue
            if file_path.suffix.lower() not in {".json", ".mcfunction", ".mcmeta", ".txt", ".properties"}:
                continue
            result.scanned_files += 1
            text = file_path.read_text(encoding="utf-8", errors="ignore")

            suspicious_found = [token for token in SUSPICIOUS_TOKENS if token in text]
            if suspicious_found:
                self.logger.info(
                    f"AI assistant found legacy tokens in {file_path}: {', '.join(suspicious_found)}"
                )

            updated, count = apply_text_replacements(text, AI_TOKEN_REPLACEMENTS)
            if count > 0:
                write_text_file(file_path, updated, logger=self.logger, dry_run=self.dry_run)
                result.fixed_files += 1
                result.replacements += count

    @staticmethod
    def _score(result: AIAssuranceResult) -> float:
        """Compute a simple confidence score in [0, 100]."""
        if result.scanned_files == 0:
            return 60.0
        penalty = min(60.0, (result.replacements * 0.8) + (len(result.warnings) * 10.0))
        raw = 100.0 - penalty
        return max(0.0, min(100.0, raw))

    def _write_report(self, result: AIAssuranceResult, output_root: Path) -> Path:
        report_dir = output_root / "logs"
        report_path = report_dir / "ai_assurance_report.txt"
        lines = [
            "AI Assurance Report",
            "===================",
            f"Scanned files      : {result.scanned_files}",
            f"Fixed files        : {result.fixed_files}",
            f"Replacements       : {result.replacements}",
            f"Warnings           : {len(result.warnings)}",
            f"Confidence score   : {result.confidence_score:.1f} / 100",
            "",
        ]
        if result.warnings:
            lines.append("Warnings:")
            for item in result.warnings:
                lines.append(f"- {item}")
            lines.append("")

        lines.append("Note: 100/100 is not guaranteed for arbitrary packs.")
        lines.append("Assistant improves quality automatically and highlights risky areas.")

        write_text_file(report_path, "\n".join(lines) + "\n", logger=self.logger, dry_run=self.dry_run)
        self.logger.info(f"AI assurance report: {report_path}")
        return report_path


__all__ = ["AIMigrationAssistant", "AIAssuranceResult"]
