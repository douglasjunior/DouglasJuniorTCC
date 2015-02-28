package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
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
import br.edu.utfpr.cm.minerador.services.socialnetwork.CommunicationNetworkBuilder;
import br.edu.utfpr.cm.minerador.services.socialnetwork.Network;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class NetworkMetricsCalculator {
    
    private final NetworkMetrics networkMetrics;
    
    public NetworkMetricsCalculator(final DirectedSparseGraph<String, String> issueGraph,
            final Map<String, Integer> edgesWeigth, final Set<Commenter> devsCommentters) {
        networkMetrics = calcule(issueGraph, edgesWeigth, devsCommentters);
    }

    public NetworkMetricsCalculator(Integer issue, BichoDAO bichoDAO) {
        final CommunicationNetworkBuilder builder = new CommunicationNetworkBuilder(bichoDAO);
        final Network<String, String> network = builder.buildDirectedWeightedNetwork(issue);
        networkMetrics = calcule(network.getNetwork(), network.getEdgesWeigth(), new HashSet<>(network.getCommenters()));
    }

    private NetworkMetrics calcule(final Graph<String, String> issueGraph,
            final Map<String, Integer> edgesWeigth,
            final Set<Commenter> devsCommentters) {
        GlobalMeasure pairFileGlobal = GlobalMeasureCalculator.calcule(issueGraph);

        // Map<String, Double> barycenter = BarycenterCalculator.calcule(pairFileGraph, edgesWeigth);
        Map<String, Double> betweenness = BetweennessCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, Double> closeness = ClosenessCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, Integer> degree = DegreeCalculator.calcule(issueGraph);
        // Map<String, Double> eigenvector = EigenvectorCalculator.calcule(pairFileGraph, edgesWeigth);
        Map<String, EgoMeasure<String>> ego = EgoMeasureCalculator.calcule(issueGraph, edgesWeigth);
        Map<String, StructuralHolesMeasure<String>> structuralHoles = StructuralHolesCalculator.calcule(issueGraph, edgesWeigth);

        final int size = devsCommentters.isEmpty() ? 1 : devsCommentters.size();

//      DescriptiveStatisticsHelper barycenterStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper betweennessStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper closenessStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper degreeStatistics = new DescriptiveStatisticsHelper(size);
//      DescriptiveStatisticsHelper eigenvectorStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper egoBetweennessStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper egoSizeStatistics = new DescriptiveStatisticsHelper(size);
//      DescriptiveStatisticsHelper egoPairsStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper egoTiesStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper egoDensityStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper efficiencyStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper effectiveSizeStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper constraintStatistics = new DescriptiveStatisticsHelper(size);
        DescriptiveStatisticsHelper hierarchyStatistics = new DescriptiveStatisticsHelper(size);

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

        return new NetworkMetrics(betweennessStatistics,
                closenessStatistics, degreeStatistics, egoBetweennessStatistics,
                egoSizeStatistics, egoTiesStatistics, egoDensityStatistics,
                efficiencyStatistics, effectiveSizeStatistics,
                constraintStatistics, hierarchyStatistics, pairFileGlobal);
    }

    public NetworkMetrics getNetworkMetrics() {
        return networkMetrics;
    }
}
