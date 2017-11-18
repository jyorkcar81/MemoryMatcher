package com.example.amd.memorymatcher.other;

import java.util.ArrayList;
import java.util.Collections;

/*  ************************************************************************************************
    * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE *
 *  ************************************************************************************************
 *
 *  Copyright 2017 by Jed York.  Copyrighted material cannot be used without express written consent.
 *  Unlawful reproduction of material forfeits all earned moneys.  If lawsuit is sought for forfeiture of damages,
 *  damages will be doubled and any and all of your rights are waived.
 */

public class Board
{
    private int rows,
                cols,
                numOfCards,
                totalMatches;//The number of total possible matches that can be made.

    public Board(int r, int c, int matchType)//Matches are made in pairs (2), triples (3), ... 4..5..6  whatever.
    {
        rows = r;
        cols = c;

        numOfCards = r * c;

        totalMatches = ( rows * cols ) / matchType;
    }

    public int getNumOfCards()
    {
        return numOfCards;
    }

    public int getTotalMatches()
    {
        return totalMatches;
    }

}
