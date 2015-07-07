package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectVersionFilePairReleaseOcurrence {

    public static String getHeader() {
        return "Project;# Versions;# Distinct Cochanges";
    }

    private final Project project;
    private final Set<Version> versions;
    private final Set<FilePair> cochanges;
    private final Map<FilePair, FilePairReleasesOccurenceCounter> filePairReleasesOccurenceCounter;
    private final FilePairOcurrencesGroup filePairOcurrencesGroup;

    public ProjectVersionFilePairReleaseOcurrence(Project project, Collection<FilterFilePairByReleaseOcurrence> filterOccurrences) {
        this.project = project;
        this.versions = new HashSet<>();
        this.cochanges = new HashSet<>();
        this.filePairOcurrencesGroup = new FilePairOcurrencesGroup(filterOccurrences);
        this.filePairReleasesOccurenceCounter = new HashMap<>();
    }

    public void addVersions(Collection<String> versions) {
        for (String version : versions) {
            this.versions.add(new Version(version));
        }
    }

    public boolean addFilePair(FilePair filePair) {
        return cochanges.add(filePair);
    }

    public boolean addFilePair(Collection<FilePair> filePair) {
        return cochanges.addAll(filePair);
    }

    public void addVersionForFilePair(FilePair filePair, String version) {
        if (this.filePairReleasesOccurenceCounter.containsKey(filePair)) {
            this.filePairReleasesOccurenceCounter.get(filePair).addReleaseOcurrence(version);
        } else {
            final FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(filePair);
            counter.addReleaseOcurrence(version);
            this.filePairReleasesOccurenceCounter.put(filePair, counter);
        }
    }

    public void addVersionForFilePair(Collection<FilePair> filePairs, String version) {
        for (FilePair filePair : filePairs) {
            if (this.filePairReleasesOccurenceCounter.containsKey(filePair)) {
                this.filePairReleasesOccurenceCounter.get(filePair).addReleaseOcurrence(version);
            } else {
                final FilePairReleasesOccurenceCounter counter = new FilePairReleasesOccurenceCounter(filePair);
                counter.addReleaseOcurrence(version);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(project).append(";");
        sb.append(versions.size()).append(";");
        sb.append(cochanges.size());
        if (!filePairReleasesOccurenceCounter.isEmpty()) {
            filePairOcurrencesGroup.groupFilePairs(filePairReleasesOccurenceCounter.values());
            sb.append(";").append(filePairOcurrencesGroup.toString());
        }
        return sb.toString();
    }

}
