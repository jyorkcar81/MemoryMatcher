package com.example.amd.memorymatcher.fragments;

import android.content.ContentValues;
import android.content.Context;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "memoryMatcher";

    private static final String TABLE_NAME = "highscores";

    private static final String ID_COLUMN_NAME = "id";

    private static final String SCORE_COLUMN_NAME = "score";

    private static final String NAME_COLUMN_NAME = "name";

    private SQLiteDatabase db;
    private Helper dbHelper;

    private Cursor cursor;

    private Button button;

    private OnFragmentInteractionListener mListener;

    public HighScoresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HighScoresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HighScoresFragment newInstance(String param1, String param2) {
        HighScoresFragment fragment = new HighScoresFragment();
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

        }

        dbHelper = new Helper(getActivity().getApplicationContext(),DATABASE_NAME,null,DATABASE_VERSION);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_high_scores, container, false);

        button = (Button)v.findViewById(R.id.buttonDropDB);

        button.setOnClickListener(this);

        updateUI(v);

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


    private void calcRank()//Calculates rank on-the-fly instead of storing directly in DB.  Maybe create a list and QuickSort it.
    {

    }

    private void updateUI(View v)
    {


        TableLayout table = (TableLayout)v.findViewById(R.id.tableLayoutHighScores);

        //Query the DB for the list of high scores, then display them.
        cursor = db.query(TABLE_NAME,null,null,null,null,null,null);
        cursor.moveToFirst();

        Log.d("db_entries",""+cursor.getCount());

        TableRow row;
        int i=1;
        while(cursor.moveToNext())
        {
            row = (TableRow)table.getChildAt(i);

            rank = cursor.getString(0);
            name = cursor.getString(1);
            score = cursor.getInt(2)+"";

            ((TextView)row.getChildAt(0)).setText(rank);
            ((TextView)row.getChildAt(1)).setText(name);
            ((TextView)row.getChildAt(2)).setText(score);

            Log.d("db_entry_name",name);

            i++;
        }

        closeCursor();
        closeDB();
    }

    public void onClick(View v)
    {


  /*      db = dbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);

        closeDB();*/
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
        Helper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            //Create tables then populate with any default values.
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME+"(" +
                    ID_COLUMN_NAME+"     INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SCORE_COLUMN_NAME+"  INTEGER NOT NULL," +
                    NAME_COLUMN_NAME+"   TEXT NOT NULL UNIQUE" +
                    ");");

            //Add some default data.

            sqLiteDatabase = this.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put(SCORE_COLUMN_NAME,"John N.");
            cv.put(NAME_COLUMN_NAME,500);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv = new ContentValues();

            cv.put(SCORE_COLUMN_NAME,"Ash A.");
            cv.put(NAME_COLUMN_NAME,1500);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv = new ContentValues();

            cv.put(SCORE_COLUMN_NAME,"Elizabeth B.");
            cv.put(NAME_COLUMN_NAME,2300);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv = new ContentValues();

            cv.put(SCORE_COLUMN_NAME,"Freddy Q.");
            cv.put(NAME_COLUMN_NAME,1400);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            cv = new ContentValues();

            cv.put(SCORE_COLUMN_NAME,"Google Inc.");
            cv.put(NAME_COLUMN_NAME,100);

            sqLiteDatabase.insert(TABLE_NAME,null,cv);

            //closedb.
            if(sqLiteDatabase != null)
            {
                sqLiteDatabase.close();
            }

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            //db.execSQL();//drop tables
            onCreate(db);
        }

    }




}
