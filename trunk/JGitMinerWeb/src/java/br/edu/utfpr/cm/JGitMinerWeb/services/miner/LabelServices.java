/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityLabel;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Label;

/**
 *
 * @author Douglas
 */
public class LabelServices implements Serializable {

    public static void addLabels(EntityIssue issue, List<Label> gitLabels, GenericDao dao) {
        for (Label gitLabel : gitLabels) {
            EntityLabel label = getLabelByName(gitLabel.getName(), dao);
            if (label == null) {
                label = new EntityLabel();
                label.setMineredAt(new Date());
                label.setName(gitLabel.getName());
                label.setColor(gitLabel.getColor());
                label.setUrl(gitLabel.getUrl());

                dao.insert(label);
            }
            issue.addLabel(label);
        }
    }

    private static EntityLabel getLabelByName(String name, GenericDao dao) {
        List<EntityLabel> labels = dao.executeNamedQueryComParametros("Label.findByName", new String[]{"name"}, new Object[]{name}, true);
        if (!labels.isEmpty()) {
            return labels.get(0);
        }
        return null;
    }
}
