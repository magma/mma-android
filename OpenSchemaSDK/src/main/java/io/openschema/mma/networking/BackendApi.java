package io.openschema.mma.networking;

import io.openschema.mma.networking.request.RegisterRequest;
import io.openschema.mma.networking.response.BaseResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BackendApi {
    @POST("register")
    Call<BaseResponse> register(@Body RegisterRequest req);
}
