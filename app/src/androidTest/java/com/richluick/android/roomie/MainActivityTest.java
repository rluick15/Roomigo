package com.richluick.android.roomie;

import android.app.Instrumentation;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.richluick.android.roomie.R;
import com.richluick.android.roomie.ui.activities.ChatActivity;
import com.richluick.android.roomie.ui.activities.MainActivity;
import com.richluick.android.roomie.ui.activities.SearchActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    @SmallTest
    public void searchButtonTest()  {
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(SearchActivity.class.getName(), null, true);
        onView(ViewMatchers.withId(R.id.searchButton)).perform(click());
        assertEquals(1, am.getHits());
    }

    @SmallTest
    public void chatsButtonTest()  {
        Instrumentation.ActivityMonitor am = getInstrumentation().addMonitor(ChatActivity.class.getName(), null, true);
        onView(ViewMatchers.withId(R.id.chatButton)).perform(click());
        assertEquals(1, am.getHits());
    }
}