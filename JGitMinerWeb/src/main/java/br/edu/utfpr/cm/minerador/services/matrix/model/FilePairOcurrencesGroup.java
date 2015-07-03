package br.edu.utfpr.cm.minerador.services.matrix.model;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOcurrencesGroup {

    private final GroupFilePairReleaseOcurrenceByQuantity grouping;
    private int quantity = 0;

    public FilePairOcurrencesGroup(GroupFilePairReleaseOcurrenceByQuantity grouping) {
        this.grouping = grouping;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increment() {
        quantity++;
    }

    public GroupFilePairReleaseOcurrenceByQuantity getGrouping() {
        return grouping;
    }

}
