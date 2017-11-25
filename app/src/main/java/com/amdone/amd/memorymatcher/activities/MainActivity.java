package com.amdone.amd.memorymatcher.activities;

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
import android.widget.Button;

import com.amdone.amd.memorymatcher.R;
import com.amdone.amd.memorymatcher.fragments.TutorialFragment;
import com.amdone.amd.memorymatcher.fragments.AboutFragment;
import com.amdone.amd.memorymatcher.fragments.GameFragment;
import com.amdone.amd.memorymatcher.fragments.HighScoresFragment;

/*  ************************************************************************************************
    * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE *
 *  ************************************************************************************************
 *
 *  Copyright 2017 by Jed York.  Copyrighted material cannot be used without express written consent.
 *  Unlawful reproduction of material forfeits all earned moneys.  If lawsuit is sought for forfeiture of damages,
 *  damages will be doubled and any and all of your rights are waived.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    public static boolean IS_LARGE_SCREEN_DEVICE;//Determine if small screen or large screen is used.

    private String title = "";

    private static final int GAME_FRAGMENT          =   0;
    private static final int ABOUT_FRAGMENT         =   1;
    private static final int TUTORIAL_FRAGMENT      =   2;
    private static final int HIGH_SCORES_FRAGMENT   =   3;

    private static int nextFragment;

    private int boardRows,
                boardColumns,
                matchType;//2 is a pair, 3, 4... whatever

    private Button  butPlay,
                    butTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butPlay     = (Button)findViewById(R.id.buttonPlay);
        butTutorial = (Button)findViewById(R.id.buttonTutorial);

        butPlay.setOnClickListener(this);
        butTutorial.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if(v.getId() == butPlay.getId())
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
        else if(v.getId() == butTutorial.getId())
        {
            title = getString(R.string.tutorial_title);

            nextFragment = TUTORIAL_FRAGMENT;

            setTitle(title);

            //Replace fragment.
            updateFragment().start();
        }
        else
        {
            //Do nothing.  Should be unreachable.
        }

    }


    private Fragment pickFragment()
    {

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
        return new Thread()
        {

            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = pickFragment();

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
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

}
