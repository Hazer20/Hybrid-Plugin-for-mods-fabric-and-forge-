package dev.sanguine.bridge;

import java.nio.file.Path;
import java.util.List;

public record DatapackBridgeReport(
    Path sourceDatapack,
    Path outputDatapack,
    int transformedFiles,
    int scannedFiles,
    List<String> warnings
) {
}
