/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityPullRequestMarker;
import java.io.Serializable;
import java.util.Date;
import org.eclipse.egit.github.core.PullRequestMarker;

/**
 *
 * @author Douglas
 */
public class PullRequestMakerServices implements Serializable  {

    static EntityPullRequestMarker createEntity(PullRequestMarker gitPullReqMaker, GenericDao dao) {
        if (gitPullReqMaker == null) {
            return null;
        }

        EntityPullRequestMarker pullReMaker = new EntityPullRequestMarker();

        pullReMaker.setMineredAt(new Date());
        pullReMaker.setLabel(gitPullReqMaker.getLabel());
        pullReMaker.setRefPullRequestMarker(gitPullReqMaker.getRef());
        pullReMaker.setRepo(RepositoryServices.createEntity(gitPullReqMaker.getRepo(), dao, false));
        pullReMaker.setSha(gitPullReqMaker.getSha());
        if (gitPullReqMaker.getUser() != null) {
            pullReMaker.setUser(UserServices.createEntity(gitPullReqMaker.getUser(), dao, false));
        }

        dao.insert(pullReMaker);

        return pullReMaker;
    }
}
