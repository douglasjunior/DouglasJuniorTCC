/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.model.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.model.EntityNode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author douglas
 */
@Entity
@DiscriminatorValue(value = "EntityMatrixNode")
public class EntityMatrixNode extends EntityNode {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matrix_id", referencedColumnName = "id", nullable = false)
    private EntityMatrix matrix;

    public EntityMatrixNode() {
        super();
    }

    public EntityMatrixNode(Object line) {
        super(line);
    }

    public EntityMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(EntityMatrix matrix) {
        this.matrix = matrix;
    }
}
