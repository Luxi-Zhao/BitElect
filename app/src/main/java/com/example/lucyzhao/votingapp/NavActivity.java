package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Base activity that provides the Navigation Drawer
 * Activities extending this layout should call
 * super.onCreateDrawer(R.layout.activity_layout) instead of setContentView
 */
public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    TextView docNum, docNumTag;
    private static final int STR_LEN = 4;

    protected void onCreateDrawer(@LayoutRes int layoutResID) {
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer_layout);
        FrameLayout content = findViewById(R.id.nav_content_frame);
        getLayoutInflater().inflate(layoutResID, content, true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        docNum = navigationView.getHeaderView(0).findViewById(R.id.nav_doc_number);
        docNumTag = navigationView.getHeaderView(0).findViewById(R.id.nav_doc_number_tag);
        setTypeFace();

        String docNumStr = getDocNum();
        if (docNumStr.equals("")) {
            docNumTag.setText(R.string.navigation_drawer_docnum_none);
            docNum.setText("");
        } else {
            docNumTag.setText(R.string.navigation_drawer_tag);
            String sub = docNumStr.substring(0, STR_LEN);
            sub += "...";
            docNum.setText(sub);
        }

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
            startActivityWithAnim(MainActivity.class);
        } else if (id == R.id.nav_poll_result) {
            startActivityWithAnim(PollResultActivity.class);
        } else if (id == R.id.nav_blockchain) {
            startActivityWithAnim(BlockchainUIActivity.class);
        } else if (id == R.id.nav_config) {
            startActivityWithAnim(CandidateInfoActivity.class);
        } else if (id == R.id.nav_clear) {
            clearProgress();
        }
        return true;
    }

    private void startActivityWithAnim(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        finish();
    }

    private String getDocNum() {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(getString(R.string.shared_pref_doc_num), "");
    }

    private void clearProgress() {
        Utils.savePassportInfoToPref(getApplicationContext(), "", "", "");
        MainActivity.setTasksCompleted(0);
        MainActivity.setVoteCompleted(0);

        docNum.setText("");
        docNumTag.setText("");
    }

    private void setTypeFace() {
        Typeface typeface = getResources().getFont(R.font.quicksand);
        docNumTag.setTypeface(typeface);
        docNum.setTypeface(typeface);
    }

}
