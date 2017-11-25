package com.amdone.amd.memorymatcher.other;

/*  ************************************************************************************************
    * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE * COPYRIGHT NOTICE *
 *  ************************************************************************************************
 *
 *  Copyright 2017 by Jed York.  Copyrighted material cannot be used without express written consent.
 *  Unlawful reproduction of material forfeits all earned moneys.  If lawsuit is sought for forfeiture of damages,
 *  damages will be doubled and any and all of your rights are waived.
 */

public class Card
{
        private int idOfImageButton;
        private int idOfPicture;

        public Card(int idOfPicture)
        {
            this.idOfPicture        = idOfPicture;
        }

        public void setIdOfImageButton(int idOfImageButton)
        {
            this.idOfImageButton    =   idOfImageButton;
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
