package com.tabscroll;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tx on 2018/8/4.
 */

public class StickyActivity extends AppCompatActivity {

    /**
     * 占位tablayout，用于滑动过程中去确定实际的tablayout的位置
     */
    private TabLayout holderTabLayout;
    /**
     * 实际操作的tablayout，
     */
    private TabLayout realTabLayout;
    private CustomScrollView scrollView;
    private LinearLayout container;
    private String[] tabTxt = {"客厅", "卧室", "餐厅", "书房", "阳台", "儿童房"};

    private List<AnchorView> anchorList = new ArrayList<>();

    //判读是否是scrollview主动引起的滑动，true-是，false-否，由tablayout引起的
    private boolean isScroll;
    //记录上一次位置，防止在同一内容块里滑动 重复定位到tablayout
    private int lastPos = 0;
    //监听判断最后一个模块的高度，不满一屏时让最后一个模块撑满屏幕
    private ViewTreeObserver.OnGlobalLayoutListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ali_home_more);
        holderTabLayout = findViewById(R.id.tablayout_holder);
        realTabLayout = findViewById(R.id.tablayout_real);
        scrollView = findViewById(R.id.scrollView);
        container = findViewById(R.id.container);

        for (int i = 0; i < tabTxt.length; i++) {
            AnchorView anchorView = new AnchorView(this);
            anchorView.setAnchorTxt(tabTxt[i]);
            anchorView.setContentTxt(tabTxt[i]);
            anchorList.add(anchorView);
            container.addView(anchorView);
        }
        for (int i = 0; i < tabTxt.length; i++) {
            holderTabLayout.addTab(holderTabLayout.newTab().setText(tabTxt[i]));
            realTabLayout.addTab(realTabLayout.newTab().setText(tabTxt[i]));
        }


        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //计算让最后一个view高度撑满屏幕
                int screenH = getScreenHeight();
                int statusBarH = getStatusBarHeight(StickyActivity.this);
                int tabH = holderTabLayout.getHeight();
                int lastH = screenH - statusBarH - tabH - 16 * 3;
                AnchorView anchorView = anchorList.get(anchorList.size() - 1);
                if (anchorView.getHeight() < lastH) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.height = lastH;
                    anchorView.setLayoutParams(params);
                }

                //一开始让实际的tablayout 移动到 占位的tablayout处，覆盖占位的tablayout
                realTabLayout.setTranslationY(holderTabLayout.getTop());
                realTabLayout.setVisibility(View.VISIBLE);
                container.getViewTreeObserver().removeOnGlobalLayoutListener(listener);

            }
        };
        container.getViewTreeObserver().addOnGlobalLayoutListener(listener);


        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isScroll = true;
                }
                return false;
            }
        });

        //监听scrollview滑动
        scrollView.setCallbacks(new CustomScrollView.Callbacks() {
            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy) {
                //根据滑动的距离y(不断变化的) 和 holderTabLayout距离父布局顶部的距离(这个距离是固定的)对比，
                //当y < holderTabLayout.getTop()时，holderTabLayout 仍在屏幕内，realTabLayout不断移动holderTabLayout.getTop()距离，覆盖holderTabLayout
                //当y > holderTabLayout.getTop()时，holderTabLayout 移出，realTabLayout不断移动y，相对的停留在顶部，看上去是静止的
                int translation = Math.max(y, holderTabLayout.getTop());
                realTabLayout.setTranslationY(translation);

                if (isScroll) {
                    for (int i = tabTxt.length - 1; i >= 0; i--) {
                        //需要y减去顶部内容区域的高度(具体看项目的高度，这里demo写死的200dp)
                        if (y - 200 * 3 > anchorList.get(i).getTop() - 10) {
                            setScrollPos(i);
                            break;
                        }
                    }
                }

            }
        });

        //实际的tablayout的点击切换
        realTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isScroll = false;
                int pos = tab.getPosition();
                int top = anchorList.get(pos).getTop();
                //同样这里滑动要加上顶部内容区域的高度(这里写死的高度)
                scrollView.smoothScrollTo(0, top + 200 * 3);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    private void setScrollPos(int newPos) {
        if (lastPos != newPos) {
            realTabLayout.setScrollPosition(newPos, 0, true);
        }
        lastPos = newPos;
    }

    private int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
