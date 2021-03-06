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

import android.content.Context;
import android.net.NetworkCapabilities;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import io.openschema.mma.backend.BackendApi;
import io.openschema.mma.data.dao.MetricsDAO;
import io.openschema.mma.data.dao.NetworkConnectionsDAO;
import io.openschema.mma.data.dao.NetworkUsageDAO;
import io.openschema.mma.data.database.MMADatabase;
import io.openschema.mma.data.entity.CellularConnectionsEntity;
import io.openschema.mma.data.entity.MetricsEntity;
import io.openschema.mma.data.entity.NetworkConnectionsEntity;
import io.openschema.mma.data.entity.NetworkUsageEntity;
import io.openschema.mma.data.entity.WifiConnectionsEntity;
import io.openschema.mma.metrics.MetricsWorker;

/**
 * Repository class to manage the metrics data.
 */
public class MetricsRepository {

    private static final String TAG = "MetricsRepository";

    //Singleton
    private static MetricsRepository _instance = null;

    /**
     * Call to retrieve a {@link MetricsRepository} object.
     */
    public static MetricsRepository getRepository(Context appContext) {
        Log.d(TAG, "UI: Fetching MetricsRepository");
        if (_instance == null) {
            synchronized (BackendApi.class) {
                if (_instance == null) {
                    _instance = new MetricsRepository(appContext);
                }
            }
        }
        return _instance;
    }

    /**
     * Thread pool used to handle multiple metrics being pushed to the database simultaneously.
     */
    private final ThreadPoolExecutor mExecutor;

    /**
     * Data access object used to interact with the data tables in the database.
     */
    private final MetricsDAO mMetricsDAO;
    private final NetworkConnectionsDAO mNetworkConnectionsDAO;
    private final NetworkUsageDAO mNetworkUsageDAO;

    private MetricsRepository(Context appContext) {
        MMADatabase db = MMADatabase.getDatabase(appContext);
        mMetricsDAO = db.metricsDAO();

        //TODO: disable with flag from MMA builder
        mNetworkConnectionsDAO = db.networkConnectionsDAO();
        mNetworkUsageDAO = db.networkUsageDAO();

        mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Writes a metrics object to the database. Queued metrics will get flushed periodically through {@link MetricsWorker}.
     */
    public void queueMetric(MetricsEntity metricsEntity) {
        mExecutor.execute(() -> mMetricsDAO.insert(metricsEntity));
    }

    /**
     * Retrieves a list of all currently queued metrics. This query is made synchronously so it can't be called from the main thread.
     */
    @WorkerThread
    public List<MetricsEntity> getEnqueuedMetricsSync() {
        return mMetricsDAO.getAllSync();
    }

    public LiveData<List<MetricsEntity>> getEnqueuedMetrics() {
        return mMetricsDAO.getAll();
    }

    /**
     * Deletes metrics that have been recently pushed by the {@link MetricsWorker}.
     *
     * @param metrics List of metrics to delete from the database
     */
    public void clearMetrics(List<MetricsEntity> metrics) {
        mMetricsDAO.delete(metrics.toArray(new MetricsEntity[0]));
    }

    //Local metrics for UI
    public void writeNetworkConnection(NetworkConnectionsEntity entity) {
        if (entity != null) {
            //TODO: disable with flag from MMA builder
            Log.d(TAG, "MMA: Writing network connection to DB");

            if (entity instanceof WifiConnectionsEntity) {
                mExecutor.execute(() -> mNetworkConnectionsDAO.insert((WifiConnectionsEntity) entity));
            } else if (entity instanceof CellularConnectionsEntity) {
                mExecutor.execute(() -> mNetworkConnectionsDAO.insert((CellularConnectionsEntity) entity));
            } else {
                Log.e(TAG, "MMA: The connection entity didn't have a valid class");
            }
        }
    }

    //Local metrics for UI
    public void writeNetworkSessionSegment(NetworkUsageEntity entity) {
        if (entity != null) {
            //TODO: disable with flag from MMA builder
            Log.d(TAG, "MMA: Writing network usage session to DB");
            mExecutor.execute(() -> mNetworkUsageDAO.insert(entity));
        }
    }

    //TODO: only expose UI related calls and hide the rest?
    public LiveData<List<NetworkConnectionsEntity>> getAllNetworkConnections(long startTime, long endTime) {
        return new NetworkConnectionsLiveData(mNetworkConnectionsDAO.getWifiConnections(startTime, endTime), mNetworkConnectionsDAO.getCellularConnections(startTime, endTime));
    }

    public void flagNetworkConnectionReported(NetworkConnectionsEntity entity) {
        switch (entity.getTransportType()) {
            case NetworkCapabilities.TRANSPORT_WIFI:
                mExecutor.execute(() -> mNetworkConnectionsDAO.setWifiReported(entity.getId()));
                break;
            case NetworkCapabilities.TRANSPORT_CELLULAR:
                mExecutor.execute(() -> mNetworkConnectionsDAO.setCellularReported(entity.getId()));
                break;
        }
    }

    public LiveData<List<NetworkUsageEntity>> getUsageEntities(long startTime, long endTime) {
        return mNetworkUsageDAO.getUsageEntities(startTime, endTime);
    }

    //MediatorLiveData used to merge both Wifi and Cellular connections into a single List stream
    static class NetworkConnectionsLiveData extends MediatorLiveData<List<NetworkConnectionsEntity>> {

        List<WifiConnectionsEntity> mLastWifiList = null;
        List<CellularConnectionsEntity> mLastCellularList = null;

        public NetworkConnectionsLiveData(LiveData<List<WifiConnectionsEntity>> wifiList, LiveData<List<CellularConnectionsEntity>> cellularList) {
            addSource(wifiList, wifiConnectionsEntities -> {
                mLastWifiList = wifiConnectionsEntities;
                update();
            });

            addSource(cellularList, cellularConnectionsEntities -> {
                mLastCellularList = cellularConnectionsEntities;
                update();
            });
        }

        private void update() {
            List<NetworkConnectionsEntity> newList = new ArrayList<>();
            if (mLastWifiList != null) newList.addAll(mLastWifiList);
            if (mLastCellularList != null) newList.addAll(mLastCellularList);
            newList.sort((o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
            setValue(newList);
        }
    }
}