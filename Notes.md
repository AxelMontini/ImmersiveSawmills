## Notes to keep the modding style the same:

##Notes to understand better:
* The "*pos*" in *IE's custom TileEntity class* is the number of blocks from the
bottom-left-front corner. It looks like the actual pos is calculated as the number
of blocks following a specific order, so  
**front-bottom-left corner** &rarr; **full width** &rarr; **full length** &rarr;
**full height**  
or  
    * **loop**(*height*)  
       * **loop**(*length*)  
          * **loop**(*width*)
