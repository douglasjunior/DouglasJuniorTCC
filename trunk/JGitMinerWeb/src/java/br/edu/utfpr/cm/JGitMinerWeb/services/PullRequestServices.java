/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityPullRequest;
import java.util.List;
import org.eclipse.egit.github.core.PullRequest;

/**
 *
 * @author Douglas
 */
public class PullRequestServices {

    public static EntityPullRequest getPullRequestByIdPull(long idPull) {
        List<EntityPullRequest> pulls = PersistenciaServices.executeNamedQueryComParametros("PullRequest.findByIdPull", new String[]{"idPullRequest"}, new Object[]{idPull});
        if (!pulls.isEmpty()) {
            return (EntityPullRequest) PersistenciaServices.buscaID(pulls.get(0).getClass(), pulls.get(0).getId() + "");
        }
        return null;
    }

    public static EntityPullRequest insertPullRequest(PullRequest gitPullRequest) {
        EntityPullRequest newPull = new EntityPullRequest(gitPullRequest);
        PersistenciaServices.insere(newPull);
        return newPull;
    }

    public static EntityPullRequest getPullRequestByIssue(EntityIssue issue) {
        List<EntityPullRequest> pulls = PersistenciaServices.executeNamedQueryComParametros("PullRequest.findByIssue", new String[]{"issue"}, new Object[]{issue});
        if (!pulls.isEmpty()) {
            return (EntityPullRequest) PersistenciaServices.buscaID(pulls.get(0).getClass(), pulls.get(0).getId() + "");
        }
        return null;
    }
}
