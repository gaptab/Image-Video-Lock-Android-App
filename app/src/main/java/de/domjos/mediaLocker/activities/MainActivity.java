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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.customwidgets.tokenizer.CommaTokenizer;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.adapter.GridAdapter;
import de.domjos.mediaLocker.dialogs.PasswordDialog;
import de.domjos.mediaLocker.dialogs.SearchDialog;
import de.domjos.mediaLocker.helper.IntentHelper;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.entities.Folder;
import de.domjos.mediaLocker.services.DeleteService;
import de.domjos.mediaLocker.tasks.folders.SearchFolderTask;
import de.domjos.mediaLocker.tasks.ItemTask;
import de.domjos.mediaLocker.tasks.folders.DeleteFolderTask;
import de.domjos.mediaLocker.tasks.folders.InsertFolderTask;
import de.domjos.mediaLocker.tasks.folders.ReloadFolderTask;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

public final class MainActivity extends AbstractActivity {
    private boolean serviceIsRunning = false;
    public static String password = "";
    private boolean notifications;

    private FloatingActionButton fabAdd;
    private GridAdapter gridAdapter;
    private GridView grd;

    private ScrollView tblFolder;
    private EditText txtFolderName;
    private AutoCompleteTextView txtFolderCategory;
    private MultiAutoCompleteTextView txtFolderTags;

    private ImageButton cmdFolderSave, cmdFolderCancel, cmdFolderImageSearch;

    private Folder currentFolder;

    private PendingIntent pendingIntent;

    public MainActivity() {
        super(R.layout.main_activity);
    }

    @Override
    protected void initActions() {
        this.cmdFolderImageSearch.setOnClickListener(event -> IntentHelper.startSingleImagePicker(this));

        this.gridAdapter.setLongClickListener(position -> {
            this.currentFolder = (Folder) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
            this.manageControls(true, true, false);
        });

        this.gridAdapter.setClickListener(position -> {
            this.currentFolder = (Folder) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
            Intent intent = new Intent(this, MediaActivity.class);
            intent.putExtra(MediaActivity.FOLDER, this.currentFolder.id);
            this.startActivityForResult(intent, MediaActivity.RELOAD);
        });

        this.gridAdapter.setDeleteListener(position -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.dialog_delete_folder_title);
            alertDialogBuilder.setMessage(R.string.dialog_delete_folder_content);
            alertDialogBuilder.setIcon(R.drawable.icon_delete);
            alertDialogBuilder.setNegativeButton(R.string.dialog_delete_folder_no, (dialogInterface, i) ->{});
            alertDialogBuilder.setPositiveButton(R.string.dialog_delete_folder_yes, ((dialogInterface, i) -> {
                this.currentFolder = (Folder) ((BaseDescriptionObject) this.gridAdapter.getItem(position)).getObject();
                this.deleteItem(this.currentFolder);
            }));
            alertDialogBuilder.create().show();
        });

        this.fabAdd.setOnClickListener(view -> {
            this.currentFolder = new Folder();
            this.manageControls(true, false, true);
        });

        this.cmdFolderSave.setOnClickListener(event -> this.saveItem());
        this.cmdFolderCancel.setOnClickListener(event -> this.cancel());
    }

    @Override
    public void reload() {
        if(!MainActivity.password.trim().equals("")) {
            this.startJobIfNotRunning();

            ReloadFolderTask folderTask = new ReloadFolderTask(this, this.notifications);
            folderTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) folders -> {
                if(folders != null) {
                    this.gridAdapter.setList(folders);
                    this.gridAdapter.notifyDataSetInvalidated();
                }
            });
            folderTask.execute();

            this.reloadItems();
        }
    }

    public void startJobIfNotRunning() {
        try {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            try {
                if(this.pendingIntent != null) {
                    assert alarmManager != null;
                    alarmManager.cancel(this.pendingIntent);
                }
            } catch (Exception ignored) {}

            Intent deleteServiceIntent = new Intent(this.getApplicationContext(), DeleteService.class);
            deleteServiceIntent.putExtra(DeleteService.PASSWORD, MainActivity.password);
            deleteServiceIntent.putExtra(DeleteService.DIFF_TIME, Settings.getUserSetting(this, Settings.PERIOD, 180));
            deleteServiceIntent.putExtra(Settings.NOTIFICATIONS, Settings.getUserSetting(this, Settings.NOTIFICATIONS, true));
            this.pendingIntent = PendingIntent.getService(this.getApplicationContext(), 348, deleteServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            if(!this.serviceIsRunning) {
                if(alarmManager != null) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 60000, this.pendingIntent);
                    this.serviceIsRunning = true;
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    private void reloadItems() {
        ItemTask categoryTask = new ItemTask(this, this.notifications);
        categoryTask.after((AbstractTask.PostExecuteListener<List<String>>) categories -> {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
            this.txtFolderCategory.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        });
        categoryTask.execute("folder", "categories");

        ItemTask tagTask = new ItemTask(this, this.notifications);
        tagTask.after((AbstractTask.PostExecuteListener<List<String>>) tags -> {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tags);
            this.txtFolderTags.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        });
        tagTask.execute("folder", "tags");
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

            List<BaseDescriptionObject> folders = new LinkedList<>();
            for(String searchItem : searchItems) {
                searchItem = searchItem.trim();
                if(!searchItem.isEmpty()) {
                    SearchFolderTask searchFolderTask = new SearchFolderTask(this, this.notifications);
                    List<BaseDescriptionObject> folderList = searchFolderTask.execute(type, searchItem).get();
                    if(folderList != null) {
                        for(BaseDescriptionObject current : folderList) {
                            boolean contains = false;
                            for(BaseDescriptionObject available : folders) {
                                if(available.getId() == current.getId()) {
                                    contains = true;
                                    break;
                                }
                            }
                            if(!contains) {
                                folders.add(current);
                            }
                        }
                    }
                }
            }

            this.gridAdapter.setList(folders);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    @Override
    protected void manageControls(boolean editMode, boolean selected, boolean reset) {
        this.tblFolder.setVisibility(editMode ? View.VISIBLE : View.GONE);
        this.fabAdd.setVisibility(editMode ? View.GONE : View.VISIBLE);

        if(reset) {
            this.txtFolderName.setText("");
            this.txtFolderTags.setText("");
            this.txtFolderCategory.setText("");
            this.cmdFolderImageSearch.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.icon_image));
            if(this.currentFolder != null) {
                this.currentFolder.cover = null;
            }
        }

        if(selected) {
            if(this.currentFolder != null) {
                this.txtFolderName.setText(this.currentFolder.name != null ? this.currentFolder.name.trim() : "");
                this.txtFolderCategory.setText(this.currentFolder.category != null ? this.currentFolder.category.trim() : "");
                this.txtFolderTags.setText(this.currentFolder.tags != null ? this.currentFolder.tags.trim() : "");
                if(this.currentFolder.cover != null) {
                    this.cmdFolderImageSearch.setImageBitmap(BitmapFactory.decodeByteArray(this.currentFolder.cover, 0, this.currentFolder.cover.length));
                }
            }
        }
    }

    private void saveItem() {
        if(this.currentFolder != null) {
            this.currentFolder.name = this.txtFolderName.getText().toString().trim();
            this.currentFolder.category = this.txtFolderCategory.getText().toString().trim();
            this.currentFolder.tags = this.txtFolderTags.getText().toString().trim();

            InsertFolderTask insertFolderTask = new InsertFolderTask(this, true);
            insertFolderTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) folders -> {
                if(folders != null) {
                    this.tblFolder.setVisibility(View.GONE);
                    this.currentFolder = null;
                    this.gridAdapter.setList(folders);
                    this.gridAdapter.notifyDataSetInvalidated();
                    this.reloadItems();
                    this.manageControls(false, false, true);
                }
            });
            insertFolderTask.execute(this.currentFolder);
        }
    }

    private void deleteItem(Folder folder) {
        final Folder[] finalFolder = new Folder[]{folder};
        try {
            if(finalFolder[0] != null) {
                finalFolder[0].name = this.txtFolderName.getText().toString().trim();
                finalFolder[0].category = this.txtFolderCategory.getText().toString().trim();
                finalFolder[0].tags = this.txtFolderTags.getText().toString().trim();

                DeleteFolderTask deleteFolderTask = new DeleteFolderTask(this, true);
                deleteFolderTask.after((AbstractTask.PostExecuteListener<List<BaseDescriptionObject>>) folders -> {
                    if(folders != null) {
                        this.tblFolder.setVisibility(View.GONE);
                        finalFolder[0] = null;
                        this.gridAdapter.setList(folders);
                        this.gridAdapter.notifyDataSetInvalidated();
                        this.reloadItems();
                        this.manageControls(false, false, true);
                        MediaHelper.callBroadCast(this);
                    }
                });
                deleteFolderTask.execute(finalFolder[0]);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    private void cancel() {
        this.currentFolder = null;
        this.tblFolder.setVisibility(View.GONE);
        this.manageControls(false, false, true);
    }

    @Override
    protected void initControls() {
        this.initPermissions();
        Utils.setCurrentActivity(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if(!IntentHelper.getMultipleImagesFromOtherApp(this) && MainActivity.password.trim().equals("")) {
            PasswordDialog passwordDialog = PasswordDialog.newInstance(this.getString(R.string.dialog_pwd));
            passwordDialog.setDismissListener(this::reload);
            passwordDialog.show(this.getSupportFragmentManager(), "password_dialog");
        }

        this.notifications = Settings.getUserSetting(this.getApplicationContext(), Settings.NOTIFICATIONS, true);
        TextView lblStatistics = this.findViewById(R.id.lblStatistics);

        this.grd = this.findViewById(R.id.grdFolder);
        this.gridAdapter = new GridAdapter(this.getApplicationContext(), lblStatistics);
        this.grd.setAdapter(this.gridAdapter);
        this.grd.setNumColumns(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
        this.gridAdapter.notifyDataSetChanged();

        this.fabAdd = this.findViewById(R.id.fabAdd);

        this.tblFolder = this.findViewById(R.id.tblFolder);
        this.txtFolderName = this.findViewById(R.id.txtFolderName);
        this.txtFolderCategory = this.findViewById(R.id.txtFolderCategory);
        this.txtFolderTags = this.findViewById(R.id.txtFolderTags);
        this.txtFolderTags.setTokenizer(new CommaTokenizer());
        this.cmdFolderImageSearch = this.findViewById(R.id.cmdFolderSearchImage);

        this.cmdFolderSave = this.findViewById(R.id.cmdFolderSave);
        this.cmdFolderCancel = this.findViewById(R.id.cmdFolderCancel);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        this.grd.setNumColumns(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Utils.setCurrentActivity(this);

        try {
            if(resultCode == RESULT_OK) {
                if(requestCode == SettingsActivity.RELOAD) {
                    this.notifications = Settings.getUserSetting(this.getApplicationContext(), Settings.NOTIFICATIONS, true);
                    this.serviceIsRunning = false;
                    this.startJobIfNotRunning();
                }
                if(requestCode == IntentHelper.IMAGE_SINGLE_REQUEST) {
                    List<MediaItem> pathList = MediaPickerActivity.Companion.getMediaItemSelected(intent);
                    if(pathList != null) {
                        Bitmap bitmap = MediaHelper.createThumbNailFromUri(pathList.get(0).getUriOrigin(), this);
                        if(bitmap != null) {
                            this.cmdFolderImageSearch.setImageBitmap(bitmap);
                            if(this.currentFolder != null) {
                                this.currentFolder.cover = ConvertHelper.convertBitmapToByteArray(bitmap);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.menSettings:
                Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
                this.startActivityForResult(intent, SettingsActivity.RELOAD);
                break;
            case R.id.actionDeleteSelected:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.dialog_delete_folder_title);
                alertDialogBuilder.setMessage(R.string.dialog_delete_folder_content);
                alertDialogBuilder.setIcon(R.drawable.icon_delete);
                alertDialogBuilder.setNegativeButton(R.string.dialog_delete_folder_no, (dialogInterface, i) ->{});
                alertDialogBuilder.setPositiveButton(R.string.dialog_delete_folder_yes, ((dialogInterface, i) -> {
                    for(BaseDescriptionObject baseDescriptionObject : this.gridAdapter.getSelectedItems()) {
                        this.deleteItem((Folder) baseDescriptionObject.getObject());
                    }
                }));
                alertDialogBuilder.create().show();
                break;
            case R.id.actionChangeSelected:
                SearchDialog changeDialog = SearchDialog.newInstance();
                changeDialog.setDismissListener((type, value) -> {
                    List<Folder> folders = new LinkedList<>();
                    for(BaseDescriptionObject baseDescriptionObject : this.gridAdapter.getSelectedItems()) {
                        Folder folder = (Folder) baseDescriptionObject.getObject();
                        if (type.equals(this.getString(R.string.tags))) {
                            folder.tags = folder.tags.trim() + ", " + value;
                        } else {
                            folder.category = value;
                        }
                        folders.add(folder);
                    }
                    InsertFolderTask insertImageTask = new InsertFolderTask(this, this.notifications);
                    insertImageTask.execute(folders.toArray(new Folder[]{}));
                });
                changeDialog.show(this.getSupportFragmentManager(), "change_dialog");
                break;
            case R.id.actionSearch:
                SearchDialog searchDialog = SearchDialog.newInstance(this.getString(R.string.search_folders));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        this.initPermissions();
    }

    private void initPermissions() {
        Activity act = MainActivity.this;
        try {
            int grant = PackageManager.PERMISSION_GRANTED;

            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            if(info.requestedPermissions != null) {

                List<String> permissions = new LinkedList<>();
                boolean grantState = true;
                for(String perm : info.requestedPermissions) {
                    permissions.add(perm);
                    if(ContextCompat.checkSelfPermission(act, perm) != grant) {
                        grantState = false;
                    }
                }
                if(!grantState) {
                    ActivityCompat.requestPermissions(act, permissions.toArray(new String[]{}), 99);
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, act);
        }
    }
}