package com.example.amd.memorymatcher.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amd.memorymatcher.R;
import com.example.amd.memorymatcher.other.Board;
import com.example.amd.memorymatcher.other.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.lang.reflect.Field;
import android.view.ViewGroup.LayoutParams;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int NUMBER_OF_CARD_BACKS   = 5;
    private static final int NUMBER_OF_BACKGROUNDS  = 9;

    public static final int MATCH_TYPE_2 = 2;
    public static final int MATCH_TYPE_3 = 3;

    private static final long CARD_FLIP_DELAY = 1000L;//wait this long before flipping over a card (in milliseconds).  i.e.  changing its image.

    private int matchType;//Match 2 or Match 3 at a time.

    private Board board;
    private ArrayList<Card> cards;

    private GameOverDialogFragment gameOverDialogFragment;
    private GoDialogFragment goDialogFragment;

    private int cardBack,
                boardSize;

    private GridLayout grid;

    private ArrayList drawableIds;

    private int gridSize,//Number of cells in the grid.
                boardRows,//Number of rows in the boardgame.
                boardColumns;//Number of columns in the boardgame.

    private long startTime,
                endTime,
                millisecondsTime,
                secondsTime,
                minutesTime,
                updateTime;

    private Card    firstCard,
                    secondCard,
                    thirdCard;


    private int totalMatchesAvailable;

    private boolean isLargeScreenDevice;

    private Handler timerHandler;

    private static  int score;

    private int matchCount;//Total matches made.

    private boolean threadBusy;

    private TextView    tvScore,
                        tvTime,
                        tvMatchCount;

    private boolean toggleTimer;

    private Button restartButton;

    private OnFragmentInteractionListener mListener;

    private MediaPlayer soundTap1,
                        soundTap2,
                        soundMatch,
                        soundMusic1,
                        soundWin1;

    private SharedPreferences saved;//Retain certain values to preserve game-state when orientation of device changes.
    private SharedPreferences.Editor editor;

    public GameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameFragment newInstance(String param1, String param2) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            boardRows = getArguments().getInt("boardRows");
            boardColumns = getArguments().getInt("boardColumns");
            matchType = getArguments().getInt("matchType");

        }


        //msg("board dimensions: "+boardRows+"x"+boardColumns);
        //msg("matchType: "+matchType);

        soundTap1   = MediaPlayer.create(getActivity(),R.raw.c1);
        soundTap2   = MediaPlayer.create(getActivity(),R.raw.c3);
        soundMatch  = MediaPlayer.create(getActivity(),R.raw.c3);
        soundMusic1 = MediaPlayer.create(getActivity(),R.raw.m1);
        soundWin1   = MediaPlayer.create(getActivity(),R.raw.w1);



       playSound(soundMusic1);

        board = new Board(boardRows,boardColumns,matchType);
        boardSize = board.getNumOfCards();
        gridSize = board.getNumOfCards();
        firstCard   = null;
        secondCard  = null;
        thirdCard   = null;
        threadBusy = false;

        score = 0;
        matchCount = 0;

        totalMatchesAvailable = board.getTotalMatches();

        timerHandler = new Handler();

        toggleTimer = true;

        drawableIds = getAllDrawableId();









        cardBack = getCardBackImage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
Toast.makeText(inflater.getContext(),"View created",Toast.LENGTH_LONG).show();
        int idOfImageButton,
            idOfPic;

        ImageButton button,
                    temp;



        // Inflate the layout for this fragment.  i.e. get xml to dynamically add buttons to build the ui
        View v = null;

        Configuration config = getResources().getConfiguration();

        if (config.smallestScreenWidthDp >= 600) //7" tablets or larger screens
        {
            isLargeScreenDevice = true;
            v = inflater.inflate(R.layout.fragment_game_large_screen, container, false);

        }
        else
        {
            isLargeScreenDevice = false;
            v = inflater.inflate(R.layout.fragment_game, container, false);

        }


        grid = (GridLayout)v.findViewById(R.id.twoByTwoGridLayout);

        tvScore         = (TextView)v.findViewById(R.id.textViewScore);
        tvTime          = (TextView)v.findViewById(R.id.textViewTime);
        tvMatchCount    = (TextView)v.findViewById(R.id.textViewMatchCount);

        restartButton   = (Button)v.findViewById(R.id.buttonRestart);

        updateStats();

        //Dynamically create a grid based upon boardSize*boardSize  e.g.  2x2, 4x4... always square
        grid.setColumnCount(boardColumns);
        grid.setRowCount(boardRows);


        /*
                    Cards ---> Grid cells
                    1,2,3,4 --- >  1,2,3,4
                    shuffle cards
                    4,2,1,3 ----> 1,2,3,4

        */

        Context context = inflater.getContext();
        ViewGroup.LayoutParams size = new ViewGroup.LayoutParams(0,0);

        for(int i=0; i < gridSize; i++)
        {
            temp = new ImageButton(context);
            temp.setScaleType(ImageView.ScaleType.FIT_XY);


            int landPortrait = config.orientation;

            if(isLargeScreenDevice)
            {
                if(landPortrait == Configuration.ORIENTATION_PORTRAIT)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);

                    /* only issue here is 4x8 does not fit */
                }
                else if (landPortrait == Configuration.ORIENTATION_LANDSCAPE)
                {
                    if (boardRows == 6 && boardColumns == 6)
                    {
                        size.height = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                        size.width = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                    }
                    else
                    {
                        //default values.
                        size.height = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                        size.width = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                    }
                }
                else if (landPortrait == Configuration.ORIENTATION_UNDEFINED)
                {
                    //untested orientation.
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                }

  /*              //Adjust size of cards based on number of cards shown and screen size used.
                if (boardRows == 2 && boardColumns == 2)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                } else if (boardRows == 3 && boardColumns == 3)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                } else if (boardRows == 4 && boardColumns == 4)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                } else if (boardRows == 4 && boardColumns == 8)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                } else if (boardRows == 6 && boardColumns == 6)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                } else {
                    //default values.
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                }
*/
        }
        else//Small screen. e.g.  Phone.
        {
            size.height = getResources().getDimensionPixelSize(R.dimen.imageSize75);
            size.width = getResources().getDimensionPixelSize(R.dimen.imageSize75);

                if(landPortrait == Configuration.ORIENTATION_LANDSCAPE)
                {
                    if (boardRows == 4 && boardColumns == 4)
                    {
                        size.height = getResources().getDimensionPixelSize(R.dimen.imageSize60);
                        size.width = getResources().getDimensionPixelSize(R.dimen.imageSize60);
                    }
                }
        }



            temp.setLayoutParams(size);

            temp.setId(View.generateViewId());

            grid.addView(temp);
        }

        initListeners();

        v.setBackgroundResource(getBackground());//Assign a random background.

        //Initialize the Card list.



        ArrayList<Card> tempList = new ArrayList<Card>();

        if(matchType == MATCH_TYPE_2)//pair-matching
        {
            for(int i=0; i < gridSize ; i+=2)
            {
                idOfPic = chooseImage();

                tempList.add(new Card(idOfPic));
                tempList.add(new Card(idOfPic));
            }
        }
        else if(matchType == MATCH_TYPE_3)//triple-matching
        {
            for(int i=0; i < gridSize ; i+=3)
            {
                idOfPic = chooseImage();

                tempList.add(new Card(idOfPic));
                tempList.add(new Card(idOfPic));
                tempList.add(new Card(idOfPic));
            }
        }
        else
        {
            //Major problem and should be unreachable.
        }

        java.util.Collections.shuffle(tempList);

        for(int i=0;i<gridSize;i++)
        {
            button = (ImageButton)grid.getChildAt(i);

            idOfImageButton = button.getId();

            button.setImageResource(cardBack);

            tempList.get(i).setIdOfImageButton(idOfImageButton);
        }


        //Show list of cards in log for debugging.
        for(int i=0;i<tempList.size();i++)
        {
            Card c = tempList.get(i);
            String s = "id_button="+c.getIdOfImageButton()+" id_pic="+c.getIdOfPic();
            Log.d("temp cards====",s);
        }


        cards = tempList;
        Log.d("address",""+cards);
        board.setCards(cards);
        Log.d("address2",""+board.getCards());


        startTimer();

        //show the cards in the log to see their order
 /*       for(int i=0;i<cards.size();i++)
        {
            Log.d("getIdOfPic:",""+cards.get(i).getIdOfPic());
        }*/



/*
        ArrayList<Card> test = new ArrayList<Card>();

        test.add(new Card(1,R.drawable.beehoneybeeapisinsect144252,false));
        test.add(new Card(2,R.drawable.beehoneybeeapisinsect144252,false));
        test.add(new Card(3,R.drawable.beehoneybeeapisinsect144252,false));
        test.add(new Card(4,R.drawable.beehoneybeeapisinsect144252,false));

        java.util.Collections.shuffle(test);

        cards=test;

        //Show list of cards in log for debugging.
        for(int i=0;i<test.size();i++)
        {


            Log.d("card",test.get(i).getIdOfImageButton()+"");
        }*/



        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }


    //MediaPlayer needs to be released onStop and onPause.
    @Override
    public void onStop()
    {
//        stopAllSounds();
//        releaseAll();
        super.onStop();
    }

    @Override
    public void onPause()
    {
//        stopAllSounds();
 //       releaseAll();

        saved = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = saved.edit();

        editor.putInt("",boardColumns);
        editor.putInt("",boardRows);
        editor.putInt("",matchType);
        editor.putInt("",score);

        editor.putBoolean("",isLargeScreenDevice);

        editor.putFloat("",updateTime);

        editor.commit();

        super.onPause();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private void showGO()
    {
        goDialogFragment = new GoDialogFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        goDialogFragment.show(ft, "godialog");
    }


    private void startTimer()
    {
        showGO();

        //Quickly get rid of the goMessage.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            private int count = 2;//Number of seconds to wait.
            private final long wait = 125L;// 1/4 second total wait.

            public void run()
            {
                if(count > 0)
                {
                    handler.postDelayed(this, wait);
                    count--;
                }
                else
                {
                    goDialogFragment.dismiss();
                }
            }
        },0);


        //The game is all setup now.  UI is ready.  So, start the timer.
        startTime = SystemClock.uptimeMillis();

        timerHandler.postDelayed(new Runnable(){

            public void run()
            {

                millisecondsTime = SystemClock.uptimeMillis() - startTime;

                //updateTime = TimeBuff + millisecondsTime;
                updateTime = millisecondsTime;

                secondsTime = (int) (updateTime / 1000);

                minutesTime = secondsTime / 60;

                secondsTime = secondsTime % 60;

                millisecondsTime = (int) (updateTime % 1000);

                updateStats();

                if(toggleTimer)
                {
                    timerHandler.postDelayed(this, 0);
                }
            }
        },0);

    }

    private void updateStats()
    {
        tvMatchCount.setText(""+matchCount);
        tvScore.setText(""+score);
        tvTime.setText(     + Long.valueOf(minutesTime).intValue() + ":"
                            + String.format(Locale.getDefault(),"%02d", Long.valueOf(secondsTime).intValue()) + ":"
                            + String.format(Locale.getDefault(),"%03d", Long.valueOf(millisecondsTime).intValue()));

    }

    //Choose an image to put in the grid.
    private int chooseImage()
    {
        int id,
            index;

        index = new Random().nextInt(drawableIds.size());// 0-to-(size-1)

        try
        {
            //If picture already exists, then remove it from the list of pictures available.  This prevents duplicate assignments.
            id = Integer.parseInt(drawableIds.get(index).toString());
        }
        catch(Exception e)
        {
            id = -9999;//Should be unreachable statement because all IDs stored are ints wrapped in a String... should be no exceptions occurring.
            Log.d("Exception chooseImage",e.toString());
        }

        drawableIds.remove(index);

        return id;
    }


    private void playMatch2(ImageButton imageButton)
    {
        if(firstCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    firstCard = (Card)cards.get(i);

                    imageButton.setImageResource(firstCard.getIdOfPic());
                    imageButton.setEnabled(false);

                    playSound(soundTap1);

                    return;
                }
            }
        }

        if(secondCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    secondCard = (Card)cards.get(i);

                    imageButton.setImageResource(secondCard.getIdOfPic());
                    imageButton.setEnabled(false);

                    playSound(soundTap1);

                    break;
                }
            }
        }

        //With two different cards selected, test for match.
        if(isMatch(firstCard,secondCard))
        {
            matchCount++;
            score += 100;

            playSound(soundMatch);

            //Since a match is made, disable the corresponding buttons in the GridLayout.
            grid.findViewById(firstCard.getIdOfImageButton()).setEnabled(false);
            grid.findViewById(secondCard.getIdOfImageButton()).setEnabled(false);

            firstCard   = null;
            secondCard  = null;
        }
        else  //No match is made, so reset the cards.  Flip.  Show cardBack after short delay.
        {

            threadBusy = true;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                public void run()
                {
                    //Turn card over.  Show backside of cards.
                    ImageButton i1 = ((ImageButton)grid.findViewById(firstCard.getIdOfImageButton()));
                    i1.setImageResource(cardBack);
                    i1.setEnabled(true);

                    ImageButton i2 = ((ImageButton)grid.findViewById(secondCard.getIdOfImageButton()));
                    i2.setImageResource(cardBack);
                    i2.setEnabled(true);

                    firstCard   = null;
                    secondCard  = null;

                    threadBusy = false;
                }
            },CARD_FLIP_DELAY);

        }
    }

    private void playMatch3(ImageButton imageButton)
    {
        if(firstCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    firstCard = (Card)cards.get(i);

                    imageButton.setImageResource(firstCard.getIdOfPic());
                    imageButton.setEnabled(false);

                    playSound(soundTap1);

                    return;
                }
            }
        }

        if(secondCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    secondCard = (Card)cards.get(i);

                    imageButton.setImageResource(secondCard.getIdOfPic());
                    imageButton.setEnabled(false);

                    playSound(soundTap1);

                    return;
                }
            }
        }

        if(thirdCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    thirdCard = (Card)cards.get(i);

                    imageButton.setImageResource(thirdCard.getIdOfPic());
                    imageButton.setEnabled(false);

                    playSound(soundTap1);

                    break;
                }
            }
        }

        //With two different cards selected, test for match.
        if(isMatch(firstCard,secondCard) && isMatch(secondCard,thirdCard) )
        {
            matchCount++;
            score += 100;

            playSound(soundMatch);

            //Since a match is made, disable the corresponding buttons in the GridLayout.
            grid.findViewById(firstCard.getIdOfImageButton()).setEnabled(false);
            grid.findViewById(secondCard.getIdOfImageButton()).setEnabled(false);
            grid.findViewById(thirdCard.getIdOfImageButton()).setEnabled(false);


            firstCard   = null;
            secondCard  = null;
            thirdCard   = null;
        }
        else  //No match is made, so reset the cards.  Flip.  Show cardBack after short delay.
        {

            threadBusy = true;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                public void run()
                {

                    //Turn card over.  Show backside of cards.

                    ImageButton i1 = ((ImageButton)grid.findViewById(firstCard.getIdOfImageButton()));
                    i1.setImageResource(cardBack);
                    i1.setEnabled(true);

                    ImageButton i2 = ((ImageButton)grid.findViewById(secondCard.getIdOfImageButton()));
                    i2.setImageResource(cardBack);
                    i2.setEnabled(true);

                    ImageButton i3 = ((ImageButton)grid.findViewById(thirdCard.getIdOfImageButton()));
                    i3.setImageResource(cardBack);
                    i3.setEnabled(true);

                    firstCard   = null;
                    secondCard  = null;
                    thirdCard   = null;

                    threadBusy = false;
                }
            },CARD_FLIP_DELAY);

        }
    }


    public void onClick(View v)
    {

        if(threadBusy){return;}

        if(v.getId() == restartButton.getId() ){resetGame();return;}

        ImageButton imageButton = (ImageButton)v;

        if(matchType == MATCH_TYPE_2)
        {
            playMatch2(imageButton);
        }
        else if(matchType == MATCH_TYPE_3)
        {
            playMatch3(imageButton);
        }

        if(isGameOver())
        {
            showGameOver();

            //Check if new high score is reached.  If so, add to new entry to DB, else do nothing.

            //Stops timer at Game Over.
            toggleTimer = false;

            stopSound(soundMusic1);

            playSound(soundWin1);

            showHighScores();

        }

    }

    private void showGameOver()
    {
        gameOverDialogFragment = new GameOverDialogFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        gameOverDialogFragment.show(ft, "gameoverdialog");
    }

    private void showHighScores()
    {
        //Wait three seconds, then show the scores.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            private int count = 5;//Number of seconds to wait.
            private final long second = 1000L;//1 second.

            public void run()
            {
                if(count > 0)
                {
                    handler.postDelayed(this, second);
                    count--;
                }
                else
                {
                    gameOverDialogFragment.dismiss();

                    HighScoresFragment fragment = HighScoresFragment.newInstance(score);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.framelayout, fragment).commit();
                }
            }
        },0);
    }


    private void enableGridImageButtons()
    {
        for(int i=0;i<gridSize;i++)
        {
            grid.getChildAt(i).setEnabled(true);
        }
    }

    private boolean isGameOver()
    {
         return matchCount == totalMatchesAvailable;
    }


    public ArrayList getAllDrawableId()
    {
        Field[] ids = com.example.amd.memorymatcher.R.drawable.class.getDeclaredFields();

        ArrayList list = new ArrayList();

        int tempId;
        String tempName;

        Log.d("fields_length",ids.length+"");

        for(int i = 0; i < ids.length; i++)
        {

            try
            {
                tempName = ids[i].getName();

                tempId = ids[i].getInt(new R.drawable());

                //Exclude launcher icons, backgrounds, and *.xml files.
                if(
                        tempName.startsWith("bg")
                        ||tempName.startsWith("ic_")
                        ||tempName.startsWith("cardback")
                        ||tempName.startsWith("abc_")
                        ||tempName.startsWith("avd_")
                        ||tempName.startsWith("design_")
                        ||tempName.startsWith("notification_")
                        ||tempName.startsWith("navigation_")
                        ||tempName.startsWith("notify_")
                        ||tempName.startsWith("side_")
                        ||tempName.startsWith("myicon")
                        )
                {
                    //Do not add these.
                }
                else
                {
                    list.add(tempId);
                    Log.d("field_name",tempName);
                }


            }
            catch (Exception e)
            {

            }
            Log.d("list_length",list.size()+"");
        }

        return list;
    }

    private void resetGame()
    {
        ImageButton button;
        int idOfImageButton;

        firstCard   = null;
        secondCard  = null;
        thirdCard   = null;

        threadBusy = false;

        score = 0;
        matchCount = 0;

        totalMatchesAvailable = board.getTotalMatches();

        timerHandler = new Handler();

        toggleTimer = true;

        drawableIds = getAllDrawableId();

        java.util.Collections.shuffle(cards);//Shuffle the cards, retaining the original selection of pictures.

        for(int i=0;i<gridSize;i++)
        {
            button = (ImageButton)grid.getChildAt(i);

            idOfImageButton = button.getId();

            button.setImageResource(cardBack);

            cards.get(i).setIdOfImageButton(idOfImageButton);
        }

        enableGridImageButtons();

        initTime();

        updateStats();

        startTimer();
    }

    private void initTime()
    {
        minutesTime = 0L;
        secondsTime = 0L;
        millisecondsTime = 0L;
    }

    public void initListeners()
    {
        for(int i=0;i<gridSize;i++)
        {
            ((ImageButton)grid.getChildAt(i)).setOnClickListener(this);
        }

        restartButton.setOnClickListener(this);
    }

    private int getCardBackImage()
    {
        switch(new Random().nextInt(NUMBER_OF_CARD_BACKS))
        {
            case 0:
                return R.drawable.cardback;
            case 1:
                return R.drawable.cardback2;
            case 2:
                return R.drawable.cardback3;
            case 3:
                return R.drawable.cardback4;
            case 4:
                return R.drawable.cardback5;
            default:
                return R.drawable.cardback;
        }
    }

    private int getBackground()
    {
        switch(new Random().nextInt(NUMBER_OF_BACKGROUNDS))
        {
            case 0:
                return R.drawable.bg1;
            case 1:
                return R.drawable.bg2;
            case 2:
                return R.drawable.bg3;
            case 3:
                return R.drawable.bg4;
            case 4:
                return R.drawable.bg5;
            case 5:
                return R.drawable.bg6;
            case 6:
                return R.drawable.bg7;
            case 7:
                return R.drawable.bg8;
            case 8:
                return R.drawable.bg9;
            default:
                return R.drawable.bg1;
        }
    }

    private void initGame(){}


    //If both cards are assigned the same R.drawable.imageblahblahblah then they are considered the same.  i.e.  a match has been made.
    private boolean isMatch(Card c1, Card c2)
    {
        return c1.getIdOfPic() == c2.getIdOfPic();
    }

    private void playSound(MediaPlayer sound)
    {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            public void run()
            {
                final SoundPool sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

                /** soundId for Later handling of sound pool **/
                final int soundId = sp.load(getActivity(), R.raw.w1, 1);

                sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        sp.play(soundId, 1, 1, 0, 0, 1);
                    }
                });

sp.release();


            }
        },0);

    }

    private void stopSound(MediaPlayer sound)//Stop playing the sound.
    {
        sound.stop();
    }

    private void stopAllSounds()
    {
        stopSound(soundTap1);
        stopSound(soundTap2);
        stopSound(soundMatch);
        stopSound(soundMusic1);
        stopSound(soundWin1);
    }

    private void releaseAll()//release all MediaPlayer sound resources to avoid later audio problems with running other apps.
    {
        soundTap1.release();
        soundTap2.release();
        soundMatch.release();
        soundMusic1.release();
        soundWin1.release();

        soundTap1   = null;
        soundTap2   = null;
        soundMatch  = null;
        soundMusic1 = null;
        soundWin1   = null;
    }


    private void msg(String m){Toast.makeText(getActivity(),m,Toast.LENGTH_SHORT).show();}

    public static class GameOverDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            final View v = inflater.inflate(R.layout.gameover, null);

            final TextView text = (TextView)v.findViewById(R.id.textViewScore);

            text.setText(Integer.toString(score));

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                        .setCancelable(false);

            return builder.create();
        }
    }

    public static class GoDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            final View v = inflater.inflate(R.layout.go, null);

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                    .setCancelable(false);

            return builder.create();
        }

        @Override
        public void onResume()
        {
            ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();

            params.width    = 100;//LayoutParams.WRAP_CONTENT;
            params.height   = 75;//LayoutParams.WRAP_CONTENT;

            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

            super.onResume();
        }
    }
}
