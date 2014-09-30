package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityMiner;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author douglas
 */
@Named
@RequestScoped
public class GitMinerViewBean implements Serializable {

    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GirMinerView
     */
    public GitMinerViewBean() {
    }

//    public void deleteNetInSession() {
//        try {
//            EntityNet netForDelete = (EntityNet) JsfUtil.getObjectFromSession(STR_NET_FOR_DELETE);
//            dao.remove(netForDelete);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            JsfUtil.addErrorMessage(ex.toString());
//        }
//        removeNetFromSession();
//    }
//
//    public void removeNetFromSession() {
//        JsfUtil.removeAttributeFromSession(STR_NET_FOR_DELETE);
//    }
//
//    public void addNetForDeleteInSession(EntityNet netForDelete) {
//        JsfUtil.addAttributeInSession(STR_NET_FOR_DELETE, netForDelete);
//    }
    public List<EntityMiner> getMiners() {
        return dao.executeNamedQuery("Miner.findAllTheLatest");
    }

    public StreamedContent downloadLOG(EntityMiner miner) {
        StreamedContent file = null;
        try {
            file = JsfUtil.downloadLogFile(miner);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        return file;
    }
}
