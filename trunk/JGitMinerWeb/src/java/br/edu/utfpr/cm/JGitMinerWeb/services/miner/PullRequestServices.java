/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 *
 * @author Douglas
 */
public class PullRequestServices implements Serializable {

    public static EntityPullRequest createEntity(PullRequest gitPullRequest, GenericDao dao) {
        if (gitPullRequest == null) {
            return null;
        }

        EntityPullRequest pull = getPullRequestByIdPull(gitPullRequest.getId(), dao);

        if (pull == null) {
            pull = new EntityPullRequest();
        }

        pull.setMineredAt(new Date());
        pull.setMergeable(gitPullRequest.isMergeable());
        pull.setMerged(gitPullRequest.isMerged());
        pull.setClosedAt(gitPullRequest.getClosedAt());
        pull.setMergedAt(gitPullRequest.getMergedAt());
        pull.setUpdatedAt(gitPullRequest.getUpdatedAt());
        pull.setCreatedAt(gitPullRequest.getCreatedAt());
        pull.setIdPullRequest(gitPullRequest.getId());
        pull.setAdditions(gitPullRequest.getAdditions());
        pull.setChangedFiles(gitPullRequest.getChangedFiles());
        pull.setCommentsCount(gitPullRequest.getComments());
        pull.setCommitsCount(gitPullRequest.getCommits());
        pull.setDeletions(gitPullRequest.getDeletions());
        pull.setNumber(gitPullRequest.getNumber());
        if (pull.getBase() == null) {
            pull.setBase(PullRequestMakerServices.createEntity(gitPullRequest.getBase(), dao));
        }
        if (pull.getHead() == null) {
            pull.setHead(PullRequestMakerServices.createEntity(gitPullRequest.getHead(), dao));
        }
        pull.setBody(gitPullRequest.getBody());
        pull.setBodyHtml(gitPullRequest.getBodyHtml());
        pull.setBodyText(gitPullRequest.getBodyText());
        pull.setDiffUrl(gitPullRequest.getDiffUrl());
        pull.setHtmlUrl(gitPullRequest.getHtmlUrl());
        pull.setIssueUrl(gitPullRequest.getIssueUrl());
        pull.setPatchUrl(gitPullRequest.getPatchUrl());
        pull.setStatePullRequest(gitPullRequest.getState());
        pull.setTitle(gitPullRequest.getTitle());
        pull.setUrl(gitPullRequest.getUrl());
        pull.setMergedBy(UserServices.createEntity(gitPullRequest.getMergedBy(), dao, false));
        pull.setUser(UserServices.createEntity(gitPullRequest.getUser(), dao, false));

        if (pull.getId() == null || pull.getId().equals(new Long(0))) {
            dao.insert(pull);
        } else {
            dao.edit(pull);
        }

        return pull;
    }

    public static EntityPullRequest getPullRequestByIdPull(long idPullRequest, GenericDao dao) {
        List<EntityPullRequest> pulls = dao.executeNamedQueryComParametros("PullRequest.findByIdPullRequest", new String[]{"idPullRequest"}, new Object[]{idPullRequest}, true);
        if (!pulls.isEmpty()) {
            return pulls.get(0);
        }
        return null;
    }

    public static List<PullRequest> getGitPullRequestsFromRepository(Repository gitRepo, boolean open, boolean closed, OutLog out) {
        List<PullRequest> pulls = new ArrayList<PullRequest>();
        try {
            PullRequestService pullServ = new PullRequestService(AuthServices.getGitHubCliente());
            if (open) {
                List<PullRequest> opensPulls;
                out.printLog("Baixando PullRequests Abertos...\n");
                opensPulls = pullServ.getPullRequests(gitRepo, "open");
                out.printLog(opensPulls.size() + " PullRequests abertos baixados!");
                pulls.addAll(opensPulls);
            }
            if (closed) {
                List<PullRequest> closedsPulls;
                out.printLog("Baixando PullRequests Fechados...\n");
                closedsPulls = pullServ.getPullRequests(gitRepo, "closed");
                out.printLog(closedsPulls.size() + " PullRequests fechados baixados!");
                pulls.addAll(closedsPulls);
            }
            out.printLog(pulls.size() + " PullRequests baixados no total!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog(pulls.size() + " PullRequests baixados no total! Erro: " + ex.toString());
        }
        return pulls;
    }

    public static EntityPullRequest getPullRequestByNumber(int number, EntityRepository repo, GenericDao dao) {
        List<EntityPullRequest> pulls = dao.executeNamedQueryComParametros("PullRequest.findByNumberAndRepository", new String[]{"number", "repository"}, new Object[]{number, repo});
        if (!pulls.isEmpty()) {
            return pulls.get(0);
        }
        return null;
    }
}
