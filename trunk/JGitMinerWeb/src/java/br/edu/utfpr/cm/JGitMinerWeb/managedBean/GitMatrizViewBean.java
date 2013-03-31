/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.AbstractMatrizServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitMatrizViewBean")
@RequestScoped
public class GitMatrizViewBean implements Serializable {

    private final String FOR_DELETE = "matrizForDelete";
    private final String LIST = "listMatrizes";
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
            EntityMatriz matrizForDelete = (EntityMatriz) JsfUtil.getObjectFromSession(FOR_DELETE);
            dao.remove(matrizForDelete);
            reloadList();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        removeMatrizFromSession();
    }

    public void removeMatrizFromSession() {
        JsfUtil.removeAttributeFromSession(FOR_DELETE);
    }

    public void addMatrizForDeleteInSession(EntityMatriz netForDelete) {
        JsfUtil.addAttributeInSession(FOR_DELETE, netForDelete);
    }

    public void reloadList() {
        dao.clearCache(true);
        List<EntityMatriz> matrizes = dao.executeNamedQuery("Matriz.findAllTheLatest");
        JsfUtil.addAttributeInSession(LIST, matrizes);
    }

    public List<EntityMatriz> getMatrizes() {
        List<EntityMatriz> matrizes = (List<EntityMatriz>) JsfUtil.getObjectFromSession(LIST);
        if (matrizes == null) {
            reloadList();
            return getMatrizes();
        }
        return matrizes;
    }

    public StreamedContent downloadCSV(EntityMatriz matriz) {
        StreamedContent file = null;
        try {
            System.out.println("Matriz tem nodes: " + matriz.getNodes().size());

            String fileName = generateFileName(matriz) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            AbstractMatrizServices services = AbstractMatrizServices.createInstance(dao, matriz.getClassServicesName());

            pw.println(services.getHeadCSV());

            for (EntityMatrizNode node : matriz.getNodes()) {
                pw.println(node + "");
            }

            pw.flush();
            pw.close();

            file = JsfUtil.downloadFile(fileName, baos.toByteArray());

            baos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        return file;
    }

    public StreamedContent downloadLOG(EntityMatriz matriz) {
        StreamedContent file = null;
        try {
            file = JsfUtil.downloadLogFile(matriz);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        return file;
    }

    private String generateFileName(EntityMatriz matriz) {
        return matriz.getRepository() + "-" + matriz.getStarted();
    }
}
