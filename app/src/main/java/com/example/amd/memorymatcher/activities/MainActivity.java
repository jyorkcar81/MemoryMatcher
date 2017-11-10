package com.example.amd.memorymatcher.activities;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;

import android.widget.Button;
import android.widget.Toast;

import com.example.amd.memorymatcher.R;
import com.example.amd.memorymatcher.fragments.AboutFragment;
import com.example.amd.memorymatcher.fragments.GameFragment;
import com.example.amd.memorymatcher.fragments.HighScoresFragment;
import com.example.amd.memorymatcher.fragments.TutorialFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    public static boolean IS_LARGE_SCREEN_DEVICE;//Determine if small screen or large screen is used.

    private String title = "";

    private static final int GAME_FRAGMENT      =   0;
    private static final int ABOUT_FRAGMENT     =   1;
    private static final int TUTORIAL_FRAGMENT  =   2;
    private static final int HIGH_SCORES_FRAGMENT = 3;

    private static int nextFragment;

    private int boardSize;//2x2, 4x4, 3x3...

    private int boardRows,
                boardColumns,
                matchType;//2 is a pair, 3, 4... whatever



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button but = (Button)findViewById(R.id.buttonPlay);

        but.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
       fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/



        //Show/hide the menuItem for 8x4 game depending on screen size used.
        Configuration config = getResources().getConfiguration();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem item4by8 = menu.findItem(R.id.nav_4by8);
        MenuItem item6by6 = menu.findItem(R.id.nav_6by6);
        MenuItem item4by5 = menu.findItem(R.id.nav_4by5);

        //Check for 7" screen devices.  This will allow the UI to adjust to these larger screens else "phone-sized" layouts will be used.
        if (config.smallestScreenWidthDp >= 600)
        {
            IS_LARGE_SCREEN_DEVICE = true;

            item4by8.setVisible(false);//Disable this option until resolved.  Possibly temporarily lock orientation.
            item6by6.setVisible(true);
            item4by5.setVisible(true);
        }
        else
        {
            IS_LARGE_SCREEN_DEVICE = false;

            item4by8.setVisible(false);
            item6by6.setVisible(false);
            item4by5.setVisible(false);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(this);
        msg("Activity onCreate");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return true;

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        //return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_2by2)
        {
            title = getString(R.string.two_by_two_title);

            boardRows       =   2;
            boardColumns    =   2;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;


        }
        else if (id == R.id.nav_3by3)
        {
            title = getString(R.string.three_by_three_title);

            boardRows       =   3;
            boardColumns    =   3;

            matchType = GameFragment.MATCH_TYPE_3;

            nextFragment = GAME_FRAGMENT;


        }
        else if (id == R.id.nav_4by4)
        {
            title = getString(R.string.four_by_four_title);

            boardRows       =   4;
            boardColumns    =   4;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;

        }
        else if (id == R.id.nav_4by5)//fits small screen landscape orientations.
        {
            title = getString(R.string.four_by_four_title);

            boardRows       =   4;
            boardColumns    =   5;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;

        }
        else if (id == R.id.nav_4by8)
        {
            title = getString(R.string.four_by_four_title);

            boardRows       =   4;
            boardColumns    =   8;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;

        }
        else if (id == R.id.nav_6by6)
        {
            title = getString(R.string.four_by_four_title);

            boardRows       =   6;
            boardColumns    =   6;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;

        }
        else if (id == R.id.nav_tutorial)
        {
            title = getString(R.string.tutorial_title);

            nextFragment = TUTORIAL_FRAGMENT;


        }
        else if (id == R.id.nav_highscores)
        {
            title = getString(R.string.highscores_title);

            nextFragment = HIGH_SCORES_FRAGMENT;




           // startActivity(new Intent(this,HighScores.class));
        }
        else if (id == R.id.nav_about)
        {
            title = getString(R.string.about_title);

            nextFragment = ABOUT_FRAGMENT;


        }
        else
        {
            title = getString(R.string.app_name);
        }

        setTitle(title);

        //Replace fragment.
        updateFragment().start();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick(View v)
    {
        title = getString(R.string.two_by_two_title);

        boardRows       =   2;
        boardColumns    =   2;

        matchType = GameFragment.MATCH_TYPE_2;

        nextFragment = GAME_FRAGMENT;

        setTitle(title);

        //Replace fragment.
        updateFragment().start();
    }


    private Fragment pickFragment()
    {
        //Log.d("nextFragment=",nextFragment+"");
        switch (nextFragment)
        {
            case GAME_FRAGMENT:

                Bundle bundle = new Bundle();

                bundle.putInt("boardRows", boardRows);
                bundle.putInt("boardColumns", boardColumns);
                bundle.putInt("matchType", matchType);

                GameFragment gf = new GameFragment();
                gf.setArguments(bundle);

                return gf;

            case ABOUT_FRAGMENT:
                return new AboutFragment();

            case TUTORIAL_FRAGMENT:
                return new TutorialFragment();

            case HIGH_SCORES_FRAGMENT:
                return new HighScoresFragment();

            default:
                return new AboutFragment();
        }
    }

    private Thread updateFragment()
    {
        return new Thread() {

            //Fragment fragment = null;

            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = pickFragment();
                Log.d("frag",fragment+"");
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.framelayout, fragment).commit();
            }
        };

    }

    //Changes title in ActionBar to reflect currently used fragment.
    private void setTitle(String s)
    {
        try
        {
            //Set title of corresponding fragment in actionBar.
            getSupportActionBar().setTitle(s);
        }
        catch(Exception e)
        {

        }
    }


    public void msg(String m)
    {
        Toast.makeText(getApplicationContext(),m,Toast.LENGTH_SHORT).show();
    }



/*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }




            int currentOrientation = getResources().getConfiguration().orientation;
        // Checks the orientation of the screen
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
*/

}
