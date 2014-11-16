package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxCodeChurn;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Cacher {

    public static AuxCodeChurn calculeFileCodeChurn(Map<String, AuxCodeChurn> codeChurnRequestFileMap, String fileName, BichoFileDAO fileDAO, Date beginDate, Date endDate, Collection<Integer> issues) {
        if (codeChurnRequestFileMap.containsKey(fileName)) {
            return codeChurnRequestFileMap.get(fileName);
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, beginDate, endDate, issues);
            codeChurnRequestFileMap.put(fileName, sumCodeChurnFile);
            return sumCodeChurnFile;
        }
    }

    public static Long calculeNumberOfIssues(Map<String, Long> issueFileMap, String fileName, BichoFileDAO fileDAO, Date futureBeginDate, Date futureEndDate) {
        Long fileNumberOfPullrequestOfPairFuture;
        if (issueFileMap.containsKey(fileName)) {
            fileNumberOfPullrequestOfPairFuture = issueFileMap.get(fileName);
        } else {
            fileNumberOfPullrequestOfPairFuture = fileDAO.calculeNumberOfIssues(fileName, futureBeginDate, futureEndDate, true);
            issueFileMap.put(fileName, fileNumberOfPullrequestOfPairFuture);
        }
        return fileNumberOfPullrequestOfPairFuture;
    }

    public static double calculeDevFileExperience(final Long changes, Map<String, AuxCodeChurn> fileUserCommitMap, String fileName, String user, BichoFileDAO fileDAO, Date beginDate, Date endDate, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCommitMap.containsKey(fileName)) {
            AuxCodeChurn sumCodeChurnFile = fileUserCommitMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, user, beginDate, endDate, issues);
            fileUserCommitMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }
        return changes == 0 ? 0.0 : (double) devChanges / (double) changes;
    }

    public static long calculeFileCommits(Map<String, Long> fileCommitsFileMap, String fileName, BichoFileDAO fileDAO, Date beginDate, Date endDate, Collection<Integer> issues) {
        if (fileCommitsFileMap.containsKey(fileName)) {
            return fileCommitsFileMap.get(fileName);
        } else {
            long commits = fileDAO.countCommitsByFilename(fileName, beginDate, endDate, issues);
            fileCommitsFileMap.put(fileName, commits);
            return commits;
        }
    }

}
