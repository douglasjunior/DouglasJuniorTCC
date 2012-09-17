/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityMilestone;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Milestone;

/**
 *
 * @author Douglas
 */
public class MilestoneServices {

    public static EntityMilestone createEntity(Milestone gitMilestone, GenericDao dao) {
        if (gitMilestone == null) {
            return null;
        }

        EntityMilestone milestone = getMilestoneByURL(gitMilestone.getUrl(), dao);

        if (milestone == null) {
            milestone = new EntityMilestone();
        }

        milestone.setCreatedAt(new Date());
        milestone.setDueOn(gitMilestone.getDueOn());
        milestone.setClosedIssues(gitMilestone.getClosedIssues());
        milestone.setNumber(gitMilestone.getNumber());
        milestone.setOpenIssues(gitMilestone.getOpenIssues());
        milestone.setDescription(gitMilestone.getDescription());
        milestone.setStateMilestone(gitMilestone.getState());
        milestone.setTitle(gitMilestone.getTitle());
        milestone.setUrl(gitMilestone.getUrl());
        milestone.setCreator(UserServices.createEntity(gitMilestone.getCreator(), dao, false));

        if (milestone.getId() == null || milestone.getId().equals(new Long(0))) {
            dao.insert(milestone);
        } else {
            dao.edit(milestone);
        }
        
        return milestone;
    }

    private static EntityMilestone getMilestoneByURL(String url, GenericDao dao) {
        List<EntityMilestone> miles = dao.executeNamedQueryComParametros("Milestone.findByURL", new String[]{"url"}, new Object[]{url});
        if (!miles.isEmpty()) {
            return miles.get(0);
        }
        return null;
    }
}
