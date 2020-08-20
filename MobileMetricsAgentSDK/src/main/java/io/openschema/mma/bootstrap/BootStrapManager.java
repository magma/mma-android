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
import java.security.SecureRandom;
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
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.openschema.mma.R;
import io.openschema.mma.bootstrapper.BootstrapperGrpc;
import io.openschema.mma.bootstrapper.Challenge;
import io.openschema.mma.certifier.Certificate;
import io.openschema.mma.helpers.ChannelHelper;
import io.openschema.mma.helpers.KeyHelper;
import io.openschema.mma.helpers.RandSByteString;
import io.openschema.mma.id.Identity;
import io.openschema.mma.identity.AccessGatewayID;
import io.openschema.mma.metricsd.MetricsControllerGrpc;

/**
 * In charge of BootStrapping flow
 *
 */
public class BootStrapManager {


    private final String KEY_STORE = "AndroidKeyStore";
    private final String HW_KEY_ALIAS = "";
    private final String GW_KEY_ALIAS = "gw_key";

    private Context mContext;

    private final String CERT_TYPE = "X.509";

    // Cloud Endpoint
    private final String CONTROLLER_ADDRESS = "controller.openschema.magma.etagecom.io";
    private final int CONTROLLER_PORT = 443;
    private final String BOOTSTRAPPER_CONTROLLER_ADDRESS = "bootstrapper-" + CONTROLLER_ADDRESS;
    private final String METRICS_AUTHORITY_HEADER = "metricsd-" + CONTROLLER_ADDRESS;

    private static Identity identity;
    private TrustManagerFactory trustManagerFactory;
    private CertificateFactory certificateFactory;
    private KeyStore keyStore;
    private SSLContext sslContext;



    private boolean bootStrapSuccess;



    public BootStrapManager(Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, NoSuchProviderException {
        mContext = context;
        // create new identity or load an exiting one
        // TODO: if this is a new identity it should be registered first
        identity = new Identity(context);
        keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null, null);
        initializeTrustManagerFactory();
    }


    private void initializeTrustManagerFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE);
        InputStream in = mContext.getResources().openRawResource(R.raw.rootca);
        java.security.cert.Certificate rootcert = cf.generateCertificate(in);
        keyStore.setCertificateEntry("rootca", rootcert);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
        trustManagerFactory.init(keyStore);
    }

    private void initSSLContext(KeyManager[] km, TrustManager[] tm) throws KeyManagementException, NoSuchAlgorithmException {
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(km, tm, new java.security.SecureRandom());
    }

    private void storeSignedCertificate(Certificate certificate) throws CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance(CERT_TYPE);
        final java.security.cert.Certificate cert = cf.generateCertificate(certificate.getCertDer().newInput());
        KeyFactory kf =  KeyFactory.getInstance("RSA");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        String password = "";
        java.security.cert.Certificate[] certChain = new java.security.cert.Certificate[1];
        certChain[0] = cert;
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(GW_KEY_ALIAS, null);
        keyStore.setKeyEntry(GW_KEY_ALIAS, privateKey, null, certChain );
    }

    public void BootstrapNow()
            throws NoSuchAlgorithmException, KeyManagementException, IOException, OperatorCreationException, UnrecoverableKeyException, CertificateException, SignatureException, KeyStoreException, InvalidKeyException {

        //final SSLContext sslContext = SSLContext.getInstance("TLS");
        //sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

        initSSLContext(null, trustManagerFactory.getTrustManagers());


        ManagedChannel bootStrapChannel = ChannelHelper.getSecureManagedChannel(
                BOOTSTRAPPER_CONTROLLER_ADDRESS,
                CONTROLLER_PORT,
                sslContext.getSocketFactory());

        BootstrapperGrpc.BootstrapperBlockingStub stub = BootstrapperGrpc.newBlockingStub(bootStrapChannel);

        AccessGatewayID hw_id = AccessGatewayID.newBuilder()
                .setId(identity.getUUID())
                .build();

        // 1) get challenge
        Challenge challenge = stub.getChallenge(hw_id);

        RandSByteString rands = KeyHelper.getRandS(challenge);

        CertSignRequest csr = new CertSignRequest(KeyHelper.generateRSAKeyPairForAlias(GW_KEY_ALIAS), identity.getUUID());

        ChallengeResponse response = new ChallengeResponse(
                identity.getUUID(),
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

        bootStrapSuccess = true;

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
        return bootStrapSuccess;
    }
}
