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

package de.domjos.mediaLocker.helper;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.dialogs.ShareDialog;
import de.domjos.mediaLocker.tasks.images.SharedImagesTask;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_SEND_MULTIPLE;

public abstract class IntentHelper {
    public static final int IMAGE_REQUEST = 6782;
    public static final int IMAGE_SINGLE_REQUEST = 6781;
    public static final int VIDEO_REQUEST = 6783;
    public static final int VIDEO_SINGLE_REQUEST = 6784;

    public static void startSingleImagePicker(Activity activity) {
        MediaHelper.callBroadCast(activity);

        MediaOptions.Builder mediaOptions = new MediaOptions.Builder();
        mediaOptions.selectPhoto();
        mediaOptions.canSelectMultiPhoto(false);

        MediaPickerActivity.Companion.open(activity, IMAGE_SINGLE_REQUEST, mediaOptions.build());
    }

    public static void startImagePicker(Activity activity) {
        MediaHelper.callBroadCast(activity);

        MediaOptions.Builder mediaOptions = new MediaOptions.Builder();
        mediaOptions.selectPhoto();
        mediaOptions.canSelectMultiPhoto(true);

        MediaPickerActivity.Companion.open(activity, IMAGE_REQUEST, mediaOptions.build());
    }

    public static void startSingleVideoPicker(Activity activity) {
        MediaHelper.callBroadCast(activity);

        MediaOptions.Builder mediaOptions = new MediaOptions.Builder();
        mediaOptions.selectVideo();
        mediaOptions.canSelectMultiVideo(false);

        MediaPickerActivity.Companion.open(activity, VIDEO_SINGLE_REQUEST, mediaOptions.build());
    }

    public static void startVideoPicker(Activity activity) {
        MediaHelper.callBroadCast(activity);

        MediaOptions.Builder mediaOptions = new MediaOptions.Builder();
        mediaOptions.selectVideo();
        mediaOptions.canSelectMultiVideo(true);

        MediaPickerActivity.Companion.open(activity, VIDEO_REQUEST, mediaOptions.build());
    }

    public static boolean getMultipleImagesFromOtherApp(Activity activity) {
        boolean notifications = Settings.getUserSetting(activity, Settings.NOTIFICATIONS, true);
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(action != null && (action.equals(ACTION_SEND) || action.equals(ACTION_SEND_MULTIPLE)) && type != null) {
            ShareDialog shareDialog = ShareDialog.newInstance();
            shareDialog.setDismissListener((id, password, newFolder) -> {
                SharedImagesTask sharedImagesTask = new SharedImagesTask(activity, notifications, intent, id, password, newFolder);
                sharedImagesTask.after(o -> {
                    MediaHelper.callBroadCast(activity);
                    activity.finish();
                    activity.startActivity(new Intent(activity, MainActivity.class));
                });
                sharedImagesTask.execute();
            });
            shareDialog.show(((AppCompatActivity) activity).getSupportFragmentManager(), "share_dialog");
            return true;
        }
        return false;
    }
}
