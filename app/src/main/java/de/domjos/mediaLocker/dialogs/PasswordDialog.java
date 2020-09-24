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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.tasks.ItemTask;

public class PasswordDialog extends DialogFragment {
    private boolean first_start;
    private Context context;

    private EditText txtPwd, txtPwdRepeat;
    private Button cmdPwdOk;
    private OnDialogDismissListener listener;

    public static PasswordDialog newInstance(String title) {
        PasswordDialog frag = new PasswordDialog();
        frag.setCancelable(false);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void setDismissListener(OnDialogDismissListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.password_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.context = this.requireContext();

        this.first_start = Settings.getSetting(this.context, Settings.FIRST_START, true);

        this.txtPwd = view.findViewById(R.id.txtPwd);
        this.txtPwdRepeat = view.findViewById(R.id.txtPwdRepeat);
        this.cmdPwdOk = view.findViewById(R.id.cmdPwdOk);

        this.changeView();
        this.initActions();
    }

    private void changeView() {
        if(!this.first_start) {
            this.txtPwdRepeat.setVisibility(View.GONE);
        }
    }

    private void initActions() {
        this.cmdPwdOk.setOnClickListener(event -> {
            if(validate()) {
                Settings.putSetting(this.context, Settings.FIRST_START, false);
                MainActivity.password = this.txtPwd.getText().toString();
                PasswordDialog.this.dismiss();
                if(this.listener != null) {
                    this.listener.execute();
                }
            }
        });
    }

    private boolean validate() {
        this.txtPwd.setError("");
        this.txtPwdRepeat.setError("");
        if(this.txtPwd.getText().toString().toCharArray().length != 8) {
            this.txtPwd.setError(this.getString(R.string.dialog_pwd_validation_max));
            this.txtPwdRepeat.setError(this.getString(R.string.dialog_pwd_validation_max));
            return false;
        }
        if(this.first_start) {
            if(!this.txtPwd.getText().toString().equals(this.txtPwdRepeat.getText().toString())) {
                this.txtPwd.setError(this.getString(R.string.dialog_pwd_validation_same));
                this.txtPwdRepeat.setError(this.getString(R.string.dialog_pwd_validation_same));
                return false;
            }
        } else {
            if(!ItemTask.testPassword(this.txtPwd.getText().toString(), this.context)) {
                this.txtPwd.setError(this.getString(R.string.dialog_pwd_validation_wrong));
                this.txtPwdRepeat.setError(this.getString(R.string.dialog_pwd_validation_wrong));
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    public interface OnDialogDismissListener {
        void execute();
    }
}
