package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

                StringBuilder csv = new StringBuilder();

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
                if (!metric.toString().matches("^" + version + "\\s.*$")
                        && !metric.toString().equals(version)) {
                    continue;
                }
                System.out.println("Metric " + metric + " tem nodes: " + metric.getNodes().size());

                String fileName = generateFileName(metric) + ".csv";

                StringBuilder csv = new StringBuilder();
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

    public void downloadAllCSVNotEmptyInFolderCustom() {
        try {
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBytes);
            zos.setLevel(9);
            String project = "All";
            Set<String> files = new HashSet<>();
            Set<String> downloaded = new HashSet<>();
            for (EntityMetric metric : getMetrics()) {
//                final String metricName = metric.toString();
                // download lasts metrics
//                if (downloaded.contains(metricName)) {
//                    continue;
//                } else {
//                    downloaded.add(metricName);
//                }


                if (metric.getNodes().size() == 1) {
                    continue;
                }
                project = metric.getParams().get("project").toString();
                String version = metric.getParams().get("versionInAnalysis").toString();
                String aprioriFilter = metric.getParams().get("aprioriFilter").toString();
                String projectVersion = project + " " + version;
                String rank = metric.getParams().get("rank").toString();
                String trainOrTest = metric.getParams().get("additionalFilename").toString();
                String path = aprioriFilter + "/" + projectVersion + "/" + rank + "/" + trainOrTest + ".csv";

                System.out.println("Metric " + path + " tem nodes: " + metric.getNodes().size());

                if (!files.contains(path)) {
                    StringBuilder csv = new StringBuilder();
                    for (EntityMetricNode node : metric.getNodes()) {
                        String line = node.toString();
                        if (line.endsWith(";")) { // prevents error weka/r, remove last ;
                            csv.append(line.replaceAll("NaN", "0.0").substring(0, line.length() - 1));
                        } else {
                            csv.append(line.replaceAll("NaN", "0.0"));
                        }
                        csv.append("\r\n");
                    }
                    ZipEntry ze = new ZipEntry(path);
                    zos.putNextEntry(ze);
                    zos.write(csv.toString().getBytes());
                    zos.closeEntry();
                    files.add(path);
                }
            }
            zos.close();
            download(StringUtils.capitalize(project) + " Metrics.zip", "application/zip", zipBytes.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void updateParameters() {
        for (EntityMetric metric : getMetrics()) {
            if (metric.getParams().containsKey("aprioriFilter")) {
                if (metric.getParams().get("aprioriFilter").toString().contains("Min issues 5, ")
                        && !metric.getParams().get("aprioriFilter").toString().contains("Min issues 5, Max issues 6, ")) {
                    metric.getParams().put("aprioriFilter",
                            metric.getParams().get("aprioriFilter").toString().replace("Min issues 5, ", "Min issues 5, Max issues 6, "));

                } else if (metric.getParams().get("aprioriFilter").toString().contains("Min issues 7, ")
                        && !metric.getParams().get("aprioriFilter").toString().contains("Min issues 7, Max issues 8, ")) {
                    metric.getParams().put("aprioriFilter",
                            metric.getParams().get("aprioriFilter").toString().replace("Min issues 7, ", "Min issues 7, Max issues 8, "));

                } else if (metric.getParams().get("aprioriFilter").toString().contains("Min issues 5, ")
                        && !metric.getParams().get("aprioriFilter").toString().contains("Min issues 5, Max issues 6, ")) {
                    metric.getParams().put("aprioriFilter",
                            metric.getParams().get("aprioriFilter").toString().replace("Min issues 5, ", "Min issues 5, Max issues 6, "));
                }
            }
        }
    }

    public void downloadAllCSVNotEmptyInFolder() {
        try {
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBytes);
            zos.setLevel(9);
            String project = "All";
            Set<String> files = new HashSet<>();
            Set<String> downloaded = new HashSet<>();
            for (EntityMetric metric : getMetrics()) {
                final String metricName = metric.toString();
                // download lasts metrics
                if (downloaded.contains(metricName)) {
                    continue;
                } else {
                    downloaded.add(metricName);
                }

                System.out.println("Metric " + metricName + " tem nodes: " + metric.getNodes().size());

                if (metric.getNodes().size() == 1) {
                    continue;
                }
                String fileName = generateFileName(metric);

                if (!files.contains(fileName)) {
                    StringBuilder csv = new StringBuilder();
                    for (EntityMetricNode node : metric.getNodes()) {
                        String line = node.toString();
                        if (line.endsWith(";")) { // prevents error weka/r, remove last ;
                            csv.append(line.replaceAll("NaN", "0.0").substring(0, line.length() - 1));
                        } else {
                            csv.append(line.replaceAll("NaN", "0.0"));
                        }
                        csv.append("\r\n");
                    }
                    ZipEntry ze = new ZipEntry(fileName);
                    zos.putNextEntry(ze);
                    zos.write(csv.toString().getBytes());
                    zos.closeEntry();
                    files.add(fileName);
                }
            }
            zos.close();
            download(project + ".zip", "application/zip", zipBytes.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void downloadCSV(EntityMetric metric) {
        try {
            System.out.println("Metric tem nodes: " + metric.getNodes().size());

            String fileName = generateFileName(metric) + ".csv";

            StringBuilder csv = new StringBuilder();

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
            download(fileName, "text/plain", metric.getLog().getBytes());
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
        try (OutputStream output = ec.getResponseOutputStream()) {
            output.write(content);
            output.flush();
            fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        }
    }

    private String generateFileName(EntityMetric metric) {
        return metric.toString();
    }
}
