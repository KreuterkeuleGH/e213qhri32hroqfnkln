package com.example.myapplication.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.app.di.AppComponent;
import javax.inject.Inject;

import com.example.myapplication.app.MyApplication;
import com.example.myapplication.app.di.AppComponent;

import com.example.myapplication.music.ui.MusicPlayerActivity;
import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    private AppComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1) Dagger-Graph vom Application-Singleton holen
        component = MyApplication.getAppComponent();

        // 2) Injection durchführen
        component.inject(this);

        // 3) Layout setzen
        setContentView(R.layout.activity_main);

        // Richte die Window Insets (Edge-to-Edge) ein
        initWindowInsets();

        // Initialisiere die Navigation (Button-Klick)
        initNavigation();
    }

    /**
     * Initialisiert die Nutzung von WindowInsets, sodass der Padding-Wert des Hauptcontainers
     * an die Systemleisten angepasst wird.
     */
    private void initWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
    }

    /**
     * Legt fest, welche Activity beim Klick auf den Musik-Button gestartet wird.
     */
    private void initNavigation() {
        Button buttonToMusic = findViewById(R.id.music_button);
        buttonToMusic.setOnClickListener(v -> {
            Intent openMusik = new Intent(this, MusicPlayerActivity.class);
            startActivity(openMusik);
        });
    }
}
