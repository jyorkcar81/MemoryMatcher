package com.example.amd.memorymatcher.other;

import android.graphics.Point;


/**
 * Created by AMD on 10/13/2017.
 */

public class Card
{
        private int idOfImageButton;
        private int idOfPicture;
        private boolean showing;//Card is showing front if true (animal image), else back (solid color or other design).
        private int idOfImageButt;


        public Card(int idOfImageButton, int idOfPicture, boolean showing)
        {
            this.idOfImageButton    = idOfImageButton;
            this.idOfPicture        = idOfPicture;
            this.showing            = showing ;

        }

        public void setShowing(boolean showing)
        {
            this.showing = showing;
        }

        public boolean isShowing()
        {
            return showing;
        }

        public int getIdOfImageButton()
        {
            return idOfImageButton;
        }

        public int getIdOfPic()
        {
            return idOfPicture;
        }


}
