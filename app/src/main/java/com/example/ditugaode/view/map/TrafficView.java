package com.example.ditugaode.view.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.amap.api.services.traffic.TrafficSearch;
import com.example.ditugaode.R;
import com.example.ditugaode.view.base.BaseIconView;

public class TrafficView extends BaseIconView {

    private OnTrafficChangeListener mListener;
    public TrafficView(Context context) {
        this(context,null);
    }

    public TrafficView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TrafficView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换icon状态
                setIconViewSelected(!isIconViewSelected());
                if(mListener != null){
                    mListener.onTrafficChanged(isIconViewSelected());
                }
            }
        });

        setIconViewSelected(true);
    }

    @Override
    public boolean createBackground() {
        setBackgroundResource(R.drawable.icon_up_selector);
        return false;
    }

    @Override
    public boolean createIcon() {
        setIconBackground(R.drawable.icon_traffic_selector);
        return false;
    }

    public void setOnTrafficChangeListener(OnTrafficChangeListener listener){
        if(listener != null){
            mListener = listener;
        }
    }


    /**
     * 交通图状态改变回调监听
     */
    public interface OnTrafficChangeListener{

        /**
         * 交通状态回调方法
         * @param selected
         */
        void onTrafficChanged(boolean selected);

    }
}
