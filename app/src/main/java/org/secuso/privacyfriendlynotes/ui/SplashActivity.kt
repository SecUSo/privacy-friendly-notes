package org.secuso.privacyfriendlynotes.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.secuso.privacyfriendlynotes.ui.helper.FirstLaunchManager
import org.secuso.privacyfriendlynotes.ui.main.MainActivity

/**
 * Activity that appears while the program is launching
 * Created by yonjuni on 22.10.16.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainIntent = if (FirstLaunchManager(this).isFirstTimeLaunch) {
            Intent(this, TutorialActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(mainIntent)
        finish()
    }
}