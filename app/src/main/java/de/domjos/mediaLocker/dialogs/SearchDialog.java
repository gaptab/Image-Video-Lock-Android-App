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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;
import java.util.List;

import de.domjos.mediaLocker.R;

public class SearchDialog extends DialogFragment {
    private OnDialogDismissListener listener;

    private Spinner spSearchType;
    private EditText txtSearch;
    private ImageButton cmdSearch;

    public static SearchDialog newInstance(String title) {
        SearchDialog frag = new SearchDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBoolean("mass", false);
        frag.setArguments(args);
        return frag;
    }

    public static SearchDialog newInstance() {
        SearchDialog frag = new SearchDialog();
        Bundle args = new Bundle();
        args.putString("title", "");
        args.putBoolean("mass", true);
        frag.setArguments(args);
        return frag;
    }

    public void setDismissListener(OnDialogDismissListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.cmdSearch = view.findViewById(R.id.cmdSearch);

        List<String> items = Arrays.asList(this.getString(R.string.title), this.getString(R.string.category), this.getString(R.string.tags));
        if(this.getArguments() != null) {
            if(this.getArguments().getBoolean("mass")) {
                items = Arrays.asList(this.getString(R.string.category), this.getString(R.string.tags));
                ((TextView) view.findViewById(R.id.lblSearchHeader)).setText(this.requireContext().getString(R.string.mass_change));
                this.cmdSearch.setImageDrawable(ContextCompat.getDrawable(this.requireContext(), R.drawable.icon_save));
            } else {
                String title = this.getArguments().getString("title");
                if(title != null) {
                    ((TextView) view.findViewById(R.id.lblSearchHeader)).setText(title);
                }
            }
        }

        this.spSearchType = view.findViewById(R.id.spSearchType);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this.requireContext(), android.R.layout.simple_spinner_item, items);
        this.spSearchType.setAdapter(typeAdapter);
        this.txtSearch = view.findViewById(R.id.txtSearch);

        this.initActions();
    }

    private void initActions() {
        this.cmdSearch.setOnClickListener(event -> {
            String type = this.spSearchType.getSelectedItem().toString();
            String search = this.txtSearch.getText().toString();
            if(!type.trim().isEmpty() && !search.trim().isEmpty()) {
                if(this.listener != null) {
                    this.listener.execute(type, search);
                    this.dismiss();
                }
            }
        });
    }

    @FunctionalInterface
    public interface OnDialogDismissListener {
        void execute(String type, String value);
    }
}
