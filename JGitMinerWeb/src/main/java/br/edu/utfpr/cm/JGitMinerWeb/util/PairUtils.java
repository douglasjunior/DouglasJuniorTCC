package br.edu.utfpr.cm.JGitMinerWeb.util;

import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PairUtils {

    /**
     *
     * @param commenters the commenters of pair file (comment on issue that pair
     *      file has committed)
     * @return pairedCommenters the commenters paired
     */
    public static Map<AuxUserUserDirectional, AuxUserUserDirectional> pairCommenters(List<Commenter> commenters) {
        Map<AuxUserUserDirectional, AuxUserUserDirectional> pairCommenter = new HashMap<>();
        for (int i = 0; i < commenters.size(); i++) {
            Commenter author1 = commenters.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Commenter author2 = commenters.get(j);
                if (!author1.equals(author2)) {
                    AuxUserUserDirectional pair = new AuxUserUserDirectional(author1.getName(), author1.getEmail(), author2.getName(), author2.getEmail());
                    if (pairCommenter.containsKey(pair)) {
                        pairCommenter.get(pair).inc();
                    } else {
                        pairCommenter.put(pair, pair);
                    }
                }
            }
        }
        return pairCommenter;
    }

}
