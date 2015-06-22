package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class Version {

    private final String version;

    public Version(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.version);
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
        final Version other = (Version) obj;
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return version;
    }
}
