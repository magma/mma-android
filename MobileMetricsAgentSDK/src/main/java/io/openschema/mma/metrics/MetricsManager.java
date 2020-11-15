package io.openschema.mma.metrics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.openschema.mma.id.Identity;
import io.openschema.mma.metricsd.MetricsContainer;
import io.openschema.mma.metricsd.MetricsControllerGrpc;
import io.openschema.mma.R;
import io.openschema.mma.metricsd.PushedMetricsContainer;


public class MetricsManager {

    private static Context mContext;
    private static Certificate mCert;
    public MetricsManager(Context context, Certificate cert, Boolean startService) {
        this.mContext = context;
        this.mCert = cert;
        Intent startMetricsServiceIntent = new Intent(context, MetricsService.class);

        context.startForegroundService(startMetricsServiceIntent);
    }

    public static void testCollection(MetricsContainer mContainer){
        // Test calling collect
        // 3 items to do for this:
        // 1- trust manager with rootCA
        // 2- add TLS cert to key managers
        // 3- rewrite authority header

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream tempInput = mContext.getResources().openRawResource(R.raw.rootca);
            java.security.cert.Certificate tempCert = cf.generateCertificate(tempInput);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("rootca", tempCert);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("gw_key", null);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            KeyStore clientStore = KeyStore.getInstance("AndroidKeyStore");
            clientStore.load(null, null);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            String password = "";
            java.security.cert.Certificate[] certChain = new java.security.cert.Certificate[1];
            certChain[0] = mCert;
            clientStore.setKeyEntry("gw_key", privateKey, null, certChain);
            kmf.init(clientStore, password.toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            ManagedChannel channel2 = OkHttpChannelBuilder.forAddress("controller.openschema.magma.etagecom.io", 443)
                    .useTransportSecurity()
                    .sslSocketFactory(context.getSocketFactory())
                    .overrideAuthority("metricsd-controller.openschema.magma.etagecom.io")
                    .build();

            MetricsControllerGrpc.MetricsControllerBlockingStub stub2 = MetricsControllerGrpc.newBlockingStub(channel2);

            Random rand = new Random();
            int RSSI= rand.nextInt(45) - 100;





            Log.d("GRPCtest", "metrics pushed - 1");


            stub2.collect(mContainer);

        } catch (Exception e) {
            Log.d("GRPCtest", e.toString());
        }
    }

    public static void testPush(PushedMetricsContainer mPush){
        // Test calling collect
        // 3 items to do for this:
        // 1- trust manager with rootCA
        // 2- add TLS cert to key managers
        // 3- rewrite authority header

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream tempInput = mContext.getResources().openRawResource(R.raw.rootca);
            java.security.cert.Certificate tempCert = cf.generateCertificate(tempInput);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("rootca", tempCert);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("gw_key", null);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            KeyStore clientStore = KeyStore.getInstance("AndroidKeyStore");
            clientStore.load(null, null);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            String password = "";
            java.security.cert.Certificate[] certChain = new java.security.cert.Certificate[1];
            certChain[0] = mCert;
            clientStore.setKeyEntry("gw_key", privateKey, null, certChain);
            kmf.init(clientStore, password.toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            ManagedChannel channel2 = OkHttpChannelBuilder.forAddress("controller.openschema.magma.etagecom.io", 443)
                    .useTransportSecurity()
                    .sslSocketFactory(context.getSocketFactory())
                    .overrideAuthority("metricsd-controller.openschema.magma.etagecom.io")
                    .build();

            MetricsControllerGrpc.MetricsControllerBlockingStub stub2 = MetricsControllerGrpc.newBlockingStub(channel2);

            Random rand = new Random();
            int RSSI= rand.nextInt(45) - 100;





            Log.d("GRPCtest", "metrics pushed - 1");


            stub2.push(mPush);

        } catch (Exception e) {
            Log.d("GRPCtest", e.toString());
        }
    }

}
