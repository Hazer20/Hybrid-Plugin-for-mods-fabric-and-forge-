#!/usr/bin/env python3
"""Entry point for Minecraft datapack/resourcepack updater.

This script wraps the modular package implementation and exposes a concise
command line interface.
"""

from __future__ import annotations

import sys

from mc_pack_updater.cli import run_cli


if __name__ == "__main__":
    sys.exit(run_cli())
