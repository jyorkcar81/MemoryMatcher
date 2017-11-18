package com.example.amd.memorymatcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.amd.memorymatcher.R;

/*  ************************************************************************************************
    * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE *
 *  ************************************************************************************************
 *
 *  Copyright 2017 by Jed York.  Copyrighted material cannot be used without express written consent.
 *  Unlawful reproduction of material forfeits all earned moneys.  If lawsuit is sought for forfeiture of damages,
 *  damages will be doubled and any and all of your rights are waived.
 */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HighScoresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HighScoresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HighScoresFragment extends Fragment implements View.OnClickListener{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "memoryMatcher";

    private static final String TABLE_NAME = "highscores";

    private static final String ID_COLUMN_NAME = "_id";

    private static final String SCORE_COLUMN_NAME = "score";

    private static final String NAME_COLUMN_NAME = "name";

    private static int MAX_USERNAME_LENGTH = 15;//Max_length for userprovided name for recording new high scores.

    private static final int ROWS_IN_TABLE_LAYOUT = 5;//Total number of records (high scores) that will be shown on the screen.

    private static final String USERNAME_DIALOG_TAG = "usernamedialog";

    private static int possibleHighScore;//After a game is completed the score is comapred to the entries in the DB.  It's then determined a high score or not.

    private static SQLiteDatabase db;
    private static Helper dbHelper;
    private static Cursor cursor;

    private static TableLayout table;

    private Button button;

    private static String username;

    private OnFragmentInteractionListener mListener;

    private String mParam1;
    private String mParam2;

    public HighScoresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param score Parameter 1.
     * @return A new instance of fragment HighScoresFragment.
     */

    public static HighScoresFragment newInstance(int score) {
        HighScoresFragment fragment = new HighScoresFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, score);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            possibleHighScore = getArguments().getInt(ARG_PARAM1);

        }

        //FOR TESTING ONLY.  DELETE DB IF ALREADY EXISTS.
        //getActivity().deleteDatabase(DATABASE_NAME);

        dbHelper = new Helper(getActivity(),DATABASE_NAME,null,DATABASE_VERSION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_high_scores, container, false);
        table = (TableLayout)v.findViewById(R.id.tableLayoutHighScores);

        button = (Button)v.findViewById(R.id.buttonDropDB);

        button.setOnClickListener(this);

        updateTableLayout();

        if(isNewHighScore())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            //If new high score is reached by the game player, then 1.  Get name.  2.  Insert new record in DB.
            UsernameDialogFragment dialog = new UsernameDialogFragment();

            dialog.setCancelable(false);

            dialog.show(ft, USERNAME_DIALOG_TAG);

            v.invalidate();
        }

        return v;
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
        closeCursor();
        closeDB();
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

    //Query the DB for the list of high scores, then display them.
    private static void updateTableLayout()
    {
        TableRow row;

        int i=1;

        String  rank="1",
                name,
                score;

        db = dbHelper.getReadableDatabase();

        //Query the DB for the list of high scores, then display them.  From High Score to Lowest Score.
        cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+SCORE_COLUMN_NAME+" DESC LIMIT "+ROWS_IN_TABLE_LAYOUT,null);

        boolean hasRows = cursor.moveToFirst();

        //If table in the DB is empty, then show a blank tableLayout.
        if(!hasRows)
        {
            for(int x=1; x<table.getChildCount(); x++)//Note:  row 0 is the headers of the table.
            {
                row = (TableRow)table.getChildAt(x);

                ((TextView)row.getChildAt(0)).setText("");
                ((TextView)row.getChildAt(1)).setText("");
                ((TextView)row.getChildAt(2)).setText("");
            }
        }

        while(hasRows)
        {
            row = (TableRow)table.getChildAt(i);

            name = cursor.getString(1);
            score = Integer.valueOf(cursor.getInt(2)).toString();

            ((TextView)row.getChildAt(0)).setText(rank);
            ((TextView)row.getChildAt(1)).setText(name);
            ((TextView)row.getChildAt(2)).setText(score);

            rank = Integer.valueOf(Integer.parseInt(rank) + 1).toString();

            i++;

            hasRows = cursor.moveToNext();
        }

    }

    public void onClick(View v)
    {
        db = dbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM "+TABLE_NAME);//similar to TRUNCATE TABLE.

        updateTableLayout();
    }

    public boolean isNewHighScore()//Determine if the new score is a high score.
    {
        String score="";
        int count;

        db = dbHelper.getReadableDatabase();

        //Query the DB for the list of high scores looking to see if the new score is a high score.
        cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+SCORE_COLUMN_NAME+" DESC LIMIT "+ROWS_IN_TABLE_LAYOUT,null);

        count = cursor.getCount();//Will be 5 or less because of LIMIT.

        if(count == ROWS_IN_TABLE_LAYOUT)
        {
            cursor.moveToLast();
            score = Integer.valueOf(cursor.getInt(2)).toString();
            if(possibleHighScore >= Integer.valueOf(Integer.parseInt(score)))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if(count < ROWS_IN_TABLE_LAYOUT)//0-to-4
        {
            /* Only use high scores of 1 or greater.  i.e.   1...999,999,9999,9999,9999 */
            if(possibleHighScore >= 1 )
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        else /* Technically, unreachable because LIMIT will never return a value greater than count=ROWS_IN_TABLE_LAYOUT */
        {
            return false;
        }

    }

    private void closeDB()
    {
        if(db != null)
        {
            db.close();
        }
    }

    private void closeCursor()
    {
        if(cursor != null)
        {
            cursor.close();
        }
    }

    private static class Helper extends SQLiteOpenHelper
    {
        public Helper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            //Create tables then populate with any default values.
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME+" (" +
                    ID_COLUMN_NAME+"     INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COLUMN_NAME+"   TEXT NOT NULL UNIQUE, " +
                    SCORE_COLUMN_NAME+"  INTEGER NOT NULL " +
                    ");");

            //Add some default data.

            ContentValues cv = new ContentValues();

            cv.put(NAME_COLUMN_NAME,"John N.");
            cv.put(SCORE_COLUMN_NAME,500);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(NAME_COLUMN_NAME,"Ash A.");
            cv.put(SCORE_COLUMN_NAME,1500);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(NAME_COLUMN_NAME,"Elizabeth B.");
            cv.put(SCORE_COLUMN_NAME,2300);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(NAME_COLUMN_NAME,"Freddyyyyyyy Q.");
            cv.put(SCORE_COLUMN_NAME,1400);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(NAME_COLUMN_NAME,"Google Inc.");
            cv.put(SCORE_COLUMN_NAME,100);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DELETE FROM "+TABLE_NAME);//truncate table(s).
            onCreate(db);
        }

    }

    public static class UsernameDialogFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            final View v = inflater.inflate(R.layout.custom_user, null);

            final EditText text = (EditText)v.findViewById(R.id.editTextUsername);

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(v)
                    // Add action buttons
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {

                            username = text.getText().toString();

                            //If usernamer exceeds 15 chars, then it needs to be truncated.
                            if(username.length() > MAX_USERNAME_LENGTH)
                            {
                                username = username.substring(0,MAX_USERNAME_LENGTH);
                            }

                            //Limit the name to a certain length.

                            //username must be at least 1 character... i.e.  The user must have entered a name then pressed OK.

                            if(username.length() >= 1)
                            {
                                ContentValues cv = new ContentValues();

                                cv.put(NAME_COLUMN_NAME,username);
                                cv.put(SCORE_COLUMN_NAME,possibleHighScore);

                                //Database may need to be opened and writable to make the insert.

                                db = dbHelper.getWritableDatabase();

                                String score="";
                                int count;

                                //Query the DB for the list of high scores looking to see if the new score is a high score.
                                cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+SCORE_COLUMN_NAME+" DESC LIMIT "+ROWS_IN_TABLE_LAYOUT,null);

                                count = cursor.getCount();//Will be ROWS_IN_TABLE_LAYOUT or less because of LIMIT.

                                if(count == ROWS_IN_TABLE_LAYOUT)
                                {
                                    cursor.moveToLast();
                                    score = Integer.valueOf(cursor.getInt(2)).toString();

                                    if(possibleHighScore >= Integer.valueOf(Integer.parseInt(score)))
                                    {
                                        int rowID = cursor.getInt(0);//Get ID of last row.  It will be used to change the DB entry at the ID for name & score.

                                        db.update(TABLE_NAME,cv,ID_COLUMN_NAME+"="+rowID,null);
                                    }
                                    else
                                    {

                                    }
                                }
                                else if(count < ROWS_IN_TABLE_LAYOUT)//0-to-4
                                {
                                    /* Only use high scores of 1 or greater.  i.e.   1...999,999,9999,9999,9999 */
                                    if(possibleHighScore >= 1 )
                                    {
                                        db.insert(TABLE_NAME,null,cv);
                                    }
                                    else
                                    {
                                        //score is 0, or negative somehow, so store nothing in the DB.
                                    }

                                }
                                else /* Technically, unreachable because LIMIT will never return a value greater than count=ROWS_IN_TABLE_LAYOUT */
                                {
                                    //Save nothing to DB.
                                }

                                updateTableLayout();

                                cv.clear();
                                possibleHighScore = -1;//wipe out the possibleHighScore to prevent it persisting.
                            }

                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            UsernameDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }
    }

}
