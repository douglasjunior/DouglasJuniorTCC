/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb;

import br.edu.utfpr.cm.JGitMinerWeb.services.PullRequestServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class TestePullRequestIssues {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        Repository repo = new RepositoryService().getRepository("rails", "rails");

        System.err.println("Repositório: " + repo.getName() + " | " + repo.getOwner().getName() + " | " + repo.getCreatedAt() + " | " + repo.getHtmlUrl());


        System.out.println(new PullRequestService().getPullRequest(repo, 7244));


        System.out.println("terminou");
    }
}
