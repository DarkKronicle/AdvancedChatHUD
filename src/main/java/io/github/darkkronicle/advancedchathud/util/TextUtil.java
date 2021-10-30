package io.github.darkkronicle.advancedchathud.util;

public class TextUtil {

    private static final char[] SUPERSCRIPTS = new char[]{
            '\u2070', '\u00B9', '\u00B2', '\u00B3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'
    };

    public static String toSuperscript(int num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(SUPERSCRIPTS[num % 10]);
        } while ((num /= 10) > 0);
        return sb.reverse().toString();
    }

}
