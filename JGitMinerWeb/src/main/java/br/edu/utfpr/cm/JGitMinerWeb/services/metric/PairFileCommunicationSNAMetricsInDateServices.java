package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.PairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInAllDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.BarycenterCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.BetweennessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.ClosenessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.DegreeCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.EigenvectorCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Rodrigo T. Kuroda
 */
public class PairFileCommunicationSNAMetricsInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public PairFileCommunicationSNAMetricsInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public PairFileCommunicationSNAMetricsInDateServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
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
        out.printLog("Iniciado cálculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        out.printLog("Iniciando construção da rede.");

        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        Map<String, Integer> edgesWeigth = new HashMap<>();
        
        // rede de comunicação de cada par de arquivo
        Map<AuxFileFile, DirectedSparseMultigraph<String, String>> pairFileNetwork = new HashMap<>();

        // construindo a rede de comunicação para cada par de arquivo (desenvolvedores que comentaram)
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            
            // ignora %README%, %Rakefile, %CHANGELOG%, %Gemfile%, %.gitignore
            if (pairFile.getFileName().endsWith("README")
                    || pairFile.getFileName().endsWith("Rakefile")
                    || pairFile.getFileName().endsWith("CHANGELOG")
                    || pairFile.getFileName().endsWith("Gemfile")
                    || pairFile.getFileName().endsWith(".gitignore")) {
                out.printLog("Ignoring " + pairFile);
                continue;
            }
            
            PairFileDAO pairFileDAO = new PairFileDAO(dao);
            
            Long pairFileNumberOfPullrequestOfPairFuture = pairFileDAO
                    .calculeNumberOfPullRequest(repository, 
                            pairFile.getFileName(), pairFile.getFileName2(), 
                            futureBeginDate, futureEndDate);
            Long numberOfAllPullrequestFuture = pairFileDAO
                    .calculeNumberOfPullRequest(repository,
                            null, null, futureBeginDate, futureEndDate);
            
            Double supportPairFile = numberOfAllPullrequestFuture == 0 ? 0d : 
                    pairFileNumberOfPullrequestOfPairFuture.doubleValue() /
                    numberOfAllPullrequestFuture.doubleValue();
            
            // minimum support is 0.01, ignore file if lower than this (0.01)
            if (supportPairFile < Double.valueOf(0.01d)) {
                out.printLog("Ignoring " + pairFile + ": support " + supportPairFile);
                continue;
            }
            
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
            
            // check if network already created
            if (pairFileNetwork.containsKey(pairFile)) {
                pairFileNetwork.get(pairFile)
                        .addEdge(edgeName, commiter1, commiter2, EdgeType.DIRECTED);
            } else {
                DirectedSparseMultigraph<String, String> graphMulti = 
                        new DirectedSparseMultigraph<>();
                graphMulti.addEdge(edgeName, commiter1, commiter2, EdgeType.DIRECTED);
                pairFileNetwork.put(pairFile, graphMulti);
            }
        }

        out.printLog("Iniciando cálculo das metricas.");

        Set<AuxFileFileMetrics> fileFileMetrics = new HashSet<>();
        
        // calcula as métricas para cada par de arquivos
        // é a soma das métricas de todos os desenvolvedores
        for (Map.Entry<AuxFileFile, DirectedSparseMultigraph<String, String>> entry : pairFileNetwork.entrySet()) {
            AuxFileFile fileFile = entry.getKey();
            DirectedSparseMultigraph<String, String> graph = entry.getValue();
            
            Map<String, Double> barycenter = BarycenterCalculator.calcule(graph, edgesWeigth);
            Map<String, Double> betweenness = BetweennessCalculator.calcule(graph, edgesWeigth);
            Map<String, Double> closeness = ClosenessCalculator.calcule(graph, edgesWeigth);
            Map<String, Integer> degree = DegreeCalculator.calcule(graph);
            Map<String, Double> eigenvector = EigenvectorCalculator.calcule(graph, edgesWeigth);
            
            Double barycenterSum = 0d, barycenterAvg, barycenterMax = Double.NEGATIVE_INFINITY;
            Double betweennessSum = 0d, betweennessAvg, betweennessMax = Double.NEGATIVE_INFINITY;
            Double closenessSum = 0d, closenessAvg, closenessMax = Double.NEGATIVE_INFINITY;
            Integer degreeSum = 0, degreeMax = Integer.MIN_VALUE; 
            Double degreeAvg;
            Double eigenvectorSum = 0d, eigenvectorAvg, eigenvectorMax = Double.NEGATIVE_INFINITY;
            
            for (String dev : graph.getVertices()) {
                barycenterSum += barycenter.get(dev);
                betweennessSum += betweenness.get(dev);
                closenessSum += closeness.get(dev);
                degreeSum += degree.get(dev);
                eigenvectorSum += eigenvector.get(dev);
                
                barycenterMax = Math.max(barycenterMax, barycenter.get(dev));
                betweennessMax = Math.max(betweennessMax, betweenness.get(dev));
                closenessMax = Math.max(closenessMax, closeness.get(dev));
                degreeMax = Math.max(degreeMax, degree.get(dev));
                eigenvectorMax = Math.max(eigenvectorMax, eigenvector.get(dev));
            }
            
            // calculando medias
            double devCount = graph.getVertexCount();
            barycenterAvg = barycenterSum / devCount;
            betweennessAvg = betweennessSum / devCount;
            closenessAvg = closenessSum / devCount;
            degreeAvg = degreeSum / devCount;
            eigenvectorAvg = eigenvectorSum / devCount;
            
            Long updates = calculeUpdates(
                    fileFile.getFileName(), fileFile.getFileName2(), 
                    getBeginDate(), getEndDate());

            Long futureUpdates = calculeUpdates(
                    fileFile.getFileName(), fileFile.getFileName2(), 
                    futureBeginDate, futureEndDate);

            Long codeChurn = calculeCodeChurn(
                    fileFile.getFileName(), fileFile.getFileName2(), 
                    getBeginDate(), getEndDate());
            Long codeChurn2 = calculeCodeChurn(
                    fileFile.getFileName2(), fileFile.getFileName(),
                    getBeginDate(), getEndDate());

            double codeChurnAvg = (codeChurn + codeChurn2) / 2.0d;
            
            AuxFileFileMetrics auxFileFileMetrics = new AuxFileFileMetrics(
                    fileFile.getFileName(), fileFile.getFileName2(), 
                    barycenterSum, barycenterAvg, barycenterMax,
                    betweennessSum, betweennessAvg, betweennessMax,
                    closenessSum, closenessAvg, closenessMax,
                    degreeSum, degreeAvg, degreeMax,
                    eigenvectorSum, eigenvectorAvg, eigenvectorMax,
                    devCount,
                    updates, futureUpdates,
                    codeChurn, codeChurn2, codeChurnAvg
            );
            
            fileFileMetrics.add(auxFileFileMetrics);
        }

        addToEntityMetricNodeList(fileFileMetrics);
    }
    
    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "brcMax;brcAvg;brcSum;"
                + "btwMax;btwAvg;btwSum;"
                + "clsMax;clsAvg;clsSum;"
                + "dgrMax;dgrAvg;dgrSum;"
                + "egvMax;egvAvg;egvSum;"
                + "developers;"
                + "upates;futureUpdates;"
                + "codeChurn;codeChurn2;codeChurnAvg";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserCommentedSamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInAllDateServices.class.getName());
    }

    private long calculeUpdates(String fileName, String fileName2, Date beginDate, Date endDate) {
        String jpql = "SELECT COUNT(pp) "
                + "FROM "
                + "EntityPullRequest pp "
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
