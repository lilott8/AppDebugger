package com.cs253.appdebugger;


import android.util.Log;
import android.widget.Toast;
import android.content.Context;

/**
 * This class represents a simple bean that will hold the values for an installed app such as name, version
 * and package name (which is needed to start the application).
 *
 * Copyright 2k11 Impressive Artworx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Manuel Schwarz (m.schwarz[at]impressive-artworx.de)
 */
public class App {

    private String title;
    private String packageName;
    private String versionName;
    private int versionCode;
    private String description;
    private String label;
    private int uid;

    // ordinary getters and setters

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) { this.label = label;}

    public String getLabel() { return this.label;}

    public void setUid(int uid) {this.uid = uid;}

    public int getUid() {return this.uid;}

}

