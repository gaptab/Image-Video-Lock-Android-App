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

package de.domjos.mediaLocker.tasks;

import android.app.Activity;
import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;

public final class ItemTask extends AbstractTask<String, Void, List<String>> {

    public ItemTask(Activity activity, boolean showNotifications) {
        super(activity, R.string.task_items_title, R.string.task_items_title, showNotifications, R.drawable.icon_notifications);
    }

    @Override
    protected List<String> doInBackground(String... strings) {
        LockerDatabase lockerDatabase = null;
        try {
            lockerDatabase = Utils.getDatabase(this.getContext(), MainActivity.password);
            if(strings[0].trim().equals("folder")) {
                if(strings[1].toLowerCase().trim().equals("tags")) {
                    List<String> tags = lockerDatabase.folderDao().getTags();
                    List<String> items = new LinkedList<>();
                    for(String tag : tags) {
                        if(tag != null) {
                            if(tag.contains(",")) {
                                for(String item : tag.split(",")) {
                                    if(!item.trim().isEmpty()) {
                                        items.add(item.trim());
                                    }
                                }
                            } else if(tag.contains(";")) {
                                for(String item : tag.split(";")) {
                                    if(!item.trim().isEmpty()) {
                                        items.add(item.trim());
                                    }
                                }
                            } else {
                                if(!tag.trim().isEmpty()) {
                                    items.add(tag.trim());
                                }
                            }
                        }
                    }
                    return items;
                } else {
                    return lockerDatabase.folderDao().getCategories();
                }
            } else {
                if(strings[1].toLowerCase().trim().equals("tags")) {
                    List<String> tags = lockerDatabase.imageDao().getTags();
                    List<String> items = new LinkedList<>();
                    for(String tag : tags) {
                        if(tag != null) {
                            if(tag.contains(",")) {
                                for(String item : tag.split(",")) {
                                    if(!item.trim().isEmpty()) {
                                        items.add(item.trim());
                                    }
                                }
                            } else if(tag.contains(";")) {
                                for(String item : tag.split(";")) {
                                    if(!item.trim().isEmpty()) {
                                        items.add(item.trim());
                                    }
                                }
                            } else {
                                if(!tag.trim().isEmpty()) {
                                    items.add(tag.trim());
                                }
                            }
                        }
                    }
                    return items;
                } else {
                    return lockerDatabase.imageDao().getCategories();
                }
            }
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

    public static boolean testPassword(String password, Context context) {
        try {
            LockerDatabase lockerDatabase = Utils.getDatabase(context, password);
            lockerDatabase.folderDao().getCategories();
            return true;
        } catch (IllegalStateException ex) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
