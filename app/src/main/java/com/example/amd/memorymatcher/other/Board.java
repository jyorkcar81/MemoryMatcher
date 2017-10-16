package com.example.amd.memorymatcher.other;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by AMD on 10/13/2017.
 */

public class Board
{
    private int rows,
                cols,
                numOfCards;

    private ArrayList<Card> cards;

    public Board(int r, int c)
    {
        rows = r;
        cols = c;

        numOfCards = r * c;

        cards = new ArrayList<Card>();
    }

    public void addCard(Card c)
    {
        cards.add(c);
    }

    public ArrayList<Card> getCards()
    {
         return cards;
    }

    public void shuffle()
    {
        Collections.shuffle(cards);
    }

    public int getNumOfCards()
    {
        return numOfCards;
    }
}
