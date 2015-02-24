package br.edu.utfpr.cm.minerador.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.nodes.NodeGeneric;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractBichoServices implements Runnable, Serializable {

    protected final GenericDao genericDao;
    protected final GenericBichoDAO dao;
    protected Map<Object, Object> params;
    protected OutLog out;

    public AbstractBichoServices() {
        this.dao = null;
        this.genericDao = null;
    }

    public AbstractBichoServices(GenericBichoDAO dao, OutLog out) {
        this.dao = dao;
        this.genericDao = null;
        this.out = out;
    }

    public AbstractBichoServices(GenericBichoDAO dao, Map<Object, Object> params, OutLog out) {
        this(dao, null, params, out);
    }

    public AbstractBichoServices(GenericBichoDAO dao, GenericDao genericDao, Map<Object, Object> params, OutLog out) {
        this.dao = dao;
        this.genericDao = genericDao;
        this.out = out;
        this.params = params;
    }


    @Override
    public abstract void run();

    public static <T> T createInstance(String className) {
        try {
            return (T) Class.forName(className).getConstructor().newInstance();
        }
        catch (Exception ex) {
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
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
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

    protected Integer getIntegerParam(Object key) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
        }
        return Util.tratarStringParaInt(getStringParam(key).trim());
    }

    protected Long getLongParam(Object key) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
        }
        return Util.tratarStringParaLong(getStringParam(key).trim());
    }

    protected String getStringParam(Object key) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
        }
        Object obj = params.get(key);
        return obj == null ? "" : obj + "";
    }

    protected Boolean getBooleanParam(Object key) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
        }
        return Boolean.parseBoolean(getStringParam(key).trim().toLowerCase());
    }

    protected List<String> getStringLinesParam(Object key, boolean trimLines, boolean emptyLines) {
        if (!params.containsKey(key)) {
            throw new IndexOutOfBoundsException("Chave não encontrada: " + key);
        }
        List<String> lines = new ArrayList<>();
        for (String line : getStringParam(key).split("\n")) {
            if (trimLines) {
                line = line.trim();
            }
            if (emptyLines || !line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }
}
