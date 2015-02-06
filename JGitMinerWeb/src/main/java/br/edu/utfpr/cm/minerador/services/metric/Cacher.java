package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxCodeChurn;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.IssueMetrics;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Cacher {

    // cache for optimization number of pull requests where file is in,
    // reducing access to database
    private final Map<String, Long> issueFileMap = new HashMap<>();

    // cache for optimization file code churn (add, del, change),
    // reducing access to database
    private final Map<String, AuxCodeChurn> fileCodeChurnMap = new HashMap<>();
    private final Map<String, AuxCodeChurn> fileCodeChurnByIssueMap = new HashMap<>();
    // cummulative (i.e. until a date)
    private final Map<String, AuxCodeChurn> cummulativeCodeChurnRequestFileMap = new HashMap<>();

    // cache for optimization file commits made by user,
    // reducing access to database
    private final Map<String, AuxCodeChurn> fileUserCodeChurnMap = new HashMap<>();
    private final Map<String, AuxCodeChurn> fileUserCummulativeCodeChurnMap = new HashMap<>();

    // future issues
    private final Map<AuxFileFile, Long> futureIssuesMap = new HashMap<>();

    private final Map<Integer, IssueMetrics> issuesCommentsCacher = new HashMap<>();

    private final Map<Integer, NetworkMetricsCalculator> networkMetricsMap = new HashMap<>();

    private final BichoFileDAO fileDAO;
    private final BichoPairFileDAO pairFileDAO;

    public Cacher(BichoFileDAO fileDAO) {
        this.fileDAO = fileDAO;
        pairFileDAO = null;
    }

    public Cacher(BichoFileDAO fileDAO, BichoPairFileDAO pairFileDAO) {
        this.fileDAO = fileDAO;
        this.pairFileDAO = pairFileDAO;
    }

    // Internal method with Map (cacher) parameter
    private AuxCodeChurn calculeFileCodeChurn(Map<String, AuxCodeChurn> codeChurnCacher,
            String fileName, Date beginDate, Date endDate, Collection<Integer> issues) {
        if (codeChurnCacher.containsKey(fileName)) {
            return codeChurnCacher.get(fileName);
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, beginDate, endDate, issues);
            codeChurnCacher.put(fileName, sumCodeChurnFile);
            return sumCodeChurnFile;
        }
    }

    // Internal method with Map (cacher) parameter
    private AuxCodeChurn calculeFileCodeChurn(Map<String, AuxCodeChurn> codeChurnCacher,
            String fileName, String fixVersion, Collection<Integer> issues) {
        if (codeChurnCacher.containsKey(fileName)) {
            return codeChurnCacher.get(fileName);
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, fixVersion, issues);
            codeChurnCacher.put(fileName, sumCodeChurnFile);
            return sumCodeChurnFile;
        }
    }

    public Long calculeNumberOfIssues(String fileName, BichoFileDAO fileDAO, Date futureBeginDate, Date futureEndDate) {
        Long fileNumberOfPullrequestOfPairFuture;
        if (issueFileMap.containsKey(fileName)) {
            fileNumberOfPullrequestOfPairFuture = issueFileMap.get(fileName);
        } else {
            fileNumberOfPullrequestOfPairFuture = fileDAO.calculeNumberOfIssues(fileName, futureBeginDate, futureEndDate);
            issueFileMap.put(fileName, fileNumberOfPullrequestOfPairFuture);
        }
        return fileNumberOfPullrequestOfPairFuture;
    }

    public Long calculeNumberOfIssues(String fileName, String fixVersion) {
        Long fileNumberOfPullrequestOfPairFuture;
        if (issueFileMap.containsKey(fileName)) {
            fileNumberOfPullrequestOfPairFuture = issueFileMap.get(fileName);
        } else {
            fileNumberOfPullrequestOfPairFuture = fileDAO.calculeNumberOfIssues(fileName, fixVersion);
            issueFileMap.put(fileName, fileNumberOfPullrequestOfPairFuture);
        }
        return fileNumberOfPullrequestOfPairFuture;
    }

    public double calculeDevFileExperience(final Long changes, String fileName, String user, Date beginDate, Date endDate, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCodeChurnMap.containsKey(fileName)) {
            AuxCodeChurn sumCodeChurnFile = fileUserCodeChurnMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, user, beginDate, endDate, issues);
            fileUserCodeChurnMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }
        return changes == 0 ? 0.0 : (double) devChanges / (double) changes;
    }

    public double calculeCummulativeDevFileExperience(final Long changes, String fileName, String user, Date endDate, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCummulativeCodeChurnMap.containsKey(fileName)) {
            AuxCodeChurn sumCodeChurnFile = fileUserCummulativeCodeChurnMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, user, null, endDate, issues);
            fileUserCummulativeCodeChurnMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }
        return changes == 0 ? 0.0 : (double) devChanges / (double) changes;
    }

    public double calculeCummulativeDevFileExperience(final Long changes, String fileName, String user, String fixVersion, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCummulativeCodeChurnMap.containsKey(fileName)) {
            AuxCodeChurn sumCodeChurnFile = fileUserCummulativeCodeChurnMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCummulativeCodeChurnByFilename(fileName, user, fixVersion, issues);
            fileUserCummulativeCodeChurnMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }
        return changes == 0 ? 0.0 : (double) devChanges / (double) changes;
    }

    public double calculeDevFileExperience(final Long changes, String fileName, String user, String fixVersion, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCodeChurnMap.containsKey(fileName)) {
            AuxCodeChurn sumCodeChurnFile = fileUserCodeChurnMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, user, fixVersion, issues);
            fileUserCodeChurnMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }
        return changes == 0 ? 0.0 : (double) devChanges / (double) changes;
    }

    public AuxCodeChurn calculeFileCummulativeCodeChurn(String fileName, Date endDate, Set<Integer> issues) {
        return calculeFileCodeChurn(cummulativeCodeChurnRequestFileMap, fileName, null, endDate, issues);
    }

    public AuxCodeChurn calculeFileCummulativeCodeChurn(String fileName, String fixVersion, Set<Integer> issues) {
        if (cummulativeCodeChurnRequestFileMap.containsKey(fileName)) {
            return cummulativeCodeChurnRequestFileMap.get(fileName);
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCummulativeCodeChurnByFilename(fileName, fixVersion, issues);
            cummulativeCodeChurnRequestFileMap.put(fileName, sumCodeChurnFile);
            return sumCodeChurnFile;
        }
    }

    public AuxCodeChurn calculeFileCodeChurnByIssues(String fileName, Date beginDate, Date endDate, Set<Integer> issues) {
        return calculeFileCodeChurn(fileCodeChurnByIssueMap, fileName, beginDate, endDate, issues);
    }

    public AuxCodeChurn calculeFileCodeChurnByIssues(String fileName, String fixVersion, Set<Integer> issues) {
        return calculeFileCodeChurn(fileCodeChurnByIssueMap, fileName, fixVersion, issues);
    }

    public AuxCodeChurn calculeFileCodeChurn(String fileName, Date beginDate, Date endDate) {
        return calculeFileCodeChurn(fileCodeChurnMap, fileName, beginDate, endDate, null);
    }

    public AuxCodeChurn calculeFileCodeChurn(String fileName, String fixVersion) {
        return calculeFileCodeChurn(fileCodeChurnMap, fileName, fixVersion, null);
    }

    public long calculeFutureNumberOfIssues(AuxFileFile fileFile, String futureVersion) {
        if (futureIssuesMap.containsKey(fileFile)) {
            return futureIssuesMap.get(fileFile);
        } else {
            long futureIssues = pairFileDAO.calculeNumberOfIssues(
                    fileFile.getFileName(), fileFile.getFileName2(),
                    futureVersion);
            futureIssuesMap.put(fileFile, futureIssues);
            return futureIssues;
        }
    }

    public IssueMetrics calculeIssueMetrics(Integer issue) {
        if (issuesCommentsCacher.containsKey(issue)) {
            return issuesCommentsCacher.get(issue);
        } else {
            IssueMetrics metric = pairFileDAO.listIssues(issue);
            issuesCommentsCacher.put(issue, metric);
            return metric;
        }
    }

    NetworkMetricsCalculator calculeNetworkMetrics(Integer issue, DirectedSparseGraph<String, String> issueGraph, Map<String, Integer> edgesWeigth, Set<Commenter> devsCommentters) {
        NetworkMetricsCalculator networkMetrics;
        if (networkMetricsMap.containsKey(issue)) {
            networkMetrics = networkMetricsMap.get(issue);
        } else {
            networkMetrics = new NetworkMetricsCalculator(issueGraph, edgesWeigth, devsCommentters);
            networkMetricsMap.put(issue, networkMetrics);
        }
        return networkMetrics;
    }
}
