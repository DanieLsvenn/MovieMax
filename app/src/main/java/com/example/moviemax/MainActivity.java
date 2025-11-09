package com.example.moviemax;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvInfo = findViewById(R.id.tvInfo);

        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");

        tvInfo.setText("🎬 Welcome " + fullName + "\n📧 " + email);
    }
}
