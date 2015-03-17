package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class QueryUtils {

    public static String getQueryForDatabase(String query, String... params) {
        return MessageFormat.format(query, (Object[]) params);
    }

    public static void filterByIssues(Collection<Integer> issues, StringBuilder sql) {
        if (issues != null && !issues.isEmpty()) {
            sql.append(" AND i.id IN (");
            boolean appendComma = false;
            for (Integer issue : issues) {
                if (appendComma) {
                    sql.append(",");
                }
                sql.append(issue);
                appendComma = true;
            }
            sql.append(")");
        }
    }

    public static void filterByIssues(Set<Issue> issues, StringBuilder sql) {
        if (issues != null && !issues.isEmpty()) {
            sql.append(" AND i.id IN (");
            boolean appendComma = false;
            for (Issue issue : issues) {
                if (appendComma) {
                    sql.append(",");
                }
                sql.append(issue.getId());
                appendComma = true;
            }
            sql.append(")");
        }
    }

    public static void filterByIssues(Integer issues, StringBuilder sql) {
        if (issues != null) {
            sql.append(" AND i.id = ")
                    .append(issues)
                    .append(" ");
        }
    }
}
