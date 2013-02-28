/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.AbstractMatrizServices;
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

    public void deleteMatrizInSession() {
        try {
            EntityMatriz netForDelete = (EntityMatriz) JsfUtil.getObjectFromSession(STR_NET_FOR_DELETE);
            dao.remove(netForDelete);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        removeMatrizFromSession();
    }

    public void removeMatrizFromSession() {
        JsfUtil.removeAttributeFromSession(STR_NET_FOR_DELETE);
    }

    public void addMatrizForDeleteInSession(EntityMatriz netForDelete) {
        JsfUtil.addAttributeInSession(STR_NET_FOR_DELETE, netForDelete);
    }

    public List<EntityMatriz> getMatriz() {
        dao.clearCache();
        return dao.executeNamedQuery("Matriz.findAllTheLatest");
    }

    public void downloadCSV(EntityMatriz matriz) {
        try {
            System.out.println("Matriz tem records: " + matriz.getRecords().size());

            String fileName = generateFileName(matriz) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);
            
            AbstractMatrizServices services = AbstractMatrizServices.createInstance(dao, matriz.getClassServicesName());

            pw.println(services.convertToCSV(matriz.getRecords()));

            pw.flush();
            pw.close();

            JsfUtil.downloadFile(fileName, baos.toByteArray());

            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadLOG(EntityMatriz matriz) {
        try {
            String fileName = generateFileName(matriz) + ".log";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = matriz.getLog().split("\n");

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

    private String generateFileName(EntityMatriz matriz) {
        return matriz.getRepository().getName() + "-" + matriz.getStarted();
    }
}
