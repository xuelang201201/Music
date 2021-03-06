package com.charles.music.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.music.activity.SplashActivity;
import com.charles.music.application.AppCache;
import com.charles.music.service.PlayService;
import com.charles.music.utils.FontUtil;

import butterknife.ButterKnife;

/**
 * 基类
 */
public abstract class BaseFragment extends Fragment implements View.OnTouchListener {
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public abstract int getLayoutId();

    public abstract void initView();

    public View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(getLayoutId(), container, false);
        }
        mView.setOnTouchListener(this); // 防止fragment被击穿
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, getActivity());
        initView();
    }

    protected PlayService getPlayService() {
        PlayService playService = AppCache.getPlayService();
        if (playService == null) {
            throw new NullPointerException("play service is null");
        }
        return playService;
    }

    protected boolean checkServiceAlive() {
        if (AppCache.getPlayService() == null) {
            startActivity(new Intent(getActivity(), SplashActivity.class));
            AppCache.clearStack();
            return false;
        }
        return true;
    }

    /**
     * 改变字体
     * @param textView TextView
     * @param isBold 是否加粗
     */
    protected void changeFont(TextView textView, boolean isBold) {
        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), textView);
        if (isBold) {
            textView.getPaint().setFakeBoldText(true); // 设置为粗体
        } else {
            textView.getPaint().setFakeBoldText(false); // 不设置为粗体
        }
    }

    /**
     * 防止fragment被击穿
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
