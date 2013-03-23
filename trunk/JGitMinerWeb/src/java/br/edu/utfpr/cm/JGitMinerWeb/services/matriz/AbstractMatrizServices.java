/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes.NodeGeneric;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractMatrizServices implements Runnable, Serializable {
    
    protected final GenericDao dao;
    private final EntityRepository repository;
    private List<EntityMatrizNode> nodes;
    protected Map params;
    
    public AbstractMatrizServices(GenericDao dao) {
        this.dao = dao;
        this.repository = null;
    }
    
    public AbstractMatrizServices(GenericDao dao, EntityRepository repository, Map params) {
        this.dao = dao;
        this.repository = repository;
        this.params = params;
    }
    
    public EntityRepository getRepository() {
        return repository;
    }
    
    public Date getBeginDate() {
        return getDateParam("beginDate");
    }
    
    public Date getEndDate() {
        return getDateParam("endDate");
    }
    
    public List<EntityMatrizNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<EntityMatrizNode> nodes) {
        this.nodes = nodes;
    }
    
    @Override
    public abstract void run();
    
    public String convertToCSV() {
        StringBuilder sb = new StringBuilder(getHeadMatriz());
        sb.append("\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getLine()).append("\n");
        }
        return sb.toString();
    }
    
    public static AbstractMatrizServices createInstance(GenericDao dao, String className) {
        try {
            return (AbstractMatrizServices) Class.forName(className).getConstructor(GenericDao.class).newInstance(dao);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    protected void incrementNode(List<NodeGeneric> nodes, NodeGeneric node) {
        int index = nodes.indexOf(node);
        if (index >= 0) {
            nodes.get(index).incWeight();
        } else {
            nodes.add(node);
        }
    }
    
    protected void addToEntityMatrizNodeList(List list) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        for (Object obj : list) {
            nodes.add(new EntityMatrizNode(obj));
        }
    }
    
    protected Date getDateParam(Object key) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException(key + "");
        }
        Object obj = params.get(key);
        if (obj != null) {
            if (obj instanceof Date) {
                return (Date) obj;
            } else {
                throw new ClassCastException(key + "");
            }
        }
        return null;
    }

    /**
     * Name of columns separated by ";".
     *
     * @return column1;column2;column3;...
     */
    public abstract String getHeadMatriz();
}
