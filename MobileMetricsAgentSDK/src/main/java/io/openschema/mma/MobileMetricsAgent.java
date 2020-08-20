package io.openschema.mma;

import android.content.Context;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import io.openschema.mma.bootstrap.BootStrapManager;

public class MobileMetricsAgent {




    // Cloud Endpoint
    private final String CONTROLLER_ADDRESS = "controller.openschema.magma.etagecom.io";
    private final int CONTROLLER_PORT = 443;
    private final String BOOTSTRAPPER_CONTROLLER_ADDRESS = "bootstrapper-" + CONTROLLER_ADDRESS;
    private final String METRICS_AUTHORITY_HEADER = "metricsd-" + CONTROLLER_ADDRESS;

    private String controllerAddress = CONTROLLER_ADDRESS;
    private String boostStrapperAddress = BOOTSTRAPPER_CONTROLLER_ADDRESS;
    private String metricsAuthorityHeader = METRICS_AUTHORITY_HEADER;
    private int controllerPort = CONTROLLER_PORT;


    private Context appContext;
    private BootStrapManager bootStrapManager;


    public MobileMetricsAgent(MobileMetricsAgentBuilder mmaBuilder) {
        this.boostStrapperAddress = mmaBuilder.boostStrapperAddress;
        this.controllerAddress = mmaBuilder.controllerAddress;
        this.metricsAuthorityHeader = mmaBuilder.metricsAuthorityHeader;
        this.controllerPort = mmaBuilder.controllerPort;
        this.appContext = mmaBuilder.appContext;

    }

    public void init() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        this.bootStrapManager = new BootStrapManager(appContext);
    }




    public static class MobileMetricsAgentBuilder {
        private String controllerAddress;
        private String boostStrapperAddress;
        private String metricsAuthorityHeader;
        private int controllerPort;
        private Context appContext;


        public MobileMetricsAgentBuilder setControllerAddress(String address) {
            this.controllerAddress = address;
            return this;
        }

        public MobileMetricsAgentBuilder setBootStrapperAddress(String address) {
            this.boostStrapperAddress = address;
            return this;
        }

        public MobileMetricsAgentBuilder setAuthorityHeader(String address) {
            this.metricsAuthorityHeader = address;
            return this;
        }

        public MobileMetricsAgentBuilder setControllerPort(int port) {
            this.controllerPort = port;
            return this;
        }

        public MobileMetricsAgentBuilder setAppContext(Context context) {
            this.appContext = context;
            return this;
        }

        public MobileMetricsAgent buildMobileMetricsAgent() {
            return new MobileMetricsAgent(this);
        }
    }

    public String getControllerAddress() {
        return controllerAddress;
    }

    public String getBoostStrapperAddress() {
        return boostStrapperAddress;
    }

    public String getMetricsAuthorityHeader() {
        return metricsAuthorityHeader;
    }

    public int getControllerPort() {
        return controllerPort;
    }

    public BootStrapManager getBootStrapManager() {
        return this.bootStrapManager;
    }

}
