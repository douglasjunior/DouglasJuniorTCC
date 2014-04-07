/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInAllDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
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
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Douglas
 */
public class PairFileBetweenessDistanceDegreeClosenessInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public PairFileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public PairFileBetweenessDistanceDegreeClosenessInDateServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    private Integer getIntervalOfMonths() {
        return getIntegerParam("intervalOfMonths");
    }

    private Date getBeginDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixBeginDate");
        }
        return getDateParam("beginDate");
    }

    private Date getEndDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixEndDate");
        }
        return getDateParam("endDate");
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

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();

        if (futureBeginDate == null && futureEndDate == null) {
            if (getIntervalOfMonths() == null || getIntervalOfMonths() == 0) {
                throw new IllegalArgumentException("A matriz selecionada não possui parâmetro de Interval Of Months, informe Future Begin Date e Future End Date.");
            }
            futureBeginDate = (Date) getEndDate().clone();
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) futureBeginDate.clone());
            cal.add(Calendar.MONTH, getIntervalOfMonths());
            futureEndDate = cal.getTime();
        }

        params.put("futureBeginDate", futureBeginDate);
        params.put("futureEndDate", futureEndDate);

        // user | file | file2 | user2 | weigth
        out.printLog("Iniciado calculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);
        AbstractTypedGraph<String, String> graphMulti;

        EdgeType type;
        out.printLog("Iniciando preenchimento do grapho.");
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInDateServices.class.getName()) || getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            graphMulti = new DirectedSparseMultigraph<>();
            type = EdgeType.DIRECTED;
        } else {
            graphMulti = new UndirectedSparseMultigraph<>();
            type = EdgeType.UNDIRECTED;
        }

        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        Set<AuxFileFile> pairFiles = new HashSet<>();
        Map<String, Integer> edgesWeigth = new HashMap<>();

        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            pairFiles.add(pairFile);

            String commiter1 = columns[0];
            String commiter2 = columns[3];

            /**
             * Extract all distinct developer that commit a pair of file
             */
            if (commitersPairFile.containsKey(pairFile)) {
                Set<String> commiters = commitersPairFile.get(pairFile);
                commiters.add(commiter1);
                commiters.add(commiter2);
            } else {
                Set<String> commiters = new HashSet<>();
                commiters.add(commiter1);
                commiters.add(commiter2);
                commitersPairFile.put(pairFile, commiters);
            }

            // adiciona conforme o peso
            String edgeName = pairFile.getFileName() + "-" + pairFile.getFileName2() + "-" + i;
            edgesWeigth.put(edgeName, Util.stringToInteger(columns[4]));

            graphMulti.addEdge(edgeName, commiter1, commiter2, type);
        }

        Transformer trans = createWeigthTransformer(graphMulti, edgesWeigth);

        BetweennessCentrality<String, String> btwGen = new BetweennessCentrality<>(graphMulti, trans);
        ClosenessCentrality<String, String> clsGen = new ClosenessCentrality<>(graphMulti, trans);
        DegreeScorer<String> dgrGen = new DegreeScorer<>(graphMulti);

        Map<String, AuxUserMetrics> usersMetrics = new HashMap<>();

        out.printLog("Iniciando calculo das metricas.");
        for (String vertexUser : graphMulti.getVertices()) {
            usersMetrics.put(vertexUser, new AuxUserMetrics(vertexUser,
                    btwGen.getVertexScore(vertexUser), // betweeness
                    dgrGen.getVertexScore(vertexUser), // degree
                    clsGen.getVertexScore(vertexUser))); // closeness
        }
        graphMulti = null;
        edgesWeigth.clear();

        List<AuxFileFileMetrics> fileMetrics = new ArrayList<>();

        out.printLog("Iniciando média, soma, updates, etc.");
        for (AuxFileFile pairFile : pairFiles) {
            Double btwMax = 0d, btwAve, btwSum = 0d;
            Double dgrMax = 0d, dgrAve, dgrSum = 0d;
            Double clsMax = 0d, clsAve, clsSum = 0d;
            Long codeChurn = 0l, futUpdates = 0l, updates = 0l, dev = 0l;
            Long codeChurn2 = 0l;

            for (String vertexUser : commitersPairFile.get(pairFile)) {
                AuxUserMetrics userMetric = usersMetrics.get(vertexUser);
                Double btw = userMetric.getMetrics()[0];
                btwMax = calculeMax(btw, btwMax);
                btwSum += btw;

                Double dgr = userMetric.getMetrics()[1];
                dgrMax = calculeMax(dgr, dgrMax);
                dgrSum += dgr;

                Double cls = userMetric.getMetrics()[2];
                clsMax = calculeMax(cls, clsMax);
                clsSum += cls;

                dev++;
            }

            btwAve = btwSum / dev;
            dgrAve = dgrSum / dev;
            clsAve = clsSum / dev;

            updates = calculeUpdates(pairFile.getFileName(), pairFile.getFileName2(), getBeginDate(), getEndDate());

            futUpdates = calculeUpdates(pairFile.getFileName(), pairFile.getFileName2(), futureBeginDate, futureEndDate);

            codeChurn = calculeCodeChurn(pairFile.getFileName(), pairFile.getFileName2(), getBeginDate(), getEndDate());
            codeChurn2 = calculeCodeChurn(pairFile.getFileName2(), pairFile.getFileName(), getBeginDate(), getEndDate());

            fileMetrics.add(new AuxFileFileMetrics(pairFile.getFileName(), pairFile.getFileName2(),
                    fixNanValue(btwMax), fixNanValue(btwAve), fixNanValue(btwSum),
                    fixNanValue(dgrMax), fixNanValue(dgrAve), fixNanValue(dgrSum),
                    fixNanValue(clsMax), fixNanValue(clsAve), fixNanValue(clsSum),
                    dev,
                    updates, futUpdates,
                    codeChurn, codeChurn2, (codeChurn + codeChurn2) / 2));
        }
        pairFiles.clear();
        commitersPairFile.clear();

        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "btwMax;btwAve;btwSum;"
                + "dgrMax;dgrAve;dgrSum;"
                + "clsMax;clsAve;clsSum;"
                + "developers;"
                + "cooUpates;futureCooUpdates;"
                + "codeChurn;codeChurn2;codeChurnAve";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInAllDateServices.class.getName());
    }

    private Double calculeMax(Double v, Double vMax) {
        if (vMax < v) {
            return v;
        }
        return vMax;
    }

    private long calculeUpdates(String fileName, String fileName2, Date beginDate, Date endDate) {
        String jpql = "SELECT COUNT(pp) "
                + "FROM "
                + "EntityPullRequest pp"
                + "WHERE "
                + "pp.repository = :repo AND "
                + "pp.createdAt BETWEEN :beginDate AND :endDate AND "
                + "EXISTS (SELECT p FROM EntityPullRequest p JOIN p.repositoryCommits r JOIN r.files f WHERE p = pp AND f.filename = :fileName) AND "
                + "EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";

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

        Long count = dao.selectOneWithParams(jpql, bdParams, bdObjects);

        return count != null ? count : 0l;
    }

    private Long calculeCodeChurn(String fileName, String fileName2, Date beginDate, Date endDate) {
        String jpql = "SELECT SUM(ff.changes) "
                + "FROM "
                + "EntityPullRequest pp JOIN pp.repositoryCommits rc JOIN rc.files ff "
                + "WHERE "
                + "pp.repository = :repo AND "
                + "pp.createdAt BETWEEN :beginDate AND :endDate AND "
                + "ff.filename = :fileName AND "
                + "EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";

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

        Long sum = dao.selectOneWithParams(jpql, bdParams, bdObjects);

        return sum != null ? sum : 0l;
    }
    
    private Double fixNanValue(Double value){
        if (value.isNaN()) {
            return 0d;
        }
        return value;
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

    private static <V, E> Transformer<E, ? extends Number> createWeigthTransformer(final Graph<V, E> graph, final Map<E, ? extends Number> edgeWeigth) {
        Transformer<E, ? extends Number> edgeWeigthTransformer = new Transformer<E, Number>() {
            @Override
            public Number transform(E edge) {
                Number num = edgeWeigth.get(edge);
                return num != null ? num : 0;
            }
        };
        return edgeWeigthTransformer;
    }
}
