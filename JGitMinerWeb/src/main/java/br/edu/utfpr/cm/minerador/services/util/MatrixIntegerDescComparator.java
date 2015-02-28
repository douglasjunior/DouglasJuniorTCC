package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import java.util.Comparator;

/**
 *
 * @author Rodrigo T. Kuroda
 */
class MatrixIntegerDescComparator implements Comparator<EntityMatrixNode> {

    private final int indexToCompare;

    MatrixIntegerDescComparator(int indexToCompare) {
        this.indexToCompare = indexToCompare;
    }

    @Override
    public int compare(EntityMatrixNode o1, EntityMatrixNode o2) {
        final String[] line1 = MatrixUtils.separateValues(o1);
        final String[] line2 = MatrixUtils.separateValues(o2);
        final int value1 = Integer.valueOf(line1[indexToCompare]);
        final int value2 = Integer.valueOf(line2[indexToCompare]);

        if (value1 > value2) {
            return -1;
        } else if (value1 < value2) {
            return 1;
        }
        return 0;
    }
}
