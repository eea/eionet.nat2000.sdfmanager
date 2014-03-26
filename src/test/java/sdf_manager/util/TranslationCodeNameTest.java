package sdf_manager.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TranslationCodeNameTest {

    @Test
    public void testGroupSpeciesByCode() {
        assertEquals("Amphibians", TranslationCodeName.getGroupSpeciesByCode("A"));
    }

    @Test
    public void checkPlusRelation() {
        assertEquals(1, TranslationCodeName.getSelectedIndexByRelationType("+"));
    }
}
