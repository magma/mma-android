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

package io.openschema.client.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import io.openschema.client.util.FormattingUtils;
import io.openschema.mma.MobileMetricsAgent;
import io.openschema.client.R;
import io.openschema.client.util.PermissionManager;
import io.openschema.mma.utils.PersistentNotification;
import io.openschema.mma.utils.UsageRetriever;

public class MainActivity extends AppCompatActivity {

    private MobileMetricsAgent mMobileMetricsAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkMandatoryPermissions()) {
            NavController navController = Navigation.findNavController(this, R.id.main_content);

            //Setup bottom navigation
            BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottom_navigation_bar);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            //Setup tool bar (top app bar)
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_usage, R.id.nav_map, R.id.nav_metric_logs).build();
            Toolbar toolbar = findViewById(R.id.main_toolbar);
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

            //Build OpenSchema agent with required data
//            mMobileMetricsAgent = new MobileMetricsAgent.Builder()
//                    .setAppContext(getApplicationContext())
//                    .setCustomNotification(createCustomNotification())
//                    .setBackendBaseURL(getString(R.string.backend_base_url))
//                    .setBackendCertificateResId(R.raw.backend)
//                    .setBackendUsername(getString(R.string.backend_username))
//                    .setBackendPassword(getString(R.string.backend_password))
//                    .build();
//
//            //Initialize agent
//            mMobileMetricsAgent.init();

            //TESTING:
            UsageRetriever usageRetriever = new UsageRetriever(this);

            //4pm to 5pm 7/8
            long windowTotal = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625778000000L, 1625781600000L);
            //15m increments
            long windowQ1 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625778000000L, 1625778900000L);
            long windowQ2 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625778900000L, 1625779800000L);
            long windowQ3 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625779800000L, 1625780700000L);
            long windowQ4 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625780700000L, 1625781600000L);

            //4:20pm to 4:21pm
            long window1 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625779200000L, 1625779260000L);
            //4:20pm to 4:30pm
            long window2 = usageRetriever.getDeviceTonnage(NetworkCapabilities.TRANSPORT_WIFI, 1625779200000L, 1625779800000L);

            Log.d("TESTING", "UI: 4pm-5pm: " + FormattingUtils.humanReadableByteCountSI(windowTotal) +
                    "\n4:00pm-4:15pm: " + FormattingUtils.humanReadableByteCountSI(windowQ1) +
                    "\n4:15pm-4:30pm: " + FormattingUtils.humanReadableByteCountSI(windowQ2) +
                    "\n4:30pm-4:45pm: " + FormattingUtils.humanReadableByteCountSI(windowQ3) +
                    "\n4:45pm-5:00pm: " + FormattingUtils.humanReadableByteCountSI(windowQ4) +
                    "\n4:20pm-4:21pm: " + FormattingUtils.humanReadableByteCountSI(window1) +
                    "\n4:20pm-4:30pm: " + FormattingUtils.humanReadableByteCountSI(window2));
        }
    }

    public void pushMetric(String metricName, List<Pair<String, String>> metricValues) {
        mMobileMetricsAgent.pushMetric(metricName, metricValues);
    }

    private boolean checkMandatoryPermissions() {
        if (!PermissionManager.areMandatoryPermissionsGranted(this)) {
            //Redirect to onboarding to go through the required permissions
            NavController navController = Navigation.findNavController(this, R.id.main_content);
            navController.navigate(R.id.action_to_onboarding);
            finish();
            return false;
        }
        return true;
    }

    private Notification createCustomNotification() {
        //Create intent to open main page
        Bundle args = new Bundle();
        args.putInt("requestCode", PersistentNotification.SERVICE_NOTIFICATION_ID);
        PendingIntent pendingIntent = new NavDeepLinkBuilder(this)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.nav_graph_main)
                .setDestination(R.id.nav_usage)
                .setArguments(args)
                .createPendingIntent();

        return new NotificationCompat.Builder(this, PersistentNotification.SERVICE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(io.openschema.mma.R.drawable.persistent_notification_icon)
                .setContentTitle("OpenSchema is running")
                .setContentText("Tap here for more information.")
                .setOngoing(true) //notification can't be swiped
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMandatoryPermissions();
    }
}