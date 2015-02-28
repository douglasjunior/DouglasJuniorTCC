package br.edu.utfpr.cm.minerador.services.metric.socialnetwork;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.util.DescriptiveStatisticsHelper;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class NetworkMetrics {

    public static final String HEADER
            = ""// + "brcAvg;brcSum;brcMax;"
            + "btwSum;btwAvg;btwMdn;btwMax;"
            + "clsSum;clsAvg;clsMdn;clsMax;"
            + "dgrSum;dgrAvg;dgrMdn;dgrMax;"
            //+ "egvSum;egvAvg;egvMax;"
            + "egoBtwSum;egoBtwAvg;egoBtwMdn;egoBtwMax;"
            + "egoSizeSum;egoSizeAvg;egoSizeMdn;egoSizeMax;"
            + "egoTiesSum;egoTiesAvg;egoTiesMdn;egoTiesMax;"
            // + "egoPairsSum;egoPairsAvg;egoPairsMax;"
            + "egoDensitySum;egoDensityAvg;egoDensityMdn;egoDensityMax;"
            + "efficiencySum;efficiencyAvg;efficiencyMdn;efficiencyMax;"
            + "efvSizeSum;efvSizeAvg;efvSizeMdn;efvSizeMax;"
            + "constraintSum;constraintAvg;constraintMdn;constraintMax;"
            + "hierarchySum;hierarchyAvg;hierarchyMdn;hierarchyMax;"
            + "size;ties;density;diameter;";

    private final DescriptiveStatisticsHelper betweennessStatistics;
    private final DescriptiveStatisticsHelper closenessStatistics;
    private final DescriptiveStatisticsHelper degreeStatistics;
//  private final DescriptiveStatisticsHelper eigenvectorStatistics;
    private final DescriptiveStatisticsHelper egoBetweennessStatistics;
    private final DescriptiveStatisticsHelper egoSizeStatistics;
//  private final  DescriptiveStatisticsHelper egoPairsStatistics;
    private final DescriptiveStatisticsHelper egoTiesStatistics;
    private final DescriptiveStatisticsHelper egoDensityStatistics;
    private final DescriptiveStatisticsHelper efficiencyStatistics;
    private final DescriptiveStatisticsHelper effectiveSizeStatistics;
    private final DescriptiveStatisticsHelper constraintStatistics;
    private final DescriptiveStatisticsHelper hierarchyStatistics;
    private final GlobalMeasure pairFileGlobal;

    public NetworkMetrics(DescriptiveStatisticsHelper betweennessStatistics,
            DescriptiveStatisticsHelper closenessStatistics,
            DescriptiveStatisticsHelper degreeStatistics,
            DescriptiveStatisticsHelper egoBetweennessStatistics,
            DescriptiveStatisticsHelper egoSizeStatistics,
            DescriptiveStatisticsHelper egoTiesStatistics,
            DescriptiveStatisticsHelper egoDensityStatistics,
            DescriptiveStatisticsHelper efficiencyStatistics,
            DescriptiveStatisticsHelper effectiveSizeStatistics,
            DescriptiveStatisticsHelper constraintStatistics,
            DescriptiveStatisticsHelper hierarchyStatistics,
            GlobalMeasure pairFileGlobal) {
        this.betweennessStatistics = betweennessStatistics;
        this.closenessStatistics = closenessStatistics;
        this.degreeStatistics = degreeStatistics;
        this.egoBetweennessStatistics = egoBetweennessStatistics;
        this.egoSizeStatistics = egoSizeStatistics;
        this.egoTiesStatistics = egoTiesStatistics;
        this.egoDensityStatistics = egoDensityStatistics;
        this.efficiencyStatistics = efficiencyStatistics;
        this.effectiveSizeStatistics = effectiveSizeStatistics;
        this.constraintStatistics = constraintStatistics;
        this.hierarchyStatistics = hierarchyStatistics;
        this.pairFileGlobal = pairFileGlobal;
    }

    public long getGlobalSize() {
        return pairFileGlobal.getSize();
    }

    public long getGlobalTies() {
        return pairFileGlobal.getTies();
    }

    public double getGlobalDensity() {
        return pairFileGlobal.getDensity();
    }

    public long getGlobalPairs() {
        return pairFileGlobal.getPairs();
    }

    public double getGlobalDiameter() {
        return pairFileGlobal.getDiameter();
    }

    public double getBetweennessMax() {
        return betweennessStatistics.getMax();
    }

    public double getBetweennessMean() {
        return betweennessStatistics.getMean();
    }

    public double getBetweennessSum() {
        return betweennessStatistics.getSum();
    }

    public double getBetweennessMedian() {
        return betweennessStatistics.getMedian();
    }

    public double getClosenessMax() {
        return closenessStatistics.getMax();
    }

    public double getClosenessMean() {
        return closenessStatistics.getMean();
    }

    public double getClosenessSum() {
        return closenessStatistics.getSum();
    }

    public double getClosenessMedian() {
        return closenessStatistics.getMedian();
    }

    public double getConstraintMax() {
        return constraintStatistics.getMax();
    }

    public double getConstraintMean() {
        return constraintStatistics.getMean();
    }

    public double getConstraintSum() {
        return constraintStatistics.getSum();
    }

    public double getConstraintMedian() {
        return constraintStatistics.getMedian();
    }

    public double getDegreeMax() {
        return degreeStatistics.getMax();
    }

    public double getDegreeMean() {
        return degreeStatistics.getMean();
    }

    public double getDegreeSum() {
        return degreeStatistics.getSum();
    }

    public double getDegreeMedian() {
        return degreeStatistics.getMedian();
    }

    public double getEffectiveSizeMax() {
        return effectiveSizeStatistics.getMax();
    }

    public double getEffectiveSizeMean() {
        return effectiveSizeStatistics.getMean();
    }

    public double getEffectiveSizeSum() {
        return effectiveSizeStatistics.getSum();
    }

    public double getEffectiveSizeMedian() {
        return effectiveSizeStatistics.getMedian();
    }

    public double getEfficiencyMax() {
        return efficiencyStatistics.getMax();
    }

    public double getEfficiencyMean() {
        return efficiencyStatistics.getMean();
    }

    public double getEfficiencySum() {
        return efficiencyStatistics.getSum();
    }

    public double getEfficiencyMedian() {
        return efficiencyStatistics.getMedian();
    }

    public double getEgoBetweennessMax() {
        return egoBetweennessStatistics.getMax();
    }

    public double getEgoBetweennessMean() {
        return egoBetweennessStatistics.getMean();
    }

    public double getEgoBetweennessSum() {
        return egoBetweennessStatistics.getSum();
    }

    public double getEgoBetweennessMedian() {
        return egoBetweennessStatistics.getMedian();
    }

    public double getEgoDensityMax() {
        return egoDensityStatistics.getMax();
    }

    public double getEgoDensityMean() {
        return egoDensityStatistics.getMean();
    }

    public double getEgoDensitySum() {
        return egoDensityStatistics.getSum();
    }

    public double getEgoDensityMedian() {
        return egoDensityStatistics.getMedian();
    }

    public double getEgoSizeMax() {
        return egoSizeStatistics.getMax();
    }

    public double getEgoSizeMean() {
        return egoSizeStatistics.getMean();
    }

    public double getEgoSizeSum() {
        return egoSizeStatistics.getSum();
    }

    public double getEgoSizeMedian() {
        return egoSizeStatistics.getMedian();
    }

    public double getEgoTiesMax() {
        return egoTiesStatistics.getMax();
    }

    public double getEgoTiesMean() {
        return egoTiesStatistics.getMean();
    }

    public double getEgoTiesSum() {
        return egoTiesStatistics.getSum();
    }

    public double getEgoTiesMedian() {
        return egoTiesStatistics.getMedian();
    }

    public double getHierarchyMax() {
        return hierarchyStatistics.getMax();
    }

    public double getHierarchyMean() {
        return hierarchyStatistics.getMean();
    }

    public double getHierarchySum() {
        return hierarchyStatistics.getSum();
    }

    public double getHierarchyMedian() {
        return hierarchyStatistics.getMedian();
    }

    @Override
    public String toString() {
        return ""// barycenterSum + ";" + barycenterAvg + ";" + barycenterMax + ";" +
                + this.getBetweennessSum() + ";" + this.getBetweennessMean() + ";" + this.getBetweennessMedian() + ";" + this.getBetweennessMax() + ";"
                + this.getClosenessSum() + ";" + this.getClosenessMean() + ";" + this.getClosenessMedian() + ";" + this.getClosenessMax() + ";"
                + this.getDegreeSum() + ";" + this.getDegreeMean() + ";" + this.getDegreeMedian() + ";" + this.getDegreeMax() + ";"
                // + eigenvectorSum + ";" + eigenvectorAvg + ";" + eigenvectorMax + ";" +
                + this.getEgoBetweennessSum() + ";" + this.getEgoBetweennessMean() + ";" + this.getEgoBetweennessMedian() + ";" + this.getEgoBetweennessMax() + ";"
                + this.getEgoSizeSum() + ";" + this.getEgoSizeMean() + ";" + this.getEgoSizeMedian() + ";" + this.getEgoSizeMax() + ";"
                + this.getEgoTiesSum() + ";" + this.getEgoTiesMean() + ";" + this.getEgoTiesMedian() + ";" + this.getEgoTiesMax() + ";"
                // + egoPairsSum + ";" + egoPairsAvg + ";" + egoPairsMax + ";" +
                + this.getEgoDensitySum() + ";" + this.getEgoDensityMean() + ";" + this.getEgoDensityMedian() + ";" + this.getEgoDensityMax() + ";"
                + this.getEfficiencySum() + ";" + this.getEfficiencyMean() + ";" + this.getEfficiencyMedian() + ";" + this.getEfficiencyMax() + ";"
                + this.getEffectiveSizeSum() + ";" + this.getEffectiveSizeMean() + ";" + this.getEffectiveSizeMedian() + ";" + this.getEffectiveSizeMax() + ";"
                + this.getConstraintSum() + ";" + this.getConstraintMean() + ";" + this.getConstraintMedian() + ";" + this.getConstraintMax() + ";"
                + this.getHierarchySum() + ";" + this.getHierarchyMean() + ";" + this.getHierarchyMedian() + ";" + this.getHierarchyMax() + ";"
                + this.getGlobalSize() + ";" + this.getGlobalTies() + ";"
                + this.getGlobalDensity() + ";" + this.getGlobalDiameter() + ";";
    }

}
