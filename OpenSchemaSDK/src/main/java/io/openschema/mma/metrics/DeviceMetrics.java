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

package io.openschema.mma.metrics;

/**
 * Collects metrics related to the device.
 */
public class DeviceMetrics {
    public static final String METRIC_FAMILY_NAME = "openschema_android_device_info";
    private static final String METRIC_MODEL = "model";
    private static final String METRIC_OS_VERSION = "os_version";
}
