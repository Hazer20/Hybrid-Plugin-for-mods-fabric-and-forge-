package dev.sanguine.bridge;

import java.nio.file.Path;

public record BridgeResult(Path sourceDatapack, Path outputDatapack, int transformedFiles) {
}
