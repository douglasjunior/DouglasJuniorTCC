/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class PegandoIssuesEComentariosDoRepository {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {
            PersistenciaServices.dataBaseConnect(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        Repository repo = new RepositoryService().getRepository("rails", "rails");

        System.err.println("Repositório: " + repo.getName() + " | " + repo.getOwner().getName() + " | " + repo.getCreatedAt() + " | " + repo.getHtmlUrl());

//        EntityRepository entityRepository = EntityRepository.create(repo);

        System.out.println("");
        IssueService issueServ = new IssueService();
        List<Issue> issues = new ArrayList<Issue>();
        HashMap<String, String> params = new HashMap<String, String>();

        System.out.println("<<<<<<<<< Baixando Issues Abertas <<<<<<<<<<<<<<<");
        params.put("state", "open");
        issues.addAll(issueServ.getIssues(repo, params));
        System.out.println("<<<<<<<<< " + issues.size() + " Issues abertas baixadas. <<<<<<<<<<<<<<<");
        System.out.println("");

        System.out.println("<<<<<<<<< Baixando Issues Fechadas <<<<<<<<<<<<<<<");
        //   params.put("state", "closed");
        //   issues.addAll(issueServ.getIssues(repo, params));
        System.out.println("<<<<<<<<< " + issues.size() + " Issues baixadas no total. <<<<<<<<<<<<<<<");
        System.out.println("");

        // inicio de coleta dos dados das issues
        for (Issue issue : issues) {
            System.err.println("Issue: " + issue.getNumber() + " | " + issue.getTitle());
            System.err.println("Assignee: " + (issue.getAssignee() != null ? issue.getAssignee().getLogin() + " ID:" + issue.getAssignee().getId() : "null"));
            System.err.println("User: " + (issue.getUser() != null ? issue.getUser().getLogin() + " ID:" + issue.getUser().getId() : "null"));

            EntityIssue entityIssue = new EntityIssue();

            System.out.println("<<<<<<<<< Baixando Comentários <<<<<<<<<<<<<<<");
            List<Comment> comments = issueServ.getComments(repo, issue.getNumber());

            // inicio de coleta dos comentários das issues
            for (Comment coment : comments) {
                System.out.println("Comment: " + coment.getUser().getLogin() + " | " + coment.getId());
                EntityComment entityComment = EntityComment.createComment(coment);

                entityIssue.addComment(entityComment);
            }

            PersistenciaServices.atualiza(entityIssue);

//            entityRepository.addIssue(entityIssue);
            System.out.println("");
        }

//        PersistenciaServices.atualiza(entityRepository);

        System.out.println("terminou");
    }
}
