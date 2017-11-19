package com.example.amd.memorymatcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.lang.reflect.Field;

import com.example.amd.memorymatcher.R;
import com.example.amd.memorymatcher.other.Board;
import com.example.amd.memorymatcher.other.Card;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment implements View.OnClickListener{

    private static final int NUMBER_OF_CARD_BACKS   = 5;
    private static final int NUMBER_OF_BACKGROUNDS  = 9;

    /* Games are played in different ways:  Matching 2 at a time, and Match 3 */
    public static final int MATCH_TYPE_2 = 2;
    public static final int MATCH_TYPE_3 = 3;

    private static final long CARD_FLIP_DELAY = 1000L;//wait this long before flipping over a card (in milliseconds).  i.e.  changing its image.

    /* Used to specify if a sound/music is repeated or not.  Loop/no loop. */
    private static final int LOOP_SOUND         = -1;
    private static final int DONT_LOOP_SOUND    = 0;

    /* Max simultaneous sounds to play */
    private static final int MAX_STREAMS    = 10;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String GAME_OVER_DIALOG_TAG    =   "gameoverdialog";
    private static final String GO_DIALOG_TAG           =   "godialog";

    private static final String RETAINED_FRAGMENT_TAG = "data";//Tag to retrieve the retained fragment that contains data for retaining game-state.

    private String mParam1;
    private String mParam2;

    private int matchType,      //Match 2 or Match 3 at a time.
                cardBack,       //ID of cardBack image to use.
                gridSize,       //Number of cells in the grid.
                boardRows,      //Number of rows in the boardgame.
                boardColumns,   //Number of columns in the boardgame.
                background,     //Randomly chosen background.
                matchCount,     //Total matches made.
                totalMatchesAvailable,
                soundTap1,      //Assigned sound ID of loaded sound for playing a SoundPool sound.
                soundTap2,      //Assigned sound ID of loaded sound for playing a SoundPool sound.
                soundMatch,     //Assigned sound ID of loaded sound for playing a SoundPool sound.
                soundWin1,      //Assigned sound ID of loaded sound for playing a SoundPool sound.
                landPortrait;   //Keeps track of orientation:  Portrait or Landscape.

    private Board board;
    private ArrayList<Card> cards;

    private GameOverDialogFragment gameOverDialogFragment;
    private GoDialogFragment goDialogFragment;

    private GridLayout grid;

    private ArrayList drawableIds;

    private long startTime,
                endTime,
                millisecondsTime,
                secondsTime,
                minutesTime,
                updateTime;

    private Card    firstCard,
                    secondCard,
                    thirdCard;

    private boolean isLargeScreenDevice,    //Used for determining if large screen is used.  Large will be considered 7inch tablet.
                    isRestoredState,        //Used for determining if Fragment already exists, i.e.  user has changed orientations.
                    threadBusy,             // Aids in responsiveness of app by waiting on UI thread at times.
                    toggleTimer;

    private Handler timerHandler;

    private static  int score;

    private TextView    tvScore,
                        tvTime,
                        tvMatchCount;

    private Button restartButton;

    private OnFragmentInteractionListener mListener;

    private SoundPool  sp;  //For sound bytes less than 1MB.
    private MediaPlayer mp; //For sounds over 1MB.

    private ArrayList<Card> tempList;

    private RetainedFragment rf; //Retain certain values to preserve game-state when orientation of device changes. Done via RetainedFragment.

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
            mParam1         = getArguments().getString(ARG_PARAM1);
            mParam2         = getArguments().getString(ARG_PARAM2);
            boardRows       = getArguments().getInt("boardRows");
            boardColumns    = getArguments().getInt("boardColumns");
            matchType       = getArguments().getInt("matchType");

        }

        initSoundPool();

        loadSoundPoolSounds();

       /* if (mp != null)
        {
            mp.reset();
            mp.release();
        }*/

        releaseMediaPlayer();

        mp = MediaPlayer.create(getActivity(), R.raw.m1);

        playMediaPlayerSound(true);

        board = new Board(boardRows,boardColumns,matchType);
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


        tempList = new ArrayList<Card>();

        cardBack = getCardBackImage();

        background = getBackground();

        isRestoredState = false;

        landPortrait = getResources().getConfiguration().orientation;//Initially orientation is undefined when Fragment is first created, then to Portrait/landscape.

        setRetainInstance(true);

        rf = (RetainedFragment) getFragmentManager().findFragmentByTag(RETAINED_FRAGMENT_TAG);

        if(rf == null)
        {
            rf = new RetainedFragment();
            getFragmentManager().beginTransaction().add(rf,RETAINED_FRAGMENT_TAG).commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int idOfImageButton;

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

        /*
                    Cards ---> Grid cells
                    1,2,3,4 --- >  1,2,3,4
                    shuffle cards
                    4,2,1,3 ----> 1,2,3,4

        */

        Context context = inflater.getContext();
        ViewGroup.LayoutParams size = determineSize();

        //Dynamically create a grid based upon e.g.  2x2, 4x4...
        grid.setColumnCount(boardColumns);
        grid.setRowCount(boardRows);

        if(isRestoredState)
        {
            //Get pre-existing values.  Score, Time, Matches made, all cards used, and whether they are displayed or not.

            GridLayout tempGrid = rf.getGrid();

            cards = rf.getList();

            for(int i=0; i < gridSize; i++)
            {
                ImageButton b = new ImageButton(context);

                b.setScaleType(((ImageButton)tempGrid.getChildAt(i)).getScaleType());

                b.setLayoutParams(tempGrid.getChildAt(i).getLayoutParams());

                b.setId(tempGrid.getChildAt(i).getId());

                b.setEnabled(tempGrid.getChildAt(i).isEnabled());

                b.setImageDrawable(((ImageButton)tempGrid.getChildAt(i)).getDrawable());

                grid.addView(b);
            }

        }
        else
        {
            //Initialize the Card list.

            for(int i=0; i < gridSize; i++)
            {
                temp = new ImageButton(context);
                temp.setScaleType(ImageView.ScaleType.FIT_XY);

                temp.setLayoutParams(size);

                temp.setId(View.generateViewId());

                grid.addView(temp);
            }

            choosePictures();

            for(int i=0;i<gridSize;i++)
            {
                button = (ImageButton)grid.getChildAt(i);

                idOfImageButton = button.getId();

                button.setImageResource(cardBack);

                tempList.get(i).setIdOfImageButton(idOfImageButton);
            }

            cards = tempList;

            startTimer();

        }

        initListeners();
        v.setBackgroundResource(background);//Assign a random background.

        return v;
    }

    /* Determine what DP units to make each ImageButton in the GridLayout */
    private ViewGroup.LayoutParams determineSize()//Adjust size of cards based on number of cards shown and screen size used.
    {
        ViewGroup.LayoutParams size = new ViewGroup.LayoutParams(0,0);

        if(isLargeScreenDevice)
        {
            if(landPortrait == Configuration.ORIENTATION_PORTRAIT)
            {
                if(boardRows == 2 && boardColumns == 2)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize200);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize200);
                }
                else if (boardRows == 3 && boardColumns == 3)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize150);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize150);
                }
                else if (boardRows == 4 && boardColumns == 4)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize125);
                }
                else if (boardRows == 6 && boardColumns == 6)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                }
                else
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                }

                    /* only issue here is 4x8 does not fit */
            }
            else if (landPortrait == Configuration.ORIENTATION_LANDSCAPE)
            {
                if(boardRows == 2 && boardColumns == 2)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize200);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize200);
                }
                else if (boardRows == 3 && boardColumns == 3)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize150);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize150);
                }
                else if (boardRows == 6 && boardColumns == 6)
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize75);
                }
                else
                {
                    size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                    size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                }

            }
            else if (landPortrait == Configuration.ORIENTATION_UNDEFINED)
            {
                //untested orientation.
                size.height = getResources().getDimensionPixelSize(R.dimen.imageSize100);
                size.width = getResources().getDimensionPixelSize(R.dimen.imageSize100);
            }

        }
        else//Small screen. e.g.  Phone.
        {
            //Defaults.
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

        return size;
    }

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //Release all sound resources to avoid later audio problems with running other sound based apps.
        releaseSoundPool();

        releaseMediaPlayer();

    }

    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);

        /* Take note of orientation change as this determines DP size of ImageButtons in the GridLayout */
        landPortrait = config.orientation;
    }

    @Override
    public void onStop()
    {
        releaseSoundPool();
        releaseMediaPlayer();
        super.onStop();
    }

    @Override
    public void onPause()
    {
        isRestoredState = true;

        rf.setData(cards);
        rf.setGridLayout(grid);

        //sp.autoPause();

        releaseSoundPool();
        releaseMediaPlayer();

        Fragment f = getFragmentManager().findFragmentByTag(GAME_OVER_DIALOG_TAG);

        if(f != null)
        {
            DialogFragment d = (DialogFragment)f;

            d.dismiss();
            getFragmentManager().beginTransaction().remove(f).commit();

        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //sp.autoResume();

        releaseMediaPlayer();

        mp = MediaPlayer.create(getActivity(), R.raw.m1);

        playMediaPlayerSound(true);

        releaseSoundPool();
        initSoundPool();
        loadSoundPoolSounds();

        landPortrait = getResources().getConfiguration().orientation;
    }

    private void initSoundPool()
    {
       /* Note that "new SoundPool(...)" is deprecated and only works under API level 21.  21 and higher API requires using SoundPool.Builder. */

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            sp = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(MAX_STREAMS)
                    .build();
        }
        else
        {
            sp = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
    }

    private void loadSoundPoolSounds()
    {
        soundTap1   = sp.load(getActivity(), R.raw.c1, 1);
        soundTap2   = sp.load(getActivity(), R.raw.c2, 1);
        soundMatch  = sp.load(getActivity(), R.raw.c3, 1);
        soundWin1   = sp.load(getActivity(), R.raw.w1, 1);
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

        void onFragmentInteraction(Uri uri);
    }
    //Accesses filesystem and loads into memory the full  image...may use large amounts of RAM for large pictures.
    private Drawable getImage(int resID, int maxW, int maxH)
    {
        Drawable d = null;

        try
        {
            d = new BitmapDrawable(getResources(), decodeSampledBitmapFromResource(getResources(), resID, maxW, maxH));
        }
        catch(IOException e)
        {

        }

        return d;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height    = options.outHeight;
        final int width     = options.outWidth;
        int inSampleSize    = 1;

        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight   = height / 2;
            final int halfWidth    = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //Gets a bitmap`s width & height without loading into memory the full image (the pixels), thusly saving memory.  Then returns
    //a scaled down version of the image.  e.g.  Take a 12Megapixel image, shrink it, then use the smaller less spacious image for a thumbnail.
    public static Bitmap decodeSampledBitmapFromResource(Resources r, int resID, int reqWidth, int reqHeight) throws IOException
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(r,resID,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return  BitmapFactory.decodeResource(r,resID,options);
    }

    private void showGO()
    {
        goDialogFragment = new GoDialogFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        goDialogFragment.show(ft, GO_DIALOG_TAG);
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

    private void choosePictures()
    {
        int idOfPic;

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
    }

    private void updateStats()
    {
        tvMatchCount.setText(String.format(Locale.getDefault(),"%d", matchCount));
        tvScore.setText(String.format(Locale.getDefault(),"%d", score));
        tvTime.setText(         Long.valueOf(minutesTime).intValue() + ":"
                            +   String.format(Locale.getDefault(),"%02d", Long.valueOf(secondsTime).intValue()) + ":"
                            +   String.format(Locale.getDefault(),"%03d", Long.valueOf(millisecondsTime).intValue()));

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

                    Drawable d = getImage(firstCard.getIdOfPic(), imageButton.getWidth(), imageButton.getHeight());

                    imageButton.setImageDrawable(d);

                    imageButton.setEnabled(false);

                    playSoundPoolSound(soundTap2,DONT_LOOP_SOUND);

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

                    Drawable d = getImage(secondCard.getIdOfPic(), imageButton.getWidth(), imageButton.getHeight());

                    imageButton.setImageDrawable(d);

                    imageButton.setEnabled(false);

                    playSoundPoolSound(soundTap2,DONT_LOOP_SOUND);

                    break;
                }
            }
        }

        //With two different cards selected, test for match.
        if(isMatch(firstCard,secondCard))
        {
            matchCount++;
            score += 100;

            playSoundPoolSound(soundMatch,DONT_LOOP_SOUND);

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

                    Drawable d = getImage(firstCard.getIdOfPic(), imageButton.getWidth(), imageButton.getHeight());

                    imageButton.setImageDrawable(d);

                    imageButton.setEnabled(false);

                    playSoundPoolSound(soundTap2,DONT_LOOP_SOUND);

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

                    Drawable d = getImage(secondCard.getIdOfPic(), imageButton.getWidth(), imageButton.getHeight());

                    imageButton.setImageDrawable(d);

                    imageButton.setEnabled(false);

                    playSoundPoolSound(soundTap2,DONT_LOOP_SOUND);

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

                    Drawable d = getImage(thirdCard.getIdOfPic(), imageButton.getWidth(), imageButton.getHeight());

                    imageButton.setImageDrawable(d);

                    imageButton.setEnabled(false);

                    playSoundPoolSound(soundTap2,DONT_LOOP_SOUND);

                    break;
                }
            }
        }

        //With two different cards selected, test for match.
        if(isMatch(firstCard,secondCard) && isMatch(secondCard,thirdCard) )
        {
            matchCount++;
            score += 100;

            playSoundPoolSound(soundMatch,DONT_LOOP_SOUND);

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

            //Stops timer at Game Over.
            toggleTimer = false;

            mp.reset();//Stop the music playing when game is over.

            playSoundPoolSound(soundWin1,DONT_LOOP_SOUND);

            showHighScores(); //Check if new high score is reached.  If so, add to new entry to DB, else do nothing.

        }

    }

    private void showGameOver()
    {
        gameOverDialogFragment = new GameOverDialogFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        gameOverDialogFragment.setCancelable(false);

        gameOverDialogFragment.show(ft, GAME_OVER_DIALOG_TAG);
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
                        ||tempName.startsWith("tut_")
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

    //If both cards are assigned the same R.drawable.imageblahblahblah then they are considered the same.  i.e.  a match has been made.
    private boolean isMatch(Card c1, Card c2)
    {
        return c1.getIdOfPic() == c2.getIdOfPic();
    }

    private void playSoundPoolSound(final int soundId, final int loop)//int: loop mode (0 = no loop, -1 = loop forever)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            public void run()
            {

                sp.play(soundId, 1, 1, 0, loop, 1);

            }
        },0);

    }

    private void playMediaPlayerSound(final boolean loop)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            public void run()
            {

                mp.setLooping(loop);
                mp.start();

            }
        },0);

    }

    //Stops SoundPool sound.
    private void stopSound(int streamID)
    {
       sp.stop(streamID);
    }

    private void stopAllSounds()
    {
        //All MediaPlayer
        mp.stop();

        //All SoundPool
        stopSound(soundTap1);
        stopSound(soundTap2);
        stopSound(soundMatch);
        stopSound(soundWin1);
    }

    public static class GameOverDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            final View v = inflater.inflate(R.layout.gameover, null);

            final TextView text = (TextView)v.findViewById(R.id.textViewScore);

            text.setText(String.format(Locale.getDefault(),"%d", score));

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

            /* These values are in Pixels.  It's just enough to fit the message */
            params.width    = 100;
            params.height   = 75;

            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

            super.onResume();
        }
    }

    //Properly free memory no longer used by MediaPlayer.
    private void releaseMediaPlayer()
    {
        if(mp != null)
        {
            mp.release();
            mp = null;
        }
    }

    //Properly free memory no longer used by SoundPool.
    private void releaseSoundPool()
    {
        if(sp != null)
        {
            sp.release();
            sp = null;
        }
    }

}

/* try to change orientation for 4x8 grids: GridLayout.setOrientation(int orientation) */