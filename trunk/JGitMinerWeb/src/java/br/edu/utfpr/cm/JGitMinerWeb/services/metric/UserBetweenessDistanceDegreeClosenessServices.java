/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserModifySameFileInMilestoneServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxUserMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.DistanceCentralityScorer;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class UserBetweenessDistanceDegreeClosenessServices extends AbstractMetricServices {

    public UserBetweenessDistanceDegreeClosenessServices(GenericDao dao) {
        super(dao);
    }

    public UserBetweenessDistanceDegreeClosenessServices(GenericDao dao, EntityMatriz matriz, Map params) {
        super(dao, matriz, params);
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getMatriz() == null
                || !getMatriz().getClassServicesName().equals(UserModifySameFileInMilestoneServices.class.getName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Service: " + UserModifySameFileInMilestoneServices.class.getName());
        }

        System.out.println("Selecionado matriz com " + getMatriz().getNodes().size() + " nodes.");
        UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();
        for (int i = 0; i < getMatriz().getNodes().size(); i++) {
            EntityMatrizNode node = getMatriz().getNodes().get(i);
            String[] coluns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            if (coluns.length >= 3) {
                graph.addEdge(coluns[2] + " (" + i + ")", coluns[1], coluns[0]);
            }
        }

        BetweennessCentrality<String, String> btw = new BetweennessCentrality<>(graph);
        DistanceCentralityScorer<String, String> dst = new DistanceCentralityScorer(graph, false);
        ClosenessCentrality<String, String> cls = new ClosenessCentrality<>(graph);
        DegreeScorer<String> dgr = new DegreeScorer<>(graph);
        List<AuxUserMetrics> userMetrics = new ArrayList<>();

        for (String vertex : graph.getVertices()) {
            userMetrics.add(new AuxUserMetrics(vertex,
                    btw.getVertexScore(vertex), // betweeness
                    dst.getVertexScore(vertex), // distance
                    dgr.getVertexScore(vertex), // degree
                    cls.getVertexScore(vertex))); // closeness
        }

        addToEntityMetricNodeList(userMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "user;betweeness;distance;degree;closeness";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySameFileInMilestoneServices.class.getName());
    }
}
