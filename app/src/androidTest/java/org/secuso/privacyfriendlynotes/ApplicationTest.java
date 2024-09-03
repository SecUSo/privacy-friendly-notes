package org.secuso.privacyfriendlynotes;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    @Test
    public void instrumentationTest() throws Exception {
        assertEquals("org.secuso.privacyfriendlynotes", InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }
}