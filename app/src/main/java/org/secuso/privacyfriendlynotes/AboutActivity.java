package org.secuso.privacyfriendlynotes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView)findViewById(R.id.about_secuso_website)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.about_github_url)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}
