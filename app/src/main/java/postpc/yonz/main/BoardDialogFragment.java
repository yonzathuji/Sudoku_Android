package postpc.yonz.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class BoardDialogFragment extends Fragment implements View.OnClickListener, PostClick, FragmentState {

    View view;
    Button input1, input2, input3,
           input4, input5, input6,
           input7, input8, input9;
    ImageButton deleteButton, valueButton;
    SparseIntArray buttonToValueMap, valueToButtonMap;
    boolean isInsertValue = true; // true for value, false for notes


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.board_dialog_layout, container, false);
        initInputButtons(view);

        deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCommander.deleteValue();
            }
        });

        valueButton = view.findViewById(R.id.value_button);
        valueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInsertValue = !isInsertValue;
                if (isInsertValue) {
                    valueButton.setImageResource(R.drawable.icon_insert);
                }
                else {
                    valueButton.setImageResource(R.drawable.icon_notes);
                }
            }
        });

        return view;
    }

    private void initInputButtons(View view) {
        buttonToValueMap = new SparseIntArray();
        valueToButtonMap = new SparseIntArray();

        input1 = view.findViewById(R.id.input1);
        input1.setOnClickListener(this);
        input2 = view.findViewById(R.id.input2);
        input2.setOnClickListener(this);
        input3 = view.findViewById(R.id.input3);
        input3.setOnClickListener(this);
        input4 = view.findViewById(R.id.input4);
        input4.setOnClickListener(this);
        input5 = view.findViewById(R.id.input5);
        input5.setOnClickListener(this);
        input6 = view.findViewById(R.id.input6);
        input6.setOnClickListener(this);
        input7 = view.findViewById(R.id.input7);
        input7.setOnClickListener(this);
        input8 = view.findViewById(R.id.input8);
        input8.setOnClickListener(this);
        input9 = view.findViewById(R.id.input9);
        input9.setOnClickListener(this);

        buttonToValueMap.put(R.id.input1, 1);
        buttonToValueMap.put(R.id.input2, 2);
        buttonToValueMap.put(R.id.input3, 3);
        buttonToValueMap.put(R.id.input4, 4);
        buttonToValueMap.put(R.id.input5, 5);
        buttonToValueMap.put(R.id.input6, 6);
        buttonToValueMap.put(R.id.input7, 7);
        buttonToValueMap.put(R.id.input8, 8);
        buttonToValueMap.put(R.id.input9, 9);

        valueToButtonMap.put(1, R.id.input1);
        valueToButtonMap.put(2, R.id.input2);
        valueToButtonMap.put(3, R.id.input3);
        valueToButtonMap.put(4, R.id.input4);
        valueToButtonMap.put(5, R.id.input5);
        valueToButtonMap.put(6, R.id.input6);
        valueToButtonMap.put(7, R.id.input7);
        valueToButtonMap.put(8, R.id.input8);
        valueToButtonMap.put(9, R.id.input9);

    }

    @Override
    public void onClick(View v) {
        int value = buttonToValueMap.get(v.getId());
        if (isInsertValue){
            activityCommander.insertValue(value);
        }
        else {
            activityCommander.insertNote(value);
        }
    }

    @Override
    public void onPostClick(int id, boolean result) {
        if (!result){
            view.findViewById(id).startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.wobble));
        }
    }

    InputListener activityCommander;

    @Override
    public void onVerificationState() {
        valueButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPlayingState() {
        valueButton.setVisibility(View.VISIBLE);
    }

    interface InputListener {
        void deleteValue();

        void insertValue(int value);

        void insertNote(int noteValue);
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
