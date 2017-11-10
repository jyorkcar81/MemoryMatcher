package com.example.amd.memorymatcher.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.GridLayout;

import com.example.amd.memorymatcher.other.Card;

import java.util.ArrayList;

/**
 * Created by AMD on 11/7/2017.
 */

public class RetainedFragment extends Fragment
{
    // data object we want to retain
    private ArrayList<Card> list;

    private GridLayout grid;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(ArrayList<Card> list) {
        this.list = list;
    }

    public void setGridLayout(GridLayout grid)
    {
        this.grid = grid;
    }

    public ArrayList<Card> getList() {
        return list;
    }

    public GridLayout getGrid()
    {
        return grid;
    }

}
