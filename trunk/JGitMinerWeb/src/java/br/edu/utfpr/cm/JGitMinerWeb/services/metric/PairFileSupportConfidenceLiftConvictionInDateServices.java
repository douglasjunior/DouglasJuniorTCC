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
public class PairFileSupportConfidenceLiftConvictionInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public PairFileSupportConfidenceLiftConvictionInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public PairFileSupportConfidenceLiftConvictionInDateServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
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

        final Map<AuxFileFileMetrics, Set<String>> commitersPairFile = new HashMap<>();
        Set<AuxFileFileMetrics> pairFileMetrics = new HashSet<>();
        Map<String, Integer> edgesWeigth = new HashMap<>();

        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            AuxFileFileMetrics pairFile = new AuxFileFileMetrics(columns[1], columns[2]);
            pairFileMetrics.add(pairFile);

            String commiter1 = columns[0];
            String commiter2 = columns[3];
            /**
             * Extract all distinct developer that commit a pair of file
             */
            Set<String> commiters = commitersPairFile.get(pairFile);
            if (commiters == null) {
                commiters = new HashSet<>();
                commitersPairFile.put(pairFile, commiters);
            }
            commiters.add(commiter1);
            commiters.add(commiter2);

            // adiciona conforme o peso
            String edgeName = pairFile.getFile() + "-" + pairFile.getFile2() + "-" + i;
            edgesWeigth.put(edgeName, Util.stringToInteger(columns[4]));

            graphMulti.addEdge(edgeName, commiter1, commiter2, type);
        }

        out.printLog("Iniciando cálculo do support, confidence, lift e conviction.");
        for (AuxFileFileMetrics pairFile : pairFileMetrics) {
            Long pairFileNumberOfPullrequestOfPair = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), getBeginDate(), getEndDate());
            Long pairFileNumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), futureBeginDate, futureEndDate);
            Long fileNumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile(), null, futureBeginDate, futureEndDate);
            Long file2NumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile2(), null, futureBeginDate, futureEndDate);
            Long numberOfAllPullrequestFuture = calculeUpdates(null, null, futureBeginDate, futureEndDate);

            pairFile.addMetrics(pairFileNumberOfPullrequestOfPair, pairFileNumberOfPullrequestOfPairFuture, fileNumberOfPullrequestOfPairFuture, file2NumberOfPullrequestOfPairFuture, numberOfAllPullrequestFuture);

            Double supportFile = numberOfAllPullrequestFuture == 0 ? 0d : fileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportFile2 = numberOfAllPullrequestFuture == 0 ? 0d : file2NumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportPairFile = numberOfAllPullrequestFuture == 0 ? 0d : pairFileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double confidence = supportFile == 0 ? 0d : supportPairFile / supportFile;
            Double lift = supportFile * supportFile2 == 0 ? 0d : supportPairFile / (supportFile * supportFile2);
            Double conviction = 1 - confidence == 0 ? 0d : (1 - supportFile2) / (1 - confidence);

            pairFile.addMetrics(supportFile, supportFile2, supportPairFile, confidence, lift, conviction);
        }

        /*   Transformer trans = createWeigthTransformer(graphMulti, edgesWeigth);

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

         out.printLog("Iniciando média, soma, updates, etc.");
         for (AuxFileFileMetrics pairFile : pairFileMetrics) {
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

         updates = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), getBeginDate(), getEndDate());

         futUpdates = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), futureBeginDate, futureEndDate);

         codeChurn = calculeCodeChurn(pairFile, getBeginDate(), getEndDate());
         codeChurn2 = calculeCodeChurn(pairFile, getBeginDate(), getEndDate());

         pairFile.addMetrics(fixNanValue(btwMax), fixNanValue(btwAve), fixNanValue(btwSum),
         fixNanValue(dgrMax), fixNanValue(dgrAve), fixNanValue(dgrSum),
         fixNanValue(clsMax), fixNanValue(clsAve), fixNanValue(clsSum),
         dev,
         updates, futUpdates,
         codeChurn, codeChurn2, (codeChurn + codeChurn2) / 2);
         }
         commitersPairFile.clear();
         */
        addToEntityMetricNodeList(pairFileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "pairFileCochange;pairFileCochangeFuture;fileChangeFuture;file2ChangeFuture;allPullrequestFuture;"
                + "supportFile;supportFile2;supportPairFile;confidence;lift;conviction";
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

    private long calculeUpdates(String file, String file2, Date beginDate, Date endDate) {
        String jpql = "SELECT COUNT(pp) "
                + "FROM "
                + "EntityPullRequest pp "
                + "WHERE "
                + "pp.repository = :repo AND "
                + "pp.createdAt BETWEEN :beginDate AND :endDate ";

        if (file != null) {
            jpql += " AND EXISTS (SELECT p FROM EntityPullRequest p JOIN p.repositoryCommits r JOIN r.files f WHERE p = pp AND f.filename = :fileName) ";
        }

        if (file2 != null) {
            jpql += " AND EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";
        }

        String[] bdParams = new String[]{
            "repo",
            file != null ? "fileName" : "#none#",
            file2 != null ? "fileName2" : "#none#",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            file,
            file2,
            beginDate,
            endDate
        };

        Long count = dao.selectOneWithParams(jpql, bdParams, bdObjects);

        return count != null ? count : 0l;
    }

    private Long calculeCodeChurn(AuxFileFileMetrics pairFile, Date beginDate, Date endDate) {
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
            pairFile.getFile(),
            pairFile.getFile2(),
            beginDate,
            endDate
        };

        Long sum = dao.selectOneWithParams(jpql, bdParams, bdObjects);

        return sum != null ? sum : 0l;
    }

    private Double fixNanValue(Double value) {
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
