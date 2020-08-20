package io.openschema.mma.id;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * generate and store uuid
 */

public class UUID {

    private static String mUuid;
    private final String KEY_UUID = "key_uuid";
    private Context mContext;


    public UUID(Context context) {
        mContext = context;
        mUuid = generateUuid();
    }

    private String generateUuid(){
        String uuid;
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                KEY_UUID, Context.MODE_PRIVATE);
        uuid = sharedPref.getString(KEY_UUID, java.util.UUID.randomUUID().toString());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_UUID, uuid);
        editor.commit();
        return uuid;
    }

    public static String getUUID() {
        return mUuid;
    }

    public void setUUID(String uuid) {
        mUuid = uuid;
    }

}