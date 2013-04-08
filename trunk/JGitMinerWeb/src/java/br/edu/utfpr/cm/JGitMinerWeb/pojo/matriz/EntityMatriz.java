/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.InterfaceEntity;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.Startable;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
@Table(name = "matriz")
@NamedQueries({
    @NamedQuery(name = "Matriz.findAllTheLatest", query = "SELECT m FROM EntityMatriz m ORDER BY m.started DESC")
})
public class EntityMatriz implements InterfaceEntity, Startable {
    
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
    private Properties params;
    private String classServicesName;
    private String repository;
    @OneToMany(mappedBy = "matriz", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntityMatrizNode> nodes;
    
    public EntityMatriz() {
        started = new Date();
        complete = false;
        nodes = new ArrayList<>();
        params = new Properties();
    }
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
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
    
    public Properties getParams() {
        return params;
    }
    
    public void setParams(Properties params) {
        this.params = params;
    }
    
    @Override
    public String getLog() {
        return log;
    }
    
    @Override
    public void setLog(String log) {
        this.log = log;
    }
    
    public List<EntityMatrizNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<EntityMatrizNode> nodes) {
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
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EntityMatriz)) {
            return false;
        }
        EntityMatriz other = (EntityMatriz) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return repository + " - " + getClassServicesSingleName() + " - " + Util.dateDataToString(started, "dd/MM/yyyy HH:mm");
    }
    
    @Override
    public String getDownloadFileName() {
        return this.repository + "-" + this.started;
    }
    
    public void setParams(Map<String, String> params) {
        Util.addMapToProperties(this.params,params);
    }
}
