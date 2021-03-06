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

package io.openschema.mma.backend;

import io.openschema.mma.backend.request.MetricsPushRequest;
import io.openschema.mma.backend.request.RegisterRequest;
import io.openschema.mma.backend.response.BaseResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interface used by Retrofit to generate HTTP calls. Implemented in {@link RetrofitService}.
 */
public interface BackendApi {
    /**
     * Calls the registration API in OpenSchema ETL.
     */
    @POST("register")
    Call<BaseResponse> register(@Body RegisterRequest req);

    /**
     * Pushes the metric to OpenSchema ETL.
     */
    @POST("metrics/push")
    Call<BaseResponse> pushMetric(@Body MetricsPushRequest req);
}
