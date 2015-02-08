package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 *
 * @author douglas
 */
@Named
@RequestScoped
public class GitMetricViewBean implements Serializable {

    private final String FOR_DELETE = "metricForDelete";
    private final String LIST = "metricList";

    @EJB
    private GenericDao dao;

    /**
     * Creates a new instance of GitNetView
     */
    public GitMetricViewBean() {
    }

    public void delete() {
        try {
            EntityMetric forDelete = (EntityMetric) JsfUtil.getObjectFromSession(FOR_DELETE);
            dao.remove(forDelete);
            reloadList();
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        removeFromSession();
    }

    public void deleteAll() {
        for (EntityMetric metric : getMetrics()) {
            dao.remove(metric);
        }
        reloadList();
    }

    public void removeFromSession() {
        JsfUtil.removeAttributeFromSession(FOR_DELETE);
    }

    public void addForDeleteInSession(EntityMetric forDelete) {
        JsfUtil.addAttributeInSession(FOR_DELETE, forDelete);
    }

    public void reloadList() {
        dao.clearCache(true);
        List<EntityMetric> metrics = dao.executeNamedQuery("Metric.findAllTheLatest");
        JsfUtil.addAttributeInSession(LIST, metrics);
    }

    public List<EntityMetric> getMetrics() {
        List<EntityMetric> metrics = (List<EntityMetric>) JsfUtil.getObjectFromSession(LIST);
        if (metrics == null) {
            reloadList();
            return getMetrics();
        }
        return metrics;
    }

    public void downloadAllCSV() {
        try {
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBytes);
            zos.setLevel(9);

            for (EntityMetric metric : getMetrics()) {
                System.out.println("Metric " + metric + " tem nodes: " + metric.getNodes().size());

                String fileName = generateFileName(metric) + ".csv";

                AbstractBichoMetricServices services = AbstractBichoMetricServices.createInstance(metric.getClassServicesName());

                StringBuilder csv = new StringBuilder(services.getHeadCSV());

                csv.append("\r\n");
                for (EntityMetricNode node : metric.getNodes()) {
                    csv.append(node).append("\r\n");
                }

                ZipEntry ze = new ZipEntry(fileName.replaceAll("/", "-"));
                zos.putNextEntry(ze);
                zos.write(csv.toString().getBytes());
                zos.closeEntry();

            }

            zos.close();
            download("All.zip", "application/zip", zipBytes.toByteArray());
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

            for (EntityMetric metric : getMetrics()) {
                if (!metric.toString().startsWith(version)) {
                    continue;
                }
                System.out.println("Metric " + metric + " tem nodes: " + metric.getNodes().size());

                String fileName = generateFileName(metric) + ".csv";

                AbstractBichoMetricServices services = AbstractBichoMetricServices.createInstance(metric.getClassServicesName());

                StringBuilder csv = new StringBuilder(services.getHeadCSV());

                csv.append("\r\n");
                for (EntityMetricNode node : metric.getNodes()) {
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

    public void downloadCSV(EntityMetric metric) {
        try {
            System.out.println("Metric tem nodes: " + metric.getNodes().size());

            String fileName = generateFileName(metric) + ".csv";

            AbstractBichoMetricServices services = AbstractBichoMetricServices.createInstance(metric.getClassServicesName());

            StringBuilder csv = new StringBuilder(services.getHeadCSV());

            csv.append("\r\n");
            for (EntityMetricNode node : metric.getNodes()) {
                csv.append(node).append("\r\n");
            }

            download(fileName, "text/csv", csv.toString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadLOG(EntityMetric metric) {
        try {
            String fileName = generateFileName(metric) + ".log";
            download(fileName, "text/plain", metric.getLog().toString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadParams(EntityMetric metric) {
        try {
            String fileName = generateFileName(metric) + ".txt";

            StringBuilder params = new StringBuilder();

            for (Map.Entry<Object, Object> entrySet : metric.getParams().entrySet()) {
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

        OutputStream output = ec.getResponseOutputStream();
        output.write(content);
        output.flush();
        fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        output.close();
    }

    private String generateFileName(EntityMetric metric) {
        return metric.toString();
    }
}
