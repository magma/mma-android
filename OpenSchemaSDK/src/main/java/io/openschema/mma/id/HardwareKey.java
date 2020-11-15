package io.openschema.mma.id;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import io.openschema.mma.helpers.KeyHelper;

/**
 * Generate and Store HW Key
 */

public class HardwareKey {

    private static final String HW_KEY_ALIAS = "hw_key_alias";
    private static final String KEY_STORE = "AndroidKeyStore";
    private static final int KEY_SIZE = 256;

    private String mHwPublicKey;

    public HardwareKey()
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        generateECKeyPairForAlias(HW_KEY_ALIAS, KEY_SIZE);
    }

    private String loadKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KEY_STORE);
        ks.load(null);
        if (ks.getCertificate(alias) != null) {
            return KeyHelper.getPubKeyString(ks.getCertificate(alias).getPublicKey());
        }

        return null;
    }

    private void generateECKeyPairForAlias(String alias, int size)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, InvalidAlgorithmParameterException, NoSuchProviderException {

        //Load key from saved keystore
        mHwPublicKey = loadKey(alias);

        if (mHwPublicKey == null) {
            //Generate new key if none was found
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, KEY_STORE);

            kpg.initialize(new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    //TODO: make the ec spec parametric
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_NONE,
                            KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
                    .setKeySize(size)
                    .build());
            KeyPair kp = kpg.generateKeyPair();
            mHwPublicKey = KeyHelper.getPubKeyString(kp.getPublic());
        }
    }


    public String getHwPublicKey() {
        return mHwPublicKey;
    }
}
