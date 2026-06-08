package com.iepca.app.controller;

import android.content.Context;

import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.Student;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Controller for administrator student management.
 *
 * MVC role: receives user intentions from Fragments, applies validation rules,
 * and delegates API persistence to StudentDao.
 */
public class StudentManagementController extends BaseApiController {

    private final StudentDao studentDao;

    public StudentManagementController(Context context) {
        this(RetrofitClient.createService(context, StudentDao.class));
    }

    public StudentManagementController(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    public void listStudents(String search, int page, int limit, ApiCallback<List<Student>> callback) {
        execute(studentDao.getStudentsManagement(page, limit, normalize(search), null), callback);
    }

    public void createStudent(Map<String, Object> data, ApiCallback<Student> callback) {
        String validation = validateStudentPayload(data);
        if (validation != null) {
            callback.onError(validation);
            return;
        }
        execute(studentDao.createStudent(data), callback);
    }

    public void updateStudent(String id, Map<String, Object> data, ApiCallback<Student> callback) {
        if (StringUtils.isBlank(id)) {
            callback.onError("Seleccione un estudiante valido");
            return;
        }
        String validation = validateStudentPayload(data);
        if (validation != null) {
            callback.onError(validation);
            return;
        }
        execute(studentDao.updateStudent(id, data), callback);
    }

    private String validateStudentPayload(Map<String, Object> data) {
        if (isBlank(data, "firstName")) return "Ingrese nombres del estudiante";
        if (isBlank(data, "lastName")) return "Ingrese apellidos del estudiante";
        if (isBlank(data, "dni")) return "Ingrese DNI del estudiante";
        if (isBlank(data, "gradeLevel")) return "Seleccione grado";
        if (isBlank(data, "section")) return "Seleccione seccion";
        return null;
    }

    private boolean isBlank(Map<String, Object> data, String key) {
        Object value = data != null ? data.get(key) : null;
        return value == null || StringUtils.isBlank(String.valueOf(value));
    }

    private String normalize(String value) {
        return StringUtils.trimToNull(value);
    }
}
