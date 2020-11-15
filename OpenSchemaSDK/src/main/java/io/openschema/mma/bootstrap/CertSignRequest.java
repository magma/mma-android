package io.openschema.mma.bootstrap;

import android.util.Log;

import com.google.protobuf.ByteString;

import org.spongycastle.openssl.PEMWriter;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;

import io.openschema.mma.helpers.CsrHelper;

public class CertSignRequest {

    private PKCS10CertificationRequest mCsr;

    public CertSignRequest(KeyPair kp, String uuid)
            throws IOException, OperatorCreationException {
        mCsr = CsrHelper.generateCSR(kp, uuid);
    }

    public PKCS10CertificationRequest getCSR(){
        return mCsr;
    }

    public ByteString getCSRByteString() throws IOException {
        Log.d("TestCSR", mCsr.getEncoded().toString());
        return ByteString.copyFrom(mCsr.getEncoded());
    }
}
