package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 * Represents a commenter. To be equals, two commenters needs to have the same
 * ID.
 *
 * @author Rodrigo T. Kuroda
 */
public class Commenter {

    private final Integer id;
    private final String name;
    private final String email;

    public Commenter(Integer id, String name, String email) {
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
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.id);
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
        final Commenter other = (Commenter) obj;
        return Objects.equals(this.id, other.id);
    }

}
