package postpc.yonz.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


public class BoardMenuFragment extends Fragment implements View.OnClickListener, PostClick, FragmentState{

    View view;
    private ImageButton hintButton, slideMenuButton, verifiedButton;
    private Animation slideRightAnimation, slideLeftAnimation;
    private RelativeLayout slideRightLayout;
    private boolean isMenuOpen = false;
    InputListener activityCommander;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.board_menu_layout, container, false);

        ImageButton mainMenuButton = view.findViewById(R.id.main_menu_button);
        mainMenuButton.setOnClickListener(this);

        verifiedButton = view.findViewById(R.id.verified_button);
        verifiedButton.setOnClickListener(this);

        slideRightAnimation = AnimationUtils.loadAnimation(getContext().getApplicationContext(),
                R.anim.slide_right);
        slideLeftAnimation = AnimationUtils.loadAnimation(getContext().getApplicationContext(),
                R.anim.slide_left);

        slideRightLayout = view.findViewById(R.id.slide_down_layout);

        hintButton = view.findViewById(R.id.hint_button);
        hintButton.setOnClickListener(this);

        slideMenuButton = view.findViewById(R.id.slide_menu_button);
        slideMenuButton.setOnClickListener(this);

        ImageButton solveButton = view.findViewById(R.id.solve_button);
        solveButton.setOnClickListener(this);

        ImageButton undoButton = view.findViewById(R.id.undo_button);
        undoButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hint_button:
                if(isMenuOpen) closeMenu();
                activityCommander.hint();
                break;

            case R.id.slide_menu_button:
                if(isMenuOpen) closeMenu();
                else openMenu();
                break;

            case R.id.solve_button:
                closeMenu();
                activityCommander.solve();
                break;

            case R.id.undo_button:
                activityCommander.undo();
                break;

            case R.id.main_menu_button:
                activityCommander.returnToMainMenu();
                break;

            case R.id.verified_button:
                activityCommander.completeVerification();
                break;
        }
    }

    public void openMenu(){
        slideRightLayout.setVisibility(View.VISIBLE);
        slideRightLayout.startAnimation(slideRightAnimation);
        slideMenuButton.animate().rotation(180).setDuration(500);

        isMenuOpen = true;
    }

    public void closeMenu(){
        slideRightLayout.startAnimation(slideLeftAnimation);
        slideMenuButton.animate().rotation(-0).setDuration(500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                slideRightLayout.setVisibility(View.GONE);
            }}, 200);

        isMenuOpen = false;
    }

    @Override
    public void onPostClick(int id, boolean result) {
        if (!result){
            this.view.findViewById(id).startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.wobble));
        }
    }

    @Override
    public void onVerificationState() {
        hintButton.setVisibility(View.INVISIBLE);
        slideMenuButton.setVisibility(View.INVISIBLE);
        verifiedButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPlayingState() {
        hintButton.setVisibility(View.VISIBLE);
        slideMenuButton.setVisibility(View.VISIBLE);
        verifiedButton.setVisibility(View.INVISIBLE);
    }

    interface InputListener {
        void hint();
        void undo();
        void solve();
        void completeVerification();
        void returnToMainMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            activityCommander = (InputListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

}
