package com.example.myapplication;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.meteor.activity.WakeTestActivity;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void aa() throws JSONException {
        WakeTestActivity.aa("TEST");
    }
}