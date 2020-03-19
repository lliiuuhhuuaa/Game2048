package com.sp.game2048.game.util;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.dialog.DataShowDialog;
import com.sp.game2048.game.entity.Ranking;
import com.sp.game2048.util.ClassUtil;

import java.util.List;

public class RankingListViewUtil {

    /**
     * 显示结果
     * @param context
     * @param list
     */
    public static void handleResult(DataShowDialog context, List<Ranking> list) {
        if(list.size()<1){
            return ;
        }
        ListView listView = context.findViewById(R.id.resultListView);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = context.getLayoutInflater();
                View view = convertView==null?inflater.inflate(R.layout.ranking_list_item, null):convertView;
                Ranking ranking = list.get(position);
                TextView textView = view.findViewById(R.id.list_item1);
                Integer rankIco = null;
                if(ranking.getRanking()==1){
                    rankIco = R.mipmap.rank_one;
                }else if(ranking.getRanking()==2){
                    rankIco = R.mipmap.rank_two;
                }else if(ranking.getRanking()==3){
                    rankIco = R.mipmap.rank_three;
                }else{
                    rankIco = R.mipmap.rank_other;
                }
                textView.setText(ranking.getRanking().toString());
                if(rankIco!=null) {
                    Drawable drawable = ClassUtil.get(MainActivity.class).getDrawable(rankIco);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth() - 20, drawable.getMinimumHeight() - 20);
                    textView.setCompoundDrawables(drawable, null, null, null);
                }
                textView = view.findViewById(R.id.list_item2);
                textView.setText(ranking.getNickname());
                textView = view.findViewById(R.id.list_item3);
                textView.setText(ranking.getScore().toString());
                return view;
            }
        });

    }
}
