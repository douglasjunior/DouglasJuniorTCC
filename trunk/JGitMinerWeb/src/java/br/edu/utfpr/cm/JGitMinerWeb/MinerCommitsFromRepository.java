/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb;

import java.io.IOException;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class MinerCommitsFromRepository {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        RepositoryService repoServ = new RepositoryService();
        for (Repository repo : repoServ.getRepositories("defunkt")) {
            System.err.println("Repositório: " + repo.getName() + " | " + repo.getOwner().getName() + " | " + repo.getCreatedAt());
            CommitService comServ = new CommitService();
            for (RepositoryCommit repoCommit : comServ.getCommits(repo)) {
                System.out.println("\tCommit: " + repoCommit.getCommit().getMessage());
            }
        }
    }
}
