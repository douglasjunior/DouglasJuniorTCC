package br.edu.utfpr.cm.JGitMinerWeb.services.metric.global;

import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.graph.Graph;

/**
 * Measures some properties of the graph.
 * 
 * @author Rodrigo T. Kuroda
 */
public class GlobalMeasureCalculator {

    /**
     * Calculates the size, ties, and diameter of the graph.
     * 
     * @param <V> Class of vertex
     * @param <E> Class of edge
     * @param graph Graph to measure
     * @return GlobalMeasure class, contains the result of each metric.
     */
    public static <V, E> GlobalMeasure calcule(
            final Graph<V, E> graph) {
        int size = graph.getVertexCount();
        int ties = graph.getEdgeCount();
        double diameter = DistanceStatistics.diameter(graph);
        return new GlobalMeasure(size, ties, diameter);
    }
}
