package com.sp.game2048.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.game.entity.Ranking;
import com.sp.game2048.game.util.RankingListViewUtil;
import com.sp.game2048.util.ClassUtil;

import java.util.List;

public class DataShowDialog extends Dialog {
    private List<Ranking> list;
    public DataShowDialog(List<Ranking> list) {
        super(ClassUtil.get(MainActivity.class), R.style.FullScreen);
        this.list = list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ranking_list);
        this.setCanceledOnTouchOutside(false);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);//宽高最大
        RankingListViewUtil.handleResult(this,list);
    }
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        // 解决横竖屏切换的适配问题
        Display display = ClassUtil.get(MainActivity.class).getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = display.getWidth();
        this.getWindow().setAttributes(params);
        this.getWindow().setGravity(Gravity.BOTTOM);
    }
}
