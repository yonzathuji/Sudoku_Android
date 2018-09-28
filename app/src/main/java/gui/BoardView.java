package gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import db.GameAction;
import game.SudokuGame;
import game.Tile;
import ocr.PuzzleNotFoundException;
import postpc.yonz.main.R;

public class BoardView extends View {



    public static final int DEFAULT_BOARD_SIZE = 100;

    private float mCellWidth;
    private float mCellHeight;

    private SudokuGame mSudokuGame;
    private Tile mTouchedTile;
    private Tile mHintedTile;
    private List<Tile> mWrongTiles;
    private List<Tile> mUnrecognizedTiles;
    private Stack<GameAction> actionsStack;

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

    public BoardView(Context context, AttributeSet attrs) throws PuzzleNotFoundException {
        super(context, attrs);

        setFocusable(true);
        setFocusableInTouchMode(true);

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

    /**
     * Initialize the game object and the grid. If in verification mode it will only create the board,
     * otherwise it will generate a solution.
     * @param isForVerification true if this is the board verification mode.
     */
    public void initSudokuGame(boolean isForVerification) {

        mSudokuGame = new SudokuGame(isForVerification);
        if (isForVerification) {
            mUnrecognizedTiles = mSudokuGame.getUnrecognizedTiles();
        }

    }

    public SudokuGame getSudokuGame() {
        return mSudokuGame;
    }

    public void setSolved(boolean result) {
        isSolved = result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }
        else {
            width = DEFAULT_BOARD_SIZE;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                width = widthSize;
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_BOARD_SIZE;
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                height = heightSize;
            }
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            width = height;
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            height = width;
        }

        if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
            height = heightSize;
        }

        mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
        mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

        setMeasuredDimension(width, height);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/gadugib.ttf");

        float cellTextSize = mCellHeight * 0.85f;
        mCellValuePaint.setTextSize(cellTextSize);
        mCellValueReadonlyPaint.setTextSize(cellTextSize);
        mCellValueHighlighted.setTextSize(cellTextSize);
        mCellNotePaint.setTextSize(mCellHeight / 3.0f);
//
//        mCellValuePaint.setTypeface(font);
//        mCellValueReadonlyPaint.setTypeface(font);
//        mCellValueHighlighted.setTypeface(font);
//        mCellNotePaint.setTypeface(font);

        mNumberLeft = (int) ((mCellWidth - mCellValuePaint.measureText("9")) / 2);
        mNumberTop = (int) ((mCellHeight - mCellValuePaint.getTextSize()) / 2);


        mNoteTop = mCellHeight / 50.0f;

        int sizeInPx = width < height ? width : height;
        float dipScale = getContext().getResources().getDisplayMetrics().density;
        float sizeInDip = sizeInPx / dipScale;

        float sectorLineWidthInDip = 2.0f;

        if (sizeInDip > 150) {
            sectorLineWidthInDip = 3.0f;
        }

        mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if on playing mode, wait for a solution to be generated before displaying the board
        // (!isVerification && isGeneratingSolution) {}

        int width = getWidth() - getPaddingRight();
        int height = getHeight() - getPaddingBottom();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        int cellLeft, cellTop;
        if (mSudokuGame != null) {

            float numberAscent = mCellValuePaint.ascent();
            float noteAscent = mCellNotePaint.ascent();
            float noteWidth = mCellWidth / 3f;

            // color the touched tile, its entire row and its entire column.
            if (mTouchedTile != null) {
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

            // color the hinted tile (playing mode)
            if (mHintedTile != null) {
                cellLeft = Math.round(mHintedTile.x * mCellWidth) + paddingLeft;
                cellTop = Math.round(mHintedTile.y * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft,  cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorHinted);
            }

            // color the wrong tiles (playing mode)
            for (Tile wrongTile : mWrongTiles) {
                cellLeft = Math.round(wrongTile.x * mCellWidth) + paddingLeft;
                cellTop = Math.round(wrongTile.y * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft,  cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorWrong);
            }


            // color the unrecognized tiles (verification mode)
            if(mUnrecognizedTiles != null) {
                for (Tile unrecognizedTile : mUnrecognizedTiles) {
                    cellLeft = Math.round(unrecognizedTile.x * mCellWidth) + paddingLeft;
                    cellTop = Math.round(unrecognizedTile.y * mCellHeight) + paddingTop;
                    canvas.drawRect(
                            cellLeft,  cellTop,
                            cellLeft + mCellWidth, cellTop + mCellHeight,
                            mBackgroundColorWrong);
                }
            }

            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {

                    cellLeft = Math.round((col * mCellWidth) + paddingLeft);
                    cellTop = Math.round((row * mCellHeight) + paddingTop);

                    // draw cell Text
                    int value = mSudokuGame.getTileValue(col, row);
                    if (value != 0) {
                        Paint cellValuePaint = mSudokuGame.isReadOnly(col, row)
                                ? mCellValueReadonlyPaint : mCellValuePaint;

                        if (mTouchedTile == null && mTouchedValue != 0 &&
                                mSudokuGame.getTileValue(col, row) == mTouchedValue)
                            cellValuePaint = mCellValueHighlighted;

                        canvas.drawText(Integer.toString(value),
                                cellLeft + mNumberLeft,
                                cellTop + mNumberTop - numberAscent,
                                cellValuePaint);
                    }
                    else if(mUnrecognizedTiles != null && mUnrecognizedTiles.contains(new Tile(col, row))) {
                        // unrecognized tiles in verification mode
                        canvas.drawText("?",
                                cellLeft + mNumberLeft,
                                cellTop + mNumberTop - numberAscent,
                                mCellValueReadonlyPaint);
                    }
                    else { // draw cell notes
                        for (int number : mSudokuGame.getNotes(col, row)) {
                            int n = number - 1;
                            int c = n % 3;
                            int r = n / 3;

                            canvas.drawText(Integer.toString(number),
                                    cellLeft + c * noteWidth + 8,
                                    cellTop + mNoteTop - noteAscent + r * noteWidth - 8,
                                    mCellNotePaint);
                        }
                    }
                }
            }


            // draw vertical lines
            for (int c = 0; c <= 9; c++) {
                float x = (c * mCellWidth) + paddingLeft;
                canvas.drawLine(x, paddingTop, x, height, mLinePaint);
            }

            // draw horizontal lines
            for (int r = 0; r <= 9; r++) {
                float y = r * mCellHeight + paddingTop;
                canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
            }

            int sectorLineWidth1 = mSectorLineWidth / 2;
            int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

            // draw sector (thick) lines
            for (int c = 0; c <= 9; c = c + 3) {
                float x = (c * mCellWidth) + paddingLeft;
                canvas.drawRect(x - sectorLineWidth1, paddingTop, x + sectorLineWidth2, height, mSectorLinePaint);
            }

            for (int r = 0; r <= 9; r = r + 3) {
                float y = r * mCellHeight + paddingTop;
                canvas.drawRect(paddingLeft, y - sectorLineWidth1, width, y + sectorLineWidth2, mSectorLinePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        mTouchedTile = getTileAtPosition(x, y);
        mTouchedValue = mSudokuGame.getTileValue(mTouchedTile);

        mHintedTile = null;
        invalidate();

        return super.onTouchEvent(event);
    }

    private Tile getTileAtPosition(int x, int y) {
        // take into account padding
        int lx = x - getPaddingLeft();
        int ly = y - getPaddingTop();

        int row = (int) (ly / mCellHeight);
        int col = (int) (lx / mCellWidth);

        if (col >= 0 && col < 9
                && row >= 0 && row < 9) {
            return new Tile(col, row);
        } else {
            return null;
        }
    }

    /**
     * Attempts to insert a given value to the touched tile.
     * @param value the value to be inserted
     * @return a GameAction object if the insertion was successful. null otherwise.
     */
    public GameAction insertValue(int value) {
        GameAction action;
        if (mTouchedTile != null) {
            int prevValue = mSudokuGame.getTileValue(mTouchedTile);
            action = new GameAction(mTouchedTile,
                    value, false);
            if (mSudokuGame.setTileValue(mTouchedTile, value)) {
                mWrongTiles.remove(mTouchedTile);
                if (!(!actionsStack.isEmpty() && action.equals(actionsStack.peek()))) {
                    actionsStack.push(new GameAction(mTouchedTile, prevValue, false));
                }
                invalidate();
                return new GameAction(mTouchedTile, value, false);
            }
        }
        else {
            mTouchedValue = value;
            invalidate();
            return new GameAction(null, value, false);
        }
        return null;
    }

    /**
     * Inserts a value to the touched tile even if it's read-only tile. ONLY USE IN VERIFICATION
     * @param value the value to be inserted.
     * @return a GameAction object if the deletion was successful. null otherwise.
     */
    public GameAction insertReadOnlyValue(int value) {
        if (mTouchedTile != null) {
            mSudokuGame.setReadOnlyTileValue(mTouchedTile, value);
            mUnrecognizedTiles.remove(mTouchedTile);
            invalidate();

            return new GameAction(mTouchedTile, value, false);
        }
        return null;
    }

    /**
     * Attempts to delete the value of the touched tile.
     * @return a GameAction object if the deletion was successful. null otherwise.
     */
    public GameAction deleteValue() {
        GameAction action;
        if (mTouchedTile != null) {
            action = new GameAction(mTouchedTile,
                    mSudokuGame.getTileValue(mTouchedTile), false);
            if (mSudokuGame.deleteValue(mTouchedTile)) {
                mSudokuGame.clearAllNotes(mTouchedTile);
                mWrongTiles.remove(mTouchedTile);
                if (!(!actionsStack.isEmpty() && action.equals(actionsStack.peek()))) {
                    actionsStack.push(action);
                }
                invalidate();
                return new GameAction(mTouchedTile, 0, false);
            }
        }
        return null;
    }

    /**
     * Deletes the value of the touched tile even if it's read-only tile. ONLY USE IN VERIFICATION
     * @return a GameAction object if the deletion was successful. null otherwise.
     */
    public GameAction deleteReadOnlyValue() {
        if (mTouchedTile != null) {
            mSudokuGame.deleteReadOnlyValue(mTouchedTile);
            mUnrecognizedTiles.remove(mTouchedTile);
            invalidate();
            return new GameAction(mTouchedTile, 0, false);
        }
        return null;
    }

    public GameAction insertNoteValue(int noteValue) {
        if (mTouchedTile != null) {
            if (mSudokuGame.setNote(mTouchedTile, noteValue)) {
                invalidate();
                return new GameAction(mTouchedTile, noteValue, true);
            }
        }
        else {
            mTouchedValue = noteValue;
            invalidate();
        }
        return null;
    }

    /**
     * Attempts to display a hint on the board.
     * @return true if a hint was displayed. false otherwise.
     */
    public GameAction hint() {
        if (isSolved) {
            if (mSudokuGame.isGridCorrect()) {
                mHintedTile = mSudokuGame.getHint();

                if (mHintedTile == null) {
                    return null;
                }

                if (mSudokuGame.getTileValue(mHintedTile) == 0) { // the tile was just marked
                    invalidate();
                    return null;
                }
                else {
                    invalidate();
                    return new GameAction(mHintedTile, mSudokuGame.getTileValue(mHintedTile), false);
                }

            }
            else {
                mWrongTiles = mSudokuGame.getWrongTiles();
                invalidate();
                return null;
            }
        }
        else {
            Toast.makeText(getContext(), "Puzzle has no solution", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Sets the grid to the solved grid if the puzzle is solvable.
     * @return true if the grid was set to solved. false otherwise.
     */
    public boolean solve() {
        if (isSolved) {
            mWrongTiles = mSudokuGame.getWrongTiles();
            mSudokuGame.setGridToSolved();
            actionsStack.clear();
            mTouchedTile = null;
            mHintedTile = null;
            mTouchedValue = 0;
            invalidate();
        }
        else {
            Toast.makeText(getContext(), "Puzzle has no solution", Toast.LENGTH_LONG).show();
        }

        return isSolved;
    }

    /**
     * Undos the last action.
     * @return true if undoing was successful. false otherwise.
     */
    public boolean undo() {
        if (!actionsStack.isEmpty()) {
            GameAction lastAction = actionsStack.pop();
            mSudokuGame.setTileValue(lastAction.tile, lastAction.value);
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * Releases the touched tile and the touched value.
     * @return true if touched tile was released or the touched value was released.
     */
    public boolean unTouchView() {
        if (mTouchedTile == null && mTouchedValue == 0) {
            return false;
        }
        if (mTouchedTile != null) {
            mTouchedTile = null;
            mTouchedValue = 0;
            invalidate();
            return true;
        }
        mTouchedValue = 0;
        invalidate();
        return true;
    }


}