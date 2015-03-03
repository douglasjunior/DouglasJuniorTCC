
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.discussion;

import br.edu.utfpr.cm.JGitMinerWeb.util.LuceneUtil;
import com.google.common.base.Strings;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class WordinessCalculator {

//    public static WordinessMeasure calcule(IssueMetrics auxWordiness) {
//        long wordiness = LuceneUtil.tokenizeString(Strings.nullToEmpty(auxWordiness.getIssueBody())).size();
//
//        for (final String comment : auxWordiness.getComments()) {
//            wordiness += LuceneUtil.tokenizeString(Strings.nullToEmpty(comment)).size();
//        }
//
//        return new WordinessMeasure(auxWordiness.getIssueNumber(), wordiness);
//    }

    public static long calcule(String body, List<String> comments) {
        long wordiness = LuceneUtil.tokenizeString(Strings.nullToEmpty(body)).size();

        for (final String comment : comments) {
            wordiness += LuceneUtil.tokenizeString(Strings.nullToEmpty(comment)).size();
        }

        return wordiness;
    }
}
