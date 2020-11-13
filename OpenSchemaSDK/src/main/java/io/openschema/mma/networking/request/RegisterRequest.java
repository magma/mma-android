package io.openschema.mma.networking.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("uuid") private String mUUID;
    @SerializedName("publicKey") private String mPublicKey;

    public RegisterRequest(String uuid, String publicKey) {
        mUUID = uuid;
        mPublicKey = publicKey;
    }
}
