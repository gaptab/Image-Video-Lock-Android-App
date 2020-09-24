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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.adapter.GridAdapter;
import de.domjos.mediaLocker.dialogs.SearchDialog;
import de.domjos.mediaLocker.helper.IntentHelper;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.folders.ReloadFolderTask;
import de.domjos.mediaLocker.tasks.images.SearchImageTask;
import de.domjos.mediaLocker.tasks.ItemTask;
import de.domjos.mediaLocker.tasks.images.DeleteImageTask;
import de.domjos.mediaLocker.tasks.images.ImportMediaTask;
import de.domjos.mediaLocker.tasks.images.InsertImageTask;
import de.domjos.mediaLocker.tasks.images.ReloadImageTask;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

public final class MediaActivity extends AbstractActivity {
    static final String FOLDER = "folder";
    static final int RELOAD = 99;
    private boolean notifications;

    private Folder currentFolder;

    private FloatingActionButton fabAdd;
    private GridView grd;
    private GridAdapter gridAdapter;

    private ScrollView tblImages;
    private EditText txtImageName;
    private AutoCompleteTextView txtImageCategory;
    private MultiAutoCompleteTextView txtImageTags;

    private ImageButton cmdImageSave, cmdImageCancel, cmdImageSearch, cmdVideoSearch;

    private Image currentImage;
    private Uri currentUri;

    public MediaActivity() {
        super(R.layout.media_activity);
    }

    @Override
    protected void initActions() {
        this.cmdImageSearch.setOnClickListener(event -> IntentHelper.startSingleImagePicker(this));
        this.cmdVideoSearch.setOnClickListener(event -> IntentHelper.startSingleVideoPicker(this));

        this.gridAdapter.setLongClickListener(position -> {
            this.currentImage = (Image) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
            this.manageControls(true, true, false);
        });

        this.gridAdapter.setClickListener(position -> {
            Image img = (Image) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
            Intent intent = new Intent(this.getApplicationContext(), ViewerActivity.class);
            intent.putExtra(ViewerActivity.IMAGE_ID, img.id);
            intent.putExtra(ViewerActivity.FOLDER_ID, currentFolder.id);
            startActivityForResult(intent, 33);
        });

        this.gridAdapter.setDeleteListener(position -> {
            this.currentImage = (Image) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
            Utils.createIfNoneExists(this.currentImage, this);
            this.deleteItem(this.currentImage);
        });

        this.fabAdd.setOnClickListener(view -> {
            this.currentImage = new Image();
            this.manageControls(true, false, true);
        });

        this.cmdImageSave.setOnClickListener(event -> this.saveItem());
        this.cmdImageCancel.setOnClickListener(event -> this.cancel());
    }

    @Override
    public void reload() {
        if(this.currentFolder == null) {
            this.loadFolder();
        }

        ReloadImageTask imageTask = new ReloadImageTask(this, this.notifications, this.currentFolder.id);
        imageTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) images -> {
            this.gridAdapter.setList(images);
            this.gridAdapter.notifyDataSetInvalidated();
        });
        imageTask.execute();
        this.reloadItems();
    }

    private void reloadItems() {
        ItemTask categoryTask = new ItemTask(this, this.notifications);
        categoryTask.after((AbstractTask.PostExecuteListener<List<String>>) categories -> {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
            this.txtImageCategory.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        });
        categoryTask.execute("image", "categories");

        ItemTask tagTask = new ItemTask(this, this.notifications);
        tagTask.after((AbstractTask.PostExecuteListener<List<String>>) tags -> {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tags);
            this.txtImageTags.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        });
        tagTask.execute("image", "tags");
    }

    private void reload(String type, String search) {
        try {
            if(type.equals(this.getString(R.string.title))) {
                type = "title";
            } else if(type.equals(this.getString(R.string.category))) {
                type = "category";
            } else if(type.equals(this.getString(R.string.tags))) {
                type = "tag";
            } else {
                return;
            }

            List<String> searchItems;
            if(search.contains(";")) {
                searchItems = new LinkedList<>(Arrays.asList(search.split(";")));
            } else if(search.contains(",")) {
                searchItems = new LinkedList<>(Arrays.asList(search.split(",")));
            } else if(search.contains("|")) {
                searchItems = new LinkedList<>(Arrays.asList(search.split("\\|")));
            } else {
                searchItems = Collections.singletonList(search);
            }

            List<BaseDescriptionObject> images = new LinkedList<>();
            for(String searchItem : searchItems) {
                searchItem = searchItem.trim();
                if(!searchItem.isEmpty()) {
                    SearchImageTask searchImageTask = new SearchImageTask(this, this.notifications, this.currentFolder.id);
                    List<BaseDescriptionObject> imageList = searchImageTask.execute(type, searchItem).get();
                    if(imageList != null) {
                        for(BaseDescriptionObject object : imageList) {
                            boolean contains = false;
                            for(BaseDescriptionObject available : images) {
                                if(available.getId() == object.getId()) {
                                    contains = true;
                                    break;
                                }
                            }
                            if(!contains) {
                                images.add(object);
                            }
                        }
                    }
                }
            }

            this.gridAdapter.setList(images);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean selected, boolean reset) {
        this.tblImages.setVisibility(editMode ? View.VISIBLE : View.GONE);
        this.fabAdd.setVisibility(editMode ? View.GONE : View.VISIBLE);

        if(reset) {
            this.txtImageName.setText("");
            this.txtImageTags.setText("");
            this.txtImageCategory.setText("");
            this.cmdImageSearch.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.icon_image));
            this.cmdVideoSearch.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.icon_video));
            if(this.currentImage != null) {
                this.currentImage.content = null;
            }
        }

        if(selected) {
            if(this.currentImage != null) {
                this.txtImageName.setText(this.currentImage.name != null ? this.currentImage.name.trim() : "");
                this.txtImageCategory.setText(this.currentImage.category != null ? this.currentImage.category.trim() : "");
                this.txtImageTags.setText(this.currentImage.tags != null ? this.currentImage.tags.trim() : "");
                if(this.currentImage.content != null) {
                    this.cmdImageSearch.setImageBitmap(BitmapFactory.decodeByteArray(this.currentImage.content, 0, this.currentImage.content.length));
                }
            }
        }
    }

    private void saveItem() {
        if(this.currentImage != null) {
            Image image =
                MediaHelper.createImageFromUri(
                        this.currentUri,
                        this.txtImageName.getText().toString().trim(),
                        this.txtImageCategory.getText().toString().trim(),
                        this.txtImageTags.getText().toString().trim(),
                        this.currentFolder.id,
                        this
                );

            InsertImageTask insertImageTask = new InsertImageTask(this, this.notifications, this.currentFolder.id);
            insertImageTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) images -> {
                if(images != null) {
                    this.tblImages.setVisibility(View.GONE);
                    this.currentImage = null;
                    this.gridAdapter.setList(images);
                    this.gridAdapter.notifyDataSetInvalidated();
                    this.manageControls(false, false, true);
                    this.reloadItems();
                    MediaHelper.callBroadCast(this);
                }
            });
            if(this.currentImage.id == 0) {
                insertImageTask.execute(image);
            } else {
                insertImageTask.cancel(true);
            }
        }
    }

    private void deleteItem(Image image) {
        final Image[] finalImage = new Image[]{image};
        if(finalImage[0] != null) {
            DeleteImageTask deleteImageTask = new DeleteImageTask(this, this.notifications, this.currentFolder.id);
            deleteImageTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) images -> {
                if(images != null) {
                    this.tblImages.setVisibility(View.GONE);
                    finalImage[0] = null;
                    this.gridAdapter.setList(images);
                    this.gridAdapter.notifyDataSetInvalidated();
                    this.manageControls(false, false, true);
                    this.reloadItems();
                    MediaHelper.callBroadCast(this);
                }
            });
            if(finalImage[0].id != 0) {
                deleteImageTask.execute(finalImage[0]);
            }
        }
    }

    private void cancel() {
        this.currentImage = null;
        this.tblImages.setVisibility(View.GONE);
        this.manageControls(false, false, true);
    }

    @Override
    protected void initControls() {
        Utils.setCurrentActivity(this);

        this.notifications = Settings.getUserSetting(this.getApplicationContext(), Settings.NOTIFICATIONS, true);

        this.fabAdd = this.findViewById(R.id.fabAdd);

        this.tblImages = this.findViewById(R.id.tblImages);
        this.txtImageName = this.findViewById(R.id.txtImageName);
        this.txtImageCategory = this.findViewById(R.id.txtImageCategory);
        this.txtImageTags = this.findViewById(R.id.txtImageTags);
        this.cmdImageSearch = this.findViewById(R.id.cmdImageSearch);
        this.cmdVideoSearch = this.findViewById(R.id.cmdVideoSearch);

        this.cmdImageSave = this.findViewById(R.id.cmdImageSave);
        this.cmdImageCancel = this.findViewById(R.id.cmdImageCancel);

        TextView lblStatistics = this.findViewById(R.id.lblStatistics);
        this.grd = this.findViewById(R.id.grdImages);
        this.grd.setNumColumns(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
        this.gridAdapter = new GridAdapter(this.getApplicationContext(), lblStatistics);
        this.grd.setAdapter(this.gridAdapter);
        this.gridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        this.grd.setNumColumns(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    private void loadFolder() {
        try {
            Intent intent = this.getIntent();
            if(intent != null) {
                if(intent.hasExtra(MediaActivity.FOLDER)) {
                    ReloadFolderTask searchFolderTask = new ReloadFolderTask(this, this.notifications);
                    List<BaseDescriptionObject> folders = searchFolderTask.execute().get();
                    if(folders != null) {
                        if(folders.size() != 0) {
                            for(BaseDescriptionObject baseDescriptionObject : folders) {
                                if(baseDescriptionObject.getId() == intent.getLongExtra(MediaActivity.FOLDER, 0)) {
                                    this.currentFolder = (Folder) baseDescriptionObject.getObject();
                                    this.setTitle(this.currentFolder.name);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);Utils.setCurrentActivity(this);
        Utils.setCurrentActivity(this);

        try {
            if(resultCode == RESULT_OK) {
                if(requestCode == IntentHelper.IMAGE_REQUEST) {
                    List<MediaItem> paths = MediaPickerActivity.Companion.getMediaItemSelected(intent);
                    if(paths != null) {
                        this.importMedia(paths);
                    }
                }
                if(requestCode == IntentHelper.VIDEO_REQUEST) {
                    List<MediaItem> paths = MediaPickerActivity.Companion.getMediaItemSelected(intent);
                    if(paths != null) {
                        this.importMedia(paths);
                    }
                }
                if(requestCode == IntentHelper.IMAGE_SINGLE_REQUEST || requestCode == IntentHelper.VIDEO_SINGLE_REQUEST) {
                    List<MediaItem> paths = MediaPickerActivity.Companion.getMediaItemSelected(intent);
                    if(paths != null) {
                        if(paths.size() == 1) {
                            this.currentUri = paths.get(0).getUriOrigin();
                            Bitmap bitmap = MediaHelper.createThumbNailFromUri(this.currentUri, this);
                            if(bitmap != null) {
                                if(requestCode == IntentHelper.IMAGE_SINGLE_REQUEST) {
                                    this.cmdImageSearch.setImageBitmap(bitmap);
                                } else {
                                    this.cmdVideoSearch.setImageBitmap(bitmap);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    @SuppressWarnings("unchecked")
    private void importMedia(List<MediaItem> pathList) {
        ImportMediaTask importMediaTask = new ImportMediaTask(this, this.notifications, this.currentFolder.id);
        importMediaTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) objects -> {
            this.gridAdapter.setList(objects);
            this.gridAdapter.notifyDataSetInvalidated();
            this.reloadItems();
            MediaHelper.callBroadCast(this);
        });
        importMediaTask.execute(pathList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_media, menu);
        this.gridAdapter.setSomethingSelectedListener(somethingSelected -> {
            menu.findItem(R.id.actionDeleteSelected).setVisible(somethingSelected);
            menu.findItem(R.id.actionChangeSelected).setVisible(somethingSelected);
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menImportImages:
                IntentHelper.startImagePicker(this);
                break;
            case R.id.menImportVideos:
                IntentHelper.startVideoPicker(this);
                break;
            case R.id.actionDeleteSelected:
                for(BaseDescriptionObject baseDescriptionObject : this.gridAdapter.getSelectedItems()) {
                    this.deleteItem((Image) baseDescriptionObject.getObject());
                }
                break;
            case R.id.actionChangeSelected:
                SearchDialog changeDialog = SearchDialog.newInstance();
                changeDialog.setDismissListener((type, value) -> {
                    List<Image> images = new LinkedList<>();
                    for(BaseDescriptionObject baseDescriptionObject : this.gridAdapter.getSelectedItems()) {
                        Image image = (Image) baseDescriptionObject.getObject();
                        if (type.equals(this.getString(R.string.tags))) {
                            image.tags = image.tags.trim() + ", " + value;
                        } else {
                            image.category = value;
                        }
                        images.add(image);
                    }
                    InsertImageTask insertImageTask = new InsertImageTask(this, this.notifications, this.currentFolder.id);
                    insertImageTask.execute(images.toArray(new Image[]{}));
                });
                changeDialog.show(this.getSupportFragmentManager(), "change_dialog");
                break;
            case R.id.actionSearch:
                SearchDialog searchDialog = SearchDialog.newInstance(this.getString(R.string.search_images));
                searchDialog.setDismissListener(this::reload);
                searchDialog.show(this.getSupportFragmentManager(), "search_dialog");
                break;
            case R.id.actionRefresh:
                this.reload();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}