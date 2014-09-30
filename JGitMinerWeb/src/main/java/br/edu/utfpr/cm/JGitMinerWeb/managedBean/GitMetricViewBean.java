package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.AbstractMetricServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
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

    public StreamedContent downloadCSV(EntityMetric metric) {
        StreamedContent file = null;
        try {
            OutLog out = new OutLog();

            System.out.println("Metric tem nodes: " + metric.getNodes().size());

            String fileName = generateFileName(metric) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            AbstractMetricServices services = AbstractMetricServices.createInstance(dao, out, metric.getClassServicesName());

            pw.println(services.getHeadCSV());

            for (EntityMetricNode node : metric.getNodes()) {
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
        return metric.getClassServicesSingleName() + " of matrix (" + metric.getMatrix() + ") - " + metric.getStarted();
    }
}
