package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class MatrixUtils {

    private static final int HEADER_INDEX = 0;
    private static final int VALUES_START_INDEX = 1;
    public static final String VALUES_SEPARATOR = ";";

    public static Map<String, Integer> extractHeaderIndexes(EntityMatrix matrix) {
        final EntityMatrixNode headerNode = matrix.getNodes().get(HEADER_INDEX);
        String[] headerIndexes = separateValues(headerNode);

        Map<String, Integer> headerIndexesMap = new HashMap<>(headerIndexes.length);
        for (int i = 0; i < headerIndexes.length; i++) {
            headerIndexesMap.put(headerIndexes[i], i);
        }

        return headerIndexesMap;
    }

    public static String[] separateValues(EntityMatrixNode node) {
        return node.getLine().split(VALUES_SEPARATOR);
    }

    public static List<EntityMatrixNode> extractValues(EntityMatrix matrix) {
        final List<EntityMatrixNode> nodes = matrix.getNodes();
        return nodes.subList(VALUES_START_INDEX, nodes.size());
    }

    public static void orderByFilePairSupport(final List<EntityMatrixNode> matrixLines, final Map<String, Integer> header) {
        final Integer supporFilePairIndex = header.get("supportFilePair");
        Collections.sort(matrixLines, new MatrixDoubleDescComparator(supporFilePairIndex));
    }

    public static void orderByNumberOfDefects(final List<EntityMatrixNode> matrixLines, final Map<String, Integer> header) {
        final Integer defectIssuesIndex = header.get("defectIssues");
        Collections.sort(matrixLines, new MatrixIntegerDescComparator(defectIssuesIndex));
    }
}
