package com.example.lucyzhao.votingapp;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.lucyzhao.votingapp.blockchain_ui.BlockchainUIActivity;
import com.example.lucyzhao.votingapp.poll_results.PollResultActivity;

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

    private static final int CONFIG = 22;
    private static final int KEY = 25;

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
        setDocNumTxt();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setDocNumTxt();
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
            //startActivityWithAnim(CandidateInfoActivity.class);
            ConfigAccessFragment.newInstance(CONFIG).show(getFragmentManager(), "configaccessfrag");
        } else if (id == R.id.nav_clear) {
            clearProgress();
        } else if (id == R.id.nav_key) {
            //startActivityWithAnim(KeyActivity.class);
            ConfigAccessFragment.newInstance(KEY).show(getFragmentManager(), "configaccessfrag");
        }
        return true;
    }

    private void startActivityWithAnim(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        finish();
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

    private void setDocNumTxt() {
        String docNumStr = Utils.getDocNum(this);
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

    public static class ConfigAccessFragment extends DialogFragment  {

        private EditText password;
        private TextView title;
        private Button okBtn;

        static ConfigAccessFragment newInstance(int activity) {
            ConfigAccessFragment f = new ConfigAccessFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("activity", activity);
            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View fragment = inflater.inflate(R.layout.fragment_config_access, container, false);
            final int activity = getArguments().getInt("activity");

            final Class toActivity;
            if(activity == CONFIG) {
                toActivity = CandidateInfoActivity.class;
            }
            else {
                toActivity = KeyActivity.class;
            }

            title = fragment.findViewById(R.id.config_access_title);
            password = fragment.findViewById(R.id.config_access_pass);
            okBtn = fragment.findViewById(R.id.config_access_btn);

            Typeface typeface = fragment.getResources().getFont(R.font.quicksand);
            title.setTypeface(typeface);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pass = password.getText().toString();
                    if(pass.equals("abc")) {
                        ((NavActivity) getActivity()).startActivityWithAnim(toActivity);
                    }
                }
            });
            return fragment;
        }

    }
}
