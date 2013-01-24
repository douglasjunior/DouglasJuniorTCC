/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

/**
 *
 * @author Douglas
 */
public class RepositoryCommitServices {

    public static List<RepositoryCommit> getGitCommitsFromRepository(Repository gitRepo, OutLog out) {
        List<RepositoryCommit> repoCommits = null;
        try {
            out.printLog("Baixando RepositoryCommits...\n");
            repoCommits = new CommitService(AuthServices.getGitHubCliente()).getCommits(gitRepo);
            out.printLog(repoCommits.size() + " RepositoryCommits baixados no total!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog(repoCommits.size() + " RepositoryCommits baixadaos no total! Erro: " + ex.toString());
        }
        return repoCommits;
    }

    public static EntityRepositoryCommit createEntity(RepositoryCommit gitRepoCommit, Repository gitRepo, GenericDao dao) {
        if (gitRepoCommit == null) {
            return null;
        }

        EntityRepositoryCommit repoCommit = getRepoCommitBySHA(gitRepoCommit.getSha(), dao);

        if (repoCommit == null) {
            repoCommit = new EntityRepositoryCommit();
        }

        repoCommit.setMineredAt(new Date());
        repoCommit.setAuthor(UserServices.createEntity(gitRepoCommit.getAuthor(), dao, false));
        repoCommit.setCommit(CommitServices.createEntity(gitRepoCommit.getCommit(), gitRepo, dao));
        repoCommit.setCommitter(UserServices.createEntity(gitRepoCommit.getCommitter(), dao, false));
        createParents(repoCommit, gitRepoCommit.getParents(), gitRepo, dao);
        repoCommit.setSha(gitRepoCommit.getSha());
        repoCommit.setUrl(gitRepoCommit.getUrl());

        if (repoCommit.getId() == null || repoCommit.getId().equals(new Long(0))) {
            dao.insert(repoCommit);
        } else {
            dao.edit(repoCommit);
        }

        return repoCommit;
    }

    private static EntityRepositoryCommit getRepoCommitBySHA(String sha, GenericDao dao) {
        List<EntityRepositoryCommit> repoCommits = dao.executeNamedQueryComParametros("RepositoryCommit.findBySHA", new String[]{"sha"}, new Object[]{sha});
        if (!repoCommits.isEmpty()) {
            return repoCommits.get(0);
        }
        return null;
    }

    private static void createParents(EntityRepositoryCommit repoCommit, List<Commit> gitParents, Repository gitRepo, GenericDao dao) {
        if (gitParents != null) {
            for (Commit gitParent : gitParents) {
                repoCommit.addParent(CommitServices.createEntity(gitParent, gitRepo, dao));
            }
        }
    }
}
