package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    public StreamedContent downloadAllCSV() {
        StreamedContent file = null;

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
            file = JsfUtil.downloadFile("All.zip", zipBytes.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }

        return file;
    }

    public StreamedContent downloadCSV(EntityMetric metric) {
        StreamedContent file = null;
        try {
            System.out.println("Metric tem nodes: " + metric.getNodes().size());

            String fileName = generateFileName(metric) + ".csv";

            AbstractBichoMetricServices services = AbstractBichoMetricServices.createInstance(metric.getClassServicesName());

            StringBuilder csv = new StringBuilder(services.getHeadCSV());

            csv.append("\r\n");
            for (EntityMetricNode node : metric.getNodes()) {
                csv.append(node).append("\r\n");
            }

            file = JsfUtil.downloadFile(fileName, csv.toString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        return file;
    }

    public StreamedContent downloadLOG(EntityMetric metric) {
        StreamedContent file = null;
        try {
            String fileName = generateFileName(metric) + ".log";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = metric.getLog().split("\n");

            for (String linha : linhas) {
                pw.println(linha);
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

    public StreamedContent downloadParams(EntityMetric metric) {
        StreamedContent file = null;
        try {
            String fileName = generateFileName(metric) + ".txt";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            for (Object key : metric.getParams().keySet()) {
                pw.println(key + "=" + metric.getParams().get(key));
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

    private String generateFileName(EntityMetric metric) {
        return metric.toString();
    }
}
