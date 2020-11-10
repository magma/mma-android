package io.openschema.mma.helpers;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.google.protobuf.ByteString;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.util.ASN1Dump;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import io.openschema.mma.R;
import io.openschema.mma.bootstrapper.Challenge;

public class KeyHelper {

    private static final String KEY_STORE = "AndroidKeyStore";
    private static final String HW_KEY_ALIAS = "hw_key_alias";

    public static KeyPair generateRSAKeyPairForAlias(String alias){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE);
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            alias,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_NONE,
                                    KeyProperties.DIGEST_SHA512,
                                    KeyProperties.DIGEST_SHA256)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            //https://github.com/google/conscrypt/issues/718
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
            KeyPair kp = keyPairGenerator.generateKeyPair();
            return kp;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String getPubKeyString(PublicKey key){
        byte[] publicKeyBytes = Base64.encode(key.getEncoded(),0);
        return new String(publicKeyBytes);
    }

    public static String getPrivKeyString(PrivateKey key){
        byte[] privateKeyBytes = Base64.encode(key.getEncoded(),0);
        return new String(privateKeyBytes);
    }


    public static RandSByteString getRandS(Challenge challenge)
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException, InvalidKeyException, SignatureException {
        RandSByteString rands = new RandSByteString();
        KeyStore ks = KeyStore.getInstance(KEY_STORE);
        ks.load(null);
        PrivateKey testPriKey = (PrivateKey) ks.getKey(HW_KEY_ALIAS, null);
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(testPriKey);
        s.update(challenge.getChallenge().toByteArray());
        byte[] signature = s.sign();

        ASN1InputStream input = new ASN1InputStream(signature);

        ASN1Primitive p;

        byte[] r_byte = new byte[0];
        byte[] s_byte = new byte[0];

        while ((p = input.readObject()) != null) {
            ASN1Sequence asn1 = ASN1Sequence.getInstance(p);
            ASN1Integer asnInt0 = ASN1Integer.getInstance(asn1.getObjectAt(0));
            ASN1Integer asnInt1 = ASN1Integer.getInstance(asn1.getObjectAt(1));
            r_byte = asnInt0.getValue().toByteArray();
            s_byte = asnInt1.getValue().toByteArray();


            if (r_byte.length > s_byte.length) {
                byte[] temp = new byte[s_byte.length];
                System.arraycopy(r_byte, 1, temp, 0, s_byte.length);
                r_byte = new byte[0];
                r_byte = temp;
            } else if (s_byte.length > r_byte.length) {
                byte[] temp = new byte[r_byte.length];
                System.arraycopy(s_byte, 1, temp, 0, r_byte.length);
                s_byte = new byte[0];
                s_byte = temp;
            } else if (s_byte.length == 49) {
                int len = s_byte.length - 1;
                byte[] temp = new byte[len];
                System.arraycopy(s_byte, 1, temp, 0, len);
                s_byte = new byte[0];
                s_byte = temp;
                System.arraycopy(r_byte, 1, temp, 0, len);
                r_byte = new byte[0];
                r_byte = temp;
            }
        }

        rands.setR(ByteString.copyFrom(r_byte));
        rands.setS(ByteString.copyFrom(s_byte));
        return rands;
    }

}
