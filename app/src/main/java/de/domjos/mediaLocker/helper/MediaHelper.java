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

package de.domjos.mediaLocker.helper;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Size;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.domjos.customwidgets.utils.ConvertHelper;
import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public abstract class MediaHelper {
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".png", ".gif", ".bmp", "webp");

    public static Bitmap createThumbNailFromUri(Uri uri, Context context) {
        try {
            String path = MediaHelper.getFilePath(context, uri);
            if(path != null) {
                Bitmap thumbNail;
                if(MediaHelper.isImage(uri, context)) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumbNail = ThumbnailUtils.createImageThumbnail(new File(path), new Size(400, 400), new CancellationSignal());
                        } else {
                            thumbNail = ThumbnailUtils.extractThumbnail(ConvertHelper.convertUriToBitmap(context, uri), 400, 400);
                        }
                    } catch (Exception ex) {
                        return null;
                    }
                } else {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumbNail = ThumbnailUtils.createVideoThumbnail(new File(path), new Size(400, 400), new CancellationSignal());
                        } else {
                            thumbNail = ThumbnailUtils.createVideoThumbnail(path, MINI_KIND);
                        }
                    } catch (Exception ex) {
                        return null;
                    }
                }
                return thumbNail;
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
        return null;
    }

    public static Image createImageFromUri(Uri uri, String title, String category, String tags, long folderId, Context context) {
        try {
            Image image = new Image();
            String path = MediaHelper.getFilePath(context, uri);
            if(path != null) {
                boolean isImage = MediaHelper.isImage(uri, context);
                Bitmap thumbNail = MediaHelper.createThumbNailFromUri(uri, context);
                File privateFile;
                do {
                    privateFile = new File(context.getFilesDir().getAbsolutePath() + File.separatorChar + UUID.randomUUID().toString());
                } while (!privateFile.createNewFile());
                Utils.encrypt(path, privateFile.getAbsolutePath(), false, context, true);

                if(title != null && !title.trim().isEmpty()) {
                    image.name = title.trim();
                } else {
                    image.name = MediaHelper.getFileName(context, uri);
                }
                if(category != null) {
                    image.category = category.trim();
                } else {
                    image.category = "";
                }
                if(tags != null) {
                    image.tags = tags.trim();
                } else {
                    image.tags = "";
                }
                if(thumbNail != null) {
                    image.content = ConvertHelper.convertBitmapToByteArray(thumbNail);
                }
                image.image = isImage;
                image.path = path;
                image.privatePath = privateFile.getAbsolutePath();
                image.folder_id = folderId;
                image.tmpPath = MediaHelper.saveTempPath(image);
                return image;
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String saveTempPath(Image image) throws Exception {
        File tmp = File.createTempFile(image.name, UUID.randomUUID().toString());
        try {
            while (tmp.createNewFile()) {
                tmp.delete();
                tmp = File.createTempFile(image.name, UUID.randomUUID().toString());
            }
            tmp.delete();
        } catch (Exception ignored) {}
        return tmp.getAbsolutePath();
    }

    public static void deleteImage(Image image, Context context) {
        try {
            File originalPath = new File(image.path);
            File privatePath = new File(image.privatePath);

            Utils.encrypt(privatePath.getAbsolutePath(), originalPath.getAbsolutePath(), true, context, true);
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getDecryptedContent(Image image, Context context) {
        try {
            File tmp = new File(image.tmpPath);
            if(!tmp.exists())  {
                tmp.createNewFile();
                Utils.copyOrMoveFile(image.privatePath, tmp.getAbsolutePath(), context, false);
                Utils.encrypt(image.privatePath, tmp.getAbsolutePath(), true, context, false);
            }
            new Thread(() -> {
                try {
                    LockerDatabase lockerDatabase = Utils.getDatabase(context, MainActivity.password);
                    lockerDatabase.imageDao().updateTimestamp(new java.util.Date().getTime(), image.id);
                    lockerDatabase.close();
                } catch (Exception ignored) {}
            }).start();
            return tmp;
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
        return null;
    }

    private static boolean isImage(Uri uri, Context context) {
        String path = MediaHelper.getFilePath(context, uri);
        boolean isImage = false;
        for(String extension : IMAGE_EXTENSIONS) {
            if(Objects.requireNonNull(path).trim().toLowerCase().endsWith(extension)) {
                isImage = true;
                break;
            }
        }
        return isImage;
    }

    public static String getFileName(Context context, Uri uri) {
        try {
            String result = null;
            if (Objects.requireNonNull(uri.getScheme()).equals("content")) {
                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = Objects.requireNonNull(result).lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
            return UUID.randomUUID().toString();
        }
    }

    public static String getFilePath(Context context, Uri uri) {
        try {
            String selection = null;
            String[] selectionArgs = null;
            // Uri is different in versions after KITKAT (Android 4.4), we need to
            if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    uri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("image".equals(type)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    selection = "_id=?";
                    selectionArgs = new String[]{
                            split[1]
                    };
                }
            }
            if ("content".equalsIgnoreCase(uri.getScheme())) {


                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                String[] projection = {
                        MediaStore.Images.Media.DATA
                };
                Cursor cursor;
                try {
                    String path = uri.getPath();
                    if(path != null) {
                        if(path.startsWith("/media")) {
                            path = path.replace("/media", Environment.getExternalStorageDirectory().getAbsolutePath());
                            return path;
                        }
                    }
                    cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                    int column_index = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void callBroadCast(Context context) {
        MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().toString()}, null, (s, uri) -> {});
    }
}
