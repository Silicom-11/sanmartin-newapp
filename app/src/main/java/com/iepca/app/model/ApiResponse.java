package com.iepca.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Generic API response wrapper matching backend ApiResponse<T>.
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int total;
    private int page;
    private int pages;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPages() { return pages; }
}
