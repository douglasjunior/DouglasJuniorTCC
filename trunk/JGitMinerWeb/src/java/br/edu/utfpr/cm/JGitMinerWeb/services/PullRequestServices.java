/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PullRequestDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityPullRequest;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.PullRequest;

/**
 *
 * @author Douglas
 */
public class PullRequestServices {

    private static PullRequestDao dao;

    public static EntityPullRequest createEntity(PullRequest gitPullRequest, EntityIssue issue, PullRequestDao pullDao, UserDao userDao) {
        if (gitPullRequest == null) {
            return null;
        }

        dao = pullDao;

        EntityPullRequest pull = getPullRequestByIdPull(gitPullRequest.getId());

        if (pull == null) {
            pull = new EntityPullRequest();
        }

        pull.setMineredAt(new Date());
        pull.setIssue(issue);
        pull.setMergeable(gitPullRequest.isMergeable());
        pull.setMerged(gitPullRequest.isMerged());
        pull.setClosedAt(gitPullRequest.getClosedAt());
        pull.setMergedAt(gitPullRequest.getMergedAt());
        pull.setUpdatedAt(gitPullRequest.getUpdatedAt());
        pull.setCreatedAt(gitPullRequest.getCreatedAt());
        pull.setIdPullRequest(gitPullRequest.getId());
        pull.setAdditions(gitPullRequest.getAdditions());
        pull.setChangedFiles(gitPullRequest.getChangedFiles());
        pull.setComments(gitPullRequest.getComments());
        pull.setCommits(gitPullRequest.getCommits());
        pull.setDeletions(gitPullRequest.getDeletions());
        pull.setNumber(gitPullRequest.getNumber());
//        this.base = EntityPullRequestMarker.create(gitPullRequest.getBase());
//        this.head = EntityPullRequestMarker.create(gitPullRequest.getHead());
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
        pull.setMergedBy(UserServices.createEntity(gitPullRequest.getMergedBy(), userDao));
        pull.setUser(UserServices.createEntity(gitPullRequest.getUser(), userDao));

        if (pull.getId() == null || pull.getId().equals(new Long(0))) {
            pullDao.insert(pull);
        } else {
            pullDao.edit(pull);
        }

        return pull;
    }

    private static EntityPullRequest getPullRequestByIdPull(long idPullRequest) {
        List<EntityPullRequest> pulls = dao.executeNamedQueryComParametros("PullRequest.findByIdPullRequest", new String[]{"idPullRequest"}, new Object[]{idPullRequest});
        if (!pulls.isEmpty()) {
            return (EntityPullRequest) dao.findByID(pulls.get(0).getId());
        }
        return null;
    }
}
