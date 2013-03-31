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
            repoCommit.setMineredAt(new Date());

//          createParents(repoCommit, gitRepoCommit.getParents(), gitRepo, dao);
            repoCommit.setSha(gitRepoCommit.getSha());
            repoCommit.setUrl(gitRepoCommit.getUrl());
            dao.insert(repoCommit);
        }
        if (repoCommit.getCommit() == null) {
            repoCommit.setCommit(CommitServices.createEntity(gitRepoCommit.getCommit(), dao));
        }
        if (repoCommit.getAuthor() == null) {
            repoCommit.setAuthor(UserServices.createEntity(gitRepoCommit.getAuthor(), dao, false));
        }
        if (repoCommit.getCommitter() == null) {
            repoCommit.setCommitter(UserServices.createEntity(gitRepoCommit.getCommitter(), dao, false));
        }

        return repoCommit;
    }

    private static EntityRepositoryCommit getRepoCommitBySHA(String sha, GenericDao dao) {
        List<EntityRepositoryCommit> repoCommits = dao.executeNamedQueryComParametros("RepositoryCommit.findBySHA", new String[]{"sha"}, new Object[]{sha}, true);
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
        PagedRequest<RepositoryCommit> request = new PagedRequest<>(1, 100);
        request.setUri(uri);
        request.setType(new TypeToken<List<RepositoryCommit>>() {
        }.getType());
        PageIterator<RepositoryCommit> iterator = new PageIterator<>(request, AuthServices.getGitHubCliente());
        List<RepositoryCommit> elements = new ArrayList<>();
        try {
            while (iterator.hasNext()) {
                elements.addAll(iterator.next());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return getGitRepoCommitsFromPullRequest(pullRequest, repositoryToMiner);
        }
        return elements;
    }

    private static String getId(EntityRepository repository) {
        return "/" + repository.getOwner().getLogin() + "/" + repository.getName();
    }

    public static RepositoryCommit getGitRepositoryCommit(Repository gitRepo, RepositoryCommit gitRepoCommit, OutLog out, int nRetries) throws Exception {
        if (nRetries <= 0) {
            return null;
        }
        try {
            return new CommitService(AuthServices.getGitHubCliente()).getCommit(gitRepo, gitRepoCommit.getSha());
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("Erro de conexão: " + ex.toString());
            out.printLog("Tentando novamente (" + nRetries + ") ...");
            return getGitRepositoryCommit(gitRepo, gitRepoCommit, out, nRetries);
        }
    }
}
