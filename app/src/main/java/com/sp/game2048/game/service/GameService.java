package com.sp.game2048.game.service;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.ScoreHistoryActivity;
import com.sp.game2048.dialog.DataShowDialog;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.enums.ResultCodeEnum;
import com.sp.game2048.game.entity.Ranking;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.socket.entity.SocketMessage;
import com.sp.game2048.socket.enums.G2048SocketCodeEnum;
import com.sp.game2048.socket.enums.MsgHandleTypeEnum;
import com.sp.game2048.socket.service.SocketService;
import com.sp.game2048.socket.util.SocketUtil;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.MODE_PRIVATE;

/**
 * @do 游戏业务处理
 * @author liuhua
 * @date 2020/3/15 2:00 PM
 */
public class GameService {
    /**
     * @do 更新排名显示
     * @author liuhua
     * @date 2020/3/15 2:00 PM
     */
    public void updateRanking(AtomicInteger ranking,Runnable runnable){
        if(!ClassUtil.get(SocketService.class).isOnline()){
            return ;
        }
        ClassUtil.get(SocketService.class).sendMessage(SocketUtil.getSocketMessageHandle(MsgHandleTypeEnum.G2048.getValue()), new SocketMessage(G2048SocketCodeEnum.GET_RANKING.getValue()), args -> {
            JSONObject socketMessage = JSONObject.parseObject(args[0].toString());
            HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
            JSONObject body = socketMessage.getJSONObject("body");
            if(body!=null) {
                message.obj = new Object[]{GameService.class,body};
                message.getData().putString("method", "refreshRanking");
                handleMessage.sendMessage(message);
                //更新游戏结束时排名
                if(ranking!=null) {
                    ranking.set(body.getInteger("ranking"));
                }
                if(runnable!=null) {
                    runnable.run();
                }
            }
        });
    }
    /**
     * @do 显示排行榜
     * @author liuhua
     * @date 2020/3/17 11:19 PM
     */
    public void showRankingList(){
        if(!ClassUtil.get(SocketService.class).isOnline()){
            AlertUtil.alertError("未登陆,无法查看排行榜");
            return ;
        }
        SweetAlertDialog sweetAlertDialog = AlertUtil.alertProcess("正在加载...");
        ClassUtil.get(SocketService.class).sendMessage(SocketUtil.getSocketMessageHandle(MsgHandleTypeEnum.G2048.getValue()), new SocketMessage(G2048SocketCodeEnum.LIST_RANKING.getValue()), args -> {
            sweetAlertDialog.cancel();
            JSONObject socketMessage = JSONObject.parseObject(args[0].toString());
            if(!ResultCodeEnum.OK.getValue().equals(socketMessage.getInteger("code"))){
                AlertUtil.alertError(socketMessage.getString("msg"));
                return;
            }
            JSONArray body = socketMessage.getJSONArray("body");
            if(body==null||body.isEmpty()){
                return;
            }
            List<Ranking> list = body.toJavaList(Ranking.class);
            HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
            message.obj = new Object[]{GameService.class,list};
            message.getData().putString("method", "showRankingList");
            handleMessage.sendMessage(message);

        });
    }
    /**
     * @do 显示分数历史
     * @author liuhua
     * @date 2020/3/17 11:19 PM
     */
    public void showScoreHistory(){
        if(!ClassUtil.get(SocketService.class).isOnline()){
            AlertUtil.alertError("未登陆,无法查看分数历史");
            return ;
        }
        SweetAlertDialog sweetAlertDialog = AlertUtil.alertProcess("正在加载...");
        ClassUtil.get(SocketService.class).sendMessage(SocketUtil.getSocketMessageHandle(MsgHandleTypeEnum.G2048.getValue()), new SocketMessage(G2048SocketCodeEnum.MY_SCORE_HISTORY.getValue()), args -> {
            sweetAlertDialog.cancel();
            JSONObject socketMessage = JSONObject.parseObject(args[0].toString());
            if(!ResultCodeEnum.OK.getValue().equals(socketMessage.getInteger("code"))){
                AlertUtil.alertError(socketMessage.getString("msg"));
                return;
            }
            JSONArray body = socketMessage.getJSONArray("body");
            if(body==null||body.isEmpty()){
                AlertUtil.toast("没有数据",Toast.LENGTH_SHORT);
                return;
            }
            List<Ranking> list = body.toJavaList(Ranking.class);
            HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
            message.obj = new Object[]{GameService.class,list};
            message.getData().putString("method", "showScoreHistory");
            handleMessage.sendMessage(message);

        });
    }
    /**
     * @do 显示排行榜
     * @author liuhua
     * @date 2020/3/17 11:33 PM
     */
    public void showRankingList(ArrayList<Ranking> list){
        DataShowDialog dataShowDialog  = new DataShowDialog(list);
        dataShowDialog.show();
    }
    /**
     * @do 显示分数历史
     * @author liuhua
     * @date 2020/3/17 11:33 PM
     */
    public void showScoreHistory(ArrayList<Ranking> list){
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        Collections.reverse(list);
        int[] ints = list.stream().mapToInt(e -> e.getScore()).toArray();
        Intent intent1 = new Intent(mainActivity,
                ScoreHistoryActivity.class);
        intent1.putExtra("scores",ints);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainActivity.startActivity(intent1);

    }
    /**
     * @do 刷新排名
     * @author liuhua
     * @date 2020/3/15 2:03 PM
     */
    public void refreshRanking(JSONObject jsonObject){
        MainActivity mainActivity = ClassUtil.get(MainActivity.class);
        SharedPreferences userInfo = mainActivity.getSharedPreferences("UserInfo", MODE_PRIVATE);
        if(jsonObject==null) {
            jsonObject = JSONObject.parseObject(userInfo.getString("ranking","{}"));
        }else{
            SharedPreferences.Editor edit = userInfo.edit();
            edit.putString("ranking", jsonObject.toJSONString());
            edit.apply();
        }
        Integer ranking = jsonObject.getInteger("ranking");
        Integer score = jsonObject.getInteger("score");
        if(ranking==null){
            return;
        }
        View viewById1 = mainActivity.findViewById(R.id.nav_view);
        TextView viewById = viewById1.findViewById(R.id.ranking);
        viewById.setText(ranking.toString());
        int rankIco = 0;
        if(ranking==1){
            rankIco = R.mipmap.rank_one;
        }else if(ranking==2){
            rankIco = R.mipmap.rank_two;
        }else if(ranking==3){
            rankIco = R.mipmap.rank_three;
        }else{
            rankIco = R.mipmap.rank_other;
        }
        Drawable drawable = mainActivity.getDrawable(rankIco);
        drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
        viewById.setCompoundDrawables(drawable,null,null,null);
        viewById = viewById1.findViewById(R.id.score);
        viewById.setText(String.format("最高分数:%d",score));
    }
    /**
     * @do 更新分数
     * @author liuhua
     * @date 2020/3/16 9:30 PM
     */
    public void updateScore(Dialog dialog, int score) {
        if(!ClassUtil.get(SocketService.class).isOnline()){
            TextView rankingText = dialog.findViewById(R.id.rankingText);
            rankingText.setText("(未登陆)");
            return ;
        }
        if(score<=0){
            TextView rankingText = dialog.findViewById(R.id.rankingText);
            rankingText.setText("(排名没有变化)");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("score",score);
        ClassUtil.get(SocketService.class).sendMessage(SocketUtil.getSocketMessageHandle(MsgHandleTypeEnum.G2048.getValue()), new SocketMessage(G2048SocketCodeEnum.UPDATE_SCORE.getValue(),jsonObject), args -> {
            SocketMessage socketMessage = JSONObject.parseObject(args[0].toString(),SocketMessage.class);
            if(ResultCodeEnum.OK.getValue().equals(socketMessage.getCode())){
                //更新分数成功
                AtomicInteger ranking = new AtomicInteger(0);
                updateRanking(ranking, () -> {
                    TextView textView = dialog.findViewById(R.id.ranking);
                    textView.setText(String.valueOf(ranking.get()));
                    Integer sRanking = (Integer) textView.getTag();
                    TextView rankingText = dialog.findViewById(R.id.rankingText);
                    if(sRanking<ranking.get()){
                        rankingText.setText("(排名上升啦)");
                    }else{
                        rankingText.setText("(排名没有变化)");
                    }
                });
            }else{
                AlertUtil.toast("更新分数失败:"+socketMessage.getMsg(),Toast.LENGTH_SHORT);
            }
        });
    }
}
