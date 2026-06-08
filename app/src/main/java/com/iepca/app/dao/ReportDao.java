package com.iepca.app.dao;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;

public interface ReportDao {

    @Streaming
    @GET("reports/students/excel")
    Call<ResponseBody> downloadStudentsExcel();
}
