package io.openschema.mma.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


import io.openschema.mma.MobileMetricsAgent;
import io.openschema.mma.bootstrap.BootStrapManager;
import io.openschema.mma.metrics.MetricsManager;
import io.openschema.mma.metrics.MetricsService;

public class MainActivity extends AppCompatActivity {

    private final String CONTROLLER_ADDRESS = "controller.openschema.magma.etagecom.io";
    private final int CONTROLLER_PORT = 443;
    private final String BOOTSTRAPPER_CONTROLLER_ADDRESS = "bootstrapper-" + CONTROLLER_ADDRESS;
    private final String METRICS_AUTHORITY_HEADER = "metricsd-" + CONTROLLER_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileMetricsAgent mmaBuilder = new MobileMetricsAgent.MobileMetricsAgentBuilder()
                .setAppContext(getApplicationContext())
                .setAuthorityHeader(METRICS_AUTHORITY_HEADER)
                .setControllerAddress(CONTROLLER_ADDRESS)
                .setBootStrapperAddress(BOOTSTRAPPER_CONTROLLER_ADDRESS)
                .setControllerPort(CONTROLLER_PORT)
                .buildMobileMetricsAgent();

        try {
            mmaBuilder.init();
            mmaBuilder.getBootStrapManager().BootstrapNow();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        // TODO: Move this inside MobileMetricsAgent
        MetricsManager mMmanager = new MetricsManager(getApplicationContext(), mmaBuilder.getBootStrapManager().getClientCert(), true);


    }
}