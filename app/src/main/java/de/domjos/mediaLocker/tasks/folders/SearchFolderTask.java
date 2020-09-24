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

package de.domjos.mediaLocker.tasks.folders;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.tasks.DialogTask;

public final class SearchFolderTask extends DialogTask<String, List<BaseDescriptionObject>> {

    public SearchFolderTask(Activity activity, boolean showNotifications) {
        super(activity, R.string.task_folder_title, R.string.task_folder_title, showNotifications);
    }

    @Override
    protected final List<BaseDescriptionObject> background(String... ts) {
        LockerDatabase lockerDatabase = null;
        try {
            lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            String type = ts[0];
            List<Folder> folders = new LinkedList<>();
            if(type.trim().toLowerCase().equals("title")) {
                folders = lockerDatabase.folderDao().loadByTitle("%" + ts[1] + "%");
            } else if(type.trim().toLowerCase().equals("category")) {
                folders = lockerDatabase.folderDao().loadByCategory("%" + ts[1] + "%");
            } else if(type.trim().toLowerCase().equals("tag")) {
                folders = lockerDatabase.folderDao().loadByTag("%" + ts[1] + "%");
            }

            List<BaseDescriptionObject> baseDescriptionObjects = new LinkedList<>();
            for(Folder folder : folders) {
                BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
                baseDescriptionObject.setTitle(folder.name);
                baseDescriptionObject.setId(folder.id);
                baseDescriptionObject.setCover(folder.cover);
                baseDescriptionObject.setObject(folder);
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
