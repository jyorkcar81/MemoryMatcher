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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog.Builder;
import com.example.amd.memorymatcher.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HighScoresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HighScoresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HighScoresFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String score;
    private String name;
    private String rank;


    private static int possibleHighScore;//After a game is completed the score is comapred to the entries in the DB.  It's then deteremined a high score or not.

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "memoryMatcher";

    private static final String TABLE_NAME = "highscores";

    private static final String ID_COLUMN_NAME = "_id";

    private static final String SCORE_COLUMN_NAME = "score";

    private static final String NAME_COLUMN_NAME = "name";



    private static int MAX_USERNAME_LENGTH=15;//Max_length for userprovided name for recording new high scores.

    private SQLiteDatabase db;
    private Helper dbHelper;

    private Cursor cursor;

    private Button button;

    private static String username;

    private TableLayout table;
    private static final int ROWS_IN_TABLE_LAYOUT=5;//Total number of records (high scores) that will be shown on the screen.

    private OnFragmentInteractionListener mListener;

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
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            possibleHighScore=getArguments().getInt(ARG_PARAM1);
            Log.d("highscore",possibleHighScore+"");
            //mParam1 = getArguments().getString(ARG_PARAM1);
           // mParam2 = getArguments().getString(ARG_PARAM2);

        }


        //FOR TESTING ONLY.  DELETE DB IF ALREADY EXISTS.
        getActivity().deleteDatabase(DATABASE_NAME);

        dbHelper = new Helper(getActivity(),DATABASE_NAME,null,DATABASE_VERSION);
        Log.d("getApplicationContext",""+getActivity());




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_high_scores, container, false);
        table = (TableLayout)v.findViewById(R.id.tableLayoutHighScores);

        button = (Button)v.findViewById(R.id.buttonDropDB);

        button.setOnClickListener(this);

        if(true)//isNewHighScore())
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            //If new high score is reached by the game player, then 1.  Get name.  2.  Insert new record in DB.
            UsernameDialogFragment dialog = new UsernameDialogFragment();

            dialog.show(ft, "dialog");









        }
        else
        {

        }

        updateUI();

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    //Query the DB for the list of high scores, then display them.
    private void updateUI()
    {
        db = dbHelper.getReadableDatabase();

        Log.d("db",db.toString());

        //Query the DB for the list of high scores, then display them.  From High Score to Lowest Score.
        cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+SCORE_COLUMN_NAME+" DESC LIMIT "+ROWS_IN_TABLE_LAYOUT,null);

        boolean hasRows = cursor.moveToFirst();

        TableRow row;
        int i=1;
        String rank="1";

        while(hasRows)
        {
            row = (TableRow)table.getChildAt(i);

            name = cursor.getString(1);
            score = Integer.valueOf(cursor.getInt(2)).toString();

            ((TextView)row.getChildAt(0)).setText(rank);
            ((TextView)row.getChildAt(1)).setText(name);
            ((TextView)row.getChildAt(2)).setText(score);

            Log.d("db_entry_name",name);

            rank = Integer.valueOf(Integer.parseInt(rank) + 1).toString();
            i++;

            hasRows = cursor.moveToNext();
        }



    }

    public void onClick(View v)
    {
        db = dbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

        updateUI();
    }


    public boolean isNewHighScore()//Determine if the new score is a high score.
    {

        db = dbHelper.getReadableDatabase();

        //Query the DB for the list of high scores looking to see if the new score is a high score.
        cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+SCORE_COLUMN_NAME+" DESC LIMIT "+ROWS_IN_TABLE_LAYOUT,null);

        boolean hasRows = cursor.moveToFirst();

        TableRow row;
        int i=1;
        String rank="1";

        while(hasRows)
        {
            row = (TableRow)table.getChildAt(i);

            name = cursor.getString(1);
            score = Integer.valueOf(cursor.getInt(2)).toString();

            rank = Integer.valueOf(Integer.parseInt(rank) + 1).toString();
            i++;

            if(Integer.valueOf(Integer.parseInt(rank)) >= 5)//Check if new score is greater than lowest of the high scores, this is the 5th one.
            {
                if(possibleHighScore >= Integer.valueOf(Integer.parseInt(score)))
                {
                    return true;
                }
            }

            hasRows = cursor.moveToNext();
        }


        return false;
    }

    public void insertScore()//Adds a new high score to the DB.
    {
        db = dbHelper.getWritableDatabase();
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

            cv.put(SCORE_COLUMN_NAME,500);
            cv.put(NAME_COLUMN_NAME,"John N.");


            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(SCORE_COLUMN_NAME,1500);
            cv.put(NAME_COLUMN_NAME,"Ash A.");


            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(SCORE_COLUMN_NAME,2300);
            cv.put(NAME_COLUMN_NAME,"Elizabeth B.");

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(SCORE_COLUMN_NAME,1400);
            cv.put(NAME_COLUMN_NAME,"Freddyyyyyyy Q.");

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv.clear();

            cv.put(SCORE_COLUMN_NAME,100);
            cv.put(NAME_COLUMN_NAME,"Google Inc.");

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            //db.execSQL();//drop tables
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
                            // sign in the user ...

                            username = text.getText().toString();

                            //If usernamer exceeds 15 chars, then it needs to be truncated.
                            if(username.length() > MAX_USERNAME_LENGTH)
                            {
                                username = username.substring(0,MAX_USERNAME_LENGTH);
                            }

                            Log.d("user",username);

                            //Limit the name to a certain length.

                            //username must be at least 1 character... i.e.  The user must have entered a name then pressed OK.

                            if(username.length() >= 1)
                            {
                                ContentValues cv = new ContentValues();

                                cv.put(SCORE_COLUMN_NAME,possibleHighScore);
                                cv.put(NAME_COLUMN_NAME,username);

                                Log.d("possibleHigh",possibleHighScore+"");
                                Log.d("uname",username);

                                // db.insert(TABLE_NAME,null,cv);

                                cv.clear();
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
