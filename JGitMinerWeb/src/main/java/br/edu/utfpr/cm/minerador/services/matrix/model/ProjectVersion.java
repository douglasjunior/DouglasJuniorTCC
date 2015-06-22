package br.edu.utfpr.cm.minerador.services.matrix.model;

import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class ProjectVersion {

    private final Project project;
    private final Version version;

    public ProjectVersion(Project project, Version version) {
        this.project = project;
        this.version = version;
    }

    public Project getProject() {
        return project;
    }

    public Version getVersion() {
        return version;
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
        final ProjectVersion other = (ProjectVersion) obj;
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return project.toString() + ";" + version.toString();
    }

}
