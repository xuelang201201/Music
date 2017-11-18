package com.charles.music.activity;

import android.support.v4.app.Fragment;

import com.charles.music.fragment.SplashFragment;

public class SplashActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return SplashFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
    }
}
