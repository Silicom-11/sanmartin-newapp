package com.iepca.app.dao.callback;

/**
 * Generic callback for API operations.
 * Follows Interface Segregation Principle (SOLID).
 * @param <T> The expected response data type.
 */
public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}
