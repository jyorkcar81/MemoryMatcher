package com.example.amd.memorymatcher.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.GridLayout;

import java.util.ArrayList;

import com.example.amd.memorymatcher.other.Card;

/*  ************************************************************************************************
    * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE *
 *  ************************************************************************************************
 *
 *  Copyright 2017 by Jed York.  Copyrighted material cannot be used without express written consent.
 *  Unlawful reproduction of material forfeits all earned moneys.  If lawsuit is sought for forfeiture of damages,
 *  damages will be doubled and any and all of your rights are waived.
 */

public class RetainedFragment extends Fragment
{
    // data object we want to retain
    private ArrayList<Card> list;

    private GridLayout grid;

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
