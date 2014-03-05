/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSameFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySameFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileCountSum;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxUserMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
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
public class FileBetweenessDistanceDegreeClosenessInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public FileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public FileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
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

        System.out.println(getMatrix().getClassServicesName());

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        System.out.println("Selecionado matriz com " + getMatrix().getNodes().size() + " nodes.");
        UndirectedSparseMultigraph<String, String> graphMulti = new UndirectedSparseMultigraph<>();
        UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();
        List<String> files = new ArrayList<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
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
            Double futCodeChurn = 0d, codeChurn = 0d, futUpdates = 0d, updates = 0d, dev = 0d;
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

            AuxFileCountSum aux = calculeCodeChurnAndUpdates(file, getBeginDate(), getEndDate());
            codeChurn = (double) aux.getSum();
            updates = (double) aux.getCount();

            aux = calculeCodeChurnAndUpdates(file, getFutureBeginDate(), getFutureEndDate());
            futCodeChurn = (double) aux.getSum();
            futUpdates = (double) aux.getCount();

            fileMetrics.add(new AuxFileMetrics(file,
                    btwMax, btwAve, btwSum,
                    dgrMax, dgrAve, dgrSum,
                    clsMax, clsAve, clsSum,
                    dev, codeChurn, futCodeChurn,
                    updates, futUpdates));
        }

        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;"
                + "btwMax;btwAve;btwSum;"
                + "dgrMax;dgrAve;dgrSum;"
                + "clsMax;clsAve;clsSum;"
                + "developers;"
                + "codeChurn;futureCodeChurn;"
                + "updates;futureUpdates";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySameFileInDateServices.class.getName(), UserCommentedSameFileInDateServices.class.getName());
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

    private AuxFileCountSum calculeCodeChurnAndUpdates(String fileName, Date beginDate, Date endDate) {
        String jpql = "SELECT NEW " + AuxFileCountSum.class.getName() + "(f.filename, COUNT(f), SUM(f.changes)) "
                + "FROM "
                + "EntityRepositoryCommit rc JOIN rc.files f  "
                + "WHERE "
                + "rc.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "f.filename = :fileName "
                + "GROUP BY f.filename";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            beginDate,
            endDate
        };

        List<AuxFileCountSum> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return new AuxFileCountSum(fileName, 0, 0);
        }
    }

    private EntityRepository getRepository() {
        String[] repoStr = getMatrix().getRepository().split("/");
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
