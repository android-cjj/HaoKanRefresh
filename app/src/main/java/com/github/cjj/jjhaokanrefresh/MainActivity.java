package com.github.cjj.jjhaokanrefresh;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.github.cjj.library.HaoKanRefreshLayout;
import com.github.cjj.library.listener.OnRefreshListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final HaoKanRefreshLayout haoKanRefreshLayout = (HaoKanRefreshLayout) findViewById(R.id.haokan_refresh);
        haoKanRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        haoKanRefreshLayout.setRefreshing(false);
                    }
                },3000);
            }
        });

        haoKanRefreshLayout.setRefreshing(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              haoKanRefreshLayout.setRefreshing(false);
            }
        },3000);

        haoKanRefreshLayout.setWaveBackgroundColor(Color.parseColor("#3F51B5"));


    }
}
