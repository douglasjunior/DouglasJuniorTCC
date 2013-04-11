/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserModifySameFileInMilestoneServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileCount;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxUserMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class FileBetweenessDistanceDegreeClosenessServices extends AbstractMetricServices {

    private EntityRepository repository;

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

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        System.out.println("Selecionado matriz com " + getMatriz().getNodes().size() + " nodes.");
        UndirectedSparseMultigraph<String, String> graphMulti = new UndirectedSparseMultigraph<>();
        UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();
        List<String> files = new ArrayList<>();
        for (int i = 0; i < getMatriz().getNodes().size(); i++) {
            EntityMatrizNode node = getMatriz().getNodes().get(i);
            String[] coluns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            colectFile(files, coluns[2]);
            graph.addEdge(
                    coluns[2] + "(" + i + ")",
                    coluns[1],
                    coluns[0],
                    EdgeType.UNDIRECTED);
            graphMulti.addEdge(
                    coluns[2] + "(" + i + ")",
                    coluns[1],
                    coluns[0],
                    EdgeType.UNDIRECTED);
        }

        BetweennessCentrality<String, String> btwGen = new BetweennessCentrality<>(graph);
        ClosenessCentrality<String, String> clsGen = new ClosenessCentrality<>(graph);
        DegreeScorer<String> dgrGen = new DegreeScorer<>(graph);

        List<AuxUserMetrics> userMetrics = new ArrayList<>();

        for (String vertexUser : graphMulti.getVertices()) {
            userMetrics.add(new AuxUserMetrics(vertexUser,
                    btwGen.getVertexScore(vertexUser), // betweeness
                    dgrGen.getVertexScore(vertexUser), // degree
                    clsGen.getVertexScore(vertexUser))); // closeness
        }

        List<AuxFileMetrics> fileMetrics = new ArrayList<>();
        List<AuxUserFile> occurrences = new ArrayList<>();

        for (String file : files) {
            Double btwMax = 0d, btwAve, btwSum = 0d;
            Double dgrMax = 0d, dgrAve, dgrSum = 0d;
            Double clsMax = 0d, clsAve, clsSum = 0d;
            Double codeChurn = 0d, updates = 0d, dev = 0d;
            for (String edgeFile : graphMulti.getEdges()) {
                if (edgeFile.startsWith(file)) {
                    for (AuxUserMetrics auxUser : userMetrics) {
                        if (graphMulti.isIncident(auxUser.getUser(), edgeFile)) {
                            AuxUserFile reg = new AuxUserFile(auxUser.getUser(), file);
                            if (!occurrences.contains(reg)) {
                                occurrences.add(reg);
                                Double btw = auxUser.getMetrics()[0];
                                btwMax = calculeMax(btw, btwMax);
                                btwSum += btw;

                                Double dgr = auxUser.getMetrics()[1];
                                dgrMax = calculeMax(dgr, dgrMax);
                                dgrSum += dgr;

                                Double cls = auxUser.getMetrics()[2];
                                clsMax = calculeMax(cls, clsMax);
                                clsSum += cls;

                                dev++;
                            }
                        }
                    }
                }
            }
            btwAve = btwSum / dev;
            dgrAve = dgrSum / dev;
            clsAve = clsSum / dev;

            codeChurn = calculeCodeChurn(file);
            updates = calculeUpdates(file);

            fileMetrics.add(new AuxFileMetrics(file,
                    btwMax, btwAve, btwSum,
                    dgrMax, dgrAve, dgrSum,
                    clsMax, clsAve, clsSum,
                    dev, codeChurn, updates));
        }

        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;"
                + "btwMax;btwAve;btwSum;"
                + "dgrMax;dgrAve;dgrSum;"
                + "clsMax;clsAve;clsSum;"
                + "developers;codeChurn;updates";
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

    private Double calculeCodeChurn(String fileName) {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, SUM(f.changes)) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p.issue.milestone.number = :milestoneNumber AND "
                + "f.filename = :fileName "
                + "GROUP BY f.filename";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "milestoneNumber"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            getMilestoneNumber()
        };

        List<AuxFileCount> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        return Double.valueOf(result.get(0).getCount());
    }

    private Double calculeUpdates(String fileName) {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, COUNT(f)) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p.issue.milestone.number = :milestoneNumber AND "
                + "f.filename = :fileName "
                + "GROUP BY f.filename";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "milestoneNumber"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            getMilestoneNumber()
        };

        List<AuxFileCount> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        return Double.valueOf(result.get(0).getCount());
    }

    private Integer getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    private EntityRepository getRepository() {
        String[] repoStr = getMatriz().getRepository().split("/");
        List<EntityRepository> repos = dao.executeNamedQueryComParametros(
                "Repository.findByNameAndOwner",
                new String[]{"login", "name"},
                new Object[]{repoStr[0], repoStr[1]});
        if (repos.size() == 1) {
            return repos.get(0);
        }
        return null;
    }
}
