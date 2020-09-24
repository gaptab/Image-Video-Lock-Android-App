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

public final class ReloadFolderTask extends DialogTask<Void, List<BaseDescriptionObject>> {
    private LockerDatabase lockerDatabase;

    public ReloadFolderTask(Activity activity, boolean notifications) {
        this(activity, notifications, null);
    }

    public ReloadFolderTask(Activity activity, boolean notifications, LockerDatabase lockerDatabase) {
        super(activity, R.string.task_folder_title, R.string.task_folder_title, notifications);

        this.lockerDatabase = lockerDatabase;
    }

    @Override
    protected List<BaseDescriptionObject> background(Void... voids) {
        try {
            if(this.lockerDatabase == null) {
                this.lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            }
            List<BaseDescriptionObject> baseDescriptionObjects = ReloadFolderTask.loadFolders(this.lockerDatabase);
            this.lockerDatabase.close();
            return baseDescriptionObjects;
        } catch (Exception ex) {
            super.printException(ex);
        }
        return new LinkedList<>();
    }

    static List<BaseDescriptionObject> loadFolders(LockerDatabase lockerDatabase) {
        List<Folder> folders = lockerDatabase.folderDao().getAll();
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
    }
}
