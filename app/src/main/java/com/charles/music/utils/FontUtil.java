package com.charles.music.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontUtil {

    public void changeFont(Context context, TextView textView) {
        // 得到AssetManager
        AssetManager manager = context.getAssets();

        //根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(manager, "fonts/lanting.ttf");

        // 设置字体
        textView.setTypeface(tf);
    }
}
