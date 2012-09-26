/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.out;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

/**
 *
 * @author Douglas
 */
public class CommitServices {

    public static EntityIssue getIssueByIdIssue(long idIssue, GenericDao dao) {
        List<EntityIssue> issues = dao.executeNamedQueryComParametros("Issue.findByIdIssue", new String[]{"idIssue"}, new Object[]{idIssue});
        if (!issues.isEmpty()) {
            return issues.get(0);
        }
        return null;
    }

    public static List<Commit> getGitCommitsFromRepository(Repository gitRepo) {
        List<Commit> commits = new ArrayList<Commit>();
        try {
//            IssueService issueServ = new IssueService();
//            List<Issue> opensIssues;
            out.printLog("Baixando Commits...\n");
//            opensIssues = issueServ.getIssues(gitRepo);
//            out.printLog(opensIssues.size() + " Issues abertas baixadas!");
//            issues.addAll(opensIssues);
            out.printLog(commits.size() + " Commits baixados no total!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog(commits.size() + " Commits baixadaos no total! Erro: " + ex.toString());
        }
        return commits;
    }

    public static EntityIssue createEntity(Issue gitIssue, GenericDao dao) {
        if (gitIssue == null) {
            return null;
        }

        EntityIssue issue = getIssueByIdIssue(gitIssue.getId(), dao);

        if (issue == null) {
            issue = new EntityIssue();
        }

        issue.setMineredAt(new Date());
        issue.setIdIssue(gitIssue.getId());
        issue.setClosedAt(gitIssue.getClosedAt());
        issue.setCreatedAt(gitIssue.getCreatedAt());
        issue.setUpdatedAt(gitIssue.getUpdatedAt());
        issue.setNumber(gitIssue.getNumber());
        LabelServices.addLabels(issue, gitIssue.getLabels(), dao);
        issue.setMilestone(MilestoneServices.createEntity(gitIssue.getMilestone(), dao));
        issue.setBody(gitIssue.getBody());
        issue.setBodyHtml(gitIssue.getBodyHtml());
        issue.setBodyText(gitIssue.getBodyText());
        issue.setHtmlUrl(gitIssue.getHtmlUrl());
        issue.setStateIssue(gitIssue.getState());
        issue.setTitle(gitIssue.getTitle());
        issue.setUrl(gitIssue.getUrl());
        issue.setAssignee(UserServices.createEntity(gitIssue.getAssignee(), dao, false));
        issue.setUserIssue(UserServices.createEntity(gitIssue.getUser(), dao, false));

        if (issue.getId() == null || issue.getId().equals(new Long(0))) {
            dao.insert(issue);
        } else {
            dao.edit(issue);
        }

        return issue;
    }

    public static EntityIssue getIssueByNumber(int number, EntityRepository repo, GenericDao dao) {
        List<EntityIssue> issues = dao.executeNamedQueryComParametros("Issue.findByNumberAndRepository", new String[]{"number", "repository"}, new Object[]{number, repo});
        if (!issues.isEmpty()) {
            return issues.get(0);
        }
        return null;
    }
}
