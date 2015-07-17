package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.minerador.preprocessor.comparator.VersionComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class VersionUtil {

    /**
     * Encontra a maior sequência de versões, dado uma conjunto de ocorrências
     * de verões e uma lista de todas as versões. É essencial que as versões
     * estejam ordenadas.
     *
     * @param ocurrencesVersions Um conjunto distinto e ordenado de versões
     * @param allVersions A lista ordenada de todas as versões.
     * @return A maior sequência de versões.
     */
    public static List<Version> getMaxVersionSequence(final List<Version> ocurrencesVersions, final List<Version> allVersions) {
        // armazena a maior sequência de versões
        final List<Version> maxSequence = new ArrayList<>();
        // armazena a sequência de versões para comparar com a sequência anterior
        final List<Version> currentSequence = new ArrayList<>();

        // ordena as versões
        final VersionComparator versionComparator = new VersionComparator();
        Collections.sort(ocurrencesVersions, versionComparator);
        Collections.sort(allVersions, versionComparator);

        // guarda o indíce da última versão
        int previousVersionIndex = -1;
        for (Version version : ocurrencesVersions) {
            // obtem o indíce da versão
            // é importante que as versões estejam na ordem
            final int currentVersionIndex = allVersions.indexOf(version);

            // se a versão corrente NÃO É a versão seguinte da versão anterior
            if (previousVersionIndex + 1 != currentVersionIndex) {
                currentSequence.clear(); // limpa a sequência de versões anteriores armazenadas
            }
            // armazena a versão corrente na lista
            currentSequence.add(version);

            // verifica se a sequência de versões corrente contém mais versões
            if (currentSequence.size() >= maxSequence.size()) {
                // caso tenha mais versões que a sequência anterior, substitui
                // pela sequência corrente
                maxSequence.clear();
                maxSequence.addAll(currentSequence);
            }
            // o indíce anterior passa a ser o indíce corrente
            previousVersionIndex = currentVersionIndex;
        }
        return maxSequence;
    }

    /**
     * Encontra a maior sequência de versões, dado uma conjunto de ocorrências
     * de verões e uma lista de todas as versões. É essencial que as versões
     * estejam ordenadas.
     *
     * @param ocurrencesVersions Um conjunto distinto e ordenado de versões
     * @param allVersions A lista ordenada de todas as versões.
     * @return A maior sequência de versões.
     */
    public static List<Version> getMaxVersionSequence(final Map<Version, AtomicInteger> ocurrencesVersions, final List<Version> allVersions, final int minOccurrencesInOneVersion) {
        // armazena a maior sequência de versões
        final List<Version> maxSequence = new ArrayList<>();
        // armazena a sequência de versões para comparar com a sequência anterior
        final List<Version> currentSequence = new ArrayList<>();

        Collections.sort(allVersions, new VersionComparator());

        // guarda o indíce da última versão
        int previousVersionIndex = -1;
        for (Version version : ocurrencesVersions.keySet()) {
            if (ocurrencesVersions.get(version).get() < minOccurrencesInOneVersion) {
                continue;
            }
            // obtem o indíce da versão
            // é importante que as versões estejam na ordem
            final int currentVersionIndex = allVersions.indexOf(version);

            // se a versão corrente NÃO É a versão seguinte da versão anterior
            if (previousVersionIndex + 1 != currentVersionIndex) {
                currentSequence.clear(); // limpa a sequência de versões anteriores armazenadas
            }
            // armazena a versão corrente na lista
            currentSequence.add(version);

            // verifica se a sequência de versões corrente contém mais versões
            if (currentSequence.size() >= maxSequence.size()) {
                // caso tenha mais versões que a sequência anterior, substitui
                // pela sequência corrente
                maxSequence.clear();
                maxSequence.addAll(currentSequence);
            }
            // o indíce anterior passa a ser o indíce corrente
            previousVersionIndex = currentVersionIndex;
        }
        return maxSequence;
    }

    public static List<Version> listStringToListVersion(final List<String> versions) {
        final List<Version> list = new ArrayList<>();
        for (final String version : versions) {
            list.add(new Version(version));
        }
        return list;
    }
}
