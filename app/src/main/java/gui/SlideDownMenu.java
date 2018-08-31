package gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import postpc.yonz.main.R;


public class SlideDownMenu extends RelativeLayout {
    private ImageView undoButton;
    private ImageView solveButton;

    public SlideDownMenu(Context context) {
        super(context);
        init();
    }

    public SlideDownMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideDownMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.slidemenu_layout, this);
    }

}
