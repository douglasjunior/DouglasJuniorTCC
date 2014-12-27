package br.edu.utfpr.cm.JGitMinerWeb.model.metric;

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
@Table(name = "metric_node", indexes = {
    @Index(columnList = "metric_id")
})
@DiscriminatorValue(value = "EntityMetricNode")
public class EntityMetricNode extends EntityNode {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", referencedColumnName = "id")
    private EntityMetric metric;

    public EntityMetricNode() {
        super();
    }

    public EntityMetricNode(String line) {
        super(line);
    }

    public EntityMetric getMetric() {
        return metric;
    }

    public void setMetric(EntityMetric metric) {
        this.metric = metric;
    }
}
