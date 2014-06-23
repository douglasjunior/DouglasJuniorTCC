
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class MatcherUtils {

    public static Pattern createExtensionIncludeMatcher(List<String> strings) {
        StringBuilder regex = new StringBuilder();
        int i = 0;
        if (strings.isEmpty()) {
            regex.append(".*");
        } else {
            regex.append("([^\\s]+(\\.(?i)(");
            for (String string : strings) {
                if (i > 0) {
                    regex.append("|");
                }
                regex.append(createMatcher(string));
                i++;
            }
            regex.append("))$)");
        }
        return Pattern.compile(regex.toString());
    }

    /**
     * TODO revisar, não funciona corretamente
     *
     * @param strings List of strings to not match
     * @return Pattern that negate that strings
     */
    @Deprecated
    public static Pattern createExtensionExcludeMatcher(List<String> strings) {
        StringBuilder regex = new StringBuilder();
        int i = 0;
        regex.append("^(?!.*(");
        for (String string : strings) {
            if (i > 0) {
                regex.append("|");
            }
            regex.append(createMatcher(string));
            i++;
        }
        regex.append(")).*$");
        return Pattern.compile(regex.toString());
    }

    public static Pattern createMatcher(String expr) {
        expr = expr.toLowerCase() // ignoring locale for now
                //                .replace(".", "\\.")
                //                .replace("?", ".")
                .replace("%.", "");
        return Pattern.compile(expr);
    }
}
