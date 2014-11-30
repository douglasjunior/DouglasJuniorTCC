package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ListUtils {

    public static <E> List<E> getTopMost25Percent(List<E> list, Comparator<E> comparator) {
        Collections.sort(list, comparator);
        return getFirst25PercentElements(list);
    }

    public static <E> List<E> getFirst25PercentElements(List<E> list) {
        float oneQuarter = list.size() / 4.0f; // 25% top
        return list.subList(0, Math.round(oneQuarter));
    }
}
