package com.luminous.dsys.youthconnect.swipemenu;

import android.widget.BaseAdapter;

/**
 * Created by Android Luminous on 2/27/2016.
 */
public abstract class BaseSwipListAdapter extends BaseAdapter {

    public boolean getSwipEnableByPosition(int position){
        return true;
    }
}
