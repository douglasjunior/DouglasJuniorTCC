/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes.NodeGeneric;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractServices implements Runnable, Serializable {

    protected final GenericDao dao;
    protected List<EntityNode> nodes;
    protected Map params;

    public AbstractServices(GenericDao dao) {
        this.dao = dao;
    }

    public AbstractServices(GenericDao dao, Map params) {
        this(dao);
        this.params = params;
    }

    public Date getBeginDate() {
        return getDateParam("beginDate");
    }

    public Date getEndDate() {
        return getDateParam("endDate");
    }

    public List<EntityNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<EntityNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public abstract void run();

    public String convertToCSV() {
        StringBuilder sb = new StringBuilder(getHeadCSV());
        sb.append("\n");
        for (EntityNode node : nodes) {
            sb.append(node.getLine()).append("\n");
        }
        return sb.toString();
    }

    public static <T> T createInstance(GenericDao dao, String className) {
        try {
            return (T) Class.forName(className).getConstructor(GenericDao.class).newInstance(dao);
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
    public abstract String getHeadCSV();
}
