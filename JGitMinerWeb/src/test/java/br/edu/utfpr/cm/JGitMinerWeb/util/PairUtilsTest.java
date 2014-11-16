package br.edu.utfpr.cm.JGitMinerWeb.util;

import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PairUtilsTest {

    @Test
    public void testPairCommenters() {
        List<Commenter> commenters = new ArrayList<>();
        Commenter commenterA = new Commenter(1, "A", "A");
        Commenter commenterB = new Commenter(2, "B", "B");
        Commenter commenterC = new Commenter(3, "C", "C");

        commenters.add(commenterA);
        commenters.add(commenterB);
        commenters.add(commenterA);
        commenters.add(commenterB);
        commenters.add(commenterB);
        commenters.add(commenterC);

        Map<AuxUserUserDirectional, AuxUserUserDirectional> expResult = new HashMap<>();
        AuxUserUserDirectional ba = new AuxUserUserDirectional("B", "A");
        AuxUserUserDirectional ab = new AuxUserUserDirectional("A", "B");
        AuxUserUserDirectional ca = new AuxUserUserDirectional("C", "A");
        AuxUserUserDirectional cb = new AuxUserUserDirectional("C", "B");
        expResult.put(ba, ba);
        expResult.put(ab, ab);
        expResult.put(ca, ca);
        expResult.put(cb, cb);

        Map<AuxUserUserDirectional, AuxUserUserDirectional> result = PairUtils.pairCommenters(commenters);

        for (AuxUserUserDirectional keySet : result.keySet()) {
            System.out.println(keySet.toString());
        }

        assertEquals(expResult, result);
    }

}
