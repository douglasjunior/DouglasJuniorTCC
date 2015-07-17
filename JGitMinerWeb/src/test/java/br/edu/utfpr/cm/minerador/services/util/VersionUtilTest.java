package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class VersionUtilTest {

    private List<Version> allVersions;
    private int minOccurrences = 2;

    @Before
    public void setUp() {
        allVersions = new ArrayList<>();
        for (String version : new String[]{"1.1", "1.2", "1.3", "1.4", "1.5", "1.6"}) {
            allVersions.add(new Version(version));
        }
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsEqual() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.4", "1.5");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.4", "1.5"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsInEnd() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.4", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.4", "1.5", "1.6"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsInBegin() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsAll() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3", "1.4", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3", "1.4", "1.5", "1.6"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsOne() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList("1.1", "1.3", "1.5");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.5"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsNone() {
        List<Version> occurrencesVersions = VersionTestHelper.getVersionsAsList();
        Assert.assertEquals(VersionTestHelper.getVersionsAsList(),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsEqualWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap(2, "1.1", "1.2", "1.4", "1.5");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.4", "1.5"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsInEndWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap(2, "1.1", "1.2", "1.4", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.4", "1.5", "1.6"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsInBeginWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap(2, "1.1", "1.2", "1.3", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsAllWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap(2, "1.1", "1.2", "1.3", "1.4", "1.5", "1.6");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.1", "1.2", "1.3", "1.4", "1.5", "1.6"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsOneWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap(2, "1.1", "1.3", "1.5");
        Assert.assertEquals(VersionTestHelper.getVersionsAsList("1.5"),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }

    @Test
    public void testGetMaxVersionSequenceMaxIsNoneWithMinOccurrencesInVersion() {
        Map<Version, AtomicInteger> occurrencesVersions = VersionTestHelper.getVersionsAsMap();
        Assert.assertEquals(VersionTestHelper.getVersionsAsList(),
                VersionUtil.getMaxVersionSequence(occurrencesVersions, allVersions, minOccurrences));
    }


}
