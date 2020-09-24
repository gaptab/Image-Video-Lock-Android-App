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

import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.TaskStatus;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.DialogTask;

public final class DeleteFolderTask extends DialogTask<Folder, List<BaseDescriptionObject>> {

    public DeleteFolderTask(Activity activity, boolean notifications) {
        super(activity, R.string.task_folder_delete_title, R.string.task_folder_delete_title, notifications);
    }

    @Override
    protected List<BaseDescriptionObject> background(Folder... folders) {
        if(folders != null) {
            LockerDatabase lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            for(int i = 0; i<=folders.length - 1; i++) {
                Folder folder = folders[i];

                List<Image> images = lockerDatabase.imageDao().getAll(folder.id);
                for(int j = 0; j<=images.size() - 1; j++) {
                    Image image = images.get(j);
                    super.publishProgress(new TaskStatus((100 / images.size()) * (j + 1), image.name));
                    MediaHelper.deleteImage(image, this.getContext());
                    lockerDatabase.imageDao().deleteAll(image);
                }
                lockerDatabase.folderDao().deleteAll(folder);
            }
            List<BaseDescriptionObject> result = ReloadFolderTask.loadFolders(lockerDatabase);
            lockerDatabase.close();
            return result;
        }
        return null;
    }
}
