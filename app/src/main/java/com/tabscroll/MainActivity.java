package com.tabscroll;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private CustomScrollView scrollView;
    private LinearLayout container;
    private String[] tabTxt = {"客厅", "卧室", "餐厅", "书房", "阳台", "儿童房"};
    //内容块view的集合
    private List<AnchorView> anchorList = new ArrayList<>();
    //判读是否是scrollview主动引起的滑动，true-是，false-否，由tablayout引起的
    private boolean isScroll;
    //记录上一次位置，防止在同一内容块里滑动 重复定位到tablayout
    private int lastPos = 0;
    //监听判断最后一个模块的高度，不满一屏时让最后一个模块撑满屏幕
    private ViewTreeObserver.OnGlobalLayoutListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tablayout);
        scrollView = findViewById(R.id.scrollView);
        container = findViewById(R.id.container);

        //模拟数据，填充scrollview
        for (int i = 0; i < tabTxt.length; i++) {
            AnchorView anchorView = new AnchorView(this);
            anchorView.setAnchorTxt(tabTxt[i]);
            anchorView.setContentTxt(tabTxt[i]);
            anchorList.add(anchorView);
            container.addView(anchorView);
        }

        //tablayout设置标签
        for (int i = 0; i < tabTxt.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(tabTxt[i]));
        }

        listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int screenH = getScreenHeight();
                int statusBarH = getStatusBarHeight(MainActivity.this);
                int tabH = tabLayout.getHeight();
                //计算内容块所在的高度，全屏高度-状态栏高度-tablayout的高度-内容container的padding 16dp
                int lastH = screenH - statusBarH - tabH - 16 * 3;
                AnchorView lastView = anchorList.get(anchorList.size() - 1);
                //当最后一个view 高度小于内容块高度时，设置其高度撑满
                if (lastView.getHeight() < lastH) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.height = lastH;
                    lastView.setLayoutParams(params);
                }
                container.getViewTreeObserver().removeOnGlobalLayoutListener(listener);

            }
        };
        container.getViewTreeObserver().addOnGlobalLayoutListener(listener);



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //点击标签，使scrollview滑动，isScroll置false
                isScroll = false;
                int pos = tab.getPosition();
                int top = anchorList.get(pos).getTop();
                scrollView.smoothScrollTo(0, top);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //当滑动由scrollview触发时，isScroll 置true
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isScroll = true;
                }
                return false;
            }
        });

        scrollView.setCallbacks(new CustomScrollView.Callbacks() {
            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy) {
                if (isScroll) {
                    for (int i = tabTxt.length - 1; i >= 0; i--) {
                        //根据滑动距离，对比各模块距离父布局顶部的高度判断
                        if (y > anchorList.get(i).getTop() - 10) {
                            setScrollPos(i);
                            break;
                        }
                    }
                }
            }
        });

    }

    //tablayout对应标签的切换
    private void setScrollPos(int newPos) {
        if (lastPos != newPos) {
            //该方法不会触发tablayout 的onTabSelected 监听
            tabLayout.setScrollPosition(newPos, 0, true);
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
