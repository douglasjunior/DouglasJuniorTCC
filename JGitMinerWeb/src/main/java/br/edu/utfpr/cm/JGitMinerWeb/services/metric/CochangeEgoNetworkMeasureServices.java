package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.PairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUser;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
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

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CochangeEgoNetworkMeasureServices extends AbstractMetricServices {

    private EntityRepository repository;

    private final PairFileDAO pairFileDAO;

    public CochangeEgoNetworkMeasureServices(GenericDao dao, OutLog out) {
        super(dao, out);
        pairFileDAO = new PairFileDAO(dao);
    }

    public CochangeEgoNetworkMeasureServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
        pairFileDAO = new PairFileDAO(dao);
    }

    public Date getBeginDate() {
        Calendar beginDateCalendar = Calendar.getInstance();
        beginDateCalendar.setTime(getDateParam("beginDate"));
        beginDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        beginDateCalendar.set(Calendar.MINUTE, 0);
        beginDateCalendar.set(Calendar.SECOND, 0);
        beginDateCalendar.set(Calendar.MILLISECOND, 0);
        return beginDateCalendar.getTime();
    }

    public Date getEndDate() {
        Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTime(getDateParam("endDate"));
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        endDateCalendar.set(Calendar.MILLISECOND, 999);
        return endDateCalendar.getTime();
    }

    public Date getFutureBeginDate() {
        Calendar futureBeginDateCalendar = Calendar.getInstance();
        futureBeginDateCalendar.setTime(getDateParam("futureBeginDate"));
        futureBeginDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        futureBeginDateCalendar.set(Calendar.MINUTE, 0);
        futureBeginDateCalendar.set(Calendar.SECOND, 0);
        futureBeginDateCalendar.set(Calendar.MILLISECOND, 0);
        return futureBeginDateCalendar.getTime();
    }

    public Date getFutureEndDate() {
        Calendar futureEndDateCalendar = Calendar.getInstance();
        futureEndDateCalendar.setTime(getDateParam("futureEndDate"));
        futureEndDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        futureEndDateCalendar.set(Calendar.MINUTE, 59);
        futureEndDateCalendar.set(Calendar.SECOND, 59);
        futureEndDateCalendar.set(Calendar.MILLISECOND, 999);
        return futureEndDateCalendar.getTime();
    }

    @Override
    public void run() {
        System.out.println(params);

        System.out.println(getMatrix().getClassServicesName());

        Date beginDate = getBeginDate();
        Date endDate = getEndDate();
        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matrix gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matrix.");
        }

        System.out.println("Selecionado matrix com " + getMatrix().getNodes().size() + " nodes.");

        final UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();

        final Map<String, Integer> edgeWeigth = new HashMap<>(getMatrix().getNodes().size());
        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        final Map<AuxFileFile, Long> pairFileCommitCount = new HashMap<>();

        final Set<String> distinctDevelopers = new HashSet<>();
        final Set<String> distinctFiles = new HashSet<>();

        Set<AuxFileFile> pairFiles = new HashSet<>();
        Map<String, AuxFileFile> edgeFiles = new HashMap<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            AuxUserUser pairUser = new AuxUserUser(columns[0], columns[3]);

            // to count unique developers
            distinctDevelopers.add(pairUser.getUser());
            distinctDevelopers.add(pairUser.getUser2());

            /**
             * Extract all distinct developer that commit a pair of file
             */
            if (commitersPairFile.containsKey(pairFile)) {
                Set<String> commiters = commitersPairFile.get(pairFile);
                commiters.add(pairUser.getUser());
                commiters.add(pairUser.getUser2());
            } else {
                Set<String> commiters = new HashSet<>();
                commiters.add(pairUser.getUser());
                commiters.add(pairUser.getUser2());
                commitersPairFile.put(pairFile, commiters);
            }

            String edgeName = pairUser.toString();

            // to count unique files
            distinctFiles.add(pairFile.getFileName());
            distinctFiles.add(pairFile.getFileName2());

            pairFiles.add(pairFile);
            edgeFiles.put(edgeName, pairFile);

            String weightedEdge = (String) params.get("weightedEdge");
            if ("false".equalsIgnoreCase(weightedEdge)) {
                // binary edge weight
                edgeWeigth.put(edgeName, 1);
            } else {
                /* Sum commit for each pair file that the pair dev has commited. */
                if (edgeWeigth.containsKey(pairUser.toStringUserAndUser2())) {
                    // edgeName = user + user2
                    edgeWeigth.put(pairUser.toStringUserAndUser2(), edgeWeigth.get(pairUser.toStringUserAndUser2()) + Integer.valueOf(columns[4]));
                } else if (edgeWeigth.containsKey(pairUser.toStringUser2AndUser())) {
                    // edgeName = user2 + user
                    edgeWeigth.put(pairUser.toStringUser2AndUser(), edgeWeigth.get(pairUser.toStringUser2AndUser()) + Integer.valueOf(columns[4]));
                } else {
                    edgeWeigth.put(edgeName, Integer.valueOf(columns[4]));
                }
            }

            if (!pairFileCommitCount.containsKey(pairFile)) {
                pairFileCommitCount.put(pairFile,
                        pairFileDAO.calculeSumUpdatesOfTwoFile( repository,
                                pairFile.getFileName(), pairFile.getFileName2(), 
                                beginDate, endDate));
            }

            /**
             * The co-change network is constructed as follow: If developer A
             * and developer B commits a pair file PF, then they are connected
             * in network.
             */
            if (!graph.containsVertex(pairUser.getUser())
                    || !graph.containsVertex(pairUser.getUser2())
                    || !graph.isNeighbor(pairUser.getUser(), pairUser.getUser2())) {
                graph.addEdge(edgeName, pairUser.getUser(), pairUser.getUser2(), EdgeType.UNDIRECTED);
            }
        }
        out.printLog("Weigth of edges:");
        for (Map.Entry<String, Integer> entry : edgeWeigth.entrySet()) {
            out.printLog(entry.getKey() + " " + entry.getValue());
        }
        out.printLog("End of weigth of edges.");

        /**
         * Calculates structural holes metrics for each developer on the
         * co-change network based.
         */
        Map<String, EgoMeasure<String>> metricsResult = EgoMeasureCalculator.calcule(graph);

        /**
         * For each pair of file, calculate the sum, average, and max of each
         * structural holes metrics of developer that commited the pair of file.
         */
        List<AuxFileFileMetrics> egoMetrics = new ArrayList<>(pairFiles.size());
        for (AuxFileFile pairFile : pairFiles) {
            long sizeMax = 0, sizeSum = 0;
            long pairsMax = 0, pairsSum = 0;
            long tiesMax = 0, tiesSum = 0;
            double sizeAvg, pairsAvg, tiesAvg;
            double densityMax = 0, densityAvg, densitySum = 0;
            double betweennessMax = 0, betweennessAvg, betweennessSum = 0;
            long developers = 0;
            for (String commiter : commitersPairFile.get(pairFile)) {
                EgoMeasure<String> commiterMetric = metricsResult.get(commiter);
                long size = commiterMetric.getSize();
                long pairs = commiterMetric.getPairs();
                long ties = commiterMetric.getTies();
                double density = commiterMetric.getDensity();
                double betweenness = commiterMetric.getBetweennessCentrality();

                sizeSum += size;
                pairsSum += pairs;
                tiesSum += ties;
                densitySum += density;
                betweennessSum += betweenness;

                sizeMax = Math.max(sizeMax, size);
                pairsMax = Math.max(pairsMax, pairs);
                tiesMax = Math.max(tiesMax, ties);
                densityMax = Math.max(densityMax, density);
                betweennessMax = Math.max(betweennessMax, betweenness);

                developers++;
            }
            sizeAvg = (double) sizeSum / (double) developers;
            tiesAvg = (double) tiesSum / (double) developers;
            pairsAvg = (double) pairsSum / (double) developers;
            densityAvg = densitySum / developers;
            betweennessAvg = betweennessSum / developers;

            Long updates = pairFileDAO.calculeUpdates(repository,
                            pairFile.getFileName(), pairFile.getFileName2(),
                            beginDate, endDate);
            Long futureUpdates = pairFileDAO.calculeUpdates(repository,
                            pairFile.getFileName(), pairFile.getFileName2(),
                            futureBeginDate, futureEndDate);

            egoMetrics.add(
                    new AuxFileFileMetrics(
                            pairFile.getFileName(), pairFile.getFileName2(),
                            sizeSum, sizeAvg, sizeMax,
                            pairsSum, pairsAvg, pairsMax,
                            tiesSum, tiesAvg, tiesMax,
                            densitySum, densityAvg, densityMax,
                            betweennessSum, betweennessAvg, betweennessMax,
                            developers, pairFileCommitCount.get(pairFile), updates, futureUpdates));
        }

        addToEntityMetricNodeList(egoMetrics);

        out.printLog("Distinct developers: " + distinctDevelopers.size());
        out.printLog("Distinct files: " + distinctFiles.size());
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "sizeSum;sizeAvg;sizeMax;"
                + "pairsSum;pairsAvg;pairsMax;"
                + "tiesSum;tiesAvg;tiesMax;"
                + "densitySum;densityAvg;densityMax;"
                + "betweennessSum;betweennessAvg;betweennessMax;"
                + "developers;commit;updates;futureUpdates";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(
                UserModifySamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInDateServices.class.getName()
        );
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
