package io.openschema.mma.metrics;

import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

public class LteMetrics {

    ID id;
    Sig sig;
    Bundle bundle;
    private static final int UNAVAILABLE =  2147483647;

    public LteMetrics(CellInfoLte cellInfoLte) {
        this.bundle = new Bundle();
        this.id = new ID(cellInfoLte.getCellIdentity());
        this.sig = new Sig(cellInfoLte.getCellSignalStrength());
    }

    public String getValueInt(int value) {
        if(value == UNAVAILABLE) return "UNAVAILABLE";
        else return value + "";
    }

    public String getValueString(String value) {
        if(value == null) return "UNAVAILABLE";
        else return value;
    }

    private class ID {
        private int ci;
        private int earfcn = UNAVAILABLE;
        private int mcc = UNAVAILABLE;
        private String s_mcc = null;
        private int mnc = UNAVAILABLE;
        private String s_mnc = null;
        private int pci;
        private int tac;
        public ID(CellIdentityLte cellIdentityLte) {
            this.ci = cellIdentityLte.getCi();
            if(Build.VERSION.SDK_INT >= 24)
                this.earfcn = cellIdentityLte.getEarfcn();
            this.mcc = cellIdentityLte.getMcc();
            this.mnc = cellIdentityLte.getMnc();
            this.pci = cellIdentityLte.getPci();
            this.tac = cellIdentityLte.getTac();

            bundle.putString("Cell Identity: ", getValueInt(this.ci));
            bundle.putString("Absolute RF channel Number: ", getValueInt(this.earfcn));
            if(Build.VERSION.SDK_INT >= 28) {
                this.s_mcc = cellIdentityLte.getMccString();
                this.s_mnc = cellIdentityLte.getMncString();
                bundle.putString("Mobile Country Code: ", getValueString(this.s_mcc));
                bundle.putString("Mobile Network Code: ", getValueString(this.s_mnc));
            }
            else {
                bundle.putString("Mobile Country Code: ", getValueInt(this.mcc));
                bundle.putString("Mobile Network Code: ", getValueInt(this.mnc));
            }
            bundle.putString("Physical Cell ID: ", getValueInt(this.pci));
            bundle.putString("Tracking Area Code: ", getValueInt(this.tac));
        }
    }

    private class Sig {
        private int asuLevel;
        private int cqi = UNAVAILABLE;
        private int dbm;
        private int level;
        private int rsrp = UNAVAILABLE;
        private int rsrq = UNAVAILABLE;
        private int rssnr = UNAVAILABLE;
        private int timingAdvance;

        //TODO: Below variables are not assigned. Find a way to assign these
        private int no;
        private int ro;
        private int rtc;
        public Sig(CellSignalStrengthLte cellSignalStrengthLte) {
            this.asuLevel = cellSignalStrengthLte.getAsuLevel();
            if(Build.VERSION.SDK_INT >= 26){
                this.cqi = cellSignalStrengthLte.getCqi();
                this.rsrp = cellSignalStrengthLte.getRsrp();
                this.rsrq = cellSignalStrengthLte.getRsrq();
                this.rssnr = cellSignalStrengthLte.getRssnr();
            }
            this.dbm = cellSignalStrengthLte.getDbm();
            this.level = cellSignalStrengthLte.getLevel();
            this.timingAdvance = cellSignalStrengthLte.getTimingAdvance();

            bundle.putString("RSRP in ASU: ", getValueInt(this.asuLevel));
            bundle.putString("Channel Quality Indicator: ", getValueInt(this.cqi));
            bundle.putString("Signal Strength in dBm: ", getValueInt(this.dbm));
            bundle.putString("Overall Signal Quality: ", getValueInt(this.level));
            bundle.putString("Reference signal received power in dBm: ", getValueInt(this.rsrp));
            bundle.putString("Reference signal received Quality: ", getValueInt(this.rsrq));
            bundle.putString("Reference signal signal-to-noise ratio: ", getValueInt(this.rssnr));
            bundle.putString("Timing Advance: ", getValueInt(this.timingAdvance));
        }
    }
    public Bundle getBundle() { return this.bundle; }
}
