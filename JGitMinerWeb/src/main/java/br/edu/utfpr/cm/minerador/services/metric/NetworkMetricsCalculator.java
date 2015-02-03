package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.BetweennessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.ClosenessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.DegreeCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.util.DescriptiveStatisticsHelper;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class NetworkMetricsCalculator {

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

    public NetworkMetricsCalculator(final DirectedSparseGraph<String, String> issueGraph,
            final Map<String, Integer> edgesWeigth, final Set<Commenter> devsCommentters) {

        pairFileGlobal = GlobalMeasureCalculator.calcule(issueGraph);

        // Map<String, Double> barycenter = BarycenterCalculator.calcule(pairFileGraph, edgesWeigth);
        Map<String, Double> betweenness = BetweennessCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, Double> closeness = ClosenessCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, Integer> degree = DegreeCalculator.calcule(issueGraph);
        // Map<String, Double> eigenvector = EigenvectorCalculator.calcule(pairFileGraph, edgesWeigth);
        Map<String, EgoMeasure<String>> ego = EgoMeasureCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, StructuralHolesMeasure<String>> structuralHoles = StructuralHolesCalculator.calcule(issueGraph, edgesWeigth);

        final int size = devsCommentters.isEmpty() ? 1 : devsCommentters.size();

//      barycenterStatistics = new DescriptiveStatisticsHelper(size);
        betweennessStatistics = new DescriptiveStatisticsHelper(size);
        closenessStatistics = new DescriptiveStatisticsHelper(size);
        degreeStatistics = new DescriptiveStatisticsHelper(size);
//      eigenvectorStatistics = new DescriptiveStatisticsHelper(size);
        egoBetweennessStatistics = new DescriptiveStatisticsHelper(size);
        egoSizeStatistics = new DescriptiveStatisticsHelper(size);
//      egoPairsStatistics = new DescriptiveStatisticsHelper(size);
        egoTiesStatistics = new DescriptiveStatisticsHelper(size);
        egoDensityStatistics = new DescriptiveStatisticsHelper(size);
        efficiencyStatistics = new DescriptiveStatisticsHelper(size);
        effectiveSizeStatistics = new DescriptiveStatisticsHelper(size);
        constraintStatistics = new DescriptiveStatisticsHelper(size);
        hierarchyStatistics = new DescriptiveStatisticsHelper(size);

        for (Commenter user : devsCommentters) {
            String commenter = user.getName();

//          barycenterStatistics.addValue(barycenter.get(commenter));
            betweennessStatistics.addValue(betweenness.get(commenter));
            closenessStatistics.addValue(closeness.get(commenter));
            degreeStatistics.addValue(degree.get(commenter));
//          eigenvectorStatistics.addValue(eigenvector.get(commenter));

            final EgoMeasure<String> egoMetrics = ego.get(commenter);
            egoBetweennessStatistics.addValue(egoMetrics.getBetweennessCentrality());
            egoSizeStatistics.addValue(egoMetrics.getSize());
//          egoPairsStatistics.addValue(ego.get(commenter).getPairs());
            egoTiesStatistics.addValue(egoMetrics.getTies());
            egoDensityStatistics.addValue(egoMetrics.getDensity());

            final StructuralHolesMeasure<String> structuralHolesMetric = structuralHoles.get(commenter);
            efficiencyStatistics.addValue(structuralHolesMetric.getEfficiency());
            effectiveSizeStatistics.addValue(structuralHolesMetric.getEffectiveSize());
            constraintStatistics.addValue(structuralHolesMetric.getConstraint());
            hierarchyStatistics.addValue(structuralHolesMetric.getHierarchy());
        }
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

}
