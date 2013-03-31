/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserModifySameFileInMilestoneServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class TestWithJungServices extends AbstractMetricServices {

    public TestWithJungServices(GenericDao dao) {
        super(dao);
    }

    public TestWithJungServices(GenericDao dao, EntityMatriz matriz, Map params) {
        super(dao, matriz, params);
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getMatriz() == null
                || !getMatriz().getClassServicesName().equals(UserModifySameFileInMilestoneServices.class.getName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Service: " + UserModifySameFileInMilestoneServices.class.getName());
        }

        System.out.println("Preenchendo grafico");
        UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();

        for (int i = 0; i < getMatriz().getNodes().size(); i++) {
            EntityMatrizNode node = getMatriz().getNodes().get(i);
            String[] coluns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            if (coluns.length == 3) {
                /*
                 * Adicionar Edge (String e, String v1, String v2)
                 * onde:
                 * e = file
                 * v1 = user1
                 * v2 = user2
                 */
                graph.addEdge(coluns[2] + " (" + i + ")", coluns[1], coluns[0]);
            }
        }

        Util.convertGraphToImage(graph);

    }

    @Override
    public String getHeadCSV() {
        return "";
    }
}
