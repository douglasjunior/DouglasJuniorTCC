/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityLabel;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.Label;

/**
 *
 * @author Douglas
 */
public class LabelServices {

    public static List<EntityLabel> getLabels(List<Label> labels) {
        List<EntityLabel> myLabels = new ArrayList<EntityLabel>();

        for (Label label : labels) {
            myLabels.add(EntityLabel.create(label));
        }

        return myLabels;
    }

    public static EntityLabel getLabelByURL(String url) {
        List<EntityLabel> labels = PersistenciaServices.executeNamedQueryComParametros("Label.findByURL", new String[]{"url"}, new Object[]{url});
        if (!labels.isEmpty()) {
            return (EntityLabel) PersistenciaServices.buscaID(labels.get(0).getClass(), labels.get(0).getId() + "");
        }
        return null;
    }

    public static EntityLabel insertLabel(Label gitLabel) {
        EntityLabel newLabel = new EntityLabel(gitLabel);
        PersistenciaServices.insere(newLabel);
        return newLabel;
    }
}
