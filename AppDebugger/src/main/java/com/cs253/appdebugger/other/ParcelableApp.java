package com.cs253.appdebugger.other;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cs253.appdebugger.App;

/**
 * Created by jason on 11/15/13.
 */
public class ParcelableApp implements Parcelable{
    private App app;

    public App getApp() {
        return this.app;
    }

    public ParcelableApp(App whichApp) {
        super();
        this.app = whichApp;
    }

    private ParcelableApp(Parcel in) {
        this.app = new App();
        this.app.setDescription(in.readString());
        this.app.setUid(in.readInt());
        this.app.setVersionCode(in.readInt());
        this.app.setVersionName(in.readString());
        this.app.setLabel(in.readString());
        this.app.setTitle(in.readString());
        this.app.setPackageName(in.readString());
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Parceling happens here!
     */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.app.getDescription());
        parcel.writeInt(this.app.getUid());
        parcel.writeInt(this.app.getVersionCode());
        parcel.writeString(this.app.getVersionName());
        parcel.writeString(this.app.getLabel());
        parcel.writeString(this.app.getTitle());
        parcel.writeString(this.app.getPackageName());
    }

    /**
     * Our mandatory Creator interface
     */
    public static final ParcelableApp.Creator<ParcelableApp> CREATOR =
            new ParcelableApp.Creator<ParcelableApp>() {
                public ParcelableApp createFromParcel(Parcel in) {
                    return new ParcelableApp(in);
                }

                public ParcelableApp[] newArray(int size) {
                    return new ParcelableApp[size];
                }
            };
}
