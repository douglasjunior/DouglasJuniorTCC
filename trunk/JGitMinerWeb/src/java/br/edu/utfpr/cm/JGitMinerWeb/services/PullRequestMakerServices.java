/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityPullRequestMarker;
import org.eclipse.egit.github.core.PullRequestMarker;

/**
 *
 * @author Douglas
 */
public class PullRequestMakerServices {

    public static EntityPullRequestMarker insert(PullRequestMarker gitMaker) {
        EntityPullRequestMarker newMaker = new EntityPullRequestMarker(gitMaker);
        PersistenciaServices.insere(newMaker);
        return newMaker;
    }
    
}
