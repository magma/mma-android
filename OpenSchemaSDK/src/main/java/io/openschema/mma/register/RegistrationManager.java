package io.openschema.mma.register;

import android.content.Context;
import android.util.Log;

import io.openschema.mma.id.Identity;
import io.openschema.mma.networking.BackendApi;
import io.openschema.mma.networking.request.RegisterRequest;
import io.openschema.mma.networking.response.BaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Client or UE need to be registered on the Cloud with their UUID and Key for the bootstrapping process.
 * Registration is manual now but should be automated.
 * <p>
 * Sample Service
 * message UeParams {
 * bytes key = 1;
 * string uuid = 2;
 * }
 * service RegisterUE {
 * rpc Register (UeParams) returns (Response) {}
 * }
 * message Response {
 * string response = 1;
 * }
 */
public class RegistrationManager {

    private static final String TAG = "RegistrationManager";

    private Identity mIdentity;
    private BackendApi mBackendApi;

    public RegistrationManager(Context context, Identity identity, BackendApi backendApi) {
        mIdentity = identity;
        mBackendApi = backendApi;
    }

    public void register() {
        mBackendApi.register(new RegisterRequest(mIdentity.getUUID(), mIdentity.getPublicKey()))
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        Log.d(TAG, "onResponse: " + response.body().getMessage());
                    }
                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.toString());
                    }
                });
    }
}
