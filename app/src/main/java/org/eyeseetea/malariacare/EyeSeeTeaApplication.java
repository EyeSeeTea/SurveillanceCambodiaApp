/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.utils.Permissions;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

/**
 * Created by nacho on 04/08/15.
 */
public class EyeSeeTeaApplication extends Dhis2Application {

    public static Permissions permissions;
    private static final String TAG = ".EyeSeeTeaApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }

    @Override
    public Class<? extends Activity> getMainActivity() {
        return DashboardActivity.class;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
