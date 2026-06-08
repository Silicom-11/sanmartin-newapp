package com.iepca.app.controller;

import com.iepca.app.dao.StudentDao;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.Student;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class StudentManagementControllerTest {

    @Test
    public void createStudentShouldValidateRequiredNamesBeforeCallingDao() {
        StudentDao studentDao = mock(StudentDao.class);
        StudentManagementController controller = new StudentManagementController(studentDao);
        TestCallback<Student> callback = new TestCallback<>();

        controller.createStudent(new HashMap<>(), callback);

        assertEquals("Ingrese nombres del estudiante", callback.error);
        verifyNoInteractions(studentDao);
    }

    @Test
    public void updateStudentShouldRejectMissingIdBeforeCallingDao() {
        StudentDao studentDao = mock(StudentDao.class);
        StudentManagementController controller = new StudentManagementController(studentDao);
        TestCallback<Student> callback = new TestCallback<>();

        controller.updateStudent("", validStudentPayload(), callback);

        assertEquals("Seleccione un estudiante valido", callback.error);
        verifyNoInteractions(studentDao);
    }

    private Map<String, Object> validStudentPayload() {
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Sofia");
        data.put("lastName", "Ramirez");
        data.put("dni", "70000004");
        data.put("gradeLevel", "3");
        data.put("section", "A");
        return data;
    }

    private static class TestCallback<T> implements ApiCallback<T> {
        private String error;

        @Override
        public void onSuccess(T data) {
        }

        @Override
        public void onError(String message) {
            this.error = message;
        }
    }
}
