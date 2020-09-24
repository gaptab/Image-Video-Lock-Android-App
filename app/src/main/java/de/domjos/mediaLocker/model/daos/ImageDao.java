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
import androidx.room.Update;

import java.util.List;

import de.domjos.mediaLocker.model.entities.Image;

@Dao
public interface ImageDao {
    @Query("Select * FROM images WHERE folder_id = :folder_id")
    List<Image> getAll(long folder_id);

    @Query("SELECT * FROM images WHERE title like :title AND folder_id = :folder_id")
    List<Image> loadByTitle(String title, long folder_id);

    @Query("SELECT * FROM images WHERE category like :category AND folder_id = :folder_id")
    List<Image> loadByCategory(String category, long folder_id);

    @Query("SELECT * FROM images WHERE tags like :tag AND folder_id = :folder_id")
    List<Image> loadByTag(String tag, long folder_id);

    @Query("UPDATE images SET timestamp=:ts WHERE id=:id")
    void updateTimestamp(long ts, long id);

    @Query("SELECT * FROM images WHERE timestamp<(:current - (:diff * 1000)) AND timestamp!=0")
    List<Image> getPathToDelete(long current, long diff);

    @Query("SELECT category FROM images GROUP BY category")
    List<String> getCategories();

    @Query("SELECT tags FROM images GROUP BY tags")
    List<String> getTags();

    @Query("SELECT count(title) FROM images WHERE title = :title AND id != :id")
    long count(String title, long id);

    @Insert
    void insertAll(Image... images);

    @Update
    void updateAll(Image... images);

    @Delete
    void deleteAll(Image... images);
}
