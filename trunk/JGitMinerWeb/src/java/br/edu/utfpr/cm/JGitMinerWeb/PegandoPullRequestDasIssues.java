/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.RepositoryServices;
import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class PegandoPullRequestDasIssues {

//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) throws IOException {
//        try {
//            PersistenciaServices.dataBaseConnect(false);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            System.exit(1);
//        }
//
//        Repository repo = new RepositoryService().getRepository("rails", "rails");
//
//        System.err.println("Repositório: " + repo.getName() + " | " + repo.getOwner().getName() + " | " + repo.getCreatedAt() + " | " + repo.getHtmlUrl());
//
//        System.out.println("");
//        PullRequestService pullService = new PullRequestService();
//
//        System.out.println("<<<<<<<<< Baixando PullRequests <<<<<<<<<<<<<<<");
//        List<PullRequest> pullRequests = pullService.getPullRequests(repo, null);
//        System.out.println("<<<<<<<<< " + pullRequests.size() + " PullRequests baixadas <<<<<<<<<<<<<<<");
//        System.out.println("");
//
//
//        EntityRepository entityRepository = EntityRepository.create(repo);
//
//        List<EntityIssue> issues = PersistenciaServices.executeNamedQueryComParametros("Issue.findByRepository", new String[]{"repository"}, new Object[]{entityRepository});
//
//        System.out.println(">>>>>>> Foram encontradas " + issues.size() + " Issues no banco de dados para este repositório.");
//
//
//        for (PullRequest gitPullRequest : pullRequests) {
//            for (EntityIssue entityIssue : issues) {
//                if (entityIssue.getNumber() == gitPullRequest.getNumber()) {
//                    System.err.println("Encontrado PullRequest da Issue número: " + entityIssue.getNumber());
//                    EntityPullRequest entityPullRequest = EntityPullRequest.create(gitPullRequest, entityIssue);
//                    entityIssue.setPullRequest(entityPullRequest);
//                    System.err.println("Adicionado PullRequest " + entityPullRequest.getIdPullRequest() + " na Issue " + entityIssue.getIdIssue());
//                    System.err.println("");
//                    break;
//                }
//            }
//        }
//
////        System.out.println("<<<<<<<<< Baixando Issues Fechadas <<<<<<<<<<<<<<<");
////        pullRequests.addAll(pullService.getIssues(repo, params));
////        System.out.println("<<<<<<<<< " + pullRequests.size() + " Issues baixadas no total. <<<<<<<<<<<<<<<");
////        System.out.println("");
//
//
//
//
//        System.out.println("terminou");
//    }
    /*
     *
     */
    public static void main(String[] args) throws IOException {
        try {
            PersistenciaServices.dataBaseConnect(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        EntityRepository repository = new EntityRepository();

        Repository repo = new RepositoryService().getRepository("rails", "rails");

        List<EntityIssue> issues = PersistenciaServices.executeNamedQueryComParametros("Issue.findByRepository", new String[]{"repository"}, new Object[]{repository});

        System.out.println(">>>>>>> Foram encontradas " + issues.size() + " Issues no banco de dados para este repositório.");

        PullRequestService pullRequestService = new PullRequestService();

        for (EntityIssue entityIssue : issues) {

            System.err.println("Issue número: " + entityIssue.getNumber());

            PullRequest gitPullRequest = null;

            try {
                gitPullRequest = pullRequestService.getPullRequest(repo, entityIssue.getNumber());
                if (gitPullRequest != null && gitPullRequest.getId() != 0) {
                    System.out.println("Issue teve Pull Request");

                    entityIssue.setPullRequest(EntityPullRequest.create(gitPullRequest));

                    PersistenciaServices.atualiza(entityIssue);
                } else {
                    System.out.println("Issue Não teve Pull Request");
                }
            } catch (org.eclipse.egit.github.core.client.RequestException ex) {
                System.err.println(ex.getMessage());
            }

        }
        System.out.println("terminou");
    }
}
