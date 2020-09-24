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
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.domjos.customwidgets.model.tasks.TaskStatus;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.DialogTask;

import static android.content.Intent.ACTION_SEND;

public final class SharedImagesTask extends DialogTask<Void, Void> {
    private Intent intent;
    private long id;
    private String password;
    private String newFolder;

    public SharedImagesTask(Activity activity, boolean showNotifications, Intent intent, long id, String password, String newFolder) {
        super(activity, R.string.task_share_title, R.string.task_share_title, showNotifications);

        this.intent = intent;
        this.id = id;
        this.password = password;
        this.newFolder = newFolder;
    }

    @Override
    protected Void background(Void... voids) {
        String action = this.intent.getAction();
        String type = Objects.requireNonNull(this.intent.getType());
        LockerDatabase lockerDatabase = Utils.getDatabase(this.getContext(), this.password);
        try {
            List<Image> images = new LinkedList<>();

            // create new folder
            super.publishProgress(new TaskStatus(0, this.getContext().getString(R.string.task_share_get_data)));
            if(this.id == 0 && !this.newFolder.trim().isEmpty()) {
                Folder folder = new Folder();
                folder.name = this.newFolder.trim();
                if(lockerDatabase.folderDao().count(folder.name, folder.id) == 0) {
                    lockerDatabase.folderDao().insertAll(folder);
                }
                List<Folder> folders = lockerDatabase.folderDao().getAll();
                if(folders != null) {
                    for(Folder current : folders) {
                        if(current.name.equals(this.newFolder.trim())) {
                            this.id = current.id;
                            break;
                        }
                    }
                }
            }


            if(ACTION_SEND.equals(action)) {
                if(type.startsWith("image/") || type.startsWith("video/")) {
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    images.add(MediaHelper.createImageFromUri(uri, "", "", "", this.id, this.getContext()));
                }
            } else {
                if(type.startsWith("image/") || type.startsWith("video/")) {
                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (imageUris != null) {
                        for(Uri uri : imageUris) {
                            images.add(MediaHelper.createImageFromUri(uri, "", "", "", this.id, this.getContext()));
                        }
                    }
                }
            }

            double factor = 100.0 / images.size();
            int i = 1;
            for(Image image : images) {
                try {
                    if(image.name != null) {
                        if(lockerDatabase.imageDao().count(image.name, image.id) == 0) {
                            lockerDatabase.imageDao().insertAll(image);
                        } else {
                            super.printMessage(this.getContext().getString(R.string.data_exists));
                        }
                        super.publishProgress(new TaskStatus(((int) (i * factor)), String.format(this.getContext().getString(R.string.task_share_import), image.name)));
                    }
                } catch (Exception ex) {
                    super.printException(ex);
                }
            }
        } catch (Exception ex) {
            super.printException(ex);
        } finally {
            lockerDatabase.close();
        }


        return null;
    }
}
