/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
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
public class IssueServices implements Serializable {

    public static EntityIssue getIssueByIdIssue(long idIssue, GenericDao dao) {
        List<EntityIssue> issues = dao.executeNamedQueryComParametros("Issue.findByIdIssue", new String[]{"idIssue"}, new Object[]{idIssue}, true);
        if (!issues.isEmpty()) {
            return issues.get(0);
        }
        return null;
    }

    public static List<Issue> getGitIssuesFromRepository(Repository gitRepo, boolean open, boolean closed, OutLog out) {
        List<Issue> issues = new ArrayList<Issue>();
        try {
            IssueService issueServ = new IssueService(AuthServices.getGitHubCliente());
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
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog(issues.size() + " Issues baixadas no total! Erro: " + ex.toString());
        }
        return issues;
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
        issue.setCommentsCount(gitIssue.getComments());
        LabelServices.addLabels(issue, gitIssue.getLabels(), dao);
        issue.setMilestone(MilestoneServices.createEntity(gitIssue.getMilestone(), dao));
        issue.setBody(JsfUtil.filterChar(gitIssue.getBody()));
        issue.setBodyHtml(gitIssue.getBodyHtml());
        issue.setBodyText(gitIssue.getBodyText());
        issue.setHtmlUrl(gitIssue.getHtmlUrl());
        issue.setStateIssue(gitIssue.getState());
        issue.setTitle(JsfUtil.filterChar(gitIssue.getTitle()));
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
