/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.MatrizServices;
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
@ManagedBean(name = "gitMatrizViewBean")
@RequestScoped
public class GitMatrizViewBean implements Serializable {

    private final String STR_NET_FOR_DELETE = "netForDelete";
    /*
     * 
     */
    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GitNetView
     */
    public GitMatrizViewBean() {
    }

    public void deleteNetInSession() {
        try {
            EntityMatriz netForDelete = (EntityMatriz) JsfUtil.getObjectFromSession(STR_NET_FOR_DELETE);
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

    public void addNetForDeleteInSession(EntityMatriz netForDelete) {
        JsfUtil.addAttributeInSession(STR_NET_FOR_DELETE, netForDelete);
    }

    public List<EntityMatriz> getNets() {
        return dao.executeNamedQuery("Matriz.findAllTheLatest");
    }

    public void downloadCSV(EntityMatriz matriz) {
        try {
            String fileName = generateFileName(matriz) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            pw.println(MatrizServices.createInstance(dao, matriz.getClassServicesName()).convertToCSV(matriz.getRecords()));

            pw.flush();
            pw.close();

            JsfUtil.downloadFile(fileName, baos.toByteArray());

            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadLOG(EntityMatriz net) {
        try {
            String fileName = generateFileName(net) + ".log";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = net.getLog().split("\n");

            for (String linha : linhas) {
                pw.println(linha);
            }

            pw.flush();
            pw.close();

            JsfUtil.downloadFile(fileName, baos.toByteArray());

            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    private String generateFileName(EntityMatriz net) {
        return net.getRepository().getName() + "-" + net.getStarted();
    }

}
