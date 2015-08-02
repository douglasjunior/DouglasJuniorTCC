package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;

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

    public void deleteAll() {
        for (EntityMatrix matrix : getMatrices()) {
            dao.remove(matrix);
        }
        reloadList();
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

    public void downloadAllCSV() {
        try {
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBytes);
            zos.setLevel(9);

            for (EntityMatrix matrix : getMatrices()) {
                System.out.println("Matrix " + matrix + " tem nodes: " + matrix.getNodes().size());

                String fileName = generateFileName(matrix) + ".csv";

                StringBuilder csv = new StringBuilder();

                for (EntityMatrixNode node : matrix.getNodes()) {
                    csv.append(node).append("\r\n");
                }

                ZipEntry ze = new ZipEntry(fileName.replaceAll("/", "-"));
                zos.putNextEntry(ze);
                zos.write(csv.toString().getBytes());
                zos.closeEntry();
            }

            zos.close();
            download(StringUtils.capitalize(getMatrices().iterator().next().getRepository()) + " Matrices.zip", "application/zip", zipBytes.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadAllCSVOfOneVersion(String version) {
        try {
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBytes);
            zos.setLevel(9);

            for (EntityMatrix matrix : getMatrices()) {
                if (!matrix.toString().startsWith(version)) {
                    continue;
                }
                System.out.println("Matrix " + matrix + " tem nodes: " + matrix.getNodes().size());

                String fileName = generateFileName(matrix) + ".csv";

                StringBuilder csv = new StringBuilder();

                for (EntityMatrixNode node : matrix.getNodes()) {
                    csv.append(node).append("\r\n");
                }

                ZipEntry ze = new ZipEntry(fileName.replaceAll("/", "-"));
                zos.putNextEntry(ze);
                zos.write(csv.toString().getBytes());
                zos.closeEntry();
            }

            zos.close();

            download(version + ".zip", "application/zip", zipBytes.toByteArray());

        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadCSV(EntityMatrix matrix) {
        try {
            System.out.println("Matriz tem nodes: " + matrix.getNodes().size());

            String fileName = generateFileName(matrix) + ".csv";

            StringBuilder csv = new StringBuilder();

            for (EntityMatrixNode node : matrix.getNodes()) {
                csv.append(node).append("\r\n");
            }

            download(fileName, "text/csv", csv.toString().getBytes());

        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadLOG(EntityMatrix matrix) {
        try {
            String fileName = generateFileName(matrix) + ".log";
            download(fileName, "text/plain", matrix.getLog().toString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadParams(EntityMatrix matrix) {
        try {
            String fileName = generateFileName(matrix) + ".txt";

            StringBuilder params = new StringBuilder();

            for (Map.Entry<Object, Object> entrySet : matrix.getParams().entrySet()) {
                Object key = entrySet.getKey();
                Object value = entrySet.getValue();
                params.append(key).append("=").append(value);
            }

            download(fileName, "text/plain", params.toString().getBytes());

        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void download(String filename, String contentType, byte[] content) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        ec.setResponseContentType(contentType); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
        ec.setResponseContentLength(content.length); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
        ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
        try (OutputStream output = ec.getResponseOutputStream()) {
            output.write(content);
            output.flush();
            fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        }
    }

    private String generateFileName(EntityMatrix matrix) {
        return matrix.getDownloadFileName();
    }
}
