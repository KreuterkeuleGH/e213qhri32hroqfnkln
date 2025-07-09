package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
