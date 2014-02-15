/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.model.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileCountSum;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxUserMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class PairFileBetweenessDistanceDegreeClosenessInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public PairFileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public PairFileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, EntityMatriz matriz, Map params, OutLog out) {
        super(dao, matriz, params, out);
    }

    public Date getFutureBeginDate() {
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
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

        System.out.println(getMatriz().getClassServicesName());

        if (getMatriz() == null
                || !getAvailableMatricesPermitted().contains(getMatriz().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        // user | file | file2 | user2 | weigth
        System.out.println("Selecionado matriz com " + getMatriz().getNodes().size() + " nodes.");
        AbstractTypedGraph<String, String> graphMulti;
        //    AbstractTypedGraph<String, String> graph;
        EdgeType type;
        if (getMatriz().getClassServicesName().equals(UserCommentedSamePairOfFileInDateServices.class.getName())) {
            //       graph = new DirectedSparseGraph<>();
            graphMulti = new DirectedSparseMultigraph<>();
            type = EdgeType.DIRECTED;
        } else {
            //     graph = new UndirectedSparseGraph<>();
            graphMulti = new UndirectedSparseMultigraph<>();
            type = EdgeType.UNDIRECTED;
        }
        List<AuxFileFile> files = new ArrayList<>();
        for (int i = 0; i < getMatriz().getNodes().size(); i++) {
            EntityMatrizNode node = getMatriz().getNodes().get(i);
            String[] coluns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            AuxFileFile auxFile = new AuxFileFile(coluns[1], coluns[2]);
            if (!files.contains(auxFile)) {
                files.add(auxFile);
            }
            //        graph.addEdge(
            //                coluns[2] + "(" + i + ")",
            //               coluns[1],
            //               coluns[0],
            //                type);
            // adiciona conforme o peso
            for (int j = 0; j < Util.stringToInteger(coluns[4]); j++) {
                graphMulti.addEdge(
                        coluns[1] + coluns[2] + i + j,
                        coluns[3],
                        coluns[0],
                        type);
            }
        }

        BetweennessCentrality<String, String> btwGen = new BetweennessCentrality<>(graphMulti);
        ClosenessCentrality<String, String> clsGen = new ClosenessCentrality<>(graphMulti);
        DegreeScorer<String> dgrGen = new DegreeScorer<>(graphMulti);

        List<AuxUserMetrics> userMetrics = new ArrayList<>();

        for (String vertexUser : graphMulti.getVertices()) {
            userMetrics.add(new AuxUserMetrics(vertexUser,
                    btwGen.getVertexScore(vertexUser), // betweeness
                    dgrGen.getVertexScore(vertexUser), // degree
                    clsGen.getVertexScore(vertexUser))); // closeness
        }

        List<AuxFileFileMetrics> fileMetrics = new ArrayList<>();
        List<AuxUserFileFile> occurrences = new ArrayList<>();
        
        for (AuxFileFile file : files) {
            Double btwMax = 0d, btwAve, btwSum = 0d;
            Double dgrMax = 0d, dgrAve, dgrSum = 0d;
            Double clsMax = 0d, clsAve, clsSum = 0d;
            Long futCodeChurn = 0l, codeChurn = 0l, futUpdates = 0l, updates = 0l, dev = 0l;
            for (String edge : graphMulti.getEdges()) {
                if (edge.startsWith(file.getFileName() + file.getFileName2())
                        || edge.startsWith(file.getFileName2() + file.getFileName())) {
                    for (AuxUserMetrics auxUser : userMetrics) {
                        if (graphMulti.isIncident(auxUser.getUser(), edge)) {
                            AuxUserFileFile reg = new AuxUserFileFile(auxUser.getUser(), file.getFileName(), file.getFileName2());
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

            updates = calculeUpdates(file.getFileName(), file.getFileName2(), getBeginDate(), getEndDate());

            futUpdates = calculeUpdates(file.getFileName(), file.getFileName2(), getFutureBeginDate(), getFutureEndDate());

            fileMetrics.add(new AuxFileFileMetrics(file.getFileName(), file.getFileName2(),
                    btwMax, btwAve, btwSum,
                    dgrMax, dgrAve, dgrSum,
                    clsMax, clsAve, clsSum,
                    dev,
                    codeChurn, futCodeChurn,
                    updates, futUpdates));
        }

        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "btwMax;btwAve;btwSum;"
                + "dgrMax;dgrAve;dgrSum;"
                + "clsMax;clsAve;clsSum;"
                + "developers;"
                + "codeChurn;futureCodeChurn;"
                + "updates;futureUpdates";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySamePairOfFileInDateServices.class.getName(), UserCommentedSamePairOfFileInDateServices.class.getName());
    }

    private Double calculeMax(Double v, Double vMax) {
        if (vMax < v) {
            return v;
        }
        return vMax;
    }

    private Long calculeUpdates(String fileName, String fileName2, Date beginDate, Date endDate) {
        String jpql = "SELECT COUNT(rc) "
                + "FROM "
                + "EntityRepositoryCommit rc "
                + "WHERE "
                + "rc.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "EXISTS (SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = rc AND f.filename = :fileName) AND "
                + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2)";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "fileName2",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(jpql, bdParams, bdObjects);
    }

    private EntityRepository getRepository() {
        String[] repoStr = getMatriz().getRepository().split("/");
        List<EntityRepository> repos = dao.executeNamedQueryWithParams(
                "Repository.findByNameAndOwner",
                new String[]{"login", "name"},
                new Object[]{repoStr[0], repoStr[1]});
        if (repos.size() == 1) {
            return repos.get(0);
        }
        return null;
    }
}
