package com.cs253.appdebugger;

import com.cs253.appdebugger.other.ParcelableApp;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * A custom list adapter which holds a reference to all installed apps and displays their respective title
 * text in each row of a vertical list.
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
public class AppListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private List<App> mApps;
    /** a map which maps the package name of an app to its icon drawable */
    private Map<String, Drawable> mIcons;
    private Drawable mStdImg;
    private Context context;
    private App app;

    /**
     * Constructor.
     *
     * @param context the application context which is needed for the layout inflater
     */
    public AppListAdapter(Context context) {
        // cache the LayoutInflater to avoid asking for a new one each time
        mInflater = LayoutInflater.from(context);

        // set the default icon until the actual icon is loaded for an app
        mStdImg = context.getResources().getDrawable(R.drawable.icon);
        this.context = context;
    }

    public void startBenchmarkService(int i) {
        ParcelableApp pa = new ParcelableApp(this.mApps.get(i));
        // Initialize an intent to our service that will monitor for stats gathering
        Intent intent = new Intent(this.context.getApplicationContext(), AppBenchmarkService.class);
        // Put our app name in our intent so we have access to it in our service
        intent.putExtra("app", pa);
        // start our service
        this.context.startService(intent);
    }

    public void startResultsActivity(int i) {
        // Create a parcelable app
        ParcelableApp pa = new ParcelableApp(this.mApps.get(i));
        // Create a new intent for our new activity
        Intent intent = new Intent(this.context.getApplicationContext(), AppDetails.class);
        // pass the parcelable to our intent
        intent.putExtra("app", pa);
        // This is needed to start our activity from outside an activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // start the activity
        this.context.startActivity(intent);
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AppViewHolder holder;
        this.app = mApps.get(position);

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.row, null);

            // creates a ViewHolder and stores a reference to the children view we want to bind data to
            holder = new AppViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.apptitle);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.appicon);

            // Set our benchmarker and add an onClickListener to this view
            holder.mBenchmark = (TextView) convertView.findViewById(R.id.appbenchmark);

            // Set our results and add an onClickListener to this view
            holder.mResults = (TextView) convertView.findViewById(R.id.appresults);
            convertView.setTag(holder);
        } else {
            // reuse/overwrite the view passed assuming(!) that it is castable!
            holder = (AppViewHolder) convertView.getTag();
        }

        holder.mBenchmark.setOnClickListener(new AppOnClickListener(position) {
            @Override
            public void onClick(View v) {
                startBenchmarkService(this.position);
            }
        });

        holder.mResults.setOnClickListener(new AppOnClickListener(position) {
            @Override
            public void onClick(View view) {
                startResultsActivity(this.position);
            }
        });

        holder.setTitle(this.app.getTitle());
        holder.setBenchmark("Benchmark " + this.app.getTitle());
        holder.setResults("Results for " + this.app.getTitle());
        if (mIcons == null || mIcons.get(this.app.getPackageName()) == null) {
            holder.setIcon(mStdImg);
        } else {
            holder.setIcon(mIcons.get(this.app.getPackageName()));
        }

        return convertView;
    }

    /**
     * Sets the list of apps to be displayed.
     *
     * @param list the list of apps to be displayed
     */
    public void setListItems(List<App> list) {
        mApps = list;
    }

    /**
     * Sets the map containing the icons for each displayed app.
     *
     * @param icons the map which maps the app's package name to its icon
     */
    public void setIcons(Map<String, Drawable> icons) {
        this.mIcons = icons;
    }

    /**
     * Returns the map containing the icons for each displayed app.
     *
     * @return a map which contains a mapping of package names to icon drawable for all displayed apps
     */
    public Map<String, Drawable> getIcons() {
        return mIcons;
    }

    /**
     * A view holder which is used to re/use views inside a list.
     */
    public class AppViewHolder {

        private TextView mTitle;
        private ImageView mIcon;
        private TextView mBenchmark;
        private TextView mResults;
        private CheckBox mCheckbox;

        /**
         * Sets the text to be shown as the app's title
         *
         * @param title the text to be shown inside the list row
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }

        /**
         * Sets the icon to be shown next to the app's title
         *
         * @param img the icon drawable to be displayed
         */
        public void setIcon(Drawable img) {
            if (img != null) {
                mIcon.setImageDrawable(img);
            }
        }

        /**
         * Sets the benchmark text that links an app to starting
         * a service to benchmark it
         * @param s the string to be displayed
         */
        public void setBenchmark(String s) {
            mBenchmark.setText(s);
        }

        /**
         * Sets the results text that links an app to starting
         * the activity that shows results
         * @param s the string to be displayed
         */
        public void setResults(String s) {
            mResults.setText(s);
        }

        /**
         * A full checkbox object that allows us to monitor an app
         * @param c
         */
        public void setCheckbox(CheckBox c) {
            mCheckbox = c;
        }
    }

    /**
     * Our custom onClickListener so we can send different
     *  parameters to our methods
     */
    public class AppOnClickListener implements OnClickListener
    {
        int position;
        public AppOnClickListener(int i) {
            this.position = i;
        }

        @Override
        public void onClick(View v)
        {
            Log.d("AppDebugger", "We clicked");
        }

    };
}
