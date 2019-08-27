package com.example.viperssc.Util;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {
    public static void setUpBottomNavigationView(BottomNavigationViewEx bottomNavigationView){
        bottomNavigationView.enableAnimation(false);
        bottomNavigationView.enableItemShiftingMode(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.setTextVisibility(true);
    }
}
