package br.edu.utfpr.cm.minerador.services.matrix.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOutputTest {

    @Test
    public void testToString() {
        FilePairOutput filePairOutput = new FilePairOutput(new FilePair("B", "A"));
        filePairOutput.setFilePairApriori(new FilePairApriori(2, 4, 2, 8));
        filePairOutput.addIssueId(1);
        filePairOutput.addIssueId(2);
        filePairOutput.addCommitId(3);
        filePairOutput.addCommitId(4);
        filePairOutput.addFutureDefectIssuesId(5);
        filePairOutput.addFutureDefectIssuesId(6);

        assertEquals("B;A;2;1,2;2;3,4;2;5,6;2;4;2;8;1.0;2.0;0.25;0.25;0.125;0.125;0.0;-1.1428571428571428;NORISKY;", filePairOutput.toString());
    }

    @Test
    public void testToStringHeader() {
        assertEquals("file1;file2;"
                + "issues;issuesId;commits;commitsId;defectIssuesId;defectIssues;"
                + "fileIssues;file2Issues;issues;allIssues;supportFile;supportFile2;"
                + "supportFilePair;confidence;confidence2;lift;conviction;conviction2;"
                + "risk;",
                FilePairOutput.getToStringHeader()
        );
    }
}
