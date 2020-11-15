package io.openschema.mma.metrics;

import android.annotation.SuppressLint;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import io.openschema.mma.id.Identity;
import io.openschema.mma.metricsd.MetricsContainer;
import io.openschema.mma.metricsd.PushedMetric;
import io.openschema.mma.metricsd.PushedMetricsContainer;

public class WifiMetrics {

    public WifiInfo wifiInfo;
    private Bundle varBundle;

    private String bid;
    private int ch;
    private int fcy;
    private boolean hdn;
    private int rss;
    private boolean sec;
    private String ssid;
    private int spd;
    private int freq;
    private String enc;
    private static MetricsContainer mContainer;
    private static PushedMetricsContainer mPush;

    public WifiMetrics(WifiManager wifiManager){
        this.wifiInfo = wifiManager.getConnectionInfo();
        this.bid = this.wifiInfo.getBSSID();
        this.fcy = this.wifiInfo.getFrequency();
        this.hdn = this.wifiInfo.getHiddenSSID();
        this.rss = this.wifiInfo.getRssi();
        this.ssid = this.wifiInfo.getSSID();
        this.spd = this.wifiInfo.getLinkSpeed();
        this.enc = this.getEncryptionType(wifiManager);
        this.sec = !this.enc.equals("WIFI_SECURITY_NONE");
        this.freq = this.wifiInfo.getFrequency();
        this.ch = getChannel(this.freq);
        buildWifiMetricsContainer();
    }

    private void buildWifiMetricsContainer(){
        mContainer = MetricsContainer.newBuilder()
                .setGatewayId(Identity.getUUID())
                .addFamily(MetricFamily.newBuilder()
                        //TODO: make these name parametric
                        .setName("ue_wifi_usage_dl")
                        .addMetric(Metric.newBuilder()
                                .addLabel(LabelPair.newBuilder()
                                        .setName("ssid")
                                        .setValue(this.ssid)
                                        .build())
                                .addLabel(LabelPair.newBuilder()
                                        .setName("timestamp")
                                        .setValue(Long.toString(System.currentTimeMillis()))
                                        .build())
                                .setGauge(Gauge.newBuilder()
                                        .setValue(Math.random() * 1000000)
                                        .build())
                                .build())
                        .setType(MetricType.GAUGE)
//                        .addMetric(Metric.newBuilder()
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("ssid")
//                                        .setValue(this.ssid)
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("hssid")
//                                        .setValue(Boolean.toString(this.hdn))
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("bssid")
//                                        .setValue(this.bid)
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("freq")
//                                        .setValue(Integer.toString(this.freq))
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("ch")
//                                        .setValue(Integer.toString(this.ch))
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("enc")
//                                        .setValue(this.enc)
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("sec")
//                                        .setValue(Boolean.toString(this.sec))
//                                        .build())
//                                .addLabel(LabelPair.newBuilder()
//                                        .setName("linkspeed")
//                                        .setValue(Integer.toString(this.spd))
//                                        .build())
//                                .setUntyped(Untyped.newBuilder()
//                                        .setValue(this.rss)
//                                        .build())
//                                .setTimestampMs(System.currentTimeMillis())
//                                .build())
//                        .setType(MetricType.UNTYPED)
//                        .build())
                )

                .build();

        mPush = PushedMetricsContainer.newBuilder()
                .setNetworkId("os1")
                .addMetrics(PushedMetric.newBuilder()
                        .setMetricName("ue_wifi_usage")
                        .setValue(Math.random() * 10000000)
                        .setTimestampMS(System.currentTimeMillis())
                        .build())
                .build();

    }

    public static void collectWifiMetrics(){
        MetricsManager.testCollection(mContainer);

    }

    public static void pushtWifiMetrics(){
        MetricsManager.testPush(mPush);

    }


    private int getChannel(int freq) {
        if (freq == 2484)
            return 14;
        if (freq < 2484)
            return (freq - 2407) / 5;
        return freq/5 - 1000;
    }

    @SuppressLint("MissingPermission")
    private String getEncryptionType(WifiManager wifiManager) {
        if (this.wifiInfo != null) {
            WifiConfiguration activeConfig = null;
            if (wifiManager.getConfiguredNetworks() != null) {
                for (WifiConfiguration conf : wifiManager.getConfiguredNetworks()) {
                    Log.d("Shoelace", "WifiConfiguration: " + this.ssid + " " + this.bid);
                    // On Pixel XL Android 9, conf.status is not set to CURRENT, hence need to
                    // match the SSID instead. BSSID is null.
                    if (conf.status == WifiConfiguration.Status.CURRENT || conf.SSID.equals(wifiInfo.getSSID())) {
                        activeConfig = conf;
                        break;
                    }
                }
            }
            if (activeConfig != null) {
                if (activeConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                    if (activeConfig.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                        return "WIFI_SECURITY_WPA2";
                    } else {
                        return "WIFI_SECURITY_WPA";
                    }
                } else if (activeConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                        || activeConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                    return "WIFI_SECURITY_EAP";

                } else if (activeConfig.wepKeys[0] != null) {
                    return "WIFI_SECURITY_WEP";
                } else {
                    return "WIFI_SECURITY_NONE";
                }
            }
        }
        return "Unknown";
    }



    public Bundle getBundle() {
        return this.varBundle;
    }

    public String checkAvailability(int value){
        if(value < 0)
            return "UNAVAILABLE";
        return value+"";
    }
    public String checkAvailability(String value){
        if(value == null)
            return "UNAVAILABLE";
        return value+"";
    }


}
