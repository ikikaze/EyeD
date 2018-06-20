package ro.lockdowncode.eyedread.UI;

import android.content.res.Resources;
import ro.lockdowncode.eyedread.Utils.Type;

public class RectSizeHandler {




    //return array of 2 elements , width and height of rect based on type
    public int[] getRectSizes(Type type)
    {
        int[] widthHeight = {0,0};
        double objHeight = 0 ,objWidth = 0;
        //lower as to not extend to edges of screen


        int sWidth = (int)(getScreenWidth() * 0.6);
        int sHeight = (int)(getScreenHeight()*0.6);

        switch (type)
        { //sizes in mm;
            case PERMIS: objHeight = 54;objWidth = 85.6; break;
            case BULETIN: objHeight = 74; objWidth = 105; break;
            case PASAPORT: objHeight = 88; objWidth = 125;break;
            default: break; //this should never ever happen
        }

        widthHeight[0] = (int)(sWidth/objWidth * objWidth);
        widthHeight[1] = (int)(sWidth/objWidth * objHeight);

        return widthHeight;
    }




    private int getScreenWidth()
    {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight()
    {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
