/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.IssueDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.util.out;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

/**
 *
 * @author Douglas
 */
public class IssueServices {

    private static IssueDao dao;

    private static EntityIssue getIssueByIdIssue(long idIssue) {
        List<EntityIssue> issues = dao.executeNamedQueryComParametros("Issue.findByIdIssue", new String[]{"idIssue"}, new Object[]{idIssue});
        if (!issues.isEmpty()) {
            return (EntityIssue) dao.findByID(issues.get(0).getId());
        }
        return null;
    }

    public static List<Issue> getGitIssuesFromRepository(Repository gitRepo, boolean open, boolean closed) throws Exception {
        IssueService issueServ = new IssueService();

        List<Issue> issues = new ArrayList<Issue>();
        HashMap<String, String> params = new HashMap<String, String>();

        if (open) {
            List<Issue> opensIssues;
            out.printLog("Baixando Issues Abertas...\n");
            params.put("state", "open");
            opensIssues = issueServ.getIssues(gitRepo, params);
            out.printLog(opensIssues.size() + " Issues abertas baixadas!");
            issues.addAll(opensIssues);
        }

        if (closed) {
            List<Issue> clodesIssues;
            params = new HashMap<String, String>();
            out.printLog("Baixando Issues Fechadas...\n");
            params.put("state", "closed");
            clodesIssues = issueServ.getIssues(gitRepo, params);
            out.printLog(clodesIssues.size() + " Issues fechadas baixadas!");
            issues.addAll(clodesIssues);
        }

        out.printLog(issues.size() + " Issues baixadas no total!");

        return issues;
    }

    public static EntityIssue createEntity(Issue gitIssue, IssueDao issueDao, UserDao userDao) {
        if (gitIssue == null) {
            return null;
        }

        IssueServices.dao = issueDao;
        EntityIssue issue = getIssueByIdIssue(gitIssue.getId());

        if (issue == null) {
            issue = new EntityIssue();
        }

        issue.setMineredAt(new Date());
        issue.setIdIssue(gitIssue.getId());
        issue.setClosedAt(gitIssue.getClosedAt());
        issue.setCreatedAt(gitIssue.getCreatedAt());
        issue.setUpdatedAt(gitIssue.getUpdatedAt());
        issue.setNumber(gitIssue.getNumber());
        //issue.setLabels(LabelServices.getLabels(gitIssue.getLabels()));
        //issue.setMilestone(EntityMilestone.createMilestone(gitIssue.getMilestone()));
        //issue.setPullRequest(EntityPullRequest.create(gitIssue.getPullRequest(), this)); // será minerado separadamente
        issue.setBody(gitIssue.getBody());
        issue.setBodyHtml(gitIssue.getBodyHtml());
        issue.setBodyText(gitIssue.getBodyText());
        issue.setHtmlUrl(gitIssue.getHtmlUrl());
        issue.setStateIssue(gitIssue.getState());
        issue.setTitle(gitIssue.getTitle());
        issue.setUrl(gitIssue.getUrl());
        issue.setAssignee(UserServices.createEntity(gitIssue.getAssignee(), userDao));
        issue.setUserIssue(UserServices.createEntity(gitIssue.getUser(), userDao));

        if (issue.getId() == null || issue.getId().equals(new Long(0))) {
            issueDao.insert(issue);
        } else {
            issueDao.edit(issue);
        }

        return issue;
    }
}
