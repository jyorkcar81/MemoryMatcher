package com.example.amd.memorymatcher.fragments;

import android.app.Activity;
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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.amd.memorymatcher.R;
import com.example.amd.memorymatcher.other.Board;
import com.example.amd.memorymatcher.other.Card;

import java.util.ArrayList;
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


    private Board board;
    private ArrayList<Card> cards;

    private int cardBack,
                boardSize;

    private GridLayout grid;

    private int[] drawableIds;

    private int gridSize,//Number of cells in the grid.
                boardRows,//Number of rows in the boardgame.
                boardColumns;//Number of columns in the boardgame.


    private boolean matched;

    private Card    firstCard,
                    secondCard;

    private boolean threadBusy;

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

        }

        msg("board dimensions: "+boardRows+"x"+boardColumns);

        board = new Board(boardRows,boardColumns);
        boardSize = board.getNumOfCards();
        gridSize = board.getNumOfCards();
        firstCard   = null;
        secondCard  = null;
        threadBusy = false;

    //  drawableIds = getAllDrawableId();
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

        int idOfImageButton;
        int idOfPic;
        ImageButton button;

        // Inflate the layout for this fragment.  i.e. get xml to dynamically add buttons to build the ui
        View v = inflater.inflate(R.layout.fragment_game, container, false);
        grid = (GridLayout)v.findViewById(R.id.twoByTwoGridLayout);

        //Dynamically create a grid based upon boardSize*boardSize  e.g.  2x2, 4x4... always square
        grid.setColumnCount(boardColumns);
        grid.setRowCount(boardRows);

        ImageButton temp;


        //use a waiting thread until this is true.



        if(isAdded())//Fragment must be attached for getApplication to not be null.
        {
            msg("board size:"+board.getNumOfCards());

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

        v.invalidate();

        initListeners();

        grid.setBackgroundResource(getBackground());//Assign a random background.

/*
        idOfPic = R.drawable.beehoneybeeapisinsect144252;

        board.addCard(new  Card(1,idOfPic,showing));
        board.addCard(new  Card(1,idOfPic,showing));

        idOfPic = R.drawable.cowcattleanimalbull162258;

        board.addCard(new  Card(2,idOfPic,showing));
        board.addCard(new  Card(2,idOfPic,showing));
*/

        //Initialize the Card list.

        cardBack = getCardBackImage();

        for(int i=0; i < gridSize ; i++)
        {

            idOfPic = R.drawable.beehoneybeeapisinsect144252;

            button = (ImageButton)grid.getChildAt(i);

            board.addCard(new Card(button.getId(),idOfPic,false));

            button.setImageResource(cardBack);

            i++;

            button = (ImageButton)grid.getChildAt(i);

            idOfPic = R.drawable.cowcattleanimalbull162258;

            board.addCard(new Card(button.getId(),idOfPic,false));

            button.setImageResource(cardBack);

        }

        board.shuffle();

        cards = board.getCards();


        //Show list of cards in log for debugging.
 /*       for(int i=0;i<gridSize;i++)
        {
            Card c = cards.get(i);
            String s = "id_button="+c.getIdOfImageButton()+" id_pic="+c.getIdOfPic();
            Log.d("card",s);
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

    public void onClick(View v)
    {

        if(threadBusy){return;}

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
        if(firstCard.isMatched()){msg("true.  isMatched.");return;}



        //With two different cards selected, test for match.
        if(firstCard.getIdOfPic() == secondCard.getIdOfPic())
        {
            msg("match-made");

            //Since a match is made, disable the corresponding buttons in the GridLayout.
            grid.findViewById(firstCard.getIdOfImageButton()).setEnabled(false);
            grid.findViewById(secondCard.getIdOfImageButton()).setEnabled(false);

            firstCard.setShowing(false);
            secondCard.setShowing(false);

            firstCard.setMatched(true);
            secondCard.setMatched(true);

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


/*
        for(int i=0;i<gridSize;i++)
        {
            if(cards.get(i).isShowing())
            {
                ((ImageButton)grid.getChildAt(i)).setImageResource(cards.get(i).getIdOfPic());
            }
        }
*/
    }


    public int[] getAllDrawableId()
    {
        Field[] ids = com.example.amd.memorymatcher.R.drawable.class.getDeclaredFields();

        //Log all the names of fields gathered.
        for(int i=0;i<ids.length;i++)
        {
            Log.d("field_name:", ids[i].getName());
        }

        int[] resArray = new int[ids.length];


        ArrayList<Integer> list = new ArrayList<Integer>();

        int tempId;

        Log.d("fields_length",ids.length+"");

        for(int i = 0; i < ids.length; i++)
        {

            try
            {
                tempId = ids[i].getInt(new R.drawable());

                //Exclude launcher icons, backgrounds, and *.xml files.
                if(
                           tempId == R.drawable.bg1
                        || tempId == R.drawable.bg2
                        || tempId == R.drawable.bg3
                        || tempId == R.drawable.bg4
                        || tempId == R.drawable.bg5
                        || tempId == R.drawable.bg6
                        || tempId == R.drawable.bg7
                        || tempId == R.drawable.bg8
                        || tempId == R.drawable.bg9
                        || tempId == R.drawable.ic_menu_camera
                        || tempId == R.drawable.ic_menu_gallery
                        || tempId == R.drawable.ic_menu_manage
                        || tempId == R.drawable.ic_menu_send
                        || tempId == R.drawable.ic_menu_share
                        || tempId == R.drawable.ic_menu_slideshow
                        || tempId == R.drawable.side_nav_bar
                        || tempId == R.drawable.cardback
                        || tempId == R.drawable.cardback2
                        || tempId == R.drawable.cardback3
                        || tempId == R.drawable.cardback4
                        || tempId == R.drawable.cardback5

                        )
                {
                    //Do not add these.
                }
                else
                {
                    list.add(tempId);
                }


            }
            catch (Exception e)
            {

            }

        }


        int cleansed[] = new int[list.size()];
        //Convert list to int[].
        for(int i=0;i<cleansed.length;i++)
        {
            cleansed[i] = list.get(i);
        }

        Log.d("id_length_63_",cleansed.length+"");


        return cleansed;
    }

    private void resetGame()
    {






        //new Board(3,3);
        //new Board(4,4);

    }

    public void initListeners()
    {
        for(int i=0;i<gridSize;i++)
        {
            ((ImageButton)grid.getChildAt(i)).setOnClickListener(this);
        }
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
    private void isGameOver(){}

    //If both cards are assigned the same R.drawable.imageblahblahblah then they are considered the same.  i.e.  a match has been made.
    private boolean isMatch(Card c1, Card c2)
    {
        return c1.getIdOfPic() == c2.getIdOfPic();
    }

    private void playSound(){}
    private void msg(String m){Toast.makeText(getActivity(),m,Toast.LENGTH_SHORT).show();}

}
