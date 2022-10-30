package com.fbarco.iluminacion;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CircularSlider extends View {

    private IWidgetActivity CircularS;

    private int mThumbX;
    private int mThumbY;

    private float X0;
    private float Y0;

    private float Xf;
    private float Yf;

    public int value;



    private int mCircleCenterX;
    private int mCircleCenterY;
    private int mCircleRadius;

    private Drawable mThumbImage;

    private int mThumbSize;
    private int mThumbColor;
    private int mBorderColor;
    private int[] mBorderGradientColors;
    private int mBorderThickness;
    private double mStartAngle;
    private double MAngle;
    private double mAngle = mStartAngle;
    private boolean mIsThumbSelected = false;
    private boolean mIsActivate = false;

    private Paint mPaint = new Paint();
    private Paint mPaintIntro = new Paint();
    private Paint mPaintTwo = new Paint();
    private Paint mPaint3 = new Paint();
    private Paint mPaintText = new Paint();
    private SweepGradient mGradientShader;


    public CircularSlider(Context context) {
        this(context, null);
    }

    public CircularSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);
    }

    // common initializer method
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularSlider, defStyleAttr, 0);

        // read all available attributes
        float startAngle = a.getFloat(R.styleable.CircularSlider_start_angle, (float) Math.PI * 2);
        float angle = a.getFloat(R.styleable.CircularSlider_angle, (float) Math.PI * 2);
        int thumbSize = a.getDimensionPixelSize(R.styleable.CircularSlider_thumb_size, 30);
        int thumbColor = a.getColor(R.styleable.CircularSlider_thumb_color, Color.BLACK);
        int borderThickness = a.getDimensionPixelSize(R.styleable.CircularSlider_border_thickness, 20);
        int borderColor = a.getColor(R.styleable.CircularSlider_border_color, Color.BLACK);
        String borderGradientColors = a.getString(R.styleable.CircularSlider_border_gradient_colors);
        Drawable thumbImage = a.getDrawable(R.styleable.CircularSlider_thumb_image);



        setmStartAngle(startAngle);
        mPaintTwo.setColor(Color.RED);
        //setmAngle(angle);
        setmBorderThickness(borderThickness);
        setmBorderColor(borderColor);
        if (borderGradientColors != null) {
            setmBorderGradientColors(borderGradientColors.split(";"));
        }
        setmThumbSize(thumbSize);
        setmThumbImage(thumbImage);
        setmThumbColor(thumbColor);

        a.recycle();
    }

    public void setmThumbImage(Drawable ThumbImage) {
        mThumbImage = ThumbImage;
    }


    public void setmThumbSize(int ThumbSize) {
        mThumbSize = ThumbSize;
    }

    public void setmThumbColor(int ThumbColor) {
        mThumbColor = ThumbColor;
    }

    public void setmBorderColor(int BorderColor) {
        mBorderColor = BorderColor;
    }

    public void setmBorderGradientColors(String[] BorderGradientColors) {
        mBorderGradientColors = new int[BorderGradientColors.length];
        for (int i = 0; i < BorderGradientColors.length; i++) {
            mBorderGradientColors[i] = Color.parseColor(BorderGradientColors[i]);
        }
    }

    public void setmBorderThickness(int BorderThickness) {
        mBorderThickness = BorderThickness;
    }

    public void setmStartAngle(double StartAngle) {
        mStartAngle = StartAngle;
    }

    public void setmAngle(double Angle) {
        mAngle = (float) Math.PI * (Angle+25) * (-1) / 50;
        value = (int) Angle;
        Log.d(TAG,"Angulo 1: "+ mAngle);
        Log.d(TAG,"Angulo 2: "+ ((Angle/5)*18));
        MAngle = ((Angle/5)*18);
    }


    public void setmIsActivate(boolean activate){

        if(!activate){
            mPaintTwo.setColor(Color.RED);
            mIsActivate = false;
        }
        else{
            mPaintTwo.setColor(getResources().getColor(R.color.Start));
            mIsActivate = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // use smaller dimension for calculations (depends on parent size)
        int smallerDim = w > h ? h : w;

        // find circle's rectangle points
        int largestCenteredSquareLeft = (w - smallerDim) / 2;
        int largestCenteredSquareTop = (h - smallerDim) / 2;
        int largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim;
        int largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim;

        // save circle coordinates and radius in fields
        mCircleCenterX = largestCenteredSquareRight / 2 + (w - largestCenteredSquareRight) / 2;
        mCircleCenterY = largestCenteredSquareBottom / 2 + (h - largestCenteredSquareBottom) / 2;
        mCircleRadius = smallerDim / 2 - 40;

        if (mBorderGradientColors != null) {
            mGradientShader = new SweepGradient(mCircleRadius, mCircleRadius, mBorderGradientColors, null);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // outer circle (ring)
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderThickness);
        mPaint.setAntiAlias(true);

        mPaintIntro.setColor(Color.WHITE);
        mPaintIntro.setStyle(Paint.Style.STROKE);
        mPaintIntro.setStrokeWidth(40);
        mPaintIntro.setAntiAlias(true);


        mPaintTwo.setStyle(Paint.Style.FILL);
        mPaintTwo.setStrokeWidth(mBorderThickness);
        mPaintTwo.setAntiAlias(true);


        mPaint3.setColor(Color.WHITE);
        mPaint3.setStyle(Paint.Style.FILL);

        mPaint3.setAntiAlias(true);

        if (mGradientShader != null) {
            mPaint.setShader(mGradientShader);
            mPaintTwo.setShader(mGradientShader);
            mPaint3.setShader(mGradientShader);
        }
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius - 40, mPaintTwo);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius - 30, mPaint);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius -0, mPaintIntro);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius + 30, mPaint);

        // find thumb position
        mThumbX = (int) (mCircleCenterX + mCircleRadius * Math.cos(mAngle));
        mThumbY = (int) (mCircleCenterY - mCircleRadius * Math.sin(mAngle));

        if (mThumbImage != null) {
            // draw png
            mThumbImage.setBounds(mThumbX - mThumbSize / 2, mThumbY - mThumbSize / 2, mThumbX + mThumbSize / 2, mThumbY + mThumbSize / 2);
            mThumbImage.draw(canvas);
        } else {
            // draw colored circle
            mPaint.setColor(mThumbColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mThumbX, mThumbY, mThumbSize, mPaint);
        }


        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setTextSize(150);
        canvas.drawText(Integer.toString(value), mCircleCenterX, mCircleCenterY+50, mPaintText);


        //Log.d(TAG, Double.toString(mAngle*180/(Math.PI)));
        for(int angle=675; angle<=945; angle+=3)
        {

            X0 = (float) (mCircleCenterX - (mCircleRadius - 40)*0.8 * Math.cos(angle*Math.PI/180));
            Y0 = (float) (mCircleCenterY - (mCircleRadius - 40)*0.8 * Math.sin(angle*Math.PI/180));
            Xf = (float) (mCircleCenterX - (mCircleRadius - 40)*0.95 * Math.cos(angle*Math.PI/180));
            Yf = (float) (mCircleCenterY - (mCircleRadius - 40)*0.95 * Math.sin(angle*Math.PI/180));

            if(angle>=675 && angle<=((MAngle)+630))
            {
                mPaint3.setStrokeWidth(4);
                canvas.drawLine(X0,Y0,Xf,Yf,mPaint3);
            }
            else
            {
                mPaint3.setStrokeWidth(1);
                canvas.drawLine(X0,Y0,Xf,Yf,mPaint3);
            }

        }

    }

    public void setOnSliderRangeMovedListener(IWidgetActivity listener) {
        CircularS = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // start moving the thumb (this is the first touch)
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize && y > mThumbY - mThumbSize) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mIsThumbSelected = true;
                    updateSliderState(x, y, true);

                }

                if (x < mCircleCenterX + (mCircleRadius - 120) && x > mCircleCenterX - (mCircleRadius - 120) &&
                        y < mCircleCenterY + (mCircleRadius - 120) && y > mCircleCenterY - (mCircleRadius - 120)) {

                    if (mIsActivate == true) {
                        mPaintTwo.setColor(Color.RED);
                        mIsActivate = false;
                        CircularS.ClickOff();

                    } else {
                        mPaintTwo.setColor(getResources().getColor(R.color.Start));
                        mIsActivate = true;
                        CircularS.ClickOn();

                    }

                    // mPaint.setStyle(Paint.Style.);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // still moving the thumb (this is not the first touch)
                if (mIsThumbSelected) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    updateSliderState(x, y, true);

                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                // finished moving (this is the last touch)
                getParent().requestDisallowInterceptTouchEvent(false);
                mIsThumbSelected = false;
                if (!mIsThumbSelected) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    updateSliderState(x, y, false);
                }
                break;
            }

        }
        invalidate();
        return true;
    }


    private void updateSliderState(int touchX, int touchY, boolean activate) {

        if (activate) {


            int distanceX = touchX - mCircleCenterX;
            int distanceY = mCircleCenterY - touchY;
            //noinspection SuspiciousNameCombination
            double c = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            mAngle = Math.acos(distanceX / c);

            if (distanceY < 0) {
                mAngle = -mAngle;
            }

            //Log.d(TAG,"Angle0 : "+ mAngle*180/(Math.PI));
            double brng = Math.atan2(distanceY, distanceX);
            brng = Math.toDegrees(brng);
            //Log.d(TAG,"Angle00 : "+ brng);
            //Log.d(TAG,"Angle1 : "+ (brng+360));
            brng = (brng + 450) % 360;
            //Log.d(TAG,"Angle2 : "+ brng);
            brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

            //Log.d(TAG,"Angle3 : "+ brng);
            if(brng>=45)
            {
                if(brng <=315)
                {
                    MAngle = brng;
                    value = (int) ((((brng-45) / (27)) * 10));
                }
                if(brng >316 && brng <360){
                    MAngle = 315;
                    value = 100;
                }
            }
            else {
                MAngle=0;
                value=0;
            }

        }

        if (CircularS != null) {
            CircularS.valuechange(value);
            if (!activate) {
                CircularS.valueStopchange(value);
            }

        }

    }


}

