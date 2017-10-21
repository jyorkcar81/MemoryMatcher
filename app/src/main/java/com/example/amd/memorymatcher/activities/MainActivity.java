package com.example.amd.memorymatcher.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import android.widget.Toast;

import com.example.amd.memorymatcher.R;
import com.example.amd.memorymatcher.fragments.AboutFragment;
import com.example.amd.memorymatcher.fragments.GameFragment;
import com.example.amd.memorymatcher.fragments.TutorialFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private String title = "";

    //private static final int MAIN_FRAGMENT      =   0;
    private static final int GAME_FRAGMENT      =   0;
    private static final int ABOUT_FRAGMENT     =   1;
    private static final int TUTORIAL_FRAGMENT  =   2;

    private static int nextFragment;

    private int boardSize;//2x2, 4x4, 3x3...

    private int boardRows,
                boardColumns,
                matchType;//2 is a pair, 3, 4... whatever


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

            msg("2by2");
        }
        else if (id == R.id.nav_3by3)
        {
            title = getString(R.string.three_by_three_title);

            boardRows       =   3;
            boardColumns    =   3;

            matchType = GameFragment.MATCH_TYPE_3;

            nextFragment = GAME_FRAGMENT;

            msg("3by3");
        }
        else if (id == R.id.nav_4by4)
        {
            title = getString(R.string.four_by_four_title);

            boardRows       =   4;
            boardColumns    =   4;

            matchType = GameFragment.MATCH_TYPE_2;

            nextFragment = GAME_FRAGMENT;

            msg("4by4");
        }
        else if (id == R.id.nav_tutorial)
        {
            title = getString(R.string.tutorial_title);

            nextFragment = TUTORIAL_FRAGMENT;

            msg("Tutorial");
        }
        else if (id == R.id.nav_highscores)
        {
            title = getString(R.string.highscores_title);

            msg("High Scores");

            startActivity(new Intent(this,HighScores.class));
        }
        else if (id == R.id.nav_about)
        {
            title = getString(R.string.about_title);

            nextFragment = ABOUT_FRAGMENT;

            msg("About");
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



    private Fragment pickFragment()
    {
        Log.d("nextFragment=",nextFragment+"");
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


            default:
                return new AboutFragment();//MainFragment();
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







}
