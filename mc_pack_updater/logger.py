"""mc_pack_updater.logger
=================================

Logging subsystem used by the Minecraft pack updater project.

The module is intentionally explicit and verbose because it acts as the
foundation for traceability of all automated changes performed during
conversion from Minecraft 1.20.4 packs to 1.21.8 packs.

Highlights
----------
1. Two separate file logs are maintained:
   - `changes.log`: information-level and successful rewrite events.
   - `errors.log`: warnings and error-level diagnostics.
2. Optional console output can be enabled for interactive sessions.
3. Every logger entry includes timestamp and contextual metadata.
4. A lightweight in-memory event collector is available for reporting.
5. Designed for future expansion (JSON logs, structured telemetry, etc.).

The logger is consumed by all other modules and should be initialized once
per execution session.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime
import logging
import os
from pathlib import Path
from typing import Dict, List, Optional


DEFAULT_CHANGE_LOG_NAME = "changes.log"
DEFAULT_ERROR_LOG_NAME = "errors.log"


@dataclass
class LogEvent:
    """Represents a single high-level event.

    Attributes:
        timestamp: UTC timestamp string when event was recorded.
        level: Textual level of the event (INFO/WARNING/ERROR/DEBUG).
        message: Human-readable event text.
        context: Optional context dictionary useful for reports.
    """

    timestamp: str
    level: str
    message: str
    context: Dict[str, str] = field(default_factory=dict)


class UpdateLogger:
    """Coordinator for project logging.

    The class wraps Python's standard logging facilities but adds
    common conventions used by the updater:

    - file output split by semantics;
    - optional console mirroring;
    - in-memory event list for summaries.

    Parameters:
        logs_dir: directory where `changes.log` and `errors.log` are stored.
        enable_console: Whether to echo logs to stdout/stderr.
        debug: If True, include DEBUG events in file/console output.
    """

    def __init__(
        self,
        logs_dir: Path,
        enable_console: bool = True,
        debug: bool = False,
    ) -> None:
        self.logs_dir = logs_dir
        self.enable_console = enable_console
        self.debug = debug

        self.logs_dir.mkdir(parents=True, exist_ok=True)

        self.change_log_path = self.logs_dir / DEFAULT_CHANGE_LOG_NAME
        self.error_log_path = self.logs_dir / DEFAULT_ERROR_LOG_NAME

        self._main_logger = logging.getLogger(f"mc_pack_updater.main.{id(self)}")
        self._error_logger = logging.getLogger(f"mc_pack_updater.error.{id(self)}")

        self._events: List[LogEvent] = []

        self._setup_loggers()

    def _setup_loggers(self) -> None:
        """Prepare handlers and formatters.

        This method resets handlers to avoid duplicate entries when
        logger is re-created in tests or repeated invocations.
        """

        for logger_obj in (self._main_logger, self._error_logger):
            logger_obj.handlers.clear()
            logger_obj.propagate = False

        level = logging.DEBUG if self.debug else logging.INFO
        self._main_logger.setLevel(level)
        self._error_logger.setLevel(logging.WARNING)

        formatter = logging.Formatter(
            "%(asctime)s | %(levelname)s | %(name)s | %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S",
        )

        change_handler = logging.FileHandler(self.change_log_path, encoding="utf-8")
        change_handler.setLevel(level)
        change_handler.setFormatter(formatter)

        error_handler = logging.FileHandler(self.error_log_path, encoding="utf-8")
        error_handler.setLevel(logging.WARNING)
        error_handler.setFormatter(formatter)

        self._main_logger.addHandler(change_handler)
        self._error_logger.addHandler(error_handler)

        if self.enable_console:
            console_handler = logging.StreamHandler()
            console_handler.setLevel(level)
            console_handler.setFormatter(formatter)
            self._main_logger.addHandler(console_handler)

            console_error_handler = logging.StreamHandler()
            console_error_handler.setLevel(logging.WARNING)
            console_error_handler.setFormatter(formatter)
            self._error_logger.addHandler(console_error_handler)

    @property
    def events(self) -> List[LogEvent]:
        """Return immutable snapshot of events."""
        return list(self._events)

    def _record_event(self, level: str, message: str, context: Optional[Dict[str, str]]) -> None:
        event = LogEvent(
            timestamp=datetime.utcnow().isoformat(timespec="seconds") + "Z",
            level=level,
            message=message,
            context=context or {},
        )
        self._events.append(event)

    def debug_log(self, message: str, context: Optional[Dict[str, str]] = None) -> None:
        """Emit a DEBUG level event."""
        self._main_logger.debug(message)
        self._record_event("DEBUG", message, context)

    def info(self, message: str, context: Optional[Dict[str, str]] = None) -> None:
        """Emit an INFO level event to changes log."""
        self._main_logger.info(message)
        self._record_event("INFO", message, context)

    def warning(self, message: str, context: Optional[Dict[str, str]] = None) -> None:
        """Emit WARNING to both change and error channels."""
        self._main_logger.warning(message)
        self._error_logger.warning(message)
        self._record_event("WARNING", message, context)

    def error(self, message: str, context: Optional[Dict[str, str]] = None) -> None:
        """Emit ERROR to both change and error channels."""
        self._main_logger.error(message)
        self._error_logger.error(message)
        self._record_event("ERROR", message, context)

    def exception(self, message: str, context: Optional[Dict[str, str]] = None) -> None:
        """Emit exception traceback to both channels."""
        self._main_logger.exception(message)
        self._error_logger.exception(message)
        self._record_event("ERROR", message, context)

    def summary(self) -> Dict[str, int]:
        """Return aggregate count of events by level.

        Returns:
            A dictionary with event level counters.
        """
        output: Dict[str, int] = {"DEBUG": 0, "INFO": 0, "WARNING": 0, "ERROR": 0}
        for event in self._events:
            if event.level in output:
                output[event.level] += 1
        return output


def create_logger(logs_dir: str | Path, enable_console: bool = True, debug: bool = False) -> UpdateLogger:
    """Factory helper to instantiate :class:`UpdateLogger`.

    Args:
        logs_dir: Base directory for log files.
        enable_console: Mirror logs to terminal.
        debug: Enable debug-level output.

    Returns:
        Configured :class:`UpdateLogger` instance.
    """
    return UpdateLogger(Path(logs_dir), enable_console=enable_console, debug=debug)


__all__ = [
    "LogEvent",
    "UpdateLogger",
    "create_logger",
    "DEFAULT_CHANGE_LOG_NAME",
    "DEFAULT_ERROR_LOG_NAME",
]
