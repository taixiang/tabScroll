package com.tabscroll;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

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
