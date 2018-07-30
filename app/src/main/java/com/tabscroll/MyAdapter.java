package com.tabscroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private String[] tabTxt;
    private int lastH;

    public MyAdapter(Context context, String[] tabTxt, int lastH) {
        this.context = context;
        this.tabTxt = tabTxt;
        this.lastH = lastH;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_view, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.anchorView.setContentTxt(tabTxt[position]);
        holder.anchorView.setAnchorTxt(tabTxt[position]);
        //判断最后一个view
        if (position == tabTxt.length - 1) {
            if (holder.anchorView.getHeight() < lastH) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.height = lastH;
                holder.anchorView.setLayoutParams(params);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private AnchorView anchorView;

        public MyViewHolder(View itemView) {
            super(itemView);
            anchorView = itemView.findViewById(R.id.anchorView);
        }
    }


}
