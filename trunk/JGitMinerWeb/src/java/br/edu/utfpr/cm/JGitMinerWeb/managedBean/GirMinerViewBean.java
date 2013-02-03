/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityMiner;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "girMinerViewBean")
@RequestScoped
public class GirMinerViewBean implements Serializable {

    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GirMinerView
     */
    public GirMinerViewBean() {
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

    public void downloadLOG(EntityMiner miner) {
        try {
            String fileName = generateFileName(miner) + ".log";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = miner.getMinerLog().split("\n");

            for (String linha : linhas) {
                pw.println(linha);
            }

            pw.flush();
            pw.close();

            JsfUtil.downloadFile(fileName, baos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    private String generateFileName(EntityMiner miner) {
        return miner.getRepository().getName() + "-" + miner.getMinerStart();
    }
}
