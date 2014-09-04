
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.discussion;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxWordiness;
import br.edu.utfpr.cm.JGitMinerWeb.util.LuceneUtil;
import com.google.common.base.Strings;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class WordinessCalculator {

//    public static WordinessMeasure calcule(AuxWordiness auxWordiness) {
//        long wordiness = LuceneUtil.tokenizeString(Strings.nullToEmpty(auxWordiness.getIssueBody())).size();
//
//        for (final String comment : auxWordiness.getComments()) {
//            wordiness += LuceneUtil.tokenizeString(Strings.nullToEmpty(comment)).size();
//        }
//
//        return new WordinessMeasure(auxWordiness.getIssueNumber(), wordiness);
//    }

    public static long calcule(AuxWordiness auxWordiness) {
        long wordiness = LuceneUtil.tokenizeString(Strings.nullToEmpty(auxWordiness.getIssueBody())).size();

        for (final String comment : auxWordiness.getComments()) {
            wordiness += LuceneUtil.tokenizeString(Strings.nullToEmpty(comment)).size();
        }

        return wordiness;
    }
}
