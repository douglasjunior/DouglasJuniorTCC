package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Issue {

    private final Integer id;
    private final String type;

    public Issue(Integer id, String type) {
        this.id = id;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ID=" + id + " Type=" + type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Issue other = (Issue) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
