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

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.customwidgets.model.BaseDescriptionObject;
import de.domjos.customwidgets.model.tasks.AbstractTask;
import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.helper.MediaHelper;
import de.domjos.mediaLocker.helper.Settings;
import de.domjos.mediaLocker.helper.Utils;
import de.domjos.mediaLocker.model.entities.Image;
import de.domjos.mediaLocker.tasks.images.ReloadImageTask;

public final class ViewerActivity extends AbstractActivity {
    static final String FOLDER_ID = "folder_id";
    static final String IMAGE_ID = "image_id";

    private List<Image> images;
    private Image currentImage;

    private ImageButton cmdMediaBack, cmdMediaNext;
    private ImageView ivMedia;
    private VideoView vvMedia;
    private TextView lblLoading;
    private boolean fullScreen = false;

    public ViewerActivity() {
        super(R.layout.viewer_activity);
    }

    @Override
    protected void initActions() {

        this.ivMedia.setOnLongClickListener(view -> {
            if(this.fullScreen) {
                this.showSystemUI();
            } else {
                this.hideSystemUI();
            }
            return true;
        });

        this.vvMedia.setOnLongClickListener(view -> {
            if(this.fullScreen) {
                this.showSystemUI();
            } else {
                this.hideSystemUI();
            }
            return true;
        });

        this.cmdMediaBack.setOnClickListener(event -> {
            if(this.images.size() != 0) {
                for(int i = 1; i<=this.images.size()-1; i++) {
                    if(this.images.get(i).id == this.currentImage.id) {
                        this.currentImage = this.images.get(i - 1);
                        this.setData();
                        break;
                    }
                }
            }
        });

        this.cmdMediaNext.setOnClickListener(event -> {
            if(this.images.size() > 1) {
                for(int i = 0; i<=this.images.size()-2; i++) {
                    if(this.images.get(i).id == this.currentImage.id) {
                        this.currentImage = this.images.get(i + 1);
                        this.setData();
                        break;
                    }
                }
            }
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        this.fullScreen = true;
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        this.fullScreen = false;
    }


    @Override
    protected void initControls() {
        Utils.setCurrentActivity(this);

        this.getDataFromParent();

        this.cmdMediaBack = this.findViewById(R.id.cmdMediaBack);
        this.cmdMediaNext = this.findViewById(R.id.cmdMediaNext);
        this.ivMedia = this.findViewById(R.id.ivMedia);
        this.vvMedia = this.findViewById(R.id.vvMedia);
        this.lblLoading = this.findViewById(R.id.lblLoading);

        this.setData();
    }

    private void getDataFromParent() {
        try {
            boolean not = Settings.getUserSetting(this, Settings.NOTIFICATIONS, true);

            long folderId = 0, imageId = 0;
            if(this.getIntent() != null) {
                folderId = this.getIntent().getLongExtra(ViewerActivity.FOLDER_ID, 0);
                imageId = this.getIntent().getLongExtra(ViewerActivity.IMAGE_ID, 0);
            }

            if(folderId != 0) {
                ReloadImageTask searchFolderTask = new ReloadImageTask(this, not, folderId);
                List<BaseDescriptionObject> folders = searchFolderTask.execute().get();
                if(folders != null) {
                    if(folders.size() != 0) {
                        this.images = new LinkedList<>();
                        for(BaseDescriptionObject baseDescriptionObject : folders) {
                            this.images.add((Image) baseDescriptionObject.getObject());
                        }
                    }
                }
                if(imageId != 0) {
                    ReloadImageTask imageTask = new ReloadImageTask(this, not, folderId);
                    List<BaseDescriptionObject> images = imageTask.execute().get();
                    for(BaseDescriptionObject baseDescriptionObject : images) {
                        if(baseDescriptionObject.getId() == imageId) {
                            this.currentImage = (Image) baseDescriptionObject.getObject();
                            break;
                        }
                    }
                } else {
                    if(this.images != null) {
                        if(this.images.size() != 0) {
                            this.currentImage = this.images.get(0);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
        }
    }

    private void setData() {
       try {
           this.vvMedia.setVisibility(View.VISIBLE);
           this.ivMedia.setVisibility(View.VISIBLE);

           if(this.currentImage != null) {
               this.setTitle(this.currentImage.name);
               DecryptTask decryptTask = new DecryptTask(this, this.currentImage);
               decryptTask.before(() -> this.lblLoading.setVisibility(View.VISIBLE));
               decryptTask.after((AbstractTask.PostExecuteListener<File>) file -> {
                   try {
                       if(file != null) {
                           if(this.currentImage.image) {
                               FileInputStream fileInputStream = new FileInputStream(file);
                               this.vvMedia.stopPlayback();
                               this.vvMedia.setVisibility(View.GONE);
                               this.ivMedia.setImageBitmap(ConvertHelper.convertByteArrayToBitmap(ConvertHelper.convertStreamToByteArray(fileInputStream)));
                               fileInputStream.close();
                           } else {
                               this.ivMedia.setVisibility(View.GONE);
                               this.vvMedia.setVideoPath(file.getAbsolutePath());
                               MediaController mediaController = new MediaController(this);
                               mediaController.setAnchorView(this.vvMedia);
                               this.vvMedia.setMediaController(mediaController);
                               if(Settings.getUserSetting(this.getApplicationContext(), Settings.START_VIDEOS, true)) {
                                   this.vvMedia.start();
                               }
                           }
                       }
                   } catch (Exception ex) {
                       MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
                   } finally {
                       this.lblLoading.setVisibility(View.GONE);
                   }
               });
               decryptTask.execute();
           } else {
               this.vvMedia.setVisibility(View.GONE);
               this.ivMedia.setVisibility(View.GONE);
           }
       } catch (Exception ex) {
           MessageHelper.printException(ex, R.mipmap.ic_launcher_round, this);
       }
    }

    private static class DecryptTask extends AbstractTask<Void, Void, File> {
        private Image image;

        public DecryptTask(Activity activity, Image image) {
            super(activity, R.string.loading, R.string.loading, false, R.mipmap.ic_launcher_round);
            this.image = image;
        }

        @Override
        protected File doInBackground(Void... voids) {
            return MediaHelper.getDecryptedContent(this.image, this.getContext());
        }
    }
}