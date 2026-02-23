package dev.sanguine.engine.parser;

import java.util.ArrayList;
import java.util.List;

public class McFunctionTokenizer {
    public List<McToken> tokenize(String line) {
        List<McToken> tokens = new ArrayList<>();
        if (line == null || line.isBlank()) {
            return tokens;
        }

        int i = 0;
        while (i < line.length()) {
            while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
                i++;
            }
            if (i >= line.length()) {
                break;
            }

            int start = i;
            char c = line.charAt(i);
            if (c == '"') {
                i++;
                while (i < line.length()) {
                    char q = line.charAt(i);
                    if (q == '"' && line.charAt(i - 1) != '\\') {
                        i++;
                        break;
                    }
                    i++;
                }
                tokens.add(new McToken(line.substring(start, i), start, i));
                continue;
            }

            int bracketDepth = 0;
            while (i < line.length()) {
                char x = line.charAt(i);
                if (x == '{' || x == '[' || x == '(') {
                    bracketDepth++;
                } else if (x == '}' || x == ']' || x == ')') {
                    bracketDepth = Math.max(0, bracketDepth - 1);
                }
                if (Character.isWhitespace(x) && bracketDepth == 0) {
                    break;
                }
                i++;
            }

            tokens.add(new McToken(line.substring(start, i), start, i));
        }

        return tokens;
    }
}
