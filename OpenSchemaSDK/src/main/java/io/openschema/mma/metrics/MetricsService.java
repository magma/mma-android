package io.openschema.mma.metrics;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.util.ASN1Dump;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.openssl.PEMWriter;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.openschema.mma.R;
import io.openschema.mma.bootstrap.BootStrapManager;

/**
 * Service Mode: start the Service and some auto metrics will be pushed
 */

public class MetricsService extends Service {

    private String mUUID;
    private String mSSID;
    private String mBSSID;
    private String mMacAddr;
    private int mRssi;
    private String mLinkSpeed;
    private String mFreq;
    private String mLat;
    private String mLong;
    private Certificate mCert;

    private ConnectivityManager mConnectivityManager;
    TelephonyManager telephonyManager;


    private static final String TAG = MetricsService.class.getSimpleName();


    private Handler mHandler = null;

    private HandlerThread mNetworkHandlerThread;
    private Handler mNetworkHandler = null;


    private Thread testThread;
    private Runnable testRunnable;
    private boolean isRunning = false;

    private static int mBondingMode;
    private static Context mContext;

    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                //new scan results available
                android.util.Log.d(TAG, "WORKER: New Wi-Fi APs available from scan");
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        mContext = this.getApplicationContext();

        mHandler = new Handler();

        mConnectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkRequest wifiRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        NetworkRequest cellularRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        mConnectivityManager.requestNetwork(wifiRequest, mWifiCallBack);
        mConnectivityManager.requestNetwork(cellularRequest, mCellularCallback);

        telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "MetricsService started: ");

        String NOTIFICATION_CHANNEL_ID = "io.openschema.mma.android.tt";
        String channelName = "Metrics Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent notificationIntent = new Intent(this, getApplicationContext().getClass());
        //Intent notificationIntent = new Intent(this, this.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("OpenSchema Metrics TestTool")
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        mHandler = null;
        this.unregisterReceiver(mWifiScanReceiver);
        mConnectivityManager.unregisterNetworkCallback(mWifiCallBack);
        if (mNetworkHandlerThread != null) {
            mNetworkHandlerThread.quitSafely();
        }

    }

    private ConnectivityManager.NetworkCallback mCellularCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            Log.d(TAG, "onAvailable");

        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.d(TAG, "onLosing");
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Log.d(TAG, "onLost");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Log.d(TAG, "onUnavailable");
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            Log.d(TAG, "onCapChanged" + networkCapabilities.toString());
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.d(TAG, "onLinPropChanged" + linkProperties.toString());
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            Log.d(TAG, "onBlocked");
        }
    };

    private ConnectivityManager.NetworkCallback mWifiCallBack = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            Log.d("ssidDebug", "Wifi Connected");

            collectWifiMetrics();
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Log.d("ssidDebug", "Wifi Disconnected");
            collectWifiMetrics();

        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            collectWifiMetrics();
            Log.d("ssidDebug", networkCapabilities.toString());



        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            collectWifiMetrics();
            Log.d("ssidDebug", linkProperties.toString());

        }
    };

    private void collectWifiMetrics() {
        WifiManager mWifiMgr = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiMetrics wifiMetrics = new WifiMetrics(mWifiMgr);
        wifiMetrics.collectWifiMetrics();

    }
}
