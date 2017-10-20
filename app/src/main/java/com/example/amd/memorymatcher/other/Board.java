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
                numOfCards,
                totalMatches,//The number of total possible matches that can be made.
                matchType;//Matches are made in pairs (2), triples (3), ... 4..5..6  whatever.

    private ArrayList<Card> cards;

    public Board(int r, int c, int matchType)
    {
        rows = r;
        cols = c;

        numOfCards = r * c;

        cards = new ArrayList<Card>();

        totalMatches = ( rows * cols ) / matchType;
    }

    public void addCard(Card c)
    {
        cards.add(c);
    }

    public void setCards(ArrayList<Card> cards){this.cards = cards;}

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

    public int getRows(){return rows;}

    public int getColumns(){return cols;}

    public int getTotalMatches(){return totalMatches;}

}
