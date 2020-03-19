package com.sp.game2048.dialog;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.game.entity.Ranking;
import com.sp.game2048.game.util.RankingListViewUtil;
import com.sp.game2048.util.ClassUtil;

import java.util.ArrayList;
import java.util.List;

public class ScoreHistoryShowDialog extends Dialog {
    private List<Ranking> list;
    public ScoreHistoryShowDialog(List<Ranking> list) {
        super(ClassUtil.get(MainActivity.class), R.style.FullScreen);
        this.list = list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_linechart);
        this.setCanceledOnTouchOutside(false);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);//宽高最大
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            entries.add(new Entry(i,list.get(i).getScore()));
        }
        LineChart chart = findViewById(R.id.chart);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragDecelerationFrictionCoef(0.9f);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setPinchZoom(true);
        chart.animateX(1500);
        LineDataSet lineDataSet = new LineDataSet(entries, "分数趋势");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(ColorTemplate.getHoloBlue());
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        lineDataSet.setDrawCircleHole(false);
        LineData data = new LineData(lineDataSet);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);
        chart.setData(data);
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
