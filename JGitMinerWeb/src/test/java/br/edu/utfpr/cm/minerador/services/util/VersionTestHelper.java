package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class VersionTestHelper {

    public static Set<Version> getVersionsAsSet(String... versions) {
        final Set<Version> occurrencesVersions = new LinkedHashSet<>();
        for (String version : versions) {
            occurrencesVersions.add(new Version(version));
        }
        return occurrencesVersions;
    }

    public static List<Version> getVersionsAsList(String... versions) {
        final List<Version> occurrencesVersions = new ArrayList<>();
        for (String version : versions) {
            occurrencesVersions.add(new Version(version));
        }
        return occurrencesVersions;
    }

    public static Map<Version, AtomicInteger> getVersionsAsMap(String... versions) {
        return getVersionsAsMap(1, versions);
    }

    public static Map<Version, AtomicInteger> getVersionsAsMap(int quantity, String... versions) {
        final Map<Version, AtomicInteger> occurrencesVersions = new LinkedHashMap<>();
        for (String versionString : versions) {
            Version version = new Version(versionString);
            if (occurrencesVersions.containsKey(version)) {
                occurrencesVersions.get(version).addAndGet(quantity);
            } else {
                final AtomicInteger atomicInteger = new AtomicInteger();
                occurrencesVersions.put(version, atomicInteger);
                atomicInteger.addAndGet(quantity);
            }
        }
        return occurrencesVersions;
    }

}
