package com.iepca.app.controller;

import com.iepca.app.dao.AuthDao;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.AuthResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class AuthControllerTest {

    @Test
    public void loginShouldRejectBlankEmailBeforeCallingDao() {
        AuthDao authDao = mock(AuthDao.class);
        AuthController controller = new AuthController(authDao);
        TestCallback<AuthResponse> callback = new TestCallback<>();

        controller.login("", "secret", callback);

        assertEquals("Ingrese su correo electronico", callback.error);
        verifyNoInteractions(authDao);
    }

    @Test
    public void loginShouldRejectBlankPasswordBeforeCallingDao() {
        AuthDao authDao = mock(AuthDao.class);
        AuthController controller = new AuthController(authDao);
        TestCallback<AuthResponse> callback = new TestCallback<>();

        controller.login("admin@iepca.edu.pe", "", callback);

        assertEquals("Ingrese su contrasena", callback.error);
        verifyNoInteractions(authDao);
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
