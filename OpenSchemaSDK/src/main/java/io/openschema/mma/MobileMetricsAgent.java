package io.openschema.mma;

import android.content.Context;

import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import io.openschema.mma.bootstrap.BootStrapManager;
import io.openschema.mma.id.Identity;
import io.openschema.mma.register.RegistrationManager;

public class MobileMetricsAgent {

    private String mControllerAddress;
    private String mBoostStrapperAddress;
    private String mMetricsAuthorityHeader;
    private int mControllerPort;

    private Context mAppContext;
    private RegistrationManager mRegistrationManager;
    private BootStrapManager mBootStrapManager;

    public MobileMetricsAgent(Builder mmaBuilder) {
        this.mBoostStrapperAddress = mmaBuilder.mBoostStrapperAddress;
        this.mControllerAddress = mmaBuilder.mControllerAddress;
        this.mMetricsAuthorityHeader = mmaBuilder.mMetricsAuthorityHeader;
        this.mControllerPort = mmaBuilder.mControllerPort;
        this.mAppContext = mmaBuilder.mAppContext;
    }

    public void init() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        Identity identity = new Identity(mAppContext);
        this.mRegistrationManager = new RegistrationManager(mAppContext, identity);
        this.mBootStrapManager = new BootStrapManager(mAppContext, identity);
    }

    public String getControllerAddress() {
        return mControllerAddress;
    }

    public String getBoostStrapperAddress() {
        return mBoostStrapperAddress;
    }

    public String getMetricsAuthorityHeader() {
        return mMetricsAuthorityHeader;
    }

    public int getControllerPort() {
        return mControllerPort;
    }

    public void bootstrap() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, OperatorCreationException, KeyStoreException, KeyManagementException {
        mBootStrapManager.bootstrapNow(mControllerAddress, mControllerPort);
    }


    public static class Builder {
        private String mControllerAddress;
        private String mBoostStrapperAddress;
        private String mMetricsAuthorityHeader;
        private int mControllerPort;
        private Context mAppContext;


        public Builder setControllerAddress(String address) {
            mControllerAddress = address;
            return this;
        }

        public Builder setBootStrapperAddress(String address) {
            mBoostStrapperAddress = address;
            return this;
        }

        public Builder setAuthorityHeader(String address) {
            mMetricsAuthorityHeader = address;
            return this;
        }

        public Builder setControllerPort(int port) {
            mControllerPort = port;
            return this;
        }

        public Builder setAppContext(Context context) {
            mAppContext = context;
            return this;
        }

        public MobileMetricsAgent build() {
            return new MobileMetricsAgent(this);
        }
    }
}
