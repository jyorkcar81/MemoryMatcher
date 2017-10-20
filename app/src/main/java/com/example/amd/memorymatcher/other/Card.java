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
        private boolean matched;


        public Card(int idOfPicture)
        {
            this.idOfPicture        = idOfPicture;
            matched                 = false;
            showing                 = false;
        }

        public void setShowing(boolean showing)
        {
            this.showing = showing;
        }

        public void setMatched(boolean b){matched=b;}

        public void setIdOfImageButton(int idOfImageButton){this.idOfImageButton=idOfImageButton;}

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

        public boolean isMatched(){return matched;}

}
