/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.net;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.edge.AbstractEdge;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import java.util.Date;
import java.util.List;

/**
 *
 * @author douglas
 */
public abstract class AbstractNet implements Runnable {

    protected GenericDao dao;
    protected EntityRepository repository;
    protected Date begin;
    protected Date end;
    protected List<AbstractEdge> net;

    public AbstractNet(EntityRepository repository, Date begin, Date end, GenericDao dao) {
        this.dao = dao;
        this.repository = repository;
        this.begin = begin;
        this.end = end;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public Date getBegin() {
        return begin;
    }

    public Date getEnd() {
        return end;
    }

    public List<AbstractEdge> getNet() {
        return net;
    }

    @Override
    public abstract void run();
}
