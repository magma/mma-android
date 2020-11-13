package io.openschema.mma.networking;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static String TAG = "RetrofitService";

    //Singleton
    private static RetrofitService _instance = null;

    public static RetrofitService getService(Context appContext) {
        Log.d(TAG, "UI: Fetching RetrofitService");
        if (_instance == null) {
            synchronized (BackendApi.class) {
                if (_instance == null) {
                    if (appContext == null) {
                        Log.e(TAG, "UI: RetrofitService can't be instantiated with a null context");
                        return _instance;
                    }

                    if (!(appContext instanceof Application)) {
                        appContext = appContext.getApplicationContext();
                    }

                    _instance = new RetrofitService(appContext);
                }
            }
        }
        return _instance;
    }

    private BackendApi mApi = null;

    private RetrofitService(Context context) {
    }

    public BackendApi getApi() { return mApi;}

    public void initApi(Context context, String baseURL, int certificateResId) {
        OkHttpClient httpClient = certificateResId == -1 ?
                getSafeHttpClient(context) :
                getUnsafeHttpClient(context, certificateResId);

        mApi = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApi.class);
    }

    private OkHttpClient getSafeHttpClient(Context context) {
        return new OkHttpClient.Builder()
                //TODO: Need to add an authentication method
//                .addInterceptor(chain -> {
//                    Request req = chain.request().newBuilder().addHeader("Authorization", "Bearer " + mToken).build();
//                    return chain.proceed(req);
//                })
                .build();
    }

    //Unsafe httpclient that allows a self-signed certificate
    private OkHttpClient getUnsafeHttpClient(Context context, int certificateResId) {
        //Load our self-signed certificate
        SSLContext sslContext = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certificate = context.getResources().openRawResource(certificateResId);
            Certificate cert = cf.generateCertificate(certificate);
            certificate.close();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", cert);

            sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Interceptor for including JWT token in every request
        return new OkHttpClient.Builder()
                //TODO: Need to add an authentication method
//                .addInterceptor(chain -> {
//                    Request req = chain.request().newBuilder().addHeader("Authorization", "Bearer " + mToken).build();
//                    return chain.proceed(req);
//                })
                .sslSocketFactory(sslContext.getSocketFactory()) //Overriding certificate verification for self-signed certificate
                .hostnameVerifier((hostname, session) -> true) //Overriding hostname verification
                .build();
    }
}
