package com.example.viperssc.Home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


import com.example.viperssc.Admin.VideosUploadActivity;
import com.example.viperssc.HomeFragment;
import com.example.viperssc.R;
import com.example.viperssc.Util.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;


public class EventsAndChatsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton imageButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_and_chats);

        imageButton = findViewById(R.id.more_button);
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_navigation);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        setUpBottomNavigationView();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent MenuIntent = new Intent(EventsAndChatsActivity.this, MenuListActivity.class);
                startActivity(MenuIntent);

            }
        });

    }


    private void GoToVideos() {
        Intent videoIntent = new Intent(EventsAndChatsActivity.this, VideosUploadActivity.class);
        startActivity(videoIntent);
        finish();
    }

    private void setUpBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);

    }



    private BottomNavigationViewEx.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    switch(menuItem.getItemId()){
                        case R.id.options_home:
                            selectedFragment = new HomeFragment();
                            setSupportActionBar(mToolbar);
                            getSupportActionBar().setTitle("Home");
                            break;
                        case R.id.options_matches:
                            selectedFragment = new MatchesFragment();
                            setSupportActionBar(mToolbar);
                            getSupportActionBar().setTitle("Matches");
                            break;
                        case R.id.options_videos:
                            selectedFragment = new VideosFragment();
                            setSupportActionBar(mToolbar);
                            getSupportActionBar().setTitle("Videos");
                            break;
                        case R.id.options_teams:
                            selectedFragment = new TeamsFragment();
                            setSupportActionBar(mToolbar);
                            getSupportActionBar().setTitle("Teams");
                            break;
                        case R.id.options_news:
                            selectedFragment = new NewsFragment();
                            setSupportActionBar(mToolbar);
                            getSupportActionBar().setTitle("News");
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
                    return true;
                }
            };



}
