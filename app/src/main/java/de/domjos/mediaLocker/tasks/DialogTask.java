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

import androidx.appcompat.app.AppCompatActivity;

import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.customwidgets.model.tasks.TaskStatus;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.dialogs.ProgressDialog;

public abstract class DialogTask<Params, Result> extends AbstractTask<Params, TaskStatus, Result> {
    private ProgressDialog progressDialog;

    public DialogTask(Activity activity, int title, int content, boolean showNotifications) {
        super(activity, title, content, showNotifications, R.drawable.icon_notifications);
        this.progressDialog = ProgressDialog.newInstance(((AppCompatActivity)activity).getSupportFragmentManager(), "progress_dialog", this);
    }

    @Override
    public void onProgressUpdate(TaskStatus... taskStatuses) {
        this.progressDialog.getProgressBar().setProgress(taskStatuses[0].getStatus());
        this.progressDialog.getTextView().setText(taskStatuses[0].getMessage());
    }

    @SuppressWarnings("unchecked")
    protected abstract Result background(Params... params);

    @SafeVarargs
    @Override
    protected final Result doInBackground(Params... params) {
        Result result = this.background(params);
        this.progressDialog.dismiss();
        return result;
    }

    @Override
    public void onCancelled() {
        this.progressDialog.dismiss();
    }
}
