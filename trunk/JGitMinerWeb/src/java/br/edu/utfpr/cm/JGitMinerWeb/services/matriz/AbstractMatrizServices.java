/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractMatrizServices implements Runnable, Serializable {

    protected GenericDao dao;
    private EntityRepository repository;
    private List<EntityMatrizNode> nodes;
    protected Map params;

    public AbstractMatrizServices(GenericDao dao) {
        this.dao = dao;
    }

    public AbstractMatrizServices(GenericDao dao, EntityRepository repository, Map params) {
        this.dao = dao;
        this.repository = repository;
        this.params = params;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public List<EntityMatrizNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<EntityMatrizNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public abstract void run();

    public abstract String convertToCSV(Collection<EntityMatrizNode> nodes);

    public static AbstractMatrizServices createInstance(GenericDao dao, String className) {
        try {
            return (AbstractMatrizServices) Class.forName(className).getConstructor(GenericDao.class).newInstance(dao);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
}