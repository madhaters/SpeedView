package com.github.anastr.speedviewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.anastr.speedviewlib.components.Indicators.NormalIndicator;

import java.util.ArrayList;

/**
 * this Library build By Anas Altair
 * see it on <a href="https://github.com/anastr/SpeedView">GitHub</a>
 */
public class BmiView extends Speedometer {

    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            speedometerPaint = new Paint(Paint.ANTI_ALIAS_FLAG),
            markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF speedometerRect = new RectF();

    private float initTickPadding = 0;

    private boolean tickRotation = true;

    private ArrayList<Float> ticks = new ArrayList<>();

    private int tickPadding = (int) (getSpeedometerWidth() + dpTOpx(3f));

    public BmiView(Context context) {
        this(context, null);
        setTicksDouble();
    }

    private void setTicksDouble() {
        ticks.clear();
        ticks.add(16f);
        ticks.add(18.5f);
        ticks.add(25.0f);
        ticks.add(29.9f);
        ticks.add(34.9f);
        ticks.add(39.9f);
        ticks.add(50f);
    }

    public BmiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setTicksDouble();
    }

    public BmiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        setTicksDouble();
        initAttributeSet(context, attrs);
    }

    @Override
    protected void defaultGaugeValues() {
    }

    @Override
    protected void defaultSpeedometerValues() {
        super.setIndicator(new NormalIndicator(getContext()));
        super.setBackgroundCircleColor(Color.TRANSPARENT);
    }

    private void init() {
        speedometerPaint.setStyle(Paint.Style.STROKE);
        markPaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.DKGRAY);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpeedView, 0, 0);

        circlePaint.setColor(a.getColor(R.styleable.SpeedView_sv_centerCircleColor, circlePaint.getColor()));
        a.recycle();
        setTicksDouble();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        updateBackgroundBitmap();
    }

    private void initDraw() {
        speedometerPaint.setStrokeWidth(getSpeedometerWidth());
        markPaint.setColor(getMarkColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initDraw();

        drawSpeedUnitText(canvas);

        drawIndicator(canvas);
        canvas.drawCircle(getSize() *.5f, getSize() *.5f, getWidthPa()/12f, circlePaint);

        drawNotes(canvas);
    }

    @Override
    protected void updateBackgroundBitmap() {
        Canvas c = createBackgroundBitmapCanvas();
        initDraw();


        float risk = getSpeedometerWidth() *.5f + getPadding();
        speedometerRect.set(risk, risk, getSize() -risk, getSize() -risk);

        speedometerPaint.setColor(Color.parseColor("#D50000"));
        c.drawArc(speedometerRect, getStartDegree(), (getEndDegree() - getStartDegree()),
                false, speedometerPaint);


        speedometerPaint.setColor(Color.parseColor("#F44336"));
        c.drawArc(speedometerRect, getStartDegree(), (getEndDegree() - getStartDegree()) * 70 * 0.01f,
                false, speedometerPaint);

        speedometerPaint.setColor(Color.parseColor("#EF9A9A"));
        c.drawArc(speedometerRect, getStartDegree(), (getEndDegree() - getStartDegree()) * 56 * 0.01f,
                false, speedometerPaint);
        speedometerPaint.setColor(Color.parseColor("#FFEB3B"));
        c.drawArc(speedometerRect, getStartDegree(), (getEndDegree() - getStartDegree()) * 41 * 0.01f,
                false, speedometerPaint);


        speedometerPaint.setColor(getMediumSpeedColor());
        c.drawArc(speedometerRect, getStartDegree()
                , (getEndDegree()- getStartDegree())*getMediumSpeedOffset(), false, speedometerPaint);

        speedometerPaint.setColor(getLowSpeedColor());
        c.drawArc(speedometerRect, getStartDegree()
                , (getEndDegree()- getStartDegree())*getLowSpeedOffset(), false, speedometerPaint);


        c.save();
        c.rotate(90f + getStartDegree(), getSize() *.5f, getSize() *.5f);
        float everyDegree = (getEndDegree() - getStartDegree()) * .111f;
        for (float i = getStartDegree(); i < getEndDegree()-(2f*everyDegree); i+=everyDegree) {
            c.rotate(everyDegree, getSize() *.5f, getSize() *.5f);
        }
        c.restore();

        drawTicks(c, ticks);
    }

    protected void drawTicks(Canvas c, ArrayList<Float> ticks) {
        if (ticks.size() == 0)
            return;

        for (int i = 0; i < ticks.size(); i++) {
            float d = getDegreeAtSpeed(ticks.get(i)) + 90f;
            c.save();
            c.rotate(d, getSize() * .5f, getSize() * .5f);
            if (!tickRotation) {
                c.save();
                c.rotate(-d, getSize() * .5f
                        , initTickPadding + textPaint.getTextSize() + getPadding() + tickPadding);
            }

            String tickLabel;

            tickLabel = String.format(getLocale(), "%.1f", ticks.get(i));
            c.drawText(tickLabel, getSize() * .5f
                    , initTickPadding + textPaint.getTextSize() + getPadding() + tickPadding, textPaint);
            if (!tickRotation)
                c.restore();
            c.restore();
        }
    }

    public int getCenterCircleColor() {
        return circlePaint.getColor();
    }

    /**
     * change the color of the center circle (if exist),
     * <b>this option is not available for all Speedometers</b>.
     * @param centerCircleColor new color.
     */
    public void setCenterCircleColor(int centerCircleColor) {
        circlePaint.setColor(centerCircleColor);
        if (!isAttachedToWindow())
            return;
        invalidate();
    }
}
