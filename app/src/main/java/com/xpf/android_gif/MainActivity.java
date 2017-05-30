package com.xpf.android_gif;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView ivGif = (ImageView) findViewById(R.id.ivGif);
        TextView tvLoadGif = (TextView) findViewById(R.id.tvLoadGif);
        tvLoadGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(MainActivity.this).load(R.drawable.gif1)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(ivGif);
            }
        });
    }
}
