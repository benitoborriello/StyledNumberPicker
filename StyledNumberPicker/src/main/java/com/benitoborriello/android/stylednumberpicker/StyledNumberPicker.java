package com.benitoborriello.android.stylednumberpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StyledNumberPicker extends LinearLayout {

    // ****** //
    //  Vars  //
    // ****** //

    // Initialization
    Context mContext;
    Boolean firstItemSelection = true;

    // Elements
    List<TextView> rows = new ArrayList<>();
    Integer currentPosition = 0;
    Integer currentValue = 0;

    // Scroll detection
    Boolean triggerOnScroll = true;
    Boolean scrolling = false;
    Boolean scrollStarted = false;
    int currentScrollPosition = 0;
    int suggestedPosition = 0;
    int lastCheckedScrollPosition = -1;
    Boolean touching = false;
    Long scrollTaskInterval = 100l;
    private Runnable mScrollingRunnable;

    // UI vars
    Integer viewWidth;
    Integer viewHeight;
    Drawable selectedBordersBackground;
    Drawable topGradientBackground;
    Drawable bottomGradientBackground;
    Integer gradientsHeight;

    // UI views
    LinearLayout mainContainer;
    ScrollView mainScroller;
    Integer textPadding;
    Integer textSize;
    Integer maxValue;
    Integer minValue;
    Integer textColor;
    ImageView topPadding;
    ImageView bottomPadding;
    View topSelectedItemBorder;
    View bottomSelectedItemBorder;
    ImageView topGradient;
    ImageView bottomGradient;

    // Event listeners instance vars
    private onChangeListener changeListener;

    //Sound
    Boolean scrollSound = true;
    Integer soundEffectId;



    // Constructor
    public StyledNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.changeListener = null;
        initialize(context, attrs);
    }


    // ******************************* //
    //  Initialization and UI Drawing  //
    // ******************************* //
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        viewHeight = h;
        viewWidth = w;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(!scrolling) {
            setTopBottomPadding();
            setSelectedItemBorders();
        }

        if(firstItemSelection) {
            firstItemSelection = false;
            setPosition(currentPosition);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void initialize(Context context, AttributeSet attrs){
        mContext = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.app, 0, 0);
        try {
            textPadding = ta.getDimensionPixelSize(R.styleable.app_textPadding, convertDpToPx(0));
            textSize = ta.getDimensionPixelSize(R.styleable.app_textSize, convertDpToPx(18));
            maxValue = ta.getInteger(R.styleable.app_maxValue, 10);
            minValue = ta.getInteger(R.styleable.app_minValue, 0);
            textColor = ta.getColor(R.styleable.app_textColor, getResources().getColor(android.R.color.black));
            selectedBordersBackground = ta.getDrawable(R.styleable.app_selectedBordersBackground);
            topGradientBackground = ta.getDrawable(R.styleable.app_topGradientBackground);
            bottomGradientBackground = ta.getDrawable(R.styleable.app_bottomGradientBackground);
            gradientsHeight = ta.getDimensionPixelOffset(R.styleable.app_gradientsHeight, convertDpToPx(40));
            scrollSound = ta.getBoolean(R.styleable.app_scrollSound, true);
        } finally {
            ta.recycle();
        }

        inflate(context, R.layout.main_view, this);

        bindViews();

        if(minValue==null) {
            minValue = 0;
        }

        if(maxValue==null) {
            maxValue = 0;
        }

        if(minValue>maxValue) {
            minValue = maxValue;
        }

        if(textPadding==null) {
            textPadding = 0;
        }

        if(textColor==null) {
            textColor = getResources().getColor(android.R.color.black);
        }

        if(selectedBordersBackground==null) {
            selectedBordersBackground = getResources().getDrawable(android.R.color.white);
        }

        if(topGradientBackground==null) {
            topGradientBackground = getResources().getDrawable(R.drawable.gradient_top);
        }

        if(bottomGradientBackground==null) {
            bottomGradientBackground = getResources().getDrawable(R.drawable.gradient_bottom);
        }

        soundEffectId = SoundEffectConstants.CLICK;
        if(scrollSound==null) {
            scrollSound = true;
        }

        if(gradientsHeight==null) {
            gradientsHeight = convertDpToPx(40);
        }

        setSelectedBordersBackground(selectedBordersBackground);
        setTopGradientBackground(topGradientBackground);
        setBottomGradientBackground(bottomGradientBackground);

        ViewGroup.LayoutParams gradientLayoutParams;
        gradientLayoutParams = topGradient.getLayoutParams();
        gradientLayoutParams.height = gradientsHeight;
        topGradient.setLayoutParams(gradientLayoutParams);
        gradientLayoutParams = bottomGradient.getLayoutParams();
        gradientLayoutParams.height = gradientsHeight;
        bottomGradient.setLayoutParams(gradientLayoutParams);

        LayoutParams paddingLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20);

        //adding top padding view
        topPadding = new ImageView(mContext);
        topPadding.setLayoutParams(paddingLayoutParams);
        topPadding.setPadding(0,0,0, 0);
        topPadding.setLongClickable(false);
        topPadding.setClickable(false);
        topPadding.setFocusableInTouchMode(false);
        topPadding.setFocusable(false);
        mainContainer.addView(topPadding);

        //adding bottom padding view
        bottomPadding = new ImageView(mContext);
        bottomPadding.setLayoutParams(paddingLayoutParams);
        bottomPadding.setPadding(0,0,0, 0);
        bottomPadding.setLongClickable(false);
        bottomPadding.setClickable(false);
        bottomPadding.setFocusableInTouchMode(false);
        bottomPadding.setFocusable(false);
        mainContainer.addView(bottomPadding);

        //Adding rows according to minValue and maxValue
        addRows();



        //set scrollListener
        mainScroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {

                scrolling = true;

                if(triggerOnScroll) {
                    onScroll();
                }

                currentScrollPosition = mainScroller.getScrollY();

            }
        });
        mainScroller.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                requestDisallowInterceptTouchEvent(true);

                try {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } catch(Exception e) {}

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        touching = true;
                    }
                    case MotionEvent.ACTION_UP:{
                        touching = false;
                        postDelayed(mScrollingRunnable, scrollTaskInterval);
                    }
                }

                return false;
            }

        });

        mScrollingRunnable = new Runnable() {
            public void run() {
                detectScrollEnd();
            }
        };

    }

    private void bindViews() {
        mainContainer = findViewById(R.id.mainContainer);
        mainScroller = findViewById(R.id.mainScroller);
        topSelectedItemBorder = findViewById(R.id.topSelectedItemBorder);
        bottomSelectedItemBorder = findViewById(R.id.bottomSelectedItemBorder);
        topGradient = findViewById(R.id.topGradient);
        bottomGradient = findViewById(R.id.bottomGradient);
    }

    private void addRow(Context context, String text) {
        TextView newTextView;
        LayoutParams mLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        newTextView = new TextView(context);
        newTextView.setLayoutParams(mLayoutParams);
        //newTextView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //newTextView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        newTextView.setGravity(Gravity.CENTER);
        newTextView.setText(String.valueOf(text));
        newTextView.setTextColor(textColor);
        newTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        newTextView.setPadding(0,textPadding,0, textPadding);
        newTextView.setLongClickable(false);
        newTextView.setClickable(false);
        newTextView.setTextIsSelectable(false);
        newTextView.setFocusableInTouchMode(false);
        newTextView.setFocusable(false);

        rows.add(newTextView);
        mainContainer.addView(newTextView);
    }

    private void setTopBottomPadding() {
        ViewGroup.LayoutParams lp;
        lp = topPadding.getLayoutParams();
        lp.height = viewHeight/2;
        topPadding.setLayoutParams(lp);
        lp = bottomPadding.getLayoutParams();
        lp.height = viewHeight/2;
        topPadding.setLayoutParams(lp);
        //forceLayout();
    }

    private void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
        }
    }

    private int getRowHeight() {
        int elementHeight = 0;

        try {
            elementHeight = rows.get(0).getHeight();
        } catch (Exception e ) {
            elementHeight = 0;
        }

        return elementHeight;
    }

    private void clearRows() {
        try {
            for(int i=0;i<rows.size();i++) {
                mainContainer.removeView(rows.get(i));
            }
            rows.clear();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addRows() {
        for(int i=minValue; i<=maxValue; i++) {
            addRow(mContext,String.valueOf(i));
        }

        mainContainer.removeView(bottomPadding);
        mainContainer.addView(bottomPadding);

        try {
            setPosition(0);
        } catch (Exception e) { }
    }


    // ************************************* //
    //  View scrolling & position detection  //
    // ************************************* //
    public void onScroll() {
        int newSuggestedPosition = getSuggestedPosition();
        if(newSuggestedPosition!=suggestedPosition) {
            if(scrollSound) {
                playScrollingSound();
            }

            suggestedPosition = newSuggestedPosition;
        }
    }

    private void detectScrollEnd() {

        if(touching==false) {
            if (lastCheckedScrollPosition == -1) {
                lastCheckedScrollPosition = currentScrollPosition;
                postDelayed(mScrollingRunnable, scrollTaskInterval);
            } else {
                if (currentScrollPosition == lastCheckedScrollPosition) {
                    // Scrolling has stopped.
                    lastCheckedScrollPosition = -1;
                    scrollEnded();
                } else {
                    lastCheckedScrollPosition = currentScrollPosition;
                    postDelayed(mScrollingRunnable, scrollTaskInterval);
                }
            }
        }
    }

    private void scrollEnded() {
        scrolling = false;
        triggerOnScroll = false;
        scrollStarted = false;
        setPosition(getSuggestedPosition());
        triggerOnScroll = true;
    }

    private int getSuggestedPosition() {
        Integer suggestedPosition = 0;

        int rowHeight = getRowHeight();

        try {
            suggestedPosition = (mainScroller.getScrollY())  / rowHeight;
        } catch (Exception e) {
            e.printStackTrace();
            suggestedPosition = 0;
        }

        if(suggestedPosition>(rows.size()-1)) {
            suggestedPosition = rows.size()-1;
        }

        if(suggestedPosition<0) {
            suggestedPosition = 0;
        }

        return suggestedPosition;
    }

    public int getCurrentPosition() {
        try {
            return currentPosition;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getValue(int itemIndex) {
        int value = 0;
        try {
            value = Integer.valueOf(rows.get(itemIndex).getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }

    public int getCurrentValue() {
        return currentValue;
    }


    // *************** //
    //  Row selection  //
    // *************** //
    public void setPosition(int rowIndex) {
        currentPosition = rowIndex;
        currentValue = getValue(rowIndex);
        triggerOnScroll = false;
        scrollToElement(rowIndex);
        if(changeListener != null) {
            changeListener.onChange(getCurrentValue(), rowIndex);
        }
    }

    private void scrollToElement(int row) {
        int rowHeight = getRowHeight();
        try {
            mainScroller.smoothScrollTo(mainScroller.getScrollX(), (rowHeight / 2) + (rowHeight * row));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // **************************** //
    //  Min and max values setting  //
    // **************************** //
    public void setMinValue(int value) {
        minValue = value;
        if(minValue>maxValue) {
            maxValue = minValue;
        }
        clearRows();
        addRows();
    }

    public void setMaxValue(int value) {
        maxValue = value;
        if(maxValue<minValue) {
            minValue = maxValue;
        }
        clearRows();
        addRows();
    }


    // ******** //
    //  Events  //
    // ******** //
    public interface onChangeListener {
        void onChange(int currentValue, int currentPosition);
    }

    public void setOnChangeListener(onChangeListener changeListener) {
        this.changeListener = changeListener;
    }


    // ************** //
    //  UI functions  //
    // ************** //
    public void setTopGradientBackground(Drawable background) {
        topGradient.setBackground(background);
    }

    public void setBottomGradientBackground(Drawable background) {
        bottomGradient.setBackground(background);
    }

    public void setSelectedBordersBackground(Drawable background) {
        topSelectedItemBorder.setBackground(background);
        bottomSelectedItemBorder.setBackground(background);
    }

    private void setSelectedItemBorders() {
        int rowHeight = getRowHeight();
        int target;
        target  = (viewHeight / 2) - (rowHeight / 2);
        setMargins(topSelectedItemBorder,0,target,0,0);
        target  = (viewHeight / 2) + (rowHeight / 2);
        setMargins(bottomSelectedItemBorder,0,target,0,0);
    }

    public void setTextPadding(int textPaddingPx) {
        textPadding = textPaddingPx;
        try {
            for (int i = 0; i < rows.size();i++) {
                rows.get(i).setPadding(0,textPadding,0,textPadding);
            }
            setSelectedItemBorders();
        } catch (Exception e) { }
    }

    public void setTextSize(int textSizePx) {
        textSize = textSizePx;
        try {
            for (int i = 0; i < rows.size();i++) {
                rows.get(i).setTextSize(textSize);
            }
            setSelectedItemBorders();
        } catch (Exception e) { }
    }

    public void setTextColor(int color) {
        textColor = color;
        try {
            for (int i = 0; i < rows.size();i++) {
                rows.get(i).setTextColor(color);
            }
        } catch (Exception e) { }
    }


    // ***************** //
    //  Scrolling sound  //
    // ***************** //
    private void playScrollingSound() {
        try {
            this.playSoundEffect(soundEffectId);
        } catch (Exception e) { }
    }

    public void scrollingSoundEnabled(boolean enabled) {
        scrollSound = enabled;
    }


    // ******* //
    //  Utils  //
    // ******* //
    private float convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    private float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public int convertDpToPx(int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));

    }

    private int convertPxToDp(int px){
        return Math.round(px/(Resources.getSystem().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
    }

    private float getDpFromPx(float px) {
        return px / this.getContext().getResources().getDisplayMetrics().density;
    }

    private float getPxFromDp(float dp) {
        return dp * this.getContext().getResources().getDisplayMetrics().density;
    }

}