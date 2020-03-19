package com.sp.game2048.handle;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.sp.game2048.GameActivity;
import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.dialog.DataShowDialog;
import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.game.service.GameService;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.MODE_PRIVATE;

public class HandleMessage extends Handler {
    SoundPool soundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).build();

    public HandleMessage() {
        super();
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        if (msg.what == CountDownMsgTypeEnum.UPDATE_NUMBER.getValue()) {
            TextView textView = (TextView) msg.obj;
            textView.setText(msg.getData().getString("number"));
        } else if (msg.what == CountDownMsgTypeEnum.GAME_OVER.getValue()) {
            final Context context = (Context) msg.obj;
            final Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog_Alert);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);// 一句话搞定
            dialog.setContentView(R.layout.game_over);
            dialog.setCanceledOnTouchOutside(false);
            //分数
            TextView viewById = dialog.findViewById(R.id.score);
            int score = msg.getData().getInt("score");
            viewById.setText(String.valueOf(score));
            //排名
            SharedPreferences userInfo = ClassUtil.get(MainActivity.class).getSharedPreferences("UserInfo", MODE_PRIVATE);
            Integer ranking = null;
            if(userInfo!=null) {
                JSONObject jsonObject = JSONObject.parseObject(userInfo.getString("ranking", "{}"));
                ranking = jsonObject.getInteger("ranking");
            }
            viewById = dialog.findViewById(R.id.ranking);
            viewById.setText(ranking==null?"未上榜":String.valueOf(ranking));
            viewById.setTag(ranking==null?0:ranking);
            //更新分数记录
            GameService gameService = ClassUtil.get(GameService.class);
            gameService.updateScore(dialog,score);
            dialog.findViewById(R.id.backHome).setOnClickListener(v -> {
                dialog.cancel();
                ClassUtil.get(GameActivity.class).finish();
            });
            dialog.findViewById(R.id.rePlay).setOnClickListener(v -> {
                ClassUtil.get(GameActivity.class).recreate();
                ClassUtil.get(GameActivity.class).recreate();
                dialog.cancel();
            });
            dialog.findViewById(R.id.ranking_list).setOnClickListener(v -> {
                ClassUtil.get(GameActivity.class).finish();
                gameService.showRankingList();
            });
            try {
                dialog.show();
            }catch (Exception e){}
        } else if (msg.what == CountDownMsgTypeEnum.START_321.getValue()) {
            final Object[] objects = (Object[]) msg.obj;
            final Context context = (Context) objects[0];
            final Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog_Alert);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);// 一句话搞定
            dialog.setContentView(R.layout.start_321);
            dialog.setCanceledOnTouchOutside(false);
            HandlerThread countDownThread = new HandlerThread("START_321");
            countDownThread.setPriority(Thread.MIN_PRIORITY);
            countDownThread.start();
            final AtomicInteger count = new AtomicInteger(msg.getData().getInt("num", 3));
            final TextView viewById = dialog.findViewById(R.id.start_num);
            viewById.setText(String.valueOf(count.get()));
            final Handler handler = new Handler(countDownThread.getLooper());
            final Runnable countDownWork = new Runnable() {
                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public void run() {
                    int num = count.decrementAndGet();
                    if(num==2){
                        //插入开始音乐
                        Message message = Message.obtain((Handler) objects[2], CountDownMsgTypeEnum.PLAY_SOUND_COMPOUND.getValue());
                        message.obj = context;
                        message.getData().putInt("sound",R.raw.ready_go);
                        ((Handler) objects[2]).sendMessage(message);
                    }
                    if (num < 1) {
                        //回调进度条处理
                        ((Handler) objects[2]).post((Runnable) objects[1]);
                        dialog.cancel();
                    } else {
                        viewById.setText(String.valueOf(num));
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(countDownWork, 1000);
            dialog.show();
        } else if (msg.what == CountDownMsgTypeEnum.PLAY_SOUND_COMPOUND.getValue()) {
            int sound = msg.getData().getInt("sound");
            final Context context = (Context) msg.obj;
            try {
                final int voiceId = soundPool.load(context, sound, 1);
                //异步需要等待加载完成，音频才能播放成功
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    if (status == 0) {
                        //第一个参数soundID
                        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
                        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
                        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
                        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
                        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
                        soundPool.play(voiceId, 1, 1, 1, 0, 1);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        } else if (msg.what == CountDownMsgTypeEnum.VIBRATE.getValue()) {
            final Context context = (Context) msg.obj;
            int time = msg.getData().getInt("time", 500);
            Vibrator vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
            vibrator.vibrate(time);
        }else if(msg.what==CountDownMsgTypeEnum.ALERT_MSG.getValue()){
            //弹提示框
            Bundle data = msg.getData();
            new SweetAlertDialog(ClassUtil.get(MainActivity.class), data.getInt("type"))
                    .setTitleText(data.getString("title"))
                    .setContentText(data.getString("msg"))
                    .setConfirmText("我知道了")
                    .show();
        }else if(msg.what==CountDownMsgTypeEnum.ALERT_SWEET.getValue()){
            //弹自定义提示框
            ((SweetAlertDialog)msg.obj).show();
        }else if(msg.what==CountDownMsgTypeEnum.ALERT_TOAST.getValue()){
            //弹系统提示
            Bundle data = msg.getData();
            Toast.makeText(ClassUtil.get(MainActivity.class),data.getString("msg"),data.getInt("length")).show();
        }else if(msg.what == CountDownMsgTypeEnum.CALL_BACK.getValue()){
            //回调指定类方法
            Bundle data = msg.getData();
            String methodName = data.getString("method");
            Object[] objects= (Object[]) msg.obj;
            Object service = ClassUtil.get((Class<? extends Object>) objects[0]);
            if(service==null){
                return;
            }
            try {
                assert methodName != null;
                if(objects.length>1) {
                    Method method = service.getClass().getMethod(methodName, objects[1].getClass());
                    method.invoke(service,objects[1]);
                }else{
                    Method method = service.getClass().getMethod(methodName);
                    method.invoke(service);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
