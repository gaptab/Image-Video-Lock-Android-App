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

package de.domjos.mediaLocker.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.mediaLocker.R;

public class ProgressDialog extends DialogFragment {
    private TextView lblMessages;
    private ProgressBar pbProgress;
    private AbstractTask<?, ?, ?> task;

    public static ProgressDialog newInstance(FragmentManager fragmentManager, String tag, AbstractTask<?, ?, ?> task) {
        ProgressDialog frag = new ProgressDialog(task);
        frag.setCancelable(false);
        Bundle args = new Bundle();
        frag.setArguments(args);
        frag.show(fragmentManager, tag);
        return frag;
    }

    public ProgressDialog(AbstractTask<?, ?, ?> task) {
        this.task = task;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.progress_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.lblMessages = view.findViewById(R.id.lblMessage);
        this.pbProgress = view.findViewById(R.id.pbProgress);
        ((ImageButton) view.findViewById(R.id.cmdCancel)).setOnClickListener((View.OnClickListener) view1 -> {
            this.task.cancel(true);
            this.dismiss();
        });
    }

    public ProgressBar getProgressBar() {
        return this.pbProgress;
    }

    public TextView getTextView() {
        return this.lblMessages;
    }

    public void close() {
        this.dismiss();
    }
}
