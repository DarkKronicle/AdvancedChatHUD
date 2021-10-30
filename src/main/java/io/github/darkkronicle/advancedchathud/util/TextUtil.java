/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchathud.util;

public class TextUtil {

    private static final char[] SUPERSCRIPTS =
            new char[] {
                '\u2070', '\u00B9', '\u00B2', '\u00B3', '\u2074', '\u2075', '\u2076', '\u2077',
                '\u2078', '\u2079'
            };

    public static String toSuperscript(int num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(SUPERSCRIPTS[num % 10]);
        } while ((num /= 10) > 0);
        return sb.reverse().toString();
    }
}
