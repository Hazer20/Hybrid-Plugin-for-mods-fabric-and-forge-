package com.hazer2_0.disc;

import java.io.IOException;
import java.nio.file.Path;

public class AudioConverter {

    public Path convertToOgg(Path input, Path output) throws IOException, InterruptedException {
        Process ffmpeg = new ProcessBuilder("ffmpeg", "-y", "-i", input.toString(), "-vn", "-ac", "1", "-ar", "48000", "-c:a", "libvorbis", output.toString())
                .redirectErrorStream(true)
                .start();
        int code = ffmpeg.waitFor();
        if (code != 0) throw new IOException("ffmpeg failed with code " + code);
        return output;
    }

    public int probeDurationSeconds(Path input) throws IOException, InterruptedException {
        Process ffprobe = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", input.toString())
                .redirectErrorStream(true)
                .start();
        String output = new String(ffprobe.getInputStream().readAllBytes()).trim();
        int code = ffprobe.waitFor();
        if (code != 0 || output.isBlank()) throw new IOException("ffprobe failed");
        return (int) Math.ceil(Double.parseDouble(output));
    }
}
