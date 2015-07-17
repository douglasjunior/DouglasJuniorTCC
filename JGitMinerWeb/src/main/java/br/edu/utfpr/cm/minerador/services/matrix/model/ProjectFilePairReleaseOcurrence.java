package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectFilePairReleaseOcurrence {

    public static String getHeader() {
        return "Project;# Versions;# Distinct Cochanges";
    }

    private final Project project;
    private final Set<Version> versions;
    private final Set<Version> maxVersionsSequence;
    private final Set<FilePair> cochanges;
    private final Map<FilePair, FilePairReleasesOccurenceCounter> filePairReleasesOccurenceCounter;
    private final FilePairOcurrencesGroup filePairOcurrencesGroup;
    private final int minOccurrencesInEachVersion;
    private List<Version> allVersions;

    public ProjectFilePairReleaseOcurrence(Project project, List<Version> allVersions, Collection<FilterFilePairByReleaseOcurrence> filtersOccurrences) {
        this.project = project;
        this.versions = new LinkedHashSet<>();
        this.maxVersionsSequence = new LinkedHashSet<>();
        this.cochanges = new HashSet<>();
        this.filePairOcurrencesGroup = new FilePairOcurrencesGroup(filtersOccurrences);
        this.filePairReleasesOccurenceCounter = new HashMap<>();
        this.minOccurrencesInEachVersion = 0;
        this.allVersions = allVersions;
    }

    public ProjectFilePairReleaseOcurrence(Project project, List<Version> allVersions, int minOccurrencesInEachVersion, List<FilterFilePairByReleaseOcurrence> filtersOccurrences) {
        this.project = project;
        this.allVersions = allVersions;
        this.minOccurrencesInEachVersion = minOccurrencesInEachVersion;
        this.versions = new LinkedHashSet<>();
        this.maxVersionsSequence = new LinkedHashSet<>();
        this.cochanges = new HashSet<>();
        this.filePairOcurrencesGroup = new FilePairOcurrencesGroup(filtersOccurrences);
        this.filePairReleasesOccurenceCounter = new HashMap<>();
    }

    public void addVersions(Collection<String> versions) {
        for (String version : versions) {
            this.versions.add(new Version(version));
        }
    }

    public void addVersion(Version version) {
        this.versions.add(version);
    }

    public boolean addFilePair(FilePair filePair) {
        return cochanges.add(filePair);
    }

    public boolean addFilePair(Collection<FilePair> filePair) {
        return cochanges.addAll(filePair);
    }

    public int addVersionForFilePair(FilePair filePair, Version version) {
        if (this.filePairReleasesOccurenceCounter.containsKey(filePair)) {
            return this.filePairReleasesOccurenceCounter.get(filePair).addVersionOccurrence(version);
        } else {
            final FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(filePair, allVersions);
            this.filePairReleasesOccurenceCounter.put(filePair, counter);
            return counter.addVersionOccurrence(version);
        }
    }

    public void addVersionForFilePair(Collection<FilePair> filePairs, Version version) {
        for (FilePair filePair : filePairs) {
            if (this.filePairReleasesOccurenceCounter.containsKey(filePair)) {
                this.filePairReleasesOccurenceCounter.get(filePair).addVersionOccurrence(version);
            } else {
                final FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(filePair, allVersions);
                counter.addVersionOccurrence(version);
                this.filePairReleasesOccurenceCounter.put(filePair, counter);
            }
        }
    }

    public Map<FilePair, FilePairReleasesOccurenceCounter> getFilePairReleasesOccurenceCounter() {
        return filePairReleasesOccurenceCounter;
    }

    public FilePairOcurrencesGroup getFilePairOcurrencesGroup() {
        return filePairOcurrencesGroup;
    }

    public Set<Version> getVersions() {
        return versions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.project);
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
        final ProjectFilePairReleaseOcurrence other = (ProjectFilePairReleaseOcurrence) obj;
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(project).append(";");
        sb.append(versions.size()).append(";");
        sb.append(cochanges.size());
        if (!filePairReleasesOccurenceCounter.isEmpty()) {
            filePairOcurrencesGroup.groupFilePairs(new ArrayList<>(getVersions()), filePairReleasesOccurenceCounter.values(), minOccurrencesInEachVersion);
            sb.append(";").append(filePairOcurrencesGroup.toString());
        }
        return sb.toString();
    }

}
