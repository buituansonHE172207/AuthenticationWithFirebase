package com.kas.authenticationwithfirebase.service;

import com.kas.authenticationwithfirebase.data.model.SendMessageDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FcmApi {
    @POST("v1/projects/{project_id}/messages:send")
    Call<Void> sendMessage(
            @Header("Authorization") String authHeader,
            @Path("project_id") String projectId,
            @Body SendMessageDto body
    );

}
