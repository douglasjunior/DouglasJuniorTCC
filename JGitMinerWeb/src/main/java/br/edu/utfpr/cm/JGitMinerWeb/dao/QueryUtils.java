package br.edu.utfpr.cm.JGitMinerWeb.dao;

import java.text.MessageFormat;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class QueryUtils {

    public static String getQueryForDatabase(String query, String... params) {
        return MessageFormat.format(query, (Object[]) params);
    }
}
