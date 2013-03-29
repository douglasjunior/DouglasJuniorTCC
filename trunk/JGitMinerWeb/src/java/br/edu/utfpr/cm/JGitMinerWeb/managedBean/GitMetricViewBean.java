/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.AbstractMetricServices;
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
@ManagedBean(name = "gitMetricViewBean")
@RequestScoped
public class GitMetricViewBean implements Serializable {

    private final String FOR_DELETE = "metricForDelete";
    private final String LIST = "metricList";
    /*
     * 
     */
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
        List<EntityMetric> matrizes = dao.executeNamedQuery("Metric.findAllTheLatest");
        JsfUtil.addAttributeInSession(LIST, matrizes);
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
            System.out.println("Metric tem nodes: " + metric.getNodes().size());

            String fileName = generateFileName(metric) + ".csv";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            AbstractMetricServices services = AbstractMetricServices.createInstance(dao, metric.getClassServicesName());

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

    private String generateFileName(EntityMetric matriz) {
        return matriz.getMatriz() + "-" + matriz.getStarted();
    }
}
