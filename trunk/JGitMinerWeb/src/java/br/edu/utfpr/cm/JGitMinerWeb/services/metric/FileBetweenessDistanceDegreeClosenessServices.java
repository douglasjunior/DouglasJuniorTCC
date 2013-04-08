/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserModifySameFileInMilestoneServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxUserMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class FileBetweenessDistanceDegreeClosenessServices extends AbstractMetricServices {

    public FileBetweenessDistanceDegreeClosenessServices(GenericDao dao) {
        super(dao);
    }

    public FileBetweenessDistanceDegreeClosenessServices(GenericDao dao, EntityMatriz matriz, Map params) {
        super(dao, matriz, params);
    }

    /*
     *
     Code churn => The number of lines of code that were either added or changed over the history of this file.
     Updates => The number of updates to the repository that included this file.
     Developers => The number of distinct developers who have updated this file over its history.
     (Sum/Average/Max) of Degree => The (sum/average/maximum) of each developer’s degree over a file’s history.
     (Sum/Average/Max) of Closeness => The (sum/average/maximum) of each developer’s Closeness over a file’s history.
     (Sum/Average/Max) of Betweenness => The (sum/average/maximum) of each developer’s Betweenness over a file’s history.
     Number of Hub Developers => The number of distinct hub developers who update this file.
     *
     Código alterado => O número de linhas de código que foram ou adicionados ou alterados ao longo da história deste arquivo.
     Atualizações => O número de atualizações para o repositório que incluiu este arquivo.
     Desenvolvedores => O número de desenvolvedores distintos que atualizaram este arquivo longo de sua história.
     (Soma / Média / Max) de grau => O (soma / média / máxima) de grau de cada desenvolvedor sobre a história de um arquivo.
     (Soma / Média / Max) de proximidade => O (soma / média / máxima) de proximidade de cada desenvolvedor sobre a história de um arquivo.
     (Soma / Média / Max) de intermediação => O (soma média / / máximo) de intermediação de cada desenvolvedor sobre a história de um arquivo.
     Número de Desenvolvedores de Hub => O número de desenvolvedores hub distintas que atualizam esse arquivo.
     */
    @Override
    public void run() {
        System.out.println(params);

        if (getMatriz() == null
                || !getMatriz().getClassServicesName().equals(UserModifySameFileInMilestoneServices.class.getName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Service: " + UserModifySameFileInMilestoneServices.class.getName());
        }

        System.out.println("Selecionado matriz com " + getMatriz().getNodes().size() + " nodes.");
        SparseGraph<String, String> graph = new SparseGraph<>();
        List<String> files = new ArrayList<>();
        for (int i = 0; i < getMatriz().getNodes().size(); i++) {
            EntityMatrizNode node = getMatriz().getNodes().get(i);
            String[] coluns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            colectFile(files, coluns[2]);
            if (coluns.length >= 3) {
                graph.addEdge(coluns[2] + "(" + i + ")", coluns[1], coluns[0]);
            }
        }

        BetweennessCentrality<String, String> btwGen = new BetweennessCentrality<>(graph);
        ClosenessCentrality<String, String> clsGen = new ClosenessCentrality<>(graph);
        DegreeScorer<String> dgrGen = new DegreeScorer<>(graph);

        List<AuxUserMetrics> userMetrics = new ArrayList<>();

        for (String vertex : graph.getVertices()) {
            userMetrics.add(new AuxUserMetrics(vertex,
                    btwGen.getVertexScore(vertex), // betweeness
                    dgrGen.getVertexScore(vertex), // degree
                    clsGen.getVertexScore(vertex))); // closeness
        }

        List<AuxFileMetrics> fileMetrics = new ArrayList<>();
        List<AuxUserFile> occurrences = new ArrayList<>();

        for (String file : files) {
            Double btwMax = 0d, btwAve, btwSum = 0d, btwCount = 0d;
            Double dgrMax = 0d, dgrAve, dgrSum = 0d, dgrCount = 0d;
            Double clsMax = 0d, clsAve, clsSum = 0d, clsCount = 0d;
            for (String edge : graph.getEdges()) {
                if (edge.startsWith(file)) {
                    for (AuxUserMetrics auxUser : userMetrics) {
                        if (graph.isIncident(auxUser.getUser(), edge)
                                || graph.isDest(auxUser.getUser(), edge)
                                || graph.isSource(auxUser.getUser(), edge)) {
                            System.out.println("       " + auxUser.getUser() + " \t\t " + file);
                            AuxUserFile reg = new AuxUserFile(auxUser.getUser(), file);
                            if (!occurrences.contains(reg)) {
                                occurrences.add(reg);
                                Double btw = auxUser.getMetrics()[0];
                                btwMax = calculeMax(btw, btwMax);
                                btwSum += btw;
                                btwCount++;

                                Double dgr = auxUser.getMetrics()[1];
                                dgrMax = calculeMax(dgr, dgrMax);
                                dgrSum += dgr;
                                dgrCount++;

                                Double cls = auxUser.getMetrics()[2];
                                clsMax = calculeMax(cls, clsMax);
                                clsSum += cls;
                                clsCount++;
                            }
                        }
                    }
                }
            }
            btwAve = btwSum / btwCount;
            dgrAve = dgrSum / dgrCount;
            clsAve = clsSum / clsCount;

            fileMetrics.add(new AuxFileMetrics(file,
                    btwMax, btwAve, btwSum,
                    dgrMax, dgrAve, dgrSum,
                    clsMax, clsAve, clsSum));
        }

        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;"
                + "btwMax;btwAve;btwSum;"
                + "dgrMax;dgrAve;dgrSum;"
                + "clsMax;clsAve;clsSum";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySameFileInMilestoneServices.class.getName());
    }

    private void colectFile(List<String> files, String file) {
        if (!files.contains(file)) {
            files.add(file);
        }
    }

    private Double calculeMax(Double v, Double vMax) {
        if (vMax < v) {
            return v;
        }
        return vMax;
    }

    private Double calculeMin(Double v, Double vMin) {
        if (vMin > v) {
            return v;
        }
        return vMin;
    }

    private Double calculeSum(Double v, Double vSum) {
        return vSum + v;
    }
}
