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

package de.domjos.mediaLocker.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.helper.Utils;

public final class SettingsActivity extends AbstractActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public static final int RELOAD = 77;
    private static final String TITLE_TAG = "settingsActivityTitle";
    private FragmentManager fragmentManager;

    public SettingsActivity() {
        super(R.layout.settings_activity);
    }

    @Override
    protected void initActions() {
        this.fragmentManager.addOnBackStackChangedListener(() -> {
            if(this.fragmentManager.getBackStackEntryCount() == 0) {
                this.setTitle(R.string.settings);
            }
        });
    }

    @Override
    protected void initControls() {
        Utils.setCurrentActivity(this);

        this.fragmentManager = this.getSupportFragmentManager();

        if(super.savedInstanceState == null) {
            this.fragmentManager.beginTransaction().replace(R.id.settings, new HeaderFragment()).commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (this.fragmentManager.popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = this.fragmentManager.getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        this.fragmentManager.beginTransaction().replace(R.id.settings, fragment).addToBackStack(null).commit();
        this.setTitle(pref.getTitle());
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.general_preferences, rootKey);
        }
    }
}