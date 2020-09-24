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

package de.domjos.mediaLocker.tasks.images;

import android.app.Activity;
import android.net.Uri;

import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.TaskStatus;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.DialogTask;
import vn.tungdx.mediapicker.MediaItem;

public final class ImportMediaTask extends DialogTask<List<MediaItem>, List<BaseDescriptionObject>> {
    private long folderId;

    public ImportMediaTask(Activity activity, boolean notifications, long folderId) {
        super(activity, R.string.task_images_import_title, R.string.task_images_import_title, notifications);
        this.folderId = folderId;
    }

    @SafeVarargs
    @Override
    protected final List<BaseDescriptionObject> background(List<MediaItem>... mediaItems) {
        LockerDatabase lockerDatabase = Utils.getDatabase(super.getContext(), MainActivity.password);
        if(mediaItems != null) {
            for(int i = 0; i<=mediaItems[0].size() - 1; i++) {
                Uri uri = mediaItems[0].get(i).getUriOrigin();
                Image image = MediaHelper.createImageFromUri(uri, null, "", "", this.folderId, super.getContext());

                if(image != null) {
                    if(lockerDatabase.imageDao().count(image.name, image.id) == 0) {
                        lockerDatabase.imageDao().insertAll(image);
                    }
                    int status = (100 / mediaItems[0].size()) * (i + 1);
                    super.publishProgress(new TaskStatus(status, image.name));
                }
            }

            List<BaseDescriptionObject> baseDescriptionObjects = ReloadImageTask.loadImages(lockerDatabase, this.folderId);
            lockerDatabase.close();
            return baseDescriptionObjects;
        }
        return null;
    }
}
