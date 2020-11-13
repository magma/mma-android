package io.openschema.mma.networking.response;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import okhttp3.ResponseBody;

public class BaseResponse {
    @SerializedName("message") private String mMessage;
    public String getMessage() { return mMessage; }

    private final static String ERROR_MESSAGE_FIELD = "message";
    public static String getErrorMessage(ResponseBody res) {
        String errorMessage = "";
        try {
            JSONObject jsonObject = new JSONObject(res.string());
            errorMessage = (String) jsonObject.get(ERROR_MESSAGE_FIELD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }
}
