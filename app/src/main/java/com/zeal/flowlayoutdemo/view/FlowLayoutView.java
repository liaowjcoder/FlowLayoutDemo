package com.zeal.flowlayoutdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeal on 16/12/6.
 * 流式布局
 * @author zeal 
 */

public class FlowLayoutView extends ViewGroup {

    /**
     * 存储每一个的view集合
     */
    private List<List<View>> mAllViews;

    /**
     * 存储每一行的高度
     */
    private List<Integer> mLineHeights;

    public FlowLayoutView(Context context) {
        this(context, null);
    }

    public FlowLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLineHeights = new ArrayList<>();
        mAllViews = new ArrayList<>();


        setBackgroundColor(Color.RED);

        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取父容器为他s设置的测量模式和大小
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);

        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);


        Log.e("zeal", "onMeasure: " + wSize + ";" + hSize);

        //当前行的高度
        int lineHeight = 0;
        //当前行的宽度
        int lineWidth = 0;


        //记录下控件的宽高
        int width = 0;
        int height = 0;

        //遍历所有的孩子，累加孩子的宽度与当前父控件为他设置的宽度做比较
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            //得到child的layoutparams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //当前子控件实际占用的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //当前子控件实际占用的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //当前子控件添加进去已经超出了当前空间的wSize大小，那么就需要换行，
            if (childWidth + lineWidth > wSize-getPaddingLeft()-getPaddingRight()) {
                //判断添加进去的控件的宽度和当前的宽度比较取最大值
                width = Math.max(width, childWidth);
                //累加高度
                height += childHeight;
                //将lineHeight更新为换行后的child高度
                lineHeight = childHeight;
                //将lineWidth更新为换行后的child宽度
                lineWidth = childWidth;
            } else {
                //判断当前添加进入的child的高度和当前的lineHeight高度做比较，取最大值
                lineHeight = Math.max(lineHeight, childHeight);
                //累加当前宽度到lineWidth中
                lineWidth += childWidth;
            }

            //如果是最后一个控件
            if (cCount - 1 == i) {
                height += lineHeight;
                width = Math.max(width, lineWidth);
            }
        }
        //当wMode是确定的，那么就是用确定值
        //当hMode是确定的，那么就是用确定值
        setMeasuredDimension(wMode == MeasureSpec.EXACTLY ? wSize : width,
                hMode == MeasureSpec.EXACTLY ? hSize+getPaddingBottom()+getPaddingTop() : height+getPaddingBottom()+getPaddingTop());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //重置
        mAllViews.clear();
        mLineHeights.clear();

        //当前控件的宽度
        int width = getMeasuredWidth()-getPaddingLeft()-getPaddingRight();
        //存储每一行的view
        List<View> views = new ArrayList<>();

        //记录每一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();
        //遍历所有的孩子，得到mAllViews集合和mLines的数据
        for (int i = 0; i < cCount; i++) {

            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //当前子控件实际占用的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //当前子控件实际占用的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //当将child添加进入会超过当前控件的大小，则需要换行
            if (childWidth + lineWidth > width) {
                //往mLineHeights添加当前行的高度
                mLineHeights.add(lineHeight);
                //更新lineHeight
                lineHeight = childHeight;
                //重置当前行
                lineWidth = 0;
                //将当前行的所有子view集合添加到mAllViews中
                mAllViews.add(views);
                //重新创建一个新的集合
                views = new ArrayList<>();
            }
            //不需要换行
            //往views添加当前子view
            views.add(child);
            //lineHeight与当前子view的childHeight比较取最大值
            lineHeight = Math.max(lineHeight, childHeight);
            lineWidth += childWidth;


            //处理最后一行
            if (cCount - 1 == i) {
                mAllViews.add(views);
                mLineHeights.add(lineHeight);
            }
        }


        //开始布局
        int left = getPaddingLeft();
        int top = getPaddingTop();

        //遍历每一行
        for (int i = 0; i < mAllViews.size(); i++) {
            //得到每一个装在view的集合
            List<View> lineViews = mAllViews.get(i);

            //遍历每一行中的所有view
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);

                //判断当前view是否是GONE
                if (child.getVisibility() == GONE) {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                //计算子view的l,t,r,b
                int cl = left + lp.leftMargin;
                int ct = top + lp.topMargin;
                int cr = cl + child.getMeasuredWidth();//注意这里不需要加right-margin
                int cb = ct + child.getMeasuredHeight();//注意这里不需要加bottom-margin
                //布局子view
                child.layout(cl, ct, cr, cb);
                //更新left
                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            top += mLineHeights.get(i);
            left = getPaddingLeft();
        }


    }


    //因为孩子之间需要控制间距大小，因此需要提供一个MarginLayoutParams

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
