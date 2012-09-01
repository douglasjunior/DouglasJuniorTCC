/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.dao;

import java.util.HashMap;
import java.util.List;
import javax.persistence.Persistence;

/**
 *
 * @author Douglas
 */
public class PersistenciaServices {

    private static AbstractDao dao;

    public static boolean insere(Object objeto) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static Object buscaID(Class classe, String id) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean atualiza(Object objeto) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean remove(Object objeto) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List seleciona(String selectSql) {
throw new UnsupportedOperationException("Not yet implemented");

    }

    public static List selecionaTodos(Class classe, String ordernarPor) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List selecionaComParametros(String select, String[] parametros, Object[] objetos) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List consultarNativo(String sql) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List executeNamedQuery(String namedQuery) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List executeNamedQueryComParametros(String namedQuery, String[] parametros, Object[] objetos) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void refreshObjeto(Object object) {
throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void dataBaseConnect(boolean dropDatabase) throws Exception {
        HashMap propriedades = new HashMap();

        propriedades.put("javax.persistence.jdbc.user", "aluno");
        propriedades.put("javax.persistence.jdbc.password", "aluno");
        propriedades.put("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/aluno");
        propriedades.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");

        if (dropDatabase) {
            propriedades.put("eclipselink.ddl-generation", "drop-and-create-tables");
        } else {
            propriedades.put("eclipselink.ddl-generation", "create-tables");
        }

//        dao = new AbstractDao(Persistence.createEntityManagerFactory("pu", propriedades).createEntityManager());
    }

    public static void dataBaseDisconnect() {
        if (estaConectado()) {
            dao.getEntityManager().close();
        }
    }

    public static boolean estaConectado() {
        if (dao == null
                || dao.getEntityManager() == null
                || !dao.getEntityManager().isOpen()) {
            return false;
        }
        return true;
    }


}
