package com.iepca.app;

import com.iepca.app.config.Constants;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for role checking logic.
 */
public class SessionManagerTest {

    @Test
    public void testRoleConstants() {
        assertEquals("administrativo", Constants.ROLE_ADMIN);
        assertEquals("docente", Constants.ROLE_TEACHER);
        assertEquals("padre", Constants.ROLE_PARENT);
        assertEquals("estudiante", Constants.ROLE_STUDENT);
    }

    @Test
    public void testDateFormats() {
        assertNotNull(Constants.DATE_FORMAT_API);
        assertNotNull(Constants.DATE_FORMAT_DISPLAY);
        assertEquals("yyyy-MM-dd", Constants.DATE_FORMAT_API);
        assertEquals("dd/MM/yyyy", Constants.DATE_FORMAT_DISPLAY);
    }

    @Test
    public void testGradeScaleConstants() {
        assertEquals(17, Constants.GRADE_AD_MIN);
        assertEquals(14, Constants.GRADE_A_MIN);
        assertEquals(11, Constants.GRADE_B_MIN);
        assertEquals(0, Constants.GRADE_C_MIN);
        assertEquals(20, Constants.GRADE_MAX);
        assertEquals(11, Constants.GRADE_PASSING);
    }
}
