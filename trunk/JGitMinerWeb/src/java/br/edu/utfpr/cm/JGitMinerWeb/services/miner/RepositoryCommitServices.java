/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.CommitService;

/**
 *
 * @author Douglas
 */
public class RepositoryCommitServices implements Serializable {

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

    public static EntityRepositoryCommit createEntity(RepositoryCommit gitRepoCommit, GenericDao dao) {
        if (gitRepoCommit == null) {
            return null;
        }

        EntityRepositoryCommit repoCommit = getRepoCommitBySHA(gitRepoCommit.getSha(), dao);

        if (repoCommit == null) {
            repoCommit = new EntityRepositoryCommit();
        }

        repoCommit.setMineredAt(new Date());
        repoCommit.setAuthor(UserServices.createEntity(gitRepoCommit.getAuthor(), dao, false));
        repoCommit.setCommit(CommitServices.createEntity(gitRepoCommit.getCommit(), dao));
        repoCommit.setCommitter(UserServices.createEntity(gitRepoCommit.getCommitter(), dao, false));
//        createParents(repoCommit, gitRepoCommit.getParents(), gitRepo, dao);
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
//    private static void createParents(EntityRepositoryCommit repoCommit, List<Commit> gitParents, Repository gitRepo, GenericDao dao) {
//        if (gitParents != null) {
//            for (Commit gitParent : gitParents) {
//                repoCommit.addParent(CommitServices.createEntity(gitParent, gitRepo, dao));
//            }
//        }
//    }

    public static List<RepositoryCommit> getGitRepoCommitsFromPullRequest(EntityPullRequest pullRequest, EntityRepository repositoryToMiner) {
        // https://api.github.com/repos/jashkenas/coffee-script/pulls/2682/commits
        String id = getId(repositoryToMiner);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append(id);
        uri.append("/pulls");
        uri.append("/").append(pullRequest.getNumber());
        uri.append("/commits");
        PagedRequest<RepositoryCommit> request = new PagedRequest<RepositoryCommit>(1, 100);
        request.setUri(uri);
        request.setType(new TypeToken<List<RepositoryCommit>>() {
        }.getType());
        PageIterator<RepositoryCommit> iterator = new PageIterator<RepositoryCommit>(request, AuthServices.getGitHubCliente());
        List<RepositoryCommit> elements = new ArrayList<RepositoryCommit>();
        try {
            while (iterator.hasNext()) {
                elements.addAll(iterator.next());
            }
        } catch (NoSuchPageException pageException) {
            throw pageException;
        }
        return elements;
    }

    private static String getId(EntityRepository repository) {
        return "/" + repository.getOwner().getLogin() + "/" + repository.getName();
    }
}
