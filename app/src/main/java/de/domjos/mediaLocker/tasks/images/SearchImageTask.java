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

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.DialogTask;

public final class SearchImageTask extends DialogTask<String, List<BaseDescriptionObject>> {
    private long folderId;

    public SearchImageTask(Activity activity, boolean showNotifications, long folderId) {
        super(activity, R.string.task_image_title, R.string.task_image_title, showNotifications);
        this.folderId = folderId;
    }

    @Override
    protected final List<BaseDescriptionObject> background(String... ts) {
        LockerDatabase lockerDatabase = null;
        try {
            lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            String type = ts[0];
            List<Image> images = new LinkedList<>();
            if(type.trim().toLowerCase().equals("title")) {
                images = lockerDatabase.imageDao().loadByTitle("%" + ts[1] + "%", this.folderId);
            } else if(type.trim().toLowerCase().equals("category")) {
                images = lockerDatabase.imageDao().loadByCategory("%" + ts[1] + "%", this.folderId);
            } else if(type.trim().toLowerCase().equals("tag")) {
                images = lockerDatabase.imageDao().loadByTag("%" + ts[1] + "%", this.folderId);
            }
            List<BaseDescriptionObject> baseDescriptionObjects = new LinkedList<>();
            for(Image image : images) {
                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                baseDescriptionObject.setTitle(image.name);
                baseDescriptionObject.setId(image.id);
                baseDescriptionObject.setCover(image.content);
                baseDescriptionObject.setObject(image);
                baseDescriptionObjects.add(baseDescriptionObject);
            }
            return baseDescriptionObjects;
        } catch (Exception ex) {
            super.printException(ex);
        } finally {
            if(lockerDatabase != null) {
                if(lockerDatabase.isOpen()) {
                    lockerDatabase.close();
                }
            }
        }
        return null;
    }
}
