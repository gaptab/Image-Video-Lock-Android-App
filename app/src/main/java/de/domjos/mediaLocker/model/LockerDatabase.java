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

package de.domjos.mediaLocker.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.domjos.mediaLocker.model.daos.FolderDao;
import de.domjos.mediaLocker.model.daos.ImageDao;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.Image;

@Database(entities = {Folder.class, Image.class}, version = 1, exportSchema = false)
public abstract class LockerDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();
    public abstract ImageDao imageDao();
}
