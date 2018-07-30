>原文链接：[https://mp.weixin.qq.com/s/EYyTBtM9qCdmB9nlDEF-3w](https://mp.weixin.qq.com/s/EYyTBtM9qCdmB9nlDEF-3w)

相信做前端的都做过页面锚点定位的功能，通过`<a href="#head">` 去设置页面内锚点定位跳转。  
本篇文章就使用`tablayout`、`scrollview`来实现android锚点定位的功能。  
效果图：  

![](https://user-gold-cdn.xitu.io/2018/7/21/164bafaa29976d72?w=236&h=387&f=gif&s=712017)   
#### 实现思路
1、监听`scrollview`滑动到的位置，`tablayout`切换到对应标签  
2、`tablayout`各标签点击，`scrollview`可滑动到对应区域   
#### 自定义scrollview
因为我们需要监听到滑动过程中`scrollview`的滑动距离，自定义`scrollview`通过接口暴露滑动的距离。
```
public class CustomScrollView extends ScrollView {

    public Callbacks mCallbacks;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mCallbacks != null) {
            mCallbacks.onScrollChanged(l, t, oldl, oldt);
        }
    }
    //定义接口用于回调
    public interface Callbacks {
        void onScrollChanged(int x, int y, int oldx, int oldy);
    }

}

```

布局文件里 `tablayout` 和 `CustomScrollView`，内容暂时使用`LinearLayout`填充。
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <android.support.design.widget.TabLayout
        android:id="@+id/tablayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabIndicatorColor="@color/colorPrimary"
        app:tabMode="scrollable"
        app:tabSelectedTextColor="@color/colorPrimary" />

    <com.tabscroll.CustomScrollView
        android:id="@+id/scrollView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:fitsSystemWindows="true">

        <LinearLayout
            android:id="@+id/container"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

        </LinearLayout>

    </com.tabscroll.CustomScrollView>

</LinearLayout>
```
#### 数据模拟
数据模拟，动态添加`scrollview`内的内容，这里自定义了`AnchorView`当作每一块的填充内容。
```
private String[] tabTxt = {"客厅", "卧室", "餐厅", "书房", "阳台", "儿童房"};
//内容块view的集合
private List<AnchorView> anchorList = new ArrayList<>();
//判读是否是scrollview主动引起的滑动，true-是，false-否，由tablayout引起的
private boolean isScroll;
//记录上一次位置，防止在同一内容块里滑动 重复定位到tablayout
private int lastPos;

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
```
定义变量标志`isScroll`，用于判断`scrollview`的滑动由谁引起的，避免通过点击`tabLayout`引起的scrollview滑动问题。   
定义变量标志`lastPos`，当`scrollview` 在同一模块中滑动时，则不再去调用`tabLayout.setScrollPosition`刷新标签。

自定义的`AnchorView`：
```
public class AnchorView extends LinearLayout {

    private TextView tvAnchor;
    private TextView tvContent;

    public AnchorView(Context context) {
        this(context, null);
    }

    public AnchorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnchorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_anchor, this, true);
        tvAnchor = view.findViewById(R.id.tv_anchor);
        tvContent = view.findViewById(R.id.tv_content);
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        tvContent.setBackgroundColor(Color.rgb(r, g, b));
    }

    public void setAnchorTxt(String txt) {
        tvAnchor.setText(txt);
    }

    public void setContentTxt(String txt) {
        tvContent.setText(txt);
    }
}

```
#### 实现 
`scrollview`的滑动监听：
```
scrollView.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //当由scrollview触发时，isScroll 置true
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

//tablayout对应标签的切换
private void setScrollPos(int newPos) {
    if (lastPos != newPos) {
        //该方法不会触发tablayout 的onTabSelected 监听
        tabLayout.setScrollPosition(newPos, 0, true);
    }
    lastPos = newPos;
}
```
`tabLayout`的点击切换：
```
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
```

![](https://user-gold-cdn.xitu.io/2018/7/21/164bae56a11986bf?w=251&h=410&f=gif&s=740251)  

至此效果出来了，但是  
**问题来了** 可以看到当点击最后一项时，`scrollView`滑动到底部时并没有呈现出我们想要的效果，希望滑到最后一个时，全屏只有最后一块内容显示。  
所以这里需要处理下最后一个view的高度，当不满全屏时，重新设置他的高度，通过计算让其撑满屏幕。
```
//监听判断最后一个模块的高度，不满一屏时让最后一个模块撑满屏幕
private ViewTreeObserver.OnGlobalLayoutListener listener;

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
```
这样就达到了预期的效果了。  

![](https://user-gold-cdn.xitu.io/2018/7/21/164bafaa29976d72?w=236&h=387&f=gif&s=712017)    

写到这里，tablayout + scrollview的锚点定位成型了，在实际项目中，我们还可以使用tablayout + recyclerview 来完成同样的效果，后续的话会带来这样的文章。  

这段时间自己在做一个小程序，包括数据爬取 + 后台 + 小程序的，可能要过段时间才能出来，主要是数据爬虫那边比较麻烦的...期待下！

详细代码见  
github地址：[https://github.com/taixiang/tabScroll](https://github.com/taixiang/tabScroll)  

欢迎关注我的博客：[https://blog.manjiexiang.cn/](https://blog.manjiexiang.cn/)  
更多精彩欢迎关注微信号：春风十里不如认识你  
![image.png](https://upload-images.jianshu.io/upload_images/7569533-cfeb1f55473a2143.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  

有个「佛系码农圈」，欢迎大家加入畅聊，开心就好！  
![](https://user-gold-cdn.xitu.io/2018/7/22/164bfe5d54f268a2?w=188&h=250&f=jpeg&s=41186)     
过期了，可加我微信 tx467220125 拉你入群。
