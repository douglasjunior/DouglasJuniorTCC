package br.edu.utfpr.cm.minerador.services.metric.model;

import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Committer {

    public final static String HEADER = "committer;";

    private final Integer id;
    private final String name;
    private final String email;

    public Committer(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return name + ";";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
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
        final Committer other = (Committer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
