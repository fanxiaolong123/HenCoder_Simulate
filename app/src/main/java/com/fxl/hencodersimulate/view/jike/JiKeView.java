package com.fxl.hencodersimulate.view.jike;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fxl.hencodersimulate.R;

/**
 * Created by fxl on 2017/11/18 0018.
 */

public class JiKeView extends View {
    // 画笔
    Paint mPaint;
    // 未点赞的手指
    Bitmap mUnSelectedFinger;
    // 点赞的手指
    Bitmap mSelectedfinger;
    // 点赞的烟花
    Bitmap mLikeFirworks;
    // 点赞数
    private int mLikeNum;
    // 点赞前的点赞数
    private String mUnselectNumText;
    // 点赞后的点赞数
    private String mSelectNumText;
    // 当前点赞数
    private String text;

    // 文字尺寸
    private Rect bounds;

    /**
     * 点赞的状态, 默认是 false
     */
    boolean onSelecting = false;
    // 控件的中心点
    private float centerX;
    private float centerY;
    // 动画进度
    private float fraction;
    // 动画
    private ValueAnimator mAnimator;


    public JiKeView(Context context) {
        this(context, null);
    }

    public JiKeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JiKeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 命名空间
        String nameSpace = "http://schemas.android.com/apk/res-auto";

        bounds = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 初始化 bitmap
        mUnSelectedFinger = BitmapFactory.decodeResource(getResources(), R.mipmap
                .ic_messages_like_unselected);
        mSelectedfinger = BitmapFactory.decodeResource(getResources(), R.mipmap
                .ic_messages_like_selected);
        mLikeFirworks = BitmapFactory.decodeResource(getResources(), R.mipmap
                .ic_messages_like_selected_shining);
        // 设置 bitmap 可变
        mUnSelectedFinger = mUnSelectedFinger.copy(Bitmap.Config.ARGB_8888, true);
        mSelectedfinger = mSelectedfinger.copy(Bitmap.Config.ARGB_8888, true);
        // 获取自定义属性
        mLikeNum = attrs.getAttributeIntValue(nameSpace, "number", 109);
        mUnselectNumText = String.valueOf(mLikeNum);
        mSelectNumText = String.valueOf(mLikeNum + 1);
        text = mUnselectNumText;
    }

    /**
     * 设置点赞数
     *
     * @param num
     */
    public void setLikeNum(int num) {
        mLikeNum = num;
        mUnselectNumText = String.valueOf(mLikeNum);
        mSelectNumText = String.valueOf(mLikeNum + 1);
        text = mUnselectNumText;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
    }

    Bitmap bitmap;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 手指的绘制起点
        int fingerImgX = getWidth() / 2 - mUnSelectedFinger.getWidth() + 10;
        int fingerImgY = getHeight() / 2 - mUnSelectedFinger.getHeight() / 2;
        // 烟花的绘制起点
        int firsWorksX = fingerImgX + 8;
        int firsWorksY = fingerImgY - 30;

        if (onSelecting) {
            // 绘制圆圈
            drawLikeCircle(canvas, fingerImgX, fingerImgY);
            // 绘制手指
            drawLikeFingerBitmap(canvas, fingerImgX, fingerImgY);
            // 绘制烟花
            drawLikeFireworks(canvas, firsWorksX, firsWorksY);
        } else {
            // 绘制手指: 点赞 --> 非点赞手指(透明度)
            drawUnlikeFingerBitmap(canvas, fingerImgX, fingerImgY);
            // 绘制烟花: 慢慢消失(透明度)
            drawUnlikeFireworks(canvas, firsWorksX, firsWorksY);
        }
        // 绘制文字
        drawUnlikeText(canvas);
    }

    private void drawUnlikeText(Canvas canvas) {
        // 透明度: 255 - 显示, 0 - 透明
        int alpha;
        float dy;

        int unselectEnd = 0;
        int selectEnd = 0;

        // 需要平移的文字
        String translateText;

        // 设置文字样式
        mPaint.setColor(Color.parseColor("#bbbbbb"));
        mPaint.setTextSize(40);
        // 获取文字尺寸
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        // 计算绘制文字的起始坐标
        int startX = getWidth() / 2 + 20;
        int startY = getHeight() / 2 - (bounds.bottom + bounds.top) / 2;

        // 判断变化的字符数
        if (mSelectNumText.length() != mUnselectNumText.length()) {
            selectEnd = 0;
            unselectEnd = 0;
        } else {
            // 计算第一个变化的字符的索引
            for (int i = 0; i < mSelectNumText.length(); i++) {
                if (mSelectNumText.charAt(i) != mUnselectNumText.charAt(i)) {
                    unselectEnd = i;
                    selectEnd = unselectEnd;
                    break;
                }
            }
        }

        // 先绘制固定不动的数字
        canvas.drawText(mUnselectNumText, 0, unselectEnd, startX, startY, mPaint);
        canvas.drawText(mSelectNumText, 0, selectEnd, startX, startY, mPaint);

        dy = mPaint.getFontSpacing() * fraction ;
        if (!onSelecting) { // 取消点赞
            // 点赞前的数字渐渐显示, 向下
            alpha = (int) (255 * fraction);
            dy = dy - mPaint.getFontSpacing();
        } else {    // 点赞
            // 点赞前的数字渐渐消失, 向上
            alpha = (int) (255 * (1 - fraction));
            dy = -dy;
        }

        // 初试状态
        if (mAnimator == null) {
            alpha = 255;
            dy = 0;
        }

        // 接着绘制需要平移变换的数字
        // 点赞前的数字
        translateText = mUnselectNumText.substring(unselectEnd, mUnselectNumText.length());
        mPaint.setAlpha(alpha);
        canvas.save();
        canvas.translate(0, dy);
        canvas.drawText(translateText, 0, translateText.length(), startX + mPaint.measureText
                (mUnselectNumText.substring(0, unselectEnd)), startY, mPaint);
        canvas.restore();


        // 点赞后的数字
        translateText = mSelectNumText.substring(selectEnd, mSelectNumText.length());
        mPaint.setAlpha(255 - alpha);
        canvas.save();
        canvas.translate(0, dy + mPaint.getFontSpacing());
        canvas.drawText(translateText, 0, translateText.length(), startX + mPaint.measureText
                (mSelectNumText.substring(0, unselectEnd)), startY, mPaint);
        canvas.restore();

        mPaint.reset();
    }

    private void drawUnlikeFireworks(Canvas canvas, int firsWorksX, int firsWorksY) {
        float tempFraction = fraction;
        // 在初始状态时
        if (mAnimator == null) {
            tempFraction = 1.0f;
        }

        canvas.save();
        int alpha = 255;
        if (tempFraction >= 0.2f && tempFraction < 0.7f) {
            alpha = (int) (255 * (0.5 - tempFraction + 0.2f) * 2);
        } else if (tempFraction >= 0.7f) {
            alpha = 0;
        }
        mPaint.setAlpha(alpha);
        canvas.drawBitmap(mLikeFirworks, firsWorksX, firsWorksY, mPaint);
        canvas.restore();
    }

    private void drawUnlikeFingerBitmap(Canvas canvas, int fingerImgX, int fingerImgY) {
        float s = 1.0f;
        float tempFraction = fraction;
        // 在初始状态时
        if (mAnimator == null) {
            tempFraction = 1.0f;
        }

        canvas.save();
        if (tempFraction < 0.2f) {
            // 变小到 0.8
            s = 1.0f - tempFraction;
        } else if (tempFraction < 0.7f) {
            // 变大到 1.0
            s = 0.8f + (tempFraction - 0.2f) * 2 / 5;
        }
        // 缩放变换
        canvas.scale(s, s, fingerImgX + mUnSelectedFinger.getWidth() / 2, fingerImgY +
                mUnSelectedFinger.getHeight() / 2);

        // 绘制点赞手指
        mPaint.setAlpha((int) (255 * (1 - tempFraction)));
        canvas.drawBitmap(mSelectedfinger, fingerImgX, fingerImgY, mPaint);
        // 绘制非点赞手指
        mPaint.setAlpha((int) (tempFraction * 255));
        canvas.drawBitmap(mUnSelectedFinger, fingerImgX, fingerImgY, mPaint);

        canvas.restore();
    }

    private void drawLikeFireworks(Canvas canvas, int firsWorksX, int firsWorksY) {
        float tempFraction = fraction;
        // 在初始状态时
        if (mAnimator == null) {
            tempFraction = 1.0f;
        }

        canvas.save();
        float s = 0.0f;
        if (tempFraction >= 0.5f && tempFraction < 0.7f) {
            // 变大到 1.2
            s = (tempFraction - 0.5f) * 1.2f;
        } else if (tempFraction >= 0.7f) {
            // 变小到 1.1
            s = 1.2f - (tempFraction - 0.7f) * 2.0f / 3.0f;
        }
        canvas.scale(s, s, firsWorksX + mLikeFirworks.getWidth() / 2, firsWorksY + mLikeFirworks
                .getHeight() / 2);
        canvas.drawBitmap(mLikeFirworks, firsWorksX, firsWorksY, mPaint);
        canvas.restore();
    }

    private void drawLikeFingerBitmap(Canvas canvas, int fingerImgX, int fingerImgY) {
        float s;

        canvas.save();
        if (fraction < 0.2f) {
            bitmap = mUnSelectedFinger;
            // 变小到 0.8
            s = 1.0f - fraction * 15 / 20;
        } else if (fraction < 0.7f) {
            bitmap = mSelectedfinger;
            // 变大到 1.1
            s = 0.85f + (fraction - 0.25f) * 25.0f / 45.0f;
        } else if (fraction < 0.85f) {
            bitmap = mSelectedfinger;
            // 变小到 0.95
            s = 1.1f - (fraction - 0.7f);
        } else {
            bitmap = mSelectedfinger;
            // 变大到 1.0
            s = fraction;
        }
        canvas.scale(s, s, fingerImgX + mUnSelectedFinger.getWidth() / 2, fingerImgY +
                mUnSelectedFinger.getHeight() / 2);
        canvas.drawBitmap(bitmap, fingerImgX, fingerImgY, mPaint);
        canvas.restore();
    }

    private void drawLikeCircle(Canvas canvas, int fingerImgX, int fingerImgY) {
        int alpha = 0;
        if (fraction > 0.2f && fraction <= 0.5f) {
            alpha = (int) (255 * (fraction - 0.2f) * 2 * 5 / 3);
        } else if (fraction > 0.5f && fraction <= 0.8f) {
            alpha = (int) (255 - 255 * (fraction - 0.5f) * 2 * 5 / 3);
        }
        // 绘制圆圈
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(alpha);

        int cX = fingerImgX + mUnSelectedFinger.getWidth() / 2;
        int cY = fingerImgY + mUnSelectedFinger.getHeight() / 2;

        // 缩放动画
        int radius = mSelectedfinger.getWidth() / 2;
        if (fraction > 0.2f) {
            canvas.drawCircle(cX, cY, radius * fraction * 1.5f, mPaint);
        }

        mPaint.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 处理点击事件
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // 开启动画
            startAnim();
            // 修改数字
            if (!onSelecting) {
                mLikeNum++;
                text = mSelectNumText;
            } else {
                mLikeNum--;
                text = mUnselectNumText;
            }
            // 切换点赞状态
            onSelecting = !onSelecting;
            // 刷新
            invalidate();
        }
        return true;
    }

    private void startAnim() {
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.setDuration(700);
        mAnimator.start();
    }
}