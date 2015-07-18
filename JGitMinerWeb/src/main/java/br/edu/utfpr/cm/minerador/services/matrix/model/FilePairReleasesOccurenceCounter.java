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

    public int getMaxVersionsSequenceOcurrences(final int minOccurrencesInOneVersion) {
        return VersionUtil.getMaxVersionSequence(releasesOcurrences, allVersions, minOccurrencesInOneVersion).size();
    }

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

    public int addVersionOccurrence(Version release) {
        if (releasesOcurrences.containsKey(release)) {
            return releasesOcurrences.get(release).incrementAndGet();
        } else {
            final AtomicInteger count = new AtomicInteger();
            releasesOcurrences.put(release, count);
            return count.incrementAndGet();
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
