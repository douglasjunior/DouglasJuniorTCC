/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityMilestone;
import java.util.List;
import org.eclipse.egit.github.core.Milestone;

/**
 *
 * @author Douglas
 */
public class MilestoneServices {

    public static EntityMilestone getMilestoneByURL(String url) {
        List<EntityMilestone> miles = PersistenciaServices.executeNamedQueryComParametros("Milestone.findByURL", new String[]{"url"}, new Object[]{url});
        if (!miles.isEmpty()) {
            return (EntityMilestone) PersistenciaServices.buscaID(miles.get(0).getClass(), miles.get(0).getId() + "");
        }
        return null;
    }

    public static EntityMilestone insert(Milestone girMilestone) {
        EntityMilestone newMilestone = new EntityMilestone(girMilestone);
        PersistenciaServices.insere(newMilestone);
        return newMilestone;
    }
}
