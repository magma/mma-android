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

package io.openschema.mma.helpers;

import com.google.protobuf.ByteString;

/**
 * Helper class for R and S byte operations.
 */
public class RandSByteString {
    private ByteString r;
    private ByteString s;

    public RandSByteString() {
        this.r = null;
        this.s = null;
    }

    public void setR(ByteString r) {
        this.r = r;
    }

    public void setS(ByteString s) {
        this.s = s;
    }

    public ByteString getR(){
        return this.r;
    }
    public ByteString getS(){
        return this.s;
    }
}
