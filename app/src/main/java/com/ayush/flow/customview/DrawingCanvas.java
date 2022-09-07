package com.ayush.flow.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.ayush.flow.R;
import com.ayush.flow.utils.Constants;
import com.ayush.flow.model.PathStore;
import com.ayush.flow.model.Shape;

import java.util.ArrayList;

/**
 * Class which forms the canvas view.
 * All the shapes are drawn on this view.
 */
public class DrawingCanvas extends View {

    private int mCanvasRightBounds;

    private int mCanvasBottomBounds;
    //variables to store the bounds of the canvas
    private int mCanvasLeftBounds = 1;

    private int mCanvasTopBounds = 1;

    private Paint mBrushPaint;

    private Path mBrushPath;

    private Paint mEraserPaint;

    private Context mContext;

    private int xPos;

    private int yPos;

    private static int mCurrentOperation;

    //Moving of view operation
    private static int mStorePrevOperationOnLongPress;

    private static int mIndexOfViewInListToMove;

    private static Shape mShapeToMove;

    //path operations
    private Paint mPathPaint;

    //Detection of Gesture
    private GestureDetector mDetectGesture;

    private Vibrator mVibrator;

    //Canvas Background
    private Bitmap mCanvasBackground;

    private static ArrayList mShapeList;

    public DrawingCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialize();
    }

    public void initialize() {

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mBrushPaint = new Paint();
        mBrushPaint.setAntiAlias(true);
        changeBrushColor(R.color.black);
        changeFillStyle(Constants.PAINT_STYLE_STROKE);
        mBrushPaint.setStrokeJoin(Paint.Join.ROUND);
        mBrushPaint.setStrokeCap(Paint.Cap.ROUND);
        mBrushPath = new Path();
        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        changeBrushStroke(5);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setColor(Color.parseColor("#ffffff"));
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mDetectGesture = new GestureDetector(mContext, new CustomGestureDetector());
        mIndexOfViewInListToMove = -1;
        mShapeList = new ArrayList();
        mShapeToMove = null;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mCanvasBackground!=null){
            mCanvasBackground = Bitmap.createScaledBitmap(mCanvasBackground,canvas.getWidth(),canvas.getHeight(),true);
            canvas.drawBitmap(mCanvasBackground,0f,0f,null);
        }
        for (Object obj : mShapeList) {
            if (obj instanceof PathStore) {
                PathStore path = (PathStore) obj;
                mPathPaint.setColor(path.getPathColor());
                mPathPaint.setStrokeWidth(path.getPathStroke());
                canvas.drawPath(path.getDrawnPath(), mPathPaint);
            }
        }
        if (getCurrentOperation() == Constants.OPERATION_DRAW_PENCIL) {
            canvas.drawPath(mBrushPath, mBrushPaint);
        } else if (getCurrentOperation() == Constants.OPERATION_ERASE) {
            canvas.drawPath(mBrushPath, mEraserPaint);
        }
    }

    /**
     * method to get the new dimensions of the canvas on change of view.
     * @param w is the new width of the canvas
     * @param h is the new height of the canvas
     * @param oldw is the old width of the canvas
     * @param oldh is the new height of the casvas
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasRightBounds = w;
        mCanvasBottomBounds = h;
    }

    /**
     * Motion event to track the touch on the screen and draw the shapes as per the operations
     * reflected by the stored variable mCurrentOperation
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchEvent = event.getAction();
        xPos = (int) event.getX();
        yPos = (int) event.getY();

        mDetectGesture.onTouchEvent(event);
        switch (touchEvent) {
            case MotionEvent.ACTION_DOWN:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL || mCurrentOperation == Constants.OPERATION_ERASE) {
                    mBrushPath.moveTo(xPos, yPos);
                    mBrushPath.lineTo(xPos, yPos);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL || mCurrentOperation == Constants.OPERATION_ERASE) {
                    mBrushPath.lineTo(xPos, yPos);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentOperation == Constants.OPERATION_DRAW_PENCIL) {
                    mShapeList.add(new PathStore(mBrushPath, mBrushPaint.getColor(), (int) mBrushPaint.getStrokeWidth()));
                    mBrushPath = new Path();
                } else if (mCurrentOperation == Constants.OPERATION_ERASE) {
                    mShapeList.add(new PathStore(mBrushPath, mEraserPaint.getColor(), (int) mEraserPaint.getStrokeWidth()));
                    mBrushPath = new Path();
                } else if (mCurrentOperation == Constants.OPERATION_MOVE_VIEW) {
                    mCurrentOperation = mStorePrevOperationOnLongPress;
                    mShapeList.remove(mIndexOfViewInListToMove);
                    mShapeList.add(mShapeToMove);
                    mIndexOfViewInListToMove = -1;
                    mShapeToMove = null;
                }
                break;
        }
        invalidate();
        return true;
    }

    public void changeFillStyle(int style) {
        switch (style) {
            case Constants.PAINT_STYLE_FILL:
                mBrushPaint.setStyle(Paint.Style.FILL);
                break;
            case Constants.PAINT_STYLE_STROKE:
                mBrushPaint.setStyle(Paint.Style.STROKE);
                break;
        }
    }

    public void changeBrushStroke(int stroke) {
        mBrushPaint.setStrokeWidth(stroke);
        mEraserPaint.setStrokeWidth(stroke * 2);
    }

    public void changeBrushColor(Integer col) {
        mBrushPaint.setColor(col);
    }

    public int getCurrentOperation() {
        return mCurrentOperation;
    }

    public void setCurrentOperation(int operation) {
        if (operation == Constants.OPERATION_DRAW_PENCIL) {
            changeFillStyle(Constants.PAINT_STYLE_STROKE);
        }
        mCurrentOperation = operation;
    }

    public void clearCompleteCanvas() {
        mShapeList.clear();
        invalidate();
    }

    public String getBrushColor() {
        return Integer.toHexString(mBrushPaint.getColor()).toUpperCase().substring(2);
    }

    public void undoPreviousOperation() {
        if (mShapeList != null && mShapeList.size() != 0) {
            mShapeList.remove(mShapeList.size() - 1);
        }else if (mShapeList == null || mShapeList.size() == 0){
            mCanvasBackground =null;
        }
        invalidate();
    }

    public class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private Context mDetectorContext;
        private int xTouchPos;
        private int yTouchPos;

        @Override
        public void onLongPress(MotionEvent e) {

            xTouchPos = (int) e.getX();
            yTouchPos = (int) e.getY();

            if (mShapeList != null && mShapeList.size() > 0) {

                Object obj = mShapeList.get(mShapeList.size() - 1);
            }
        }

        /**
         * method in invoked when the user double clicks the shape. This event is used to change
         * the color of the view by opening the color drawer.
         * @param e
         * @return
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            xTouchPos = (int) e.getX();
            yTouchPos = (int) e.getY();
            if (mShapeList != null && mShapeList.size() > 0) {

                Object obj = mShapeList.get(mShapeList.size() - 1);

            }
            return true;
        }
    }

    /**
     * Method to check if the point at which the user has touched is inside the circle or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the x coordinate of the screen where the touch is detected.
     * @param circle is the circle object to check
     * @return
     */


    /**
     *  Method to check if the point at which the user has touched is inside the rectangle or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param rectangle is the object to check.
     * @return
     */


    /**
     *  Method to check if the point at which the user has touched is inside the oval or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param oval is the object to check.
     * @return
     */


    /**
     * Method to check if the point at which the user has touched is inside the TextBox or not.
     * @param xTouch is the x coordinate of the screen where the touch is detected.
     * @param yTouch is the y coordinate of the screen where the touch is detected.
     * @param textBox is the object to check.
     * @return
     */


    /**
     * Method to set the color chosen by the user to the view on which the double tap event
     * was detected.
     */
    public void applyColorToView() {
        if (mCurrentOperation == Constants.OPERATION_FILL_VIEW) {
            Object obj = mShapeList.get(mShapeList.size() - 1);
            invalidate();
            mCurrentOperation = Constants.OPERATION_NO_OPERATION;
        }
    }


    public void setCanvasBackground(Bitmap background){
        mCanvasBackground = background;
        invalidate();
    }

}
