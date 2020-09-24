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

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.domjos.mediaLocker.R;
import de.domjos.mediaLocker.TestUtils;
import de.domjos.mediaLocker.helper.Settings;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    private Context context;

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public void before() {
        this.context = TestUtils.getContext();
    }

    @Test
    public void testPasswordDialogRepeat() {
        MainActivity.password = "";
        Settings.putSetting(this.context, Settings.FIRST_START, true);

        String min_pwd = "1234";
        String pwd = "12345678";

        onView(withId(R.id.txtPwd)).perform(typeText(min_pwd));
        onView(withId(R.id.cmdPwdOk)).perform(click());
        onView(withId(R.id.txtPwd)).check(matches(hasErrorText(this.context.getString(R.string.dialog_pwd_validation_max))));

        onView(withId(R.id.txtPwd)).perform(clearText());
        onView(withId(R.id.txtPwd)).perform(typeText(pwd));
        onView(withId(R.id.cmdPwdOk)).perform(click());
        onView(withId(R.id.txtPwd)).check(matches(hasErrorText(this.context.getString(R.string.dialog_pwd_validation_same))));

        onView(withId(R.id.txtPwd)).perform(clearText());
        onView(withId(R.id.txtPwd)).perform(typeText(pwd));
        onView(withId(R.id.txtPwdRepeat)).perform(typeText(pwd));
        onView(withId(R.id.cmdPwdOk)).perform(click());

        assertThat(MainActivity.password, is(pwd));
    }

    @Test
    public void testPasswordDialog() {
        MainActivity.password = "12345678";
        Settings.putSetting(this.context, Settings.FIRST_START, false);

        String min_pwd = "1234";
        String pwd = "56789123";

        onView(withId(R.id.txtPwd)).perform(typeText(min_pwd));
        onView(withId(R.id.cmdPwdOk)).perform(click());
        onView(withId(R.id.txtPwd)).check(matches(hasErrorText(this.context.getString(R.string.dialog_pwd_validation_max))));

        onView(withId(R.id.txtPwd)).perform(clearText());
        onView(withId(R.id.txtPwd)).perform(typeText(pwd));
        onView(withId(R.id.cmdPwdOk)).perform(click());
        onView(withId(R.id.txtPwd)).check(matches(hasErrorText(this.context.getString(R.string.dialog_pwd_validation_wrong))));

        onView(withId(R.id.txtPwd)).perform(clearText());
        onView(withId(R.id.txtPwd)).perform(typeText(MainActivity.password));
        onView(withId(R.id.cmdPwdOk)).perform(click());

        assertThat(MainActivity.password, is(pwd));
    }
}
