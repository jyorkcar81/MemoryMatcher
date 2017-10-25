package com.example.amd.memorymatcher.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.media.Image;
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

    private int matchType;//Match 2 or Match 3 at a time.

    private Board board;
    private ArrayList<Card> cards;

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

    private boolean matched;

    private Card    firstCard,
                    secondCard;


    private int totalMatchesAvailable;


    private Handler timerHandler;

    private int score,
                matchCount;//Total matches made.



    private boolean threadBusy;

    private TextView    tvScore,
                        tvTime,
                        tvMatchCount;

    private boolean toggleTimer;

    private Button restartButton;

    private OnFragmentInteractionListener mListener;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            boardRows = getArguments().getInt("boardRows");
            boardColumns = getArguments().getInt("boardColumns");
            matchType = getArguments().getInt("matchType");

        }

        //msg("board dimensions: "+boardRows+"x"+boardColumns);
        //msg("matchType: "+matchType);

        board = new Board(boardRows,boardColumns,matchType);
        boardSize = board.getNumOfCards();
        gridSize = board.getNumOfCards();
        firstCard   = null;
        secondCard  = null;
        threadBusy = false;

        score = 0;
        matchCount = 0;

        totalMatchesAvailable = board.getTotalMatches();

        timerHandler = new Handler();

        toggleTimer = true;

        drawableIds = getAllDrawableId();
    }

    /*
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated( view,  savedInstanceState);

        GridLayout grid = (GridLayout)(getActivity().findViewById(R.layout.two_by_two).findViewById(R.id.twoByTwoGridLayout));
        Log.d("grid",grid+"");
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int idOfImageButton,
            idOfPic;

        ImageButton button,
                    temp;

        // Inflate the layout for this fragment.  i.e. get xml to dynamically add buttons to build the ui
        View v = inflater.inflate(R.layout.fragment_game, container, false);
        grid = (GridLayout)v.findViewById(R.id.twoByTwoGridLayout);

        tvScore         = (TextView)v.findViewById(R.id.textViewScore);
        tvTime          = (TextView)v.findViewById(R.id.textViewTime);
        tvMatchCount    = (TextView)v.findViewById(R.id.textViewMatchCount);

        restartButton   = (Button)v.findViewById(R.id.buttonRestart);

        updateStats();

        //Dynamically create a grid based upon boardSize*boardSize  e.g.  2x2, 4x4... always square
        grid.setColumnCount(boardColumns);
        grid.setRowCount(boardRows);


        //use a waiting thread until this is true.



        if(isAdded())//Fragment must be attached for getApplication to not be null.
        {

            msg("board size:"+board.getNumOfCards());

              /*
                    Cards ---> Grid cells
                    1,2,3,4 --- >  1,2,3,4
                    shuffle cards
                    4,2,1,3 ----> 1,2,3,4

              */

                for(int i=0; i < gridSize; i++)
                {
                    Context context = inflater.getContext();
                    temp = new ImageButton(context);
                    temp.setScaleType(ImageView.ScaleType.FIT_XY);
                    temp.setLayoutParams(new ViewGroup.LayoutParams(100,100));
                    temp.setId(View.generateViewId());

                    grid.addView(temp);
                }

        }
        else
        {
            //
        }



        initListeners();


        v.setBackgroundResource(getBackground());//Assign a random background.

        //Initialize the Card list.

        cardBack = getCardBackImage();

        ArrayList<Card> tempList = new ArrayList<Card>();
//for multiple of 2 boards:
        for(int i=0; i < gridSize ; i++)
        {
            idOfPic = chooseImage();

            tempList.add(new Card(idOfPic));

            i++;

            tempList.add(new Card(idOfPic));
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





        //cards = board.getCards();
        //board.shuffle();


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








        startTimer();





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







    //************************************************************

    private void startTimer()
    {

        timerHandler.postDelayed(new Runnable(){

            private int count = 3;

            public void run()
            {
                if(count > 0)
                {
                    msg("Ready "+count);

                    count--;

                    timerHandler.postDelayed(this, 1000);
                }
                else
                {
                    msg("GO!");



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

    public void onClick(View v)
    {

        if(threadBusy){return;}

        if(v.getId() == restartButton.getId() ){msg("reset");resetGame();return;}

        ImageButton imageButton = (ImageButton)v;

        if(firstCard == null)
        {
            //Determine which card this imageButton is for.
            for(int i=0;i<gridSize;i++)
            {
                if( ((Card)cards.get(i)).getIdOfImageButton() == imageButton.getId() )
                {
                    firstCard = (Card)cards.get(i);
                    firstCard.setShowing(true);
                    imageButton.setImageResource(firstCard.getIdOfPic());
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
                    secondCard.setShowing(true);
                    imageButton.setImageResource(secondCard.getIdOfPic());
                    break;
                }
            }
        }



        //If same card is clicked twice, do nothing.  Return.
        if(firstCard.getIdOfImageButton() == secondCard.getIdOfImageButton()){msg("same");secondCard=null;return;}

        //If clicked card is already matched, do nothing.  Return.
        //if(firstCard.isMatched()){msg("true.  isMatched.");return;}



        //With two different cards selected, test for match.
        if(firstCard.getIdOfPic() == secondCard.getIdOfPic())
        {
            matchCount++;
            score += 100;

            msg("match-made");

            //Since a match is made, disable the corresponding buttons in the GridLayout.
            grid.findViewById(firstCard.getIdOfImageButton()).setEnabled(false);
            grid.findViewById(secondCard.getIdOfImageButton()).setEnabled(false);

            firstCard.setShowing(false);
            secondCard.setShowing(false);

            //firstCard.setMatched(true);
            //secondCard.setMatched(true);

            firstCard = null;
            secondCard = null;
        }
        else  //No match is made, so reset the cards.  Flip.  Show cardBack after short delay.
        {
            long delay = 1000L;

            firstCard.setShowing(false);
            secondCard.setShowing(false);

            threadBusy = true;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                    public void run()
                    {
                        //Turn card over.  Show backside of cards.
                        ((ImageButton)grid.findViewById(firstCard.getIdOfImageButton())).setImageResource(cardBack);
                        ((ImageButton)grid.findViewById(secondCard.getIdOfImageButton())).setImageResource(cardBack);

                        firstCard   = null;
                        secondCard  = null;

                        threadBusy = false;
                    }
             },delay);

        }



        if(isGameOver())
        {
            msg("Game Over!");

            //Check if new high score is reached.  If so, add to new entry to DB, else do nothing.


            //Stops timer at Game Over.
            toggleTimer = false;


            HighScoresFragment fragment = HighScoresFragment.newInstance(score);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.framelayout, fragment).commit();
        }



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
        boolean gameOver = false;

        if(matchCount == totalMatchesAvailable)
        {
            gameOver = true;
        }
        else
        {
            gameOver = false;
        }

        return gameOver;
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

    //Go through the list of cards and flip-over all the non-matched cards back to showing their backside.
    private void turnCardsOver()
    {

        for(int i=0;i<cards.size();i++)
        {
            if(!cards.get(i).isShowing())
            {
                //flip card.
                ((ImageButton)grid.getChildAt(i)).setImageResource(cardBack);
            }
        }
    }

    private void initGame(){}


    //If both cards are assigned the same R.drawable.imageblahblahblah then they are considered the same.  i.e.  a match has been made.
    private boolean isMatch(Card c1, Card c2)
    {
        return c1.getIdOfPic() == c2.getIdOfPic();
    }

    private void playSound(){}
    private void msg(String m){Toast.makeText(getActivity(),m,Toast.LENGTH_SHORT).show();}

}
