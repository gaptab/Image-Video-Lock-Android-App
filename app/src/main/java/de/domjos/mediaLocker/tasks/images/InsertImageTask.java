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

import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.TaskStatus;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.DialogTask;

public final class InsertImageTask extends DialogTask<Image, List<BaseDescriptionObject>> {
    private long folderId;

    public InsertImageTask(Activity activity, boolean notifications, long folderId) {
        super(activity, R.string.task_images_insert_title, R.string.task_images_insert_title, notifications);
        this.folderId = folderId;
    }

    @Override
    protected List<BaseDescriptionObject> background(Image... images) {
        if(images != null) {
            LockerDatabase lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            for(int i = 0; i<=images.length - 1; i++) {
                Image image = images[i];
                if(lockerDatabase.imageDao().count(image.name, image.id) == 0) {
                    if(image.id != 0) {
                        lockerDatabase.imageDao().updateAll(image);
                    } else {
                        lockerDatabase.imageDao().insertAll(image);
                    }
                } else {
                    super.printMessage(this.getContext().getString(R.string.data_exists));
                }
                super.publishProgress(new TaskStatus(((100 / images.length) * (i + 1)), image.name));
            }
            List<BaseDescriptionObject> baseDescriptionObjects = ReloadImageTask.loadImages(lockerDatabase, this.folderId);
            lockerDatabase.close();
            return baseDescriptionObjects;
        }
        return null;
    }
}
