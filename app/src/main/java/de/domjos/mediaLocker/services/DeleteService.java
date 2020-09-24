/*
 * Copyright (c) 2020.
 *
 * This file is part of MediaLocker.
 *
 * MediaLocker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MediaLocker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.mediaLocker.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;

public class DeleteService extends Service {
    public static final String PASSWORD = "pwd";
    public static final String DIFF_TIME = "diff";
    private BackgroundTask backgroundTask;

    public DeleteService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        this.backgroundTask.cancel(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.backgroundTask = new BackgroundTask(intent, this.getApplicationContext());
        this.backgroundTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    private static class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private Intent intent;
        private WeakReference<Context> contextWeakReference;

        public BackgroundTask(Intent intent, Context context) {
            this.intent = intent;
            this.contextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long now = new Date().getTime();
            String pwd = "";
            long diff = 180;
            boolean notifications = true;
            if(this.intent != null) {
                Bundle bundle = this.intent.getExtras();
                if(bundle != null) {
                    if(bundle.containsKey(DeleteService.PASSWORD)) {
                        pwd = bundle.getString(DeleteService.PASSWORD);
                    }
                    if(bundle.containsKey(DeleteService.DIFF_TIME)) {
                        diff = bundle.getInt(DeleteService.DIFF_TIME, 180);
                    }
                    if(bundle.containsKey(Settings.NOTIFICATIONS)) {
                        notifications = bundle.getBoolean(Settings.NOTIFICATIONS);
                    }
                }

                try {
                    if(pwd != null && !pwd.isEmpty()) {
                        LockerDatabase lockerDatabase = Utils.getDatabase(this.contextWeakReference.get(), pwd);
                        List<Image> imagesToDelete = lockerDatabase.imageDao().getPathToDelete(now, diff);
                        if(!Utils.isViewerActivity(this.contextWeakReference.get())) {
                            int counter = 0;
                            for(Image image : imagesToDelete) {
                                File fileToDelete = new File(image.tmpPath);
                                if(fileToDelete.exists()) {
                                    if(!fileToDelete.delete()) {
                                        fileToDelete.deleteOnExit();
                                    }
                                    counter++;
                                }
                                lockerDatabase.imageDao().updateTimestamp(0, image.id);
                            }

                            if(counter != 0 && notifications) {
                                String title = this.contextWeakReference.get().getString(R.string.services_delete_title);
                                String content;
                                if(counter == 1) {
                                    content = this.contextWeakReference.get().getString(R.string.services_delete_single_content);
                                } else {
                                    content = String.format(this.contextWeakReference.get().getString(R.string.services_delete_content), counter);
                                }
                                MessageHelper.showNotification(this.contextWeakReference.get(), title, content, R.drawable.icon_notifications);
                            }
                        } else {
                            for(Image image : imagesToDelete) {
                                lockerDatabase.imageDao().updateTimestamp(new Date().getTime(), image.id);
                            }
                        }
                        lockerDatabase.close();
                    }
                } catch (Exception ignored) {}
            }
            return null;
        }
    }
}
