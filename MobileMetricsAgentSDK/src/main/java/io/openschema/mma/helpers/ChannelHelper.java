package io.openschema.mma.helpers;

import javax.net.ssl.SSLSocketFactory;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

public class ChannelHelper {

    public static ManagedChannel getSecureManagedChannel(
            String host,
            int port,
            SSLSocketFactory factory) {
        return OkHttpChannelBuilder
                .forAddress(host, port)
                .useTransportSecurity()
                .sslSocketFactory(factory)
                .build();
    }

    public static ManagedChannel getSecureManagedChannelwithAuthorityHeader(
            String host,
            int port,
            SSLSocketFactory factory,
            String authority) {
        return OkHttpChannelBuilder
                .forAddress(host, port)
                .useTransportSecurity()
                .sslSocketFactory(factory)
                .overrideAuthority(authority)
                .build();
    }
}
