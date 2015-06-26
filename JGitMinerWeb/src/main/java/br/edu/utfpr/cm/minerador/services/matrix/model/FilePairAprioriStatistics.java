package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairAprioriStatistics {

    private static final double MEDIAN_PERCENTILE = 50.0d;

    private final Set<FilePairApriori> filePairsApriori;
    private final DescriptiveStatistics supportStatistics;
    private final DescriptiveStatistics confidenceStatistics;
    private final Map<FilterByApriori, AtomicInteger> countByFilter;
    private final Set<FilterByApriori> aprioriFilters;

    public FilePairAprioriStatistics() {
        aprioriFilters = new HashSet<>();
        filePairsApriori = new HashSet<>();
        supportStatistics = new DescriptiveStatistics();
        confidenceStatistics = new DescriptiveStatistics();
        countByFilter = new HashMap<>();
    }

    public FilePairAprioriStatistics(Set<FilterByApriori> aprioriFilters) {
        this.aprioriFilters = aprioriFilters;
        filePairsApriori = new HashSet<>();
        supportStatistics = new DescriptiveStatistics();
        confidenceStatistics = new DescriptiveStatistics();
        countByFilter = new HashMap<>();
        for (FilterByApriori aprioriFilter : aprioriFilters) {
            countByFilter.put(aprioriFilter, new AtomicInteger());
        }
    }

    public Set<FilePairApriori> getFilePairsApriori() {
        return Collections.unmodifiableSet(filePairsApriori);
    }

    public boolean addFilePairApriori(FilePairApriori filePairApriori) {
        boolean added = filePairsApriori.add(filePairApriori);
        if (added) {
            supportStatistics.addValue(filePairApriori.getSupportFilePair());
            confidenceStatistics.addValue(filePairApriori.getHigherConfidence());
            for (FilterByApriori aprioriFilter : aprioriFilters) {
                if (filePairApriori.fits(aprioriFilter)) {
                    countByFilter.get(aprioriFilter).incrementAndGet();
                }
            }
        }
        return added;
    }

    public boolean addFilePairApriori(Collection<FilePairApriori> filePairsApriori) {
        boolean added = false;
        for (FilePairApriori filePairsApriori1 : filePairsApriori) {
            added |= addFilePairApriori(filePairsApriori1);
        }
        return added;
    }

    public double getConfidenceMin() {
        return confidenceStatistics.getMin();
    }

    public double getConfidenceMax() {
        return confidenceStatistics.getMax();
    }

    public double getConfidenceMedian() throws MathIllegalStateException, MathIllegalArgumentException {
        return confidenceStatistics.getPercentile(MEDIAN_PERCENTILE);
    }

    public double getSupportMin() {
        return supportStatistics.getMin();
    }

    public double getSupportMax() {
        return supportStatistics.getMax();
    }

    public double getSupportMedian() throws MathIllegalArgumentException, MathIllegalStateException {
        return supportStatistics.getPercentile(MEDIAN_PERCENTILE);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSupportMedian()).append(";")
                .append(getSupportMax()).append(";")
                .append(getSupportMin()).append(";")
                .append(getConfidenceMedian()).append(";")
                .append(getConfidenceMax()).append(";")
                .append(getConfidenceMin());

        for (FilterByApriori aprioriFilter : aprioriFilters) {
            sb.append(";").append(countByFilter.get(aprioriFilter));
        }
        return sb.toString();
    }

    public static String getHeader() {
        return "Median Support;Max Support;Min Support;Median Confidence;Max Confidence;Min Confidence";
    }

    public String getDynamicHeader() {
        StringBuilder sb = new StringBuilder();
        for (FilterByApriori aprioriFilter : aprioriFilters) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            if (aprioriFilter.getMinSupport() > 0.0d) {
                sb.append(aprioriFilter.getMinSupport()).append("-");
            }
            sb.append(aprioriFilter.getMaxSupport());

            sb.append("|");

            if (aprioriFilter.getMinConfidence() > 0.0d) {
                sb.append(aprioriFilter.getMinConfidence()).append("-");
            }
            sb.append(aprioriFilter.getMaxConfidence());
        }
        return sb.toString();
    }
}
