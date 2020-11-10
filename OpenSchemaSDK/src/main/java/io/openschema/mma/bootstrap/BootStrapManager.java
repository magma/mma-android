package io.openschema.mma.bootstrap;

import android.content.Context;

import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.grpc.ManagedChannel;
import io.openschema.mma.R;
import io.openschema.mma.bootstrapper.BootstrapperGrpc;
import io.openschema.mma.bootstrapper.Challenge;
import io.openschema.mma.certifier.Certificate;
import io.openschema.mma.helpers.ChannelHelper;
import io.openschema.mma.helpers.KeyHelper;
import io.openschema.mma.helpers.RandSByteString;
import io.openschema.mma.id.Identity;
import io.openschema.mma.identity.AccessGatewayID;

/**
 * In charge of BootStrapping flow
 *
 */
public class BootStrapManager {

    private static final String KEY_STORE = "AndroidKeyStore";
    private static final String HW_KEY_ALIAS = "";
    private static final String GW_KEY_ALIAS = "gw_key";
    private static final String CERT_TYPE = "X.509";

    private Identity mIdentity;
    private TrustManagerFactory mTrustManagerFactory;
    private CertificateFactory mCertificateFactory;
    private KeyStore mKeyStore;
    private SSLContext mSSLContext;

    private boolean mBootStrapSuccess;

    public BootStrapManager(Context context, Identity identity) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchProviderException {
        // create new identity or load an exiting one
        // TODO: if this is a new identity it should be registered first
        mIdentity = identity;
        mKeyStore = KeyStore.getInstance(KEY_STORE);
        mKeyStore.load(null, null);
        initializeTrustManagerFactory(context);
    }


    private void initializeTrustManagerFactory(Context context)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE);
        InputStream in = context.getResources().openRawResource(R.raw.rootca);
        java.security.cert.Certificate rootcert = cf.generateCertificate(in);
        mKeyStore.setCertificateEntry("rootca", rootcert);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        mTrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
        mTrustManagerFactory.init(mKeyStore);
    }

    private void initSSLContext(KeyManager[] km, TrustManager[] tm) throws KeyManagementException, NoSuchAlgorithmException {
        mSSLContext = SSLContext.getInstance("TLS");
        mSSLContext.init(km, tm, new java.security.SecureRandom());
    }

    private void storeSignedCertificate(Certificate certificate) throws CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE);
        final java.security.cert.Certificate cert = cf.generateCertificate(certificate.getCertDer().newInput());
        KeyFactory kf =  KeyFactory.getInstance("RSA");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        String password = "";
        java.security.cert.Certificate[] certChain = new java.security.cert.Certificate[1];
        certChain[0] = cert;
        PrivateKey privateKey = (PrivateKey) mKeyStore.getKey(GW_KEY_ALIAS, null);
        mKeyStore.setKeyEntry(GW_KEY_ALIAS, privateKey, null, certChain );
    }

    public void bootstrapNow(String controllerAddress, int controllerPort)
            throws NoSuchAlgorithmException, KeyManagementException, IOException, OperatorCreationException, UnrecoverableKeyException, CertificateException, SignatureException, KeyStoreException, InvalidKeyException {

        //final SSLContext sslContext = SSLContext.getInstance("TLS");
        //sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

        initSSLContext(null, mTrustManagerFactory.getTrustManagers());

        ManagedChannel bootStrapChannel = ChannelHelper.getSecureManagedChannel(
                controllerAddress,
                controllerPort,
                mSSLContext.getSocketFactory());

        BootstrapperGrpc.BootstrapperBlockingStub stub = BootstrapperGrpc.newBlockingStub(bootStrapChannel);

        AccessGatewayID hw_id = AccessGatewayID.newBuilder()
                .setId(mIdentity.getUUID())
                .build();

        // 1) get challenge
        Challenge challenge = stub.getChallenge(hw_id);

        RandSByteString rands = KeyHelper.getRandS(challenge);

        CertSignRequest csr = new CertSignRequest(KeyHelper.generateRSAKeyPairForAlias(GW_KEY_ALIAS), mIdentity.getUUID());

        ChallengeResponse response = new ChallengeResponse(
                mIdentity.getUUID(),
                challenge,
                0,
                10000,
                csr.getCSRByteString(),
                rands.getR(),
                rands.getS());

        // 2) send CSR to sign
        Certificate certificate = stub.requestSign(response.getResponse());


        // 3) Add cert to keystore for mutual TLS and use for calling Collect() and Push()


        storeSignedCertificate(certificate);

        mBootStrapSuccess = true;

//        kmf.init(keyStore, password.toCharArray());
//        SSLContext context = SSLContext.getInstance("TLS");
//        context.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

//        bootStrapChannel = OkHttpChannelBuilder.forAddress(CONTROLLER_ADDRESS, 443)
//                .useTransportSecurity()
//                .sslSocketFactory(context.getSocketFactory())
//                .overrideAuthority(METRICS_AUTHORITY_HEADER)
//                .build();
//
//        MetricsControllerGrpc.MetricsControllerBlockingStub stub2 = MetricsControllerGrpc.newBlockingStub(bootStrapChannel);
    }

    public boolean isBootStrapSuccess() {
        return mBootStrapSuccess;
    }
}
