package gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import game.SudokuGame;
import game.Tile;
import postpc.yonz.main.R;

public class BoardView extends View {

    public static final int DEFAULT_BOARD_SIZE = 100;

    private float mCellWidth;
    private float mCellHeight;

    private SudokuGame mSudokuGame;
    private Stack<Action> actionsStack;
    private Tile mTouchedTile;
    private Tile mHintedTile;
    private List<Tile> mWrongTiles;

    private Paint mLinePaint;
    private Paint mSectorLinePaint;
    private Paint mCellValuePaint;
    private Paint mCellValueReadonlyPaint;
    private Paint mCellValueHighlighted;
    private Paint mCellNotePaint;

    private Paint mBackgroundColorTouched;
    private Paint mBackgroundColorWrong;
    private Paint mBackgroundColorHinted;

    private int mNumberLeft;
    private int mNumberTop;
    private float mNoteTop;
    private int mSectorLineWidth;
    private int mTouchedValue;

    private boolean isSolved = false;

    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mSudokuGame = new SudokuGame("/storage/emulated/0/Puzzle.txt");
        new Thread(new Runnable(){
            public void run(){
                mSudokuGame.generateSolution();  // todo it returns boolean if solvable
                isSolved = true;
            }
        }).start();

        mWrongTiles = new ArrayList<>();
        actionsStack = new Stack<>();
        
        mLinePaint = new Paint();
        mSectorLinePaint = new Paint();
        mCellValuePaint = new Paint();
        mCellValueReadonlyPaint = new Paint();
        mCellValueHighlighted = new Paint();
        mCellNotePaint = new Paint();
        mBackgroundColorTouched = new Paint();
        mBackgroundColorWrong = new Paint();
        mBackgroundColorHinted = new Paint();

        mCellValuePaint.setAntiAlias(true);
        mCellValueReadonlyPaint.setAntiAlias(true);
        mCellValueHighlighted.setAntiAlias(true);

        mCellValueHighlighted.setTypeface(Typeface.create("Arial", Typeface.BOLD));
        mCellNotePaint.setAntiAlias(true);


        mLinePaint.setColor(getResources().getColor(R.color.gridBorders));

        mSectorLinePaint.setColor(getResources().getColor(R.color.gridBorders));
        mCellValuePaint.setColor(getResources().getColor(R.color.gridValue));
        mCellValueReadonlyPaint.setColor(getResources().getColor(R.color.gridValueReadOnly));
        mCellValueHighlighted.setColor(getResources().getColor(R.color.gridValueHighlighted));
        mCellNotePaint.setColor(getResources().getColor(R.color.gridNote));

        mBackgroundColorTouched.setColor(getResources().getColor(R.color.gridTouchedTile));
        mBackgroundColorWrong.setColor(getResources().getColor(R.color.gridWrongTile));
        mBackgroundColorHinted.setColor(getResources().getColor(R.color.gridHintedTile));

        mTouchedValue = 0;

        context.obtainStyledAttributes(attrs, R.styleable.BoardView).recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = -1, height = -1;
        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        } else {
            width = DEFAULT_BOARD_SIZE;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize){
                width = widthSize;
            }
        }
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else {
            height = DEFAULT_BOARD_SIZE;
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize){
                height = heightSize;
            }
        }

        if (widthMode != MeasureSpec.EXACTLY){
            width = height;
        }

        if (heightMode != MeasureSpec.EXACTLY){
            height = width;
        }

        if (widthMode == MeasureSpec.AT_MOST && width > widthSize){
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize){
            height = heightSize;
        }

        mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
        mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

        setMeasuredDimension(width, height);

        float cellTextSize = mCellHeight * 0.75f;
        mCellValuePaint.setTextSize(cellTextSize);
        mCellValueReadonlyPaint.setTextSize(cellTextSize);
        mCellValueHighlighted.setTextSize(cellTextSize);
        mCellNotePaint.setTextSize(mCellHeight / 3.0f);
        // compute offsets in each cell to center the rendered number
        mNumberLeft = (int) ((mCellWidth - mCellValuePaint.measureText("9")) / 2);
        mNumberTop = (int) ((mCellHeight - mCellValuePaint.getTextSize()) / 2);

        // add some offset because in some resolutions notes are cut-off in the top
        mNoteTop = mCellHeight / 50.0f;

        int sizeInPx = width < height ? width : height;
        float dipScale = getContext().getResources().getDisplayMetrics().density;
        float sizeInDip = sizeInPx / dipScale;

        float sectorLineWidthInDip = 2.0f;

        if (sizeInDip > 150){
            sectorLineWidthInDip = 3.0f;
        }

        mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        while (!isSolved){}

        int width = getWidth() - getPaddingRight();
        int height = getHeight() - getPaddingBottom();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        // draw cells
        int cellLeft, cellTop;
        if (mSudokuGame != null){

            float numberAscent = mCellValuePaint.ascent();
            float noteAscent = mCellNotePaint.ascent();
            float noteWidth = mCellWidth / 3f;

            if (mTouchedTile != null){
                cellLeft = Math.round(mTouchedTile.x * mCellWidth) + paddingLeft;
                cellTop = Math.round(mTouchedTile.y * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft, paddingTop,
                        cellLeft + mCellWidth, height,
                        mBackgroundColorTouched);
                canvas.drawRect(
                        paddingLeft, cellTop,
                        width, cellTop + mCellHeight,
                        mBackgroundColorTouched);
            }

            if (mHintedTile != null){
                cellLeft = Math.round(mHintedTile.x * mCellWidth) + paddingLeft;
                cellTop = Math.round(mHintedTile.y * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft,  cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorHinted);
            }

            for (Tile wrongTile : mWrongTiles){
                cellLeft = Math.round(wrongTile.x * mCellWidth) + paddingLeft;
                cellTop = Math.round(wrongTile.y * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft,  cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorWrong);
            }


            for (int row = 0; row < 9; row++){
                for (int col = 0; col < 9; col++){

                    cellLeft = Math.round((col * mCellWidth) + paddingLeft);
                    cellTop = Math.round((row * mCellHeight) + paddingTop);

                    // draw cell Text
                    int value = mSudokuGame.getTileValue(col, row);
                    if (value != 0){
                        Paint cellValuePaint = mSudokuGame.isReadOnly(col, row)
                                ? mCellValueReadonlyPaint : mCellValuePaint;

                        if (mTouchedTile == null && mTouchedValue != 0 &&
                                mSudokuGame.getTileValue(col, row) == mTouchedValue)
                            cellValuePaint = mCellValueHighlighted;

                        canvas.drawText(Integer.toString(value),
                                cellLeft + mNumberLeft,
                                cellTop + mNumberTop - numberAscent,
                                cellValuePaint);
                    } else {
                        for (int number : mSudokuGame.getNotes(col, row)){
                            int n = number - 1;
                            int c = n % 3;
                            int r = n / 3;

                            // todo now way it should be 8 (just works for my phone)
                            canvas.drawText(Integer.toString(number),
                                    cellLeft + c * noteWidth + 8,
                                    cellTop + mNoteTop - noteAscent + r * noteWidth - 8,
                                    mCellNotePaint);
                        }
                    }
                }
            }


            // draw vertical lines
            for (int c = 0; c <= 9; c++){
                float x = (c * mCellWidth) + paddingLeft;
                canvas.drawLine(x, paddingTop, x, height, mLinePaint);
            }

            // draw horizontal lines
            for (int r = 0; r <= 9; r++){
                float y = r * mCellHeight + paddingTop;
                canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
            }

            int sectorLineWidth1 = mSectorLineWidth / 2;
            int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

            // draw sector (thick) lines
            for (int c = 0; c <= 9; c = c + 3){
                float x = (c * mCellWidth) + paddingLeft;
                canvas.drawRect(x - sectorLineWidth1, paddingTop, x + sectorLineWidth2, height, mSectorLinePaint);
            }

            for (int r = 0; r <= 9; r = r + 3){
                float y = r * mCellHeight + paddingTop;
                canvas.drawRect(paddingLeft, y - sectorLineWidth1, width, y + sectorLineWidth2, mSectorLinePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        int x = (int) event.getX();
        int y = (int) event.getY();
        mTouchedTile = getTileAtPosition(x, y);
        mTouchedValue = mSudokuGame.getTileValue(mTouchedTile);

        mHintedTile = null;
        invalidate();

        return super.onTouchEvent(event);
    }

    private Tile getTileAtPosition(int x, int y){
        // take into account padding
        int lx = x - getPaddingLeft();
        int ly = y - getPaddingTop();

        int row = (int) (ly / mCellHeight);
        int col = (int) (lx / mCellWidth);

        if (col >= 0 && col < 9
                && row >= 0 && row < 9){
            return new Tile(col, row);
        } else {
            return null;
        }
    }

    public boolean insertValue(int value){
        boolean result = true;
        if (mTouchedTile != null){
            Action action = new Action(mTouchedTile,
                    mSudokuGame.getTileValue(mTouchedTile));
            if (!mSudokuGame.setTileValue(mTouchedTile, value)){
                result = false;
            }
            else {
                if (!(!actionsStack.isEmpty() && action.equals(actionsStack.peek()))){
                    actionsStack.push(action);
                }
                mWrongTiles.remove(mTouchedTile);
                invalidate();
            }
        }
        else {
            mTouchedValue = value;
            invalidate();
        }
        return result;
    }

    public boolean deleteValue(){
        boolean result = true;
        if (mTouchedTile != null){
            Action action = new Action(mTouchedTile,
                    mSudokuGame.getTileValue(mTouchedTile));
            if (!mSudokuGame.deleteValue(mTouchedTile)){
                result = false;
            }
            else {
                if (!(!actionsStack.isEmpty() && action.equals(actionsStack.peek()))){
                    actionsStack.push(action);
                }
                mSudokuGame.clearAllNotes(mTouchedTile);
                mWrongTiles.remove(mTouchedTile);
                invalidate();
            }
        }
        else {
            result = false;
        }

        return result;
    }

    public boolean insertNoteValue(int noteValue){
        boolean result = true;
        if (mTouchedTile != null){
            if (!mSudokuGame.setNote(mTouchedTile, noteValue)){
                result = false;
            }
            else {
                invalidate();
            }
        }
        else {
            mTouchedValue = noteValue;
            invalidate();
        }

        return result;
    }

    public void hint(){

        if (mSudokuGame.isGridCorrect()){
            new Thread(new Runnable(){
                public void run(){
                    mHintedTile = mSudokuGame.getHint();
                    invalidate();
                }
            }).start();
        }
        else {
            mWrongTiles = mSudokuGame.getWrongTiles();
            invalidate();
        }
    }
    
    public void solve(){
        mWrongTiles = mSudokuGame.getWrongTiles();

        mSudokuGame.setGridToSolved();

        actionsStack.clear();
        mTouchedTile = null;
        mHintedTile = null;
        mTouchedValue = 0;

        invalidate();
    }

    public boolean undo(){
       if (!actionsStack.isEmpty()){
           Action lastAction = actionsStack.pop();
           mSudokuGame.setTileValue(lastAction.t, lastAction.value);
           invalidate();
           return true;
       }
       return false;
    }

    public boolean unTouchView(){
        if (mTouchedTile == null && mTouchedValue == 0){
            return false;
        }
        if (mTouchedTile != null){
            mTouchedTile = null;
            mTouchedValue = 0;
            invalidate();
            return true;
        }
        mTouchedValue = 0;
        invalidate();
        return true;
    }

    private class Action{
        Tile t;
        int value;
        Action(Tile t, int value){
            this.t = new Tile(t);
            this.value = value;
        }

        @Override
        public boolean equals(Object obj){
            if (obj == this)
                return true;

            if (!(obj instanceof Action)){
                return false;
            }

            Action other = (Action)obj;

            return other.t.equals(t) && other.value == value;
        }
    }

}