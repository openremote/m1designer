package org.openremote.shared.util;

import java.util.Locale;

public class Util {

    public static String toLowerCaseDash(String camelCase) {
        // Transforms 'EXFooBar123' into 'ex-foo-bar-123 and "attributeX" into "attribute-x" without regex (GWT!)
        if (camelCase == null)
            return null;
        if (camelCase.length() == 0)
            return camelCase;
        StringBuilder sb = new StringBuilder();
        char[] chars = camelCase.toCharArray();
        boolean inNonLowerCase = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (!Character.isLowerCase(c)) {
                if (!inNonLowerCase) {
                    if (i > 0)
                        sb.append("-");
                } else if (i < chars.length -1 && Character.isLowerCase(chars[i+1])) {
                    sb.append("-");
                }
                inNonLowerCase = true;
            } else {
                inNonLowerCase = false;
            }
            sb.append(c);
        }
        String name = sb.toString();
        name = name.toLowerCase(Locale.ROOT);
        return name;
    }

}
