package com.example.amd.memorymatcher.other;

import android.graphics.Point;


/**
 * Created by AMD on 10/13/2017.
 */

public class Card
{
        private int idOfImageButton;
        private int idOfPicture;
        //Card is showing front if true (animal image), else back (solid color or other design).

        public Card(int idOfPicture)
        {
            this.idOfPicture        = idOfPicture;
        }

        public void setIdOfImageButton(int idOfImageButton){this.idOfImageButton=idOfImageButton;}

        public int getIdOfImageButton()
        {
            return idOfImageButton;
        }

        public int getIdOfPic()
        {
            return idOfPicture;
        }

}
