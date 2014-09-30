package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.AbstractMatrixServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
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
public class GitMatrixViewBean implements Serializable {

    private final String FOR_DELETE = "matrixForDelete";
    private final String LIST = "listMatrices";

    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GitNetView
     */
    public GitMatrixViewBean() {
    }

    public void deleteMatrixInSession() {
        try {
            EntityMatrix matrixForDelete = (EntityMatrix) JsfUtil.getObjectFromSession(FOR_DELETE);
            dao.remove(matrixForDelete);
            reloadList();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        } finally {
            removeMatrixFromSession();
        }
    }

    public void removeMatrixFromSession() {
        JsfUtil.removeAttributeFromSession(FOR_DELETE);
    }

    public void addMatrixForDeleteInSession(EntityMatrix netForDelete) {
        JsfUtil.addAttributeInSession(FOR_DELETE, netForDelete);
    }

    public void reloadList() {
        dao.clearCache(true);
        List<EntityMatrix> matrices = null;
        try {
            matrices = dao.executeNamedQuery("Matrix.findAllTheLatest");
        } catch (Exception ex) {
            ex.printStackTrace();
            matrices = new ArrayList<>();
        } finally {
            JsfUtil.addAttributeInSession(LIST, matrices);
        }
    }

    public List<EntityMatrix> getMatrices() {
        List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        if (matrices == null) {
            reloadList();
            return getMatrices();
        }
        return matrices;
    }

    public StreamedContent downloadCSV(EntityMatrix matrix) {
        StreamedContent file = null;
        try {
            System.out.println("Matriz tem nodes: " + matrix.getNodes().size());

            String fileName = generateFileName(matrix) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            AbstractMatrixServices services = AbstractMatrixServices.createInstance(null, null, matrix.getClassServicesName());

            pw.println(services.getHeadCSV());

            for (EntityMatrixNode node : matrix.getNodes()) {
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

    public StreamedContent downloadLOG(EntityMatrix matrix) {
        StreamedContent file = null;
        try {
            file = JsfUtil.downloadLogFile(matrix);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        return file;
    }

    public StreamedContent downloadParams(EntityMatrix matrix) {
        StreamedContent file = null;
        try {
            String fileName = generateFileName(matrix) + ".txt";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            for (Object key : matrix.getParams().keySet()) {
                pw.println(key + "=" + matrix.getParams().get(key));
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

    private String generateFileName(EntityMatrix matrix) {
        return "(" + matrix.getId() + ") " + matrix.getClassServicesSingleName() + " (" + matrix.getRepository() + ")";
    }
}
