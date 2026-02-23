package dev.sanguine.engine;

import java.nio.file.Path;

public record PackWorkspace(
    Path pluginRoot,
    Path inputDatapack,
    Path inputResourcepack,
    Path generatedDatapack,
    Path generatedResourcepack,
    Path tmpDatapack,
    Path tmpResourcepack,
    Path generatedDatapackZip,
    Path generatedResourcepackZip
) {
}
