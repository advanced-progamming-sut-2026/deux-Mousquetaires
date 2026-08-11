package pvz.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Args {

    private static final Pattern LOCATION = Pattern.compile("\\(?\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)?");

    private final Map<String, String> flags = new HashMap<>();

    private Args() {}

    static Args parse(String line) {
        Args args = new Args();
        Matcher matcher = Pattern.compile("-([A-Za-z-]+)\\s+([^-]*(?:-(?![A-Za-z-]+\\s)[^-]*)*)").matcher(line);
        while (matcher.find()) {
            args.flags.put(matcher.group(1).toLowerCase(), matcher.group(2).trim());}

        Matcher bool = Pattern.compile("-([A-Za-z-]+)(?=\\s|$)").matcher(line);
        while (bool.find()) {
            args.flags.putIfAbsent(bool.group(1).toLowerCase(), "");}
        return args;
    }

    boolean has(String flag) {return flags.containsKey(flag.toLowerCase());}

    String get(String flag) {return flags.get(flag.toLowerCase());}

    String get(String flag, String fallback) {
        String value = flags.get(flag.toLowerCase());
        return value == null || value.isEmpty() ? fallback : value;}

    Integer getInt(String flag) {
        String value = flags.get(flag.toLowerCase());
        if (value == null) return null;
        Matcher matcher = Pattern.compile("-?\\d+").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }
    static int[] location(String text) {
        if (text == null) return null;
        Matcher matcher = LOCATION.matcher(text);
        if (matcher.find()) return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
        return null;
    }
}