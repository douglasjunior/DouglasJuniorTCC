/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.model.metric;

import br.edu.utfpr.cm.JGitMinerWeb.model.InterfaceEntity;
import br.edu.utfpr.cm.JGitMinerWeb.model.Startable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "metric")
@NamedQueries({
    @NamedQuery(name = "Metric.findAllTheLatest", query = "SELECT m FROM EntityMetric m ORDER BY m.started DESC")
})
public class EntityMetric implements InterfaceEntity, Startable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date started;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date stoped;
    private boolean complete;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String log;
    private Map<Object, Object> params;
    @OneToMany(mappedBy = "metric", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntityMetricNode> nodes;
    private String classServicesName;
    private String matrix;

    public EntityMetric() {
        started = new Date();
        complete = false;
        nodes = new ArrayList<>();
        params = new HashMap();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getStoped() {
        return stoped;
    }

    public void setStoped(Date stoped) {
        this.stoped = stoped;
    }

    @Override
    public String getLog() {
        return log;
    }

    @Override
    public void setLog(String log) {
        this.log = log;
    }

    public List<EntityMetricNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<EntityMetricNode> nodes) {
        this.nodes = nodes;
    }

    public String getClassServicesName() {
        return classServicesName;
    }

    public String getClassServicesSingleName() {
        if (classServicesName != null) {
            String[] tokens = classServicesName.split("\\.");
            if (tokens.length > 0) {
                return tokens[tokens.length - 1];
            }
        }
        return classServicesName;
    }

    public void setClassServicesName(String classServices) {
        this.classServicesName = classServices;
    }

    public Map<Object, Object> getParams() {
        return params;
    }

    public void setParams(Map<Object, Object> params) {
        this.params = params;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EntityMetric)) {
            return false;
        }
        EntityMetric other = (EntityMetric) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final Object filename = params.get("filename");
        if (filename != null) {
            return matrix + " - metrics " + filename;
        } else {
            return matrix + " - metrics";
        }
    }

    @Override
    public String getDownloadFileName() {
        return this.matrix + "-" + this.started;
    }

}
