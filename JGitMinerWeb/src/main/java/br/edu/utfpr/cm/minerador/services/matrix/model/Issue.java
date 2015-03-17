package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Issue implements Comparable<Issue> {

    private final Integer id;
    private final String type;
    private final Date fixDate;

    public Issue(Integer id, String type) {
        this.id = id;
        this.type = type;
        this.fixDate = null;
    }

    public Issue(Integer id, String type, Date fixDate) {
        this.id = id;
        this.type = type;
        this.fixDate = fixDate;
    }

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Date getFixDate() {
        return fixDate;
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
        return Objects.equals(this.id, other.id);
    }

    /**
     * Order by fix date ascendant (older to newer)
     */
    @Override
    public int compareTo(Issue other) {
        if (fixDate == null) {
            return 0;
        }
        if (fixDate.after(other.getFixDate())) {
            return 1;
        } else if (fixDate.before(other.getFixDate())) {
            return -1;
        } else {
            return 0;
        }
    }

}
