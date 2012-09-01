package br.edu.utfpr.cm.JGitMinerWeb.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class AbstractDao<T> {

    private Class<T> entityClass;

    public AbstractDao(Class<T> classeEntidade) {
        this.entityClass = classeEntidade;
    }

    protected abstract EntityManager getEntityManager();

    public void insert(T entidade) {
        getEntityManager().persist(entidade);
    }
    
    public void edit(T entidade) {
        getEntityManager().merge(entidade);
    }

    public void remove(T entidade) {
        getEntityManager().remove(getEntityManager().merge(entidade));
    }

    public T findByID(Object id) {
        return (T) getEntityManager().find(entityClass, id);
    }

    public List<T> selectAll() {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> selectBy(int[] intervalo) {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(intervalo[1] - intervalo[0]);
        q.setFirstResult(intervalo[0]);
        return q.getResultList();
    }

    public int count() {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
}
