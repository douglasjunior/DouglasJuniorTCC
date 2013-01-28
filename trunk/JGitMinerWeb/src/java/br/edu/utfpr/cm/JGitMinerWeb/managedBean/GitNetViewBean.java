/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNet;
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
@ManagedBean(name = "gitNetViewBean")
@RequestScoped
public class GitNetViewBean implements Serializable {

    private final String STR_NET_FOR_DELETE = "netForDelete";
    /*
     * 
     */
    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GitNetView
     */
    public GitNetViewBean() {
    }

    public void deleteNetInSession() {
        try {
            EntityNet netForDelete = (EntityNet) JsfUtil.getObjectFromSession(STR_NET_FOR_DELETE);
            dao.remove(netForDelete);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        removeNetFromSession();
    }

    public void removeNetFromSession() {
        JsfUtil.removeAttributeFromSession(STR_NET_FOR_DELETE);
    }

    public void addNetForDeleteInSession(EntityNet netForDelete) {
        JsfUtil.addAttributeInSession(STR_NET_FOR_DELETE, netForDelete);
    }

    public List<EntityNet> getNets() {
        return dao.executeNamedQuery("Net.findAllTheLatest");
    }

    public void downloadCSV(EntityNet net) {
        try {
            String fileName = generateFileName(net) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = net.getNetResult().split("\n");

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

    public void downloadLOG(EntityNet net) {
        try {
            String fileName = generateFileName(net) + ".log";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = net.getNetLog().split("\n");

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

    private String generateFileName(EntityNet net) {
        return net.getRepository().getName() + "-" + net.getNetStart();
    }
}
