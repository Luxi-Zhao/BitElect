package com.example.lucyzhao.votingapp;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.bytedeco.javacv.Frame;

/**
 * Base activity that provides the Navigation Drawer
 * Activities extending this layout should call
 * super.onCreateDrawer(R.layout.activity_layout) instead of setContentView
 */
public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    protected void onCreateDrawer(@LayoutRes int layoutResID) {
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer_layout);
        FrameLayout content = findViewById(R.id.nav_content_frame);
        getLayoutInflater().inflate(layoutResID, content, true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_vote) {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            finish();
        } else if (id == R.id.nav_poll_result) {
            startActivity(new Intent(this, PollResultActivity.class));
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            finish();
        } else if (id == R.id.nav_blockchain) {

        } else if (id == R.id.nav_config) {
            startActivity(new Intent(this, CandidateInfoActivity.class));
            overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
            finish();
        }
        return true;
    }

}
