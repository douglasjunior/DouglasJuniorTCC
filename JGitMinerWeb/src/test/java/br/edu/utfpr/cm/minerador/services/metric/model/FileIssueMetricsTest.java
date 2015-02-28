package br.edu.utfpr.cm.minerador.services.metric.model;

import br.edu.utfpr.cm.minerador.services.metric.committer.Committer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FileIssueMetricsTest {

    public FileIssueMetricsTest() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testHashCode() {
    }

    @Test
    public void testEqualsTrue() {
        FileIssueMetrics m = new FileIssueMetrics("A.java", "B.java", new Commit(1, new Committer(1, "Foo", "Bar")), new EmptyIssueMetrics());
        FileIssueMetrics m2 = new FileIssueMetrics("A.java", "C.java", new Commit(1, new Committer(1, "Foo", "Bar")), new EmptyIssueMetrics());
        Assert.assertTrue(m.equals(m));
        Assert.assertTrue(m2.equals(m2));
        Assert.assertTrue(m.equals(m2));
        Assert.assertTrue(m2.equals(m));
    }

    @Test
    public void testEqualsFalse() {
        FileIssueMetrics m = new FileIssueMetrics("A.java", "B.java", new Commit(1, new Committer(1, "Foo", "Bar")), new EmptyIssueMetrics());
        FileIssueMetrics m2 = new FileIssueMetrics("A.java", "B.java", new Commit(2, new Committer(1, "Foo", "Bar")), new EmptyIssueMetrics());
        Assert.assertFalse(m.equals(m2));
        Assert.assertFalse(m2.equals(m));
    }

}
