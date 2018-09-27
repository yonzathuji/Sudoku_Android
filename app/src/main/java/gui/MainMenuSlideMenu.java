package gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import postpc.yonz.main.R;


public class MainMenuSlideMenu extends RelativeLayout{


    public MainMenuSlideMenu(Context context) {
        super(context);
        init();
    }

    public MainMenuSlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainMenuSlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.main_slidemenu_layout, this);
    }
}
