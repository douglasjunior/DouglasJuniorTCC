package br.edu.utfpr.cm.minerador.services.matrix.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FilePairOutputTest {

    @Test
    public void testToStringBA() {
        FilePairOutput filePairOutput = new FilePairOutput(new FilePair("A", "B"));
        filePairOutput.setFilePairApriori(new FilePairApriori(2, 4, 2, 8));
        filePairOutput.addIssueId(1);
        filePairOutput.addIssueId(2);
        filePairOutput.addCommitId(3);
        filePairOutput.addCommitId(4);
        filePairOutput.addFutureDefectIssuesId(5);
        filePairOutput.addFutureDefectIssuesId(6);

        assertEquals("A;B;2;1,2;2;3,4;0;;0;;0;;2;5,6;0;;2;4;2;8;0.25;0.5;0.25;1.0;0.5;2.0;0.0;1.0;", filePairOutput.toString());
    }

    @Test
    public void testToStringAB() {
        FilePairOutput filePairOutput = new FilePairOutput(new FilePair("B", "A"));
        filePairOutput.setFilePairApriori(new FilePairApriori(2, 4, 2, 8));
        filePairOutput.addIssueId(1);
        filePairOutput.addIssueId(2);
        filePairOutput.addCommitId(3);
        filePairOutput.addCommitId(4);
        filePairOutput.addFutureDefectIssuesId(5);
        filePairOutput.addFutureDefectIssuesId(6);

        assertEquals("B;A;2;1,2;2;3,4;0;;0;;0;;2;5,6;0;;2;4;2;8;0.25;0.5;0.25;1.0;0.5;2.0;0.0;1.0;", filePairOutput.toString());
    }

    @Test
    public void testToStringHeader() {
        assertEquals("file1;file2;"
                + "issues;issuesId;"
                + "commits;commitsId;"
                + "commitsFile1;commitsFile1Id;"
                + "commitsFile2;commitsFile2Id;"
                + "defectIssues;defectIssuesId;"
                + "futureDefectIssues;futureDefectIssuesId;"
                + "futureIssues;futureIssuesId;"
                + "fileIssues;file2Issues;issues;allIssues;supportFile;supportFile2;"
                + "supportFilePair;confidence;confidence2;lift;conviction;conviction2;",
                FilePairOutput.getToStringHeader()
        );
    }
}
