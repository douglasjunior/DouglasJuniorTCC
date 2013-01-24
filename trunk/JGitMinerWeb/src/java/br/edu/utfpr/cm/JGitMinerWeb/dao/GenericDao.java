package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.edge.AbstractEdge;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class GenericDao {

    @PersistenceContext(unitName = "pu")
    private EntityManager em;

    private EntityManager getEntityManager() {
        return em;
    }

    public void insert(Object entidade) {
        getEntityManager().persist(entidade);
    }

    public void edit(Object entidade) {
        getEntityManager().merge(entidade);
    }

    public void remove(Object entidade) {
        getEntityManager().remove(getEntityManager().merge(entidade));
    }

    public Object findByID(Object id, Class classe) {
        return getEntityManager().find(classe, id);
    }

    public List selectAll(Class classe) {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(classe));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List selectBy(int[] intervalo, Class classe) {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(classe));
        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(intervalo[1] - intervalo[0]);
        q.setFirstResult(intervalo[0]);
        return q.getResultList();
    }

    public int count(Class classe) {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root rt = cq.from(classe);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    public List executeNamedQueryComParametros(String namedQuery, String[] parametros, Object[] objetos) {
        Query query = getEntityManager().createNamedQuery(namedQuery);
        if (parametros.length != objetos.length) {
            System.err.println("A quantidade de parametros difere da quantidade de atributos.");
            return null;
        }
        for (int i = 0; i < parametros.length; i++) {
            String atributo = parametros[i];
            Object parametro = objetos[i];
            query.setParameter(atributo, parametro);
        }
        List list = query.getResultList();
        return list;
    }

    public List executeNamedQuery(String namedQuery) {
        return getEntityManager().createNamedQuery(namedQuery).getResultList();
    }

    public List selectWithParams(String select, String[] params, Object[] objects) {
        Query query = em.createQuery(select);
        if (params.length != objects.length) {
            throw new IndexOutOfBoundsException("The lenght of params array is not equals lenght of objects array.");
        }
        for (int i = 0; i < params.length; i++) {
            String atributo = params[i];
            Object parametro = objects[i];
            query.setParameter(atributo, parametro);
        }
        return query.getResultList();
    }
}
