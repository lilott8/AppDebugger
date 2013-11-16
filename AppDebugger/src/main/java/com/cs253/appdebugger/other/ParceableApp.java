package com.cs253.appdebugger.other;

import android.os.Parcel;
import android.os.Parcelable;

import com.cs253.appdebugger.App;

/**
 * Created by jason on 11/15/13.
 */
public class ParceableApp implements Parcelable{
    private App app;

    public App getApp() {
        return this.app;
    }

    public ParceableApp(App app) {
        super();
        this.app = app;
    }

    private ParceableApp(Parcel in) {
        this.app = new App();
        this.app.setDescription(in.readString());
        this.app.setUid(in.readInt());
        this.app.setVersionCode(in.readInt());
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
        parcel.writeLong(this.app.getUid());
        parcel.writeInt(this.app.getVersionCode());
        parcel.writeString(this.app.getVersionName());
        parcel.writeString(this.app.getLabel());
        parcel.writeString(this.app.getTitle());
        parcel.writeString(this.app.getPackageName());
    }

    /**
     * Our mandatory Creator interface
     */
    public static final ParceableApp.Creator<ParceableApp> CREATOR =
            new ParceableApp.Creator<ParceableApp>() {
                public ParceableApp createFromParcel(Parcel in) {
                    return new ParceableApp(in);
                }

                public ParceableApp[] newArray(int size) {
                    return new ParceableApp[size];
                }
            };
}
