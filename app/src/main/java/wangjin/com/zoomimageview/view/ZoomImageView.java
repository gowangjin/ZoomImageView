package wangjin.com.zoomimageview.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ZoomImageView extends android.support.v7.widget.AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    private boolean mOnce = false;

    /**
     *初始化时缩放的值
     */
    private float mInitScale;
    /**
     * 双击放大时的缩放值
     */
    private float mMidScale;
    /**
     * 放大的最大值
     */
    private float mMaxScale;
    /**
     * 捕获用户多指触控时缩放比例
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private Matrix mScaleMatrix;
    public ZoomImageView(Context context) {
        this(context,null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        mScaleMatrix = new Matrix();

        mScaleGestureDetector = new ScaleGestureDetector(context,this);
        setOnTouchListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 获取ImageView加载完成的图片
     */
    @Override
    public void onGlobalLayout() {
        if(!mOnce) {
            mOnce = true;
            //控件的宽和高
            int width = getWidth();
            int height = getHeight();
            //得到我们的图片以及宽和高
            Drawable d = getDrawable();
            if( d== null) return;
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = 1.0f;
            if(dw > width && dh < height){
                scale = width * 1.0f / dw;
            }
            if(dh > height && dw < width){
                scale = height * 1.0f / dh;
            }
            if(dw > width && dh > height){
                scale = Math.min(width * 1.0f /dw,height * 1.0f / dh);
            }

            if( dw < width && dh < height){
                scale = Math.min(width * 1.0f / dw,height * 1.0f / dh);
            }
            mInitScale = scale;
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;
            //将图片移动至控件的中心
            int dx = getWidth()/2 - dw/2;
            int dy = getHeight()/2 - dh/2;

            mScaleMatrix.postTranslate(dx,dy);
            mScaleMatrix.postScale(mInitScale,mInitScale,(float) width/2,(float)height/2);
            setImageMatrix(mScaleMatrix);
        }
    }

    /**
     * 获取当前图片的缩放值
     * @return
     */
    public  float getScale() {
        float[] value = new float[9];
        mScaleMatrix.getValues(value);
        return  value[Matrix.MSCALE_X];
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        if(getDrawable() == null) return true;
        //缩放范围控制
        if((scale < mMaxScale && scaleFactor > 1.0f) ||
                (scale > mInitScale && scaleFactor < 1.0f)) {
            if(scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale /scale;
            }
            if(scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }
            mScaleMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusY());

            checkBorderAndCenterWhenScale();

            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    /**
     * 获得图片放大缩小以后的宽和高
     * @return
     */
    private RectF getMatrixRectf() {
        Matrix matrix = mScaleMatrix;
        Drawable d = getDrawable();
        RectF rectF = new RectF();
        if(d != null) {
            rectF.set(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }
    /**
     * 在缩放的时候进行边界控制和位置控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectf();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if(rect.width() >= width) {
            if(rect.left > 0) {
                deltaX = -rect.left;
            }
            if(rect.right < width) {
                deltaX = width - rect.right;
            }
        }

        if(rect.height() >= height) {
            if(rect.top > 0) {
                deltaY = - rect.top;
            }
            if(rect.bottom < height) {
               deltaY = height - rect.bottom;
            }
        }
        //如果宽度或者高度小于控件的宽高，让其居中
        if(rect.width() < width) {
            deltaX = width/2 - rect.right + rect.width()/2;
        }
        if(rect.height() < height) {
            deltaY = height/2 - rect.bottom + rect.height() / 2;
        }
        mScaleMatrix.postTranslate(deltaX,deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }
}
