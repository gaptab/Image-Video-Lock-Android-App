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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.tasks.ItemTask;
import de.domjos.mediaLocker.tasks.folders.ReloadFolderTask;

public class ShareDialog extends DialogFragment {
    private Context context;

    private EditText txtPwd;
    private Spinner cmbFolder;
    private EditText txtFolder;
    private Button cmdPwdOk;
    private OnDialogDismissListener listener;

    private Map<String, Long> folders = new LinkedHashMap<>();

    public static ShareDialog newInstance() {
        ShareDialog frag = new ShareDialog();
        frag.setCancelable(false);
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    public void setDismissListener(OnDialogDismissListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.share_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.context = this.requireContext();

        this.txtPwd = view.findViewById(R.id.txtPwd);
        this.txtFolder = view.findViewById(R.id.txtFolder);
        this.cmbFolder = view.findViewById(R.id.cmbFolder);

        this.cmdPwdOk = view.findViewById(R.id.cmdPwdOk);

        this.initActions();
    }

    private void initActions() {

        this.txtPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(validate()) {
                    boolean notifications = Settings.getUserSetting(context, Settings.NOTIFICATIONS, true);
                    LockerDatabase lockerDatabase = Utils.getDatabase(context, editable.toString());

                    folders.clear();
                    ReloadFolderTask reloadFolderTask = new ReloadFolderTask(getActivity(), notifications, lockerDatabase);
                    reloadFolderTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) o -> {
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
                        for(BaseDescriptionObject baseDescriptionObject : o) {
                            arrayAdapter.add(baseDescriptionObject.getTitle());
                            folders.put(baseDescriptionObject.getTitle(), baseDescriptionObject.getId());
                        }
                        cmbFolder.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                    });
                    reloadFolderTask.execute();
                }
            }
        });

        this.cmdPwdOk.setOnClickListener(event -> {
            if(validate()) {
                ShareDialog.this.dismiss();
                String newName = this.txtFolder.getText().toString().trim();
                String pwd =  this.txtPwd.getText().toString().trim();
                String item = this.cmbFolder.getSelectedItem().toString();
                long folderId = newName.isEmpty() ? Objects.requireNonNull(this.folders.get(item)) : 0;

                if(this.listener != null) {
                    this.listener.execute(folderId, pwd, newName);
                }
            }
        });
    }

    private boolean validate() {
        this.txtPwd.setError("");
        if(this.txtPwd.getText().toString().toCharArray().length != 8) {
            this.txtPwd.setError(this.getString(R.string.dialog_pwd_validation_max));
            return false;
        }
        if(!ItemTask.testPassword(this.txtPwd.getText().toString(), this.context)) {
            this.txtPwd.setError(this.getString(R.string.dialog_pwd_validation_wrong));
            return false;
        }
        return true;
    }

    @FunctionalInterface
    public interface OnDialogDismissListener {
        void execute(long id, String password, String newFolder);
    }
}
