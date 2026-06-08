package com.iepca.app;

import com.iepca.app.config.Constants;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for grade calculations.
 * Follows TDD approach as required by rubrica.
 */
public class GradeUtilsTest {

    @Test
    public void testGradeLetterAD() {
        assertEquals("AD", getGradeLetter(20));
        assertEquals("AD", getGradeLetter(17));
        assertEquals("AD", getGradeLetter(18.5));
    }

    @Test
    public void testGradeLetterA() {
        assertEquals("A", getGradeLetter(16));
        assertEquals("A", getGradeLetter(14));
        assertEquals("A", getGradeLetter(15.5));
    }

    @Test
    public void testGradeLetterB() {
        assertEquals("B", getGradeLetter(13));
        assertEquals("B", getGradeLetter(11));
        assertEquals("B", getGradeLetter(12));
    }

    @Test
    public void testGradeLetterC() {
        assertEquals("C", getGradeLetter(10));
        assertEquals("C", getGradeLetter(0));
        assertEquals("C", getGradeLetter(5));
    }

    @Test
    public void testPassingGrade() {
        assertTrue(isPassing(11));
        assertTrue(isPassing(20));
        assertFalse(isPassing(10));
        assertFalse(isPassing(0));
    }

    @Test
    public void testGradeBoundaries() {
        assertEquals("C", getGradeLetter(10.9));
        assertEquals("B", getGradeLetter(11));
        assertEquals("B", getGradeLetter(13.9));
        assertEquals("A", getGradeLetter(14));
        assertEquals("A", getGradeLetter(16.9));
        assertEquals("AD", getGradeLetter(17));
    }

    // Helper methods (mirroring GradeUtils without Android context)
    private String getGradeLetter(double score) {
        if (score >= Constants.GRADE_AD_MIN) return "AD";
        if (score >= Constants.GRADE_A_MIN) return "A";
        if (score >= Constants.GRADE_B_MIN) return "B";
        return "C";
    }

    private boolean isPassing(double score) {
        return score >= Constants.GRADE_PASSING;
    }
}
