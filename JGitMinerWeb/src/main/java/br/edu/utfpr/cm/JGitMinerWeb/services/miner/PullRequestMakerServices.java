/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityPullRequestMarker;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityUser;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.PullRequestMarker;

/**
 *
 * @author Douglas
 */
public class PullRequestMakerServices implements Serializable {

    static EntityPullRequestMarker createEntity(PullRequestMarker gitPullReqMaker, GenericDao dao) {
        if (gitPullReqMaker == null) {
            return null;
        }

        EntityRepository repository = RepositoryServices.createEntity(gitPullReqMaker.getRepo(), dao, false);
        EntityUser user = UserServices.createEntity(gitPullReqMaker.getUser(), dao, false);

        EntityPullRequestMarker pullReMaker = getByRefRepoUser(gitPullReqMaker.getRef(), repository, user, dao);

        if (pullReMaker == null) {
            pullReMaker = new EntityPullRequestMarker();
            pullReMaker.setMineredAt(new Date());
            pullReMaker.setLabel(gitPullReqMaker.getLabel());
            pullReMaker.setRefPullRequestMarker(gitPullReqMaker.getRef());
            pullReMaker.setRepo(repository);
            pullReMaker.setSha(gitPullReqMaker.getSha());
            pullReMaker.setUser(user);

            dao.insert(pullReMaker);
        }

        return pullReMaker;
    }

    private static EntityPullRequestMarker getByRefRepoUser(String ref, EntityRepository repo, EntityUser user, GenericDao dao) {
        List<EntityPullRequestMarker> pms = dao.executeNamedQueryWithParams("PullRequestMarker.findByRefRepoUser",
                new String[]{"ref", "user", "repo"},
                new Object[]{ref, user, repo}, true);
        if (!pms.isEmpty()) {
            return pms.get(0);
        }
        return null;
    }
}
