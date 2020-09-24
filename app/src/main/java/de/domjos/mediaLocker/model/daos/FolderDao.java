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

package de.domjos.mediaLocker.model.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.FolderWithImages;

@Dao
public interface FolderDao {
    @Query("SELECT * FROM folders")
    List<Folder> getAll();

    @Query("SELECT * FROM folders WHERE id IN (:ids)")
    List<Folder> loadByIds(long[] ids);

    @Query("SELECT * FROM folders WHERE title like :title")
    List<Folder> loadByTitle(String title);

    @Query("SELECT * FROM folders WHERE category like :category")
    List<Folder> loadByCategory(String category);

    @Query("SELECT * FROM folders WHERE tags like :tag")
    List<Folder> loadByTag(String tag);

    @Query("SELECT category FROM folders GROUP BY category")
    List<String> getCategories();

    @Query("SELECT tags FROM folders GROUP BY tags")
    List<String> getTags();

    @Query("SELECT count(title) FROM folders WHERE title = :title AND id != :id")
    long count(String title, long id);

    @Transaction
    @Query("SELECT * FROM folders WHERE id=:id")
    List<FolderWithImages> getFolder(long id);

    @Insert
    void insertAll(Folder... folders);

    @Update
    void updateAll(Folder... folders);

    @Delete
    void deleteAll(Folder... folders);
}
