package com.cs253.appdebugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.widget.Toast;

import com.cs253.appdebugger.other.ParcelableApp;

/**
 * This demo program displays all currently installed apps of the device in a list. An app can be started
 * upon clicking on its row.
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
//extend ActionBarActivity to get the actionbar!
public class ListApps extends Activity implements OnItemClickListener {

    /* whether or not to include system apps */
    private static final boolean INCLUDE_SYSTEM_APPS = false;

    private ListView mAppsList;
    private AppListAdapter mAdapter;
    private List<App> mApps;
    private Context context;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate our list apps view
        setContentView(R.layout.activity_listapps);
        // It's always handy to keep our app's context around for use elsewhere
        this.context = getApplicationContext();

        // Inflate our listview view
        mAppsList = (ListView) findViewById(R.id.appslist);
        // add a listener to this view
        //mAppsList.setOnItemClickListener(this);

        // Get the apps that are on the system
        mApps = loadInstalledApps(INCLUDE_SYSTEM_APPS);

        // Add a listadapter to our view
        mAdapter = new AppListAdapter(getApplicationContext());
        // put the apps onto our listview
        mAdapter.setListItems(mApps);
        // add the listener to our listview
        mAppsList.setAdapter(mAdapter);

        // Load our app's icons in the background so we don't leave our main thread
        // hangin if a problem arises
        new LoadIconsTask().execute(mApps.toArray(new App[]{}));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final App app = (App) parent.getItemAtPosition(position);

        Intent intent = new Intent(view.getContext(), AppDetails.class);

        intent.putExtra("app", new ParcelableApp(app));
        startActivity(intent);
    }

    /**
     * Uses the package manager to query for all currently installed apps which are put into beans and returned
     * in form of a list.
     *
     * @param includeSysApps whether or not to include system applications
     * @return a list containing an {@code App} bean for each installed application
     */
    private List<App> loadInstalledApps(boolean includeSysApps) {
        List<App> apps = new ArrayList<App>();

        // the package manager contains the information about all installed apps
        PackageManager packageManager = getPackageManager();

        List<PackageInfo> packs = packageManager.getInstalledPackages(0); //PackageManager.GET_META_DATA

        for(int i=0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo a = p.applicationInfo;
            // skip system apps if they shall not be included
            if ((!includeSysApps) && ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                continue;
            }
            App app = new App();
            app.setTitle(p.applicationInfo.loadLabel(packageManager).toString());
            app.setPackageName(p.packageName);
            app.setVersionName(p.versionName);
            app.setVersionCode(p.versionCode);
            CharSequence description = p.applicationInfo.loadDescription(packageManager);
            app.setDescription(description != null ? description.toString() : "");
            app.setUid(a.uid);
            apps.add(app);
        }
        // Sort our apps
        Collections.sort(apps, new Comparator<App>() {
            public int compare(App a, App b) {
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });

        return apps;
    }

    /**
     * An asynchronous task to load the icons of the installed applications.
     */
    private class LoadIconsTask extends AsyncTask<App, Void, Void> {
        @Override
        protected Void doInBackground(App... apps) {

            Map<String, Drawable> icons = new HashMap<String, Drawable>();
            PackageManager manager = getApplicationContext().getPackageManager();

            for (App app : apps) {
                String pkgName = app.getPackageName();
                Drawable ico = null;
                try {
                    Intent i = manager.getLaunchIntentForPackage(pkgName);
                    if (i != null) {
                        ico = manager.getActivityIcon(i);
                    }
                } catch (NameNotFoundException e) {
                    Log.e("ERROR", "Unable to find icon for package '" + pkgName + "': " + e.getMessage());
                }
                icons.put(app.getPackageName(), ico);
            }
            mAdapter.setIcons(icons);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
        }
    }
}