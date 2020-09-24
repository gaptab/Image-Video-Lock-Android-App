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

package de.domjos.mediaLocker.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import de.domjos.mediaLocker.R;

public abstract class Settings<T> {
    public static final String NOTIFICATIONS = "notifications";
    public static final String START_VIDEOS = "startVideos";
    public static final String PERIOD = "period";

    public static final String FIRST_START = "first_start";
    public static final String CURRENT_ACTIVITY = "current_activity";

    public static <T> T getUserSetting(Context context, String key, T defValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Settings.returnValue(sharedPreferences, key, defValue);
    }


    public static <T> T getSetting(Context context, String key, T defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        return Settings.returnValue(sharedPreferences, key, defValue);
    }

    public static <T> void putSetting(Context context, String key, T value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        if(value instanceof String) {
            editor.putString(key, (String) value);
        }
        if(value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        }
        if(value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
        if(value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    private static <T> T returnValue(SharedPreferences sharedPreferences, String key, T defValue) {
        try {
            if(defValue instanceof Boolean) {
                return (T) ((Boolean) sharedPreferences.getBoolean(key, (Boolean) defValue));
            }
            if(defValue instanceof String) {
                return (T) sharedPreferences.getString(key, (String) defValue);
            }
            if(defValue instanceof Integer) {
                return (T) ((Integer) sharedPreferences.getInt(key, (Integer) defValue));
            }
            if(defValue instanceof Float) {
                return (T) ((Float) sharedPreferences.getFloat(key, (Float) defValue));
            }
            if(defValue instanceof Long) {
                return (T) ((Long) sharedPreferences.getLong(key, (Long) defValue));
            }
        } catch (Exception ignored) {}
        return defValue;
    }
}
