/*
 * Copyright (c) 2020, The Magma Authors
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openschema.mma.data;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import androidx.room.TypeConverter;
import io.openschema.mma.data.pojo.Timestamp;

/**
 * Class used by Room to handle complex data objects.
 */
public class TimestampTypeConverter {
    private static final String TAG = "MetricsTypeConverter";

    /**
     * Create the metrics holder object from its string representation
     */
    @TypeConverter
    public static Timestamp fromString(String value) {
        Type type = new TypeToken<Timestamp>() {}.getType();
        try {
            return new Gson().fromJson(value, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "Json string was " + value);
            return null;
        }
    }

    /**
     * Convert the metrics holder object to its string representation
     */
    @TypeConverter
    public static String toString(Timestamp timestamp) {
        GsonBuilder builder = new GsonBuilder();
        builder.enableComplexMapKeySerialization();
        Gson gson = builder.create();
        return gson.toJson(timestamp);
    }
}