package io.openschema.mma.helpers;

import com.google.protobuf.ByteString;

public class RandSByteString {
    private ByteString r;
    private ByteString s;

    public RandSByteString() {
        this.r = null;
        this.s = null;
    }

    public void setR(ByteString r) {
        this.r = r;
    }

    public void setS(ByteString s) {
        this.s = s;
    }

    public ByteString getR(){
        return this.r;
    }
    public ByteString getS(){
        return this.s;
    }
}
