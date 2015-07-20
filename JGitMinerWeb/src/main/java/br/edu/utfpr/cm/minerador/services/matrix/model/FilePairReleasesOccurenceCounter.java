package br.edu.utfpr.cm.minerador.services.matrix.model;

import br.edu.utfpr.cm.minerador.services.util.VersionUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairReleasesOccurenceCounter {

    private final FilePair filePair;
    private final Map<Version, AtomicInteger> releasesOcurrences;
    /**
     * For determine the maximum sequence of version
     */
    private final List<Version> allVersions;

    public FilePairReleasesOccurenceCounter(FilePair filePair) {
        this.filePair = filePair;
        this.releasesOcurrences = new LinkedHashMap<>();
        this.allVersions = new ArrayList<>();

        for (Version version : allVersions) {
            final AtomicInteger counter = new AtomicInteger();
            releasesOcurrences.put(version, counter);
        }
    }

    public FilePairReleasesOccurenceCounter(FilePair filePair, List<Version> allVersions) {
        this.filePair = filePair;
        this.releasesOcurrences = new LinkedHashMap<>();
        this.allVersions = allVersions;

        for (Version version : allVersions) {
            final AtomicInteger counter = new AtomicInteger();
            releasesOcurrences.put(version, counter);
        }
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public int getVersionsOcurrencesSize() {
        return releasesOcurrences.size();
    }

    public Set<Version> getVersionsOcurrences() {
        return releasesOcurrences.keySet();
    }

    public int getMaxVersionsSequenceOcurrences() {
        return getMaxVersionsSequenceOcurrences(1);
    }

    /**
     * Returns the maximum sequence of versions with minimum occurrences in each
     * version. For example, suppose the file pair AB changed 1 time in version
     * 1.0 and 2 times in versions 1.1. If the minimum occurrence is 2, then the
     * maximum sequence will 1 (i.e. AB occurred 2 times only in version 1.1).
     *
     * @param minOccurrencesInOneVersion The minimum occurrences to considers a
     * version.
     * @return the quantity of maximum sequence.
     */
    public int getMaxVersionsSequenceOcurrences(final int minOccurrencesInOneVersion) {
        return VersionUtil.getMaxVersionSequence(releasesOcurrences, allVersions, minOccurrencesInOneVersion).size();
    }

    /**
     * Returns if this file pair has the minimum occurrences in specified
     * version.
     *
     * @param version Version to check the minimum occurrence
     * @param minOccurrencesInVersion The number of minimum occurrence in
     * version
     * @return true if the occurrences greater than or equal to (>=) minimum
     * occurrences specified, otherwise false.
     */
    public boolean hasAtLeastOccurrencesInVersion(Version version, int minOccurrencesInVersion) {
        AtomicInteger value = releasesOcurrences.get(version);
        return value.get() >= minOccurrencesInVersion;
    }

    public boolean hasAtLeastOccurrencesInOneVersion(int minOccurrencesInOneVersion) {
        for (Map.Entry<Version, AtomicInteger> entrySet : releasesOcurrences.entrySet()) {
            AtomicInteger value = entrySet.getValue();
            if (value.get() >= minOccurrencesInOneVersion) {
                return true;
            }
        }
        return false;
    }

    public void addVersionOccurrence(Version release) {
        if (releasesOcurrences.containsKey(release)) {
            releasesOcurrences.get(release).incrementAndGet();
        } else {
            final AtomicInteger count = new AtomicInteger();
            count.incrementAndGet();
            releasesOcurrences.put(release, count);
        }
    }

    public void addVersionOccurrence(Version release, int quantity) {
        if (releasesOcurrences.containsKey(release)) {
            releasesOcurrences.get(release).addAndGet(quantity);
        } else {
            final AtomicInteger count = new AtomicInteger(quantity);
            releasesOcurrences.put(release, count);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.filePair);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilePairReleasesOccurenceCounter other = (FilePairReleasesOccurenceCounter) obj;
        if (!Objects.equals(this.filePair, other.filePair)) {
            return false;
        }
        return true;
    }

}
