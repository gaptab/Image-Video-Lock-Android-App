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

package de.domjos.mediaLocker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.Image;

public class GridAdapter extends BaseAdapter {
    private List<BaseDescriptionObject> objects;
    private Context context;
    private LayoutInflater inflater;
    private DeleteListener deleteListener;
    private ClickListener clickListener;
    private ClickListener longClickListener;
    private SomethingSelectedListener somethingSelectedListener;
    private List<Long> selectedItems;
    private TextView lblStatistics;

    public GridAdapter(Context context, TextView lblStatistics) {
        this.selectedItems = new LinkedList<>();
        this.context = context;
        this.objects = new LinkedList<>();
        this.inflater = LayoutInflater.from(this.context);
        this.lblStatistics = lblStatistics;
        this.setStatistics();
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(ClickListener clickListener) {
        this.longClickListener = clickListener;
    }

    public void setSomethingSelectedListener(SomethingSelectedListener somethingSelectedListener) {
        this.somethingSelectedListener = somethingSelectedListener;
    }

    public List<BaseDescriptionObject> getSelectedItems() {
        List<BaseDescriptionObject> baseDescriptionObjects = new LinkedList<>();
        for(long id : this.selectedItems) {
            for(BaseDescriptionObject current : this.objects) {
                if(current.getId() == id) {
                    baseDescriptionObjects.add(current);
                    break;
                }
            }
        }
        return baseDescriptionObjects;
    }

    public int getSelectedItemsCount() {
        return this.selectedItems.size();
    }

    public void setList(List<BaseDescriptionObject> objects) {
        if(objects.isEmpty()) {
            BaseDescriptionObject baseDescriptionObject = new BaseDescriptionObject();
            baseDescriptionObject.setTitle(this.context.getString(R.string.item_noData));
            objects.add(baseDescriptionObject);
        }
        this.objects = objects;
        this.selectedItems.clear();
        if(this.somethingSelectedListener != null) {
            this.somethingSelectedListener.somethingSelected(false);
        }
        this.setStatistics();
    }

    @Override
    public int getCount() {
        return this.objects.size();
    }

    @Override
    public Object getItem(int position) {
        return this.objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.objects.get(position).getId();
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        view = this.inflater.inflate(R.layout.grid_item, null);
        holder = new ViewHolder();
        holder.ivFolderCover = view.findViewById(R.id.ivFolderCover);
        holder.lblFolderName = view.findViewById(R.id.lblFolderName);
        holder.cmdFolderDelete = view.findViewById(R.id.cmdFolderDelete);
        holder.ivSelected = view.findViewById(R.id.ivSelected);

        BaseDescriptionObject baseDescriptionObject = this.objects.get(position);
        if(baseDescriptionObject.getCover() != null) {
            holder.ivFolderCover.setImageBitmap(baseDescriptionObject.getCover());
        } else {
            if(baseDescriptionObject.getObject() instanceof Folder) {
                Drawable drawable = ContextCompat.getDrawable(this.context, R.drawable.icon_folder);
                holder.ivFolderCover.setImageDrawable(drawable);
            }
            if(baseDescriptionObject.getObject() instanceof Image) {
                Drawable drawable = ContextCompat.getDrawable(this.context, R.drawable.icon_image);
                holder.ivFolderCover.setImageDrawable(drawable);
            }
        }
        holder.lblFolderName.setText(baseDescriptionObject.getTitle());

        if(!holder.lblFolderName.getText().equals(this.context.getString(R.string.item_noData))) {
            holder.ivSelected.setVisibility(View.VISIBLE);
            holder.cmdFolderDelete.setOnClickListener(view1 -> {
                if(this.deleteListener != null) {
                    this.deleteListener.delete(position);
                }
            });

            holder.ivFolderCover.setOnClickListener(view1 -> {
                if(this.clickListener != null) {
                    this.clickListener.click(position);
                }
            });
            holder.lblFolderName.setOnClickListener(view1 -> {
                if(this.clickListener != null) {
                    this.clickListener.click(position);
                }
            });

            holder.ivFolderCover.setOnLongClickListener(view1 -> {
                if(this.longClickListener != null) {
                    this.longClickListener.click(position);
                }
                return true;
            });
            holder.lblFolderName.setOnLongClickListener(view1 -> {
                if(this.longClickListener != null) {
                    this.longClickListener.click(position);
                }
                return true;
            });

            holder.ivSelected.setOnClickListener(view1 -> {
                if(holder.ivSelected.getTag().toString().equals("unselected")) {
                    holder.ivSelected.setTag("selected");
                    holder.ivSelected.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.icon_checked));
                    if(!this.selectedItems.contains(baseDescriptionObject.getId())) {
                        this.selectedItems.add(baseDescriptionObject.getId());
                    }
                } else {
                    holder.ivSelected.setTag("unselected");
                    holder.ivSelected.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.icon_unchecked));
                    this.selectedItems.remove(baseDescriptionObject.getId());
                }
                if(this.somethingSelectedListener != null) {
                    this.somethingSelectedListener.somethingSelected(!this.selectedItems.isEmpty());
                }
                this.setStatistics();
            });
        } else {
            holder.ivFolderCover.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.icon_no_data));
            holder.cmdFolderDelete.setVisibility(View.GONE);
            holder.ivSelected.setVisibility(View.GONE);
        }
        return view;
    }

    private void setStatistics() {
        if(this.lblStatistics != null) {
            long count = this.objects.size();
            if(count == 1) {
                if(this.objects.get(0).getTitle().equals(this.context.getString(R.string.item_noData))) {
                    count = 0;
                }
            }
            this.lblStatistics.setText(String.format(this.context.getString(R.string.added_and_selected), count, this.selectedItems.size()));
        }
    }

    static class ViewHolder {
        ImageView ivFolderCover;
        TextView lblFolderName;
        ImageButton cmdFolderDelete;
        ImageView ivSelected;
    }

    @FunctionalInterface
    public interface DeleteListener {
        void delete(int position);
    }

    @FunctionalInterface
    public interface ClickListener {
        void click(int position);
    }

    @FunctionalInterface
    public interface SomethingSelectedListener {
        void somethingSelected(boolean somethingSelected);
    }
}
