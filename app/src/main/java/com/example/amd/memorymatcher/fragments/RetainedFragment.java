package com.example.amd.memorymatcher.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.example.amd.memorymatcher.other.Card;

import java.util.ArrayList;

/**
 * Created by AMD on 11/7/2017.
 */

public class RetainedFragment extends Fragment
{
    // data object we want to retain
    private ArrayList<Card> list;

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

    public ArrayList<Card> getList() {
        return list;
    }


}
