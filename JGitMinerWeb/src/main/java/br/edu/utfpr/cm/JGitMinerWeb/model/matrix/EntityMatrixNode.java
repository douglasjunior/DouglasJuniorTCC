package br.edu.utfpr.cm.JGitMinerWeb.model.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.model.EntityNode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "matrix_node", indexes = {
    @Index(columnList = "matrix_id")
})
@DiscriminatorValue(value = "EntityMatrixNode")
public class EntityMatrixNode extends EntityNode {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matrix_id", referencedColumnName = "id")
    private EntityMatrix matrix;

    public EntityMatrixNode() {
        super();
    }

    public EntityMatrixNode(String line) {
        super(line);
    }

    public EntityMatrix getMatrix() {
        return matrix;
    }

    public void setMatrix(EntityMatrix matrix) {
        this.matrix = matrix;
    }
}
