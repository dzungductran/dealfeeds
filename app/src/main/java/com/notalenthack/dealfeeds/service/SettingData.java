package com.notalenthack.dealfeeds.service;

import android.os.Parcel;
import android.os.Parcelable;
import com.notalenthack.dealfeeds.common.Constant;

/**
 * Setting data pass back and forth between service and application
 */
public class SettingData implements Parcelable {

    private static int PULL_DATA_ON_STARTUP = 0;
    private static int COMPARE_WITH_EBAY    = 1;
    private static int PULL_DATA_ONCE_DAILY = 2;
    private static int PULL_DATA_TWICE_DAILY = 3;

    private boolean bConditions[] = new boolean[]{
            true,     // pull data on startup
            true,     // compare with Ebay
            true,     // pull data once daily
            false};   // pull data twice daily

    // Data
    private String currentSearchTerms[] = null;

    // defaults
    private long timeOnce = 6 * Constant.MILLISECS_IN_HOUR;     // 06:00 am
    private long timeTwice = 12 * Constant.MILLISECS_IN_HOUR;   // 12:00 pm
    private int keepDays = Constant.DEFAULT_KEEP_DATA_DAYS;     // keep data for n number of days

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBooleanArray(bConditions);
        parcel.writeLong(timeOnce);
        parcel.writeLong(timeTwice);
        parcel.writeInt(keepDays);
        if (currentSearchTerms != null && currentSearchTerms.length > 0) {
            parcel.writeInt(currentSearchTerms.length);
            for (int i=0; i<currentSearchTerms.length; i++) {
                parcel.writeString(currentSearchTerms[i]);
            }
        } else {
            parcel.writeInt(0);
        }
    }

    public void readFromParcel(Parcel parcel) {
        parcel.readBooleanArray(bConditions);
        timeOnce = parcel.readLong();
        timeTwice = parcel.readLong();
        keepDays = parcel.readInt();
        int length = parcel.readInt();
        if (length > 0) {
            currentSearchTerms = new String[length];
            for (int i=0; i<length; i++) {
                currentSearchTerms[i] = parcel.readString();
            }
        } else {
            currentSearchTerms = null;
        }
    }

    public static final Parcelable.Creator<SettingData> CREATOR = new Parcelable.ClassLoaderCreator<SettingData>() {
        @Override
        public SettingData createFromParcel(Parcel parcel, ClassLoader classLoader) {
            return new SettingData(parcel);
        }

        @Override
        public SettingData createFromParcel(Parcel parcel) {
            return createFromParcel(parcel, ClassLoader.getSystemClassLoader());
        }

        @Override
        public SettingData[] newArray(int i) {
            return new SettingData[i];
        }
    };

    public SettingData() {
    }

    public SettingData(Parcel parcel) {
        readFromParcel(parcel);
    }

    public boolean isPullDataOnStartup() {
        return bConditions[PULL_DATA_ON_STARTUP];
    }

    public void setPullDataOnStartup(boolean pullDataOnStartup) {
        bConditions[PULL_DATA_ON_STARTUP] = pullDataOnStartup;
    }

    public boolean isCompareWithEbay() {
        return bConditions[COMPARE_WITH_EBAY];
    }

    public void setCompareWithEbay(boolean compareWithEbay) {
        bConditions[COMPARE_WITH_EBAY] = compareWithEbay;
    }

    public String[] getCurrentSearchTerms() {
        return currentSearchTerms;
    }

    public void setCurrentSearchTerms(String terms[]) {
        currentSearchTerms = terms;
    }

    public boolean isPullDataOnceDaily() {
        return bConditions[PULL_DATA_ONCE_DAILY];
    }

    public void setPullDataOnceDaily(boolean pullDataOnceDaily) {
        bConditions[PULL_DATA_ONCE_DAILY] = pullDataOnceDaily;
    }

    public boolean isPullDataTwiceDaily() {
        return bConditions[PULL_DATA_TWICE_DAILY];
    }

    public void setPullDataTwiceDaily(boolean pullDataTwiceDaily) {
        bConditions[PULL_DATA_TWICE_DAILY] = pullDataTwiceDaily;
    }

    public void setPullTimeOnceDaily(long time) {
        timeOnce = time;
    }

    public long getPullTimeOnceDaily() {
        return timeOnce;
    }

    public void setPullTimeTwiceDaily(long time) {
        timeTwice = time;
    }

    public long getPullTimeTwiceDaily() {
        return timeTwice;
    }

    public void setKeepDataForDays(int days) {
        keepDays = days;
    }

    public int getKeepDataForDays() {
        return keepDays;
    }
}
