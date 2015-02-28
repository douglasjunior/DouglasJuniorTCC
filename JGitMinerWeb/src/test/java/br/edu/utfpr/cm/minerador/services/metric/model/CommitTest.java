package br.edu.utfpr.cm.minerador.services.metric.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommitTest {

    public CommitTest() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testHashCode() {
    }

    @Test
    public void testEquals() {
        final Commit commit1 = new Commit(1, new Committer(1, "Foo", "Bar"));
        final Commit commit2 = new Commit(1, new Committer(2, "Bar", "Foo"));

        assertTrue(commit1.equals(commit1));
        assertTrue(commit2.equals(commit2));
        assertTrue(commit1.equals(commit2));
        assertTrue(commit2.equals(commit1));
    }

    @Test
    public void testNotEquals() {
        final Commit commit1 = new Commit(1, new Committer(1, "Foo", "Bar"));
        final Commit commit2 = new Commit(2, new Committer(1, "Foo", "Bar"));

        assertFalse(commit1.equals(commit2));
        assertFalse(commit2.equals(commit1));
    }

}
