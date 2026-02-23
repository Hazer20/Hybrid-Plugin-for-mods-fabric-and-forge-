package dev.sanguine.engine;

import java.nio.file.Path;

public record PackWorkspace(
    Path root,
    Path inputDatapacks,
    Path inputResourcepacks,
    Path generatedDatapacks,
    Path generatedResourcepacks,
    Path temp,
    Path backup,
    Path unsupported,
    Path logs,
    Path reportFile
) {
}
