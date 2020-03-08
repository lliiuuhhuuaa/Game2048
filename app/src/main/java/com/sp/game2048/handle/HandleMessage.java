package com.sp.game2048.handle;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.sp.game2048.MainActivity;
import com.sp.game2048.R;
import com.sp.game2048.enums.CountDownMsgTypeEnum;

public class HandleMessage extends Handler {
    public HandleMessage(){
        super();
    }
    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        if(msg.what==CountDownMsgTypeEnum.UPDATE_NUMBER.getValue()){
            TextView textView = (TextView) msg.obj;
            textView.setText(msg.getData().getString("number"));
        }else if(msg.what==CountDownMsgTypeEnum.GAME_OVER.getValue()){
            final Context context = (Context) msg.obj;
            Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog_Alert);
            dialog.setContentView(R.layout.game_over);
            dialog.setCanceledOnTouchOutside(false);
            TextView viewById = dialog.findViewById(R.id.score);
            viewById.setText(String.valueOf(msg.getData().getInt("score")));
            dialog.findViewById(R.id.backHome).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(context,
                            MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent1);
                }
            });
            dialog.show();
        }else if(msg.what==CountDownMsgTypeEnum.PLAY_SOUND_COMPOUND.getValue()){
            final Context context = (Context) msg.obj;
            int sound = msg.getData().getInt("sound");
            MediaPlayer  mPlayer = MediaPlayer.create(context,sound);
            mPlayer.start();
        }else if(msg.what==CountDownMsgTypeEnum.VIBRATE.getValue()){
            final Context context = (Context) msg.obj;
            int time = msg.getData().getInt("time",500);
            Vibrator vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            vibrator.vibrate(time);
        }
    }
}
