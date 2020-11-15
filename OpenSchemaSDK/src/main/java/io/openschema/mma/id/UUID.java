package io.openschema.mma.id;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * generate and store uuid
 */

public class UUID {

    private static final String KEY_UUID = "key_uuid";

    private String mUUID;

    public UUID(Context context) {
        mUUID = generateUUID(context);
    }

    @SuppressLint("ApplySharedPref")
    private String generateUUID(Context context) {
        String uuid;

        //Load saved value from shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(
                KEY_UUID, Context.MODE_PRIVATE);
        uuid = sharedPref.getString(KEY_UUID, null);

        //Generate new UUID if none was found
        if (uuid == null) {
            uuid = java.util.UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_UUID, uuid);
            editor.commit();
        }

        return uuid;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String uuid) {
        mUUID = uuid;
    }

}