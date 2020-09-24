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

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import de.domjos.mediaLocker.TestUtils;
import de.domjos.mediaLocker.activities.MainActivity;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UtilsTest {
    private Context context;

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void initClass() {
        this.context = TestUtils.getContext();
    }

    @Test
    public void testEncryption() throws Exception {
        String try1 = this.encryptWithPassword("12345678");
        String try2 = this.encryptWithPassword("90123456");

        assertThat(try1, is(not(try2)));
    }

    private String encryptWithPassword(String password) throws Exception {
        MainActivity.password = password;
        String file_content = "This is a Test!";

        File originalFile = this.createTemp();
        assertThat(originalFile.exists(), is(true));

        this.writeToFile(originalFile, file_content);
        String content = this.readFromFile(originalFile);
        assertThat(content, is(file_content));

        File encryptedFile = this.createTemp();
        assertThat(encryptedFile.exists(), is(true));

        Utils.encrypt(originalFile.getAbsolutePath(), encryptedFile.getAbsolutePath(), false, this.context, false);
        String encrypted_content = this.readFromFile(encryptedFile);
        assertThat(content, is(not(encrypted_content)));

        File decryptedFile = this.createTemp();
        assertThat(decryptedFile.exists(), is(true));

        Utils.encrypt(encryptedFile.getAbsolutePath(), decryptedFile.getAbsolutePath(), true, this.context, false);
        String decrypted_content = this.readFromFile(decryptedFile);
        assertThat(content, is(decrypted_content));

        assertThat(encryptedFile.delete(), is(true));
        assertThat(decryptedFile.delete(), is(true));
        assertThat(originalFile.delete(), is(true));
        MainActivity.password = "";
        return encrypted_content;
    }

    private File createTemp() throws Exception {
        return File.createTempFile("test", String.valueOf(UUID.randomUUID().getMostSignificantBits()));
    }

    private void writeToFile(File file, String content) throws Exception {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
        outputStreamWriter.write(content);
        outputStreamWriter.flush();
        outputStreamWriter.close();
    }

    private String readFromFile(File file) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        bufferedReader.close();
        return (sb.toString() + ")))").replace("\n)))","");
    }
}
