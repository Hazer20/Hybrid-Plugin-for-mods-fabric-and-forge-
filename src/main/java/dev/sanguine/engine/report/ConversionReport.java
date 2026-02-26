package dev.sanguine.engine.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversionReport {
    private final List<String> converted = Collections.synchronizedList(new ArrayList<>());
    private final List<String> skipped = Collections.synchronizedList(new ArrayList<>());
    private final List<String> fixedSyntax = Collections.synchronizedList(new ArrayList<>());
    private final List<String> deprecatedCommands = Collections.synchronizedList(new ArrayList<>());
    private final List<String> incompatible = Collections.synchronizedList(new ArrayList<>());
    private final List<String> manualFixes = Collections.synchronizedList(new ArrayList<>());

    public void converted(String message) { converted.add(message); }
    public void skipped(String message) { skipped.add(message); }
    public void fixed(String message) { fixedSyntax.add(message); }
    public void deprecated(String message) { deprecatedCommands.add(message); }
    public void incompatible(String message) { incompatible.add(message); }
    public void manual(String message) { manualFixes.add(message); }

    public void writeTo(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        StringBuilder sb = new StringBuilder();
        section(sb, "Converted Files", converted);
        section(sb, "Skipped Files", skipped);
        section(sb, "Fixed Syntax", fixedSyntax);
        section(sb, "Deprecated Commands Found", deprecatedCommands);
        section(sb, "Incompatible Files", incompatible);
        section(sb, "Required Manual Fixes", manualFixes);
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private void section(StringBuilder sb, String title, List<String> entries) {
        sb.append("=== ").append(title).append(" ===").append(System.lineSeparator());
        if (entries.isEmpty()) {
            sb.append("- none").append(System.lineSeparator());
        } else {
            for (String entry : entries) {
                sb.append("- ").append(entry).append(System.lineSeparator());
            }
        }
        sb.append(System.lineSeparator());
    }
}
