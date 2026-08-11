package pvz.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text;
    }

    public static Object parseFile(Path path) throws IOException {
        return parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }
    public static Object parse(String text) {
        Json parser = new Json(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos < text.length()) throw parser.error("unexpected trailing characters");
        return value;
    }

    ///  parser
    private Object readValue() {
        skipWhitespace();
        char c = peek();
        switch (c) {
            case '{': return readObject();
            case '[': return readArray();
            case '"': return readString();
            case 't': expect("true");  return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null");  return null;
            default:  return readNumber();
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') throw error("expected object key");
            String key = readString();
            skipWhitespace();
            if (peek() != ':') throw error("expected ':' after object key");
            pos++;

            map.put(key,readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == '}') {
                pos++;
                return map;
            }
            throw error("expected ',' or '}' in object");
        }
    }

    private List<Object> readArray() {
        List<Object> list = new ArrayList<>();
        pos++;
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            list.add(readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == ']') {
                pos++;
                return list;
            }
            throw error("expected ',' or ']' in array");
        }
    }

    private String readString() {
        StringBuilder stringBuilder = new StringBuilder();
        pos++;
        while (true) {
            if (pos >= text.length()) {
                throw error("unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return stringBuilder.toString();
            }
            if (c != '\\') {
                stringBuilder.append(c);
                continue;
            }
            char esc = text.charAt(pos++);
            switch (esc) {
                case '"':  stringBuilder.append('"');  break;
                case '\\': stringBuilder.append('\\'); break;
                case '/':  stringBuilder.append('/');  break;
                case 'b':  stringBuilder.append('\b'); break;
                case 'f':  stringBuilder.append('\f'); break;
                case 'n':  stringBuilder.append('\n'); break;
                case 'r':  stringBuilder.append('\r'); break;
                case 't':  stringBuilder.append('\t'); break;
                case 'u':
                    stringBuilder.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                    break;
                default:
                    throw error("invalid escape sequence \\" + esc);
            }
        }
    }

    private Double readNumber() {
        int start = pos;
        while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error("invalid JSON value");
        return Double.parseDouble(text.substring(start, pos));
    }

    private void expect(String literal) {
        if (!text.startsWith(literal, pos)) throw error("invalid literal, expected '" + literal + "'");
        pos += literal.length();
    }

    private char peek() {
        if (pos >= text.length()) throw error("unexpected end of input");
        return text.charAt(pos);
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("JSON parse error at offset " + pos + ": " + message);
    }
}
