package gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import postpc.yonz.main.R;


public class BoardSlideMenu extends RelativeLayout {

    public BoardSlideMenu(Context context) {
        super(context);
        init();
    }

    public BoardSlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardSlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.board_slidemenu_layout, this);
    }

}
