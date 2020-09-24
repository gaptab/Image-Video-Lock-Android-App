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

import android.app.Activity;
import android.content.Context;
import androidx.room.Room;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.domjos.customwidgets.utils.MessageHelper;
import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.activities.MainActivity;
import de.domjos.mediaLocker.activities.ViewerActivity;
import de.domjos.mediaLocker.model.LockerDatabase;
import de.domjos.mediaLocker.model.entities.Image;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class Utils {
    private static final String ALGORITHM = "AES";
    private static final byte[] SALT = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };

    public static LockerDatabase getDatabase(Context context, String password) {
        return Room.databaseBuilder(context, LockerDatabase.class, "lockerDatabase").openHelperFactory(new SupportFactory(SQLiteDatabase.getBytes(password.toCharArray()))).build();
    }

    public static void setCurrentActivity(Activity activity) {
        Settings.putSetting(activity, Settings.CURRENT_ACTIVITY, activity.getClass().getName());
    }

    public static boolean isViewerActivity(Context context) {
        String current = Settings.getSetting(context, Settings.CURRENT_ACTIVITY, MainActivity.class.getName());
        return current.equals(ViewerActivity.class.getName());
    }

    public static void createIfNoneExists(Image image, Context context) {
        try {
            File originalFile = new File(image.path);
            File privateFile = new File(image.privatePath);

            if(!originalFile.exists()) {
                originalFile.createNewFile();
            }
            Utils.encrypt(privateFile.getAbsolutePath(), originalFile.getAbsolutePath(), true, context, true);
        } catch (Exception ignored) {}
    }

    public static void encrypt(String path, String target, boolean decryptMode, Context context, boolean delete) {
        try {
            File targetFile = new File(target);
            if(!targetFile.exists()) {
                targetFile.createNewFile();
            }

            if(decryptMode) {
                Utils.doCrypto(Cipher.DECRYPT_MODE, path, target, context);
            } else {
                Utils.doCrypto(Cipher.ENCRYPT_MODE, path, target, context);
            }
            if(delete) {
                new File(path).delete();
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
    }

    private static void doCrypto(int cipherMode, String inputFile, String outputFile, Context context) {
        File input = new File(inputFile);
        File output = new File(outputFile);
        try {
            if(input.exists()) {
                if(!output.exists()) {
                    output.createNewFile();
                }

                Key secretKey = new SecretKeySpec((MainActivity.password + MainActivity.password).getBytes(), ALGORITHM);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(cipherMode, secretKey, new IvParameterSpec(SALT));

                InputStream inputStream;
                OutputStream outputStream;
                if(cipherMode == Cipher.DECRYPT_MODE) {
                    outputStream = new CipherOutputStream(new FileOutputStream(output), cipher);
                    inputStream = new FileInputStream(input);
                } else {
                    outputStream = new FileOutputStream(output);
                    inputStream = new CipherInputStream(new FileInputStream(input), cipher);
                }

                Utils.doCopy(inputStream, outputStream, context);
                MediaHelper.callBroadCast(context);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        }
    }

    private static void doCopy(InputStream is, OutputStream os, Context context) {
        try {
            byte[] bytes = new byte[2048];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                os.write(bytes, 0, numBytes);
            }
        } catch (Exception ex) {
            MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        } finally {
            try {
                if(is != null) {
                   is.close();
                }
                if(os != null) {
                    os.flush();
                    os.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public static void copyOrMoveFile(String inputPath, String outputPath, Context context, boolean move) {
        InputStream in = null;
        OutputStream out = null;
        try {

            File dir = new File(Objects.requireNonNull(new File(outputPath).getParent()));
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            if(move) {
                new File(inputPath).delete();
            }
        } catch (Exception ex) {
           MessageHelper.printException(ex, R.mipmap.ic_launcher_round, context);
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
                if(out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception ignored) {}
        }
    }
}
