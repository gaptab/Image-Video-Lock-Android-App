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

package de.domjos.mediaLocker.model.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "images")
public class Image {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "title", index = true)
    public String name;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "tags")
    public String tags;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "privatePath")
    public String privatePath;

    @ColumnInfo(name = "tmpPath")
    public String tmpPath;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "image")
    public boolean image;

    @ColumnInfo(name = "content")
    public byte[] content;

    @ColumnInfo(name = "folder_id")
    public long folder_id;
}
