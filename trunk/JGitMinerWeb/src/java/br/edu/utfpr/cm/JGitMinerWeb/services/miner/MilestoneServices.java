/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityMilestone;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.MilestoneService;

/**
 *
 * @author Douglas
 */
public class MilestoneServices implements Serializable {

    public static EntityMilestone createEntity(Milestone gitMilestone, GenericDao dao) {
        if (gitMilestone == null) {
            return null;
        }

        EntityMilestone milestone = getMilestoneByURL(gitMilestone.getUrl(), dao);

        if (milestone == null) {
            milestone = new EntityMilestone();
            milestone.setCreator(UserServices.createEntity(gitMilestone.getCreator(), dao, false));
            milestone.setUrl(gitMilestone.getUrl());
            milestone.setNumber(gitMilestone.getNumber());
            milestone.setCreatedAt(gitMilestone.getCreatedAt());
        }

        milestone.setDueOn(gitMilestone.getDueOn());
        milestone.setClosedIssues(gitMilestone.getClosedIssues());
        milestone.setOpenIssues(gitMilestone.getOpenIssues());
        milestone.setDescription(gitMilestone.getDescription());
        milestone.setStateMilestone(gitMilestone.getState());
        milestone.setTitle(gitMilestone.getTitle());

        if (milestone.getId() == null || milestone.getId().equals(new Long(0))) {
            dao.insert(milestone);
        } else {
            dao.edit(milestone);
        }

        return milestone;
    }

    private static EntityMilestone getMilestoneByURL(String url, GenericDao dao) {
        List<EntityMilestone> miles = dao.executeNamedQueryComParametros("Milestone.findByURL", new String[]{"url"}, new Object[]{url}, true);
        if (!miles.isEmpty()) {
            return miles.get(0);
        }
        return null;
    }

    public static List<Milestone> getGitMilestoneFromRepository(Repository gitRepo, boolean open, boolean closed, OutLog out) {
        List<Milestone> milestones = new ArrayList<Milestone>();
        try {
            MilestoneService service = new MilestoneService(AuthServices.getGitHubCliente());
            if (open) {
                List<Milestone> opens;
                out.printLog("Baixando Milestones Abertos...\n");
                opens = service.getMilestones(gitRepo, "open");
                out.printLog(opens.size() + " Milestones abertos baixadas!");
                milestones.addAll(opens);
            }
            if (closed) {
                List<Milestone> closeds;
                out.printLog("Baixando Milestones Fechados...\n");
                closeds = service.getMilestones(gitRepo, "closed");
                out.printLog(closeds.size() + " Milestones fechados baixadas!");
                milestones.addAll(closeds);
            }
            out.printLog(milestones.size() + " Milestones baixados no total!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("Erro: " + ex.toString());
        }
        return milestones;
    }
}
