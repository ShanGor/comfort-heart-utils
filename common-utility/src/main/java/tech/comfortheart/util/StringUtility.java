package tech.comfortheart.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
    public static final boolean isEmpty(final String s) {
        return s == null || s.trim().equals("");
    }

    public static final boolean notEmpty(final String s) {
        return ! isEmpty(s);
    }

    public static final boolean equalRegardlessCaseOrRoundingSpaces(String s1, String s2) {
        if (isEmpty(s1) && notEmpty(s2)) {return false; }
        if (isEmpty(s2) && notEmpty(s1)) {return false; }

        return s1.trim().toUpperCase().equals(s2.trim().toUpperCase());
    }

    /**
     * Capitalize a string, return the result.
     * @param s
     * @return
     */
    public static final String capitalize(final String s) {
        if (isEmpty(s)) {
            return s;
        } else {
            char a = s.charAt(0);
            if (a <='z' && a >='a') {
                char A = (char) (a + ('A' - 'a'));
                return A + s.substring(1);
            } else {
                return s;
            }
        }
    }

    /**
     * Replace the suffix with a replacement.
     * @param s
     * @param suffix
     * @param replacement
     * @return
     */
    public static final String replaceSuffix(String s, final String suffix, final String replacement) {
        if (s.endsWith(s)) {
            int suffixLength = suffix.length();
            if (!suffix.startsWith(".")) {
                suffixLength += 1;
            }

            String withoutSuffix = s.substring(0, s.length() - suffixLength);
            return withoutSuffix + replacement;
        } else {
            return s;
        }
    }

    public static Pattern PATTERN_VAR = Pattern.compile("\\$([A-Za-z0-9_-]+)");
    public static final String replaceVariable(final String s, final Map<String, Object> variables) {
        Matcher m = PATTERN_VAR.matcher(s);
        String tmp = s;
        while (m.find()) {
            String key = m.group(1);
            Object value = variables.get(key);
            if (value == null) {
                continue;
            }
            tmp = tmp.replace('$' + key, value.toString());
            m = PATTERN_VAR.matcher(tmp);
        }

        return tmp;
    }
}
