package com.sp.game2048;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sp.game2048.enums.CountDownMsgTypeEnum;
import com.sp.game2048.enums.CountDownStateEnum;
import com.sp.game2048.handle.HandleMessage;
import com.sp.game2048.util.AlertUtil;
import com.sp.game2048.util.ClassUtil;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class GameActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "game";
    private HandleMessage handleMessage = new HandleMessage();
    int[] numbs = {2,2,2,2,4,4,8};
    private Button moveButton = null;
    LinearLayout layoutBox = null;
    TextView scoreNum = null;
    Button button = null;
    private int topHeight;
    //分数
    private int total = 0;
    private boolean start = false;
    HandlerThread countDownThread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("status_bar_height","dimen","android");
        topHeight = resources.getDimensionPixelSize(resourceId);
        //初始化视图
        Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
        message.obj = new Object[]{GameActivity.class};
        message.getData().putString("method", "initView");
        handleMessage.sendMessage(message);
        //开启一个线程倒计时
        message = Message.obtain(handleMessage, CountDownMsgTypeEnum.START_321.getValue());
        message.obj = new Object[]{GameActivity.this, (Runnable) () -> countDown(CountDownStateEnum.START.getState()),handleMessage};
        message.getData().putInt("num",3);
        handleMessage.sendMessage(message);
        //初始化
        init();

    }
    private void init() {
        ClassUtil.push(this);
    }
    /***
     * 初始化视图
     */
    public void initView() {
        scoreNum = findViewById(R.id.score_num);
        layoutBox = findViewById(R.id.layout_box);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0.2f);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,0.2f);
        LinearLayout linearLayout = null;
        String text = null;
        for(int i=0;i<7;i++){
            linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(layoutParams);
            layoutBox.addView(linearLayout);
            for(int j=0;j<5;j++){
                int id = this.getResources().getIdentifier(String.format("button_%d_%d", i, j), "id", this.getPackageName());
                button = findViewById(id);
                if(button==null){
                    button = new Button(this);
                    button.setTextSize(30);
                    button.setOnTouchListener(this);
                }
                button.setLayoutParams(buttonParams);
                int val = numbs[Double.valueOf(Math.random()*numbs.length).intValue()];
                button.setTag(new int[]{j,i,val});
                text = String.valueOf(val);
                button.setText(text);
                button.setTextColor(this.getColor(R.color.colorText));
                button.setId(id);
                button.setBackgroundResource(R.drawable.button_border);
                button.getBackground().setColorFilter(ContextCompat.getColor(this,this.getResources().getIdentifier(String.format("grid_color_%s", text), "color", this.getPackageName())),PorterDuff.Mode.ADD);
                linearLayout.addView(button);
            }
        }
        moveButton = new Button(this);
        moveButton.setTextSize(30);
        moveButton.setVisibility(View.GONE);
        RelativeLayout viewById = findViewById(R.id.layout_relative_box);
        viewById.addView(moveButton);
    }
    private float[] position = {0f,0f,0f};

    /***
     * 绑定事件
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!start){
            return false;
        }
        Button buttonView = (Button) v;
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            moveButton.setText(buttonView.getText());
            moveButton.getLayoutParams().height = v.getHeight();
            moveButton.getLayoutParams().width = v.getWidth();
            moveButton.setX(event.getRawX()-event.getX());
            moveButton.setY(event.getRawY()-event.getY()-topHeight);
            moveButton.setVisibility(View.VISIBLE);
            moveButton.setBackground(buttonView.getBackground());
            position[0] = event.getRawX();
            position[1] = event.getRawY();
            position[2] = 0;
            v.setAlpha(0.5f);
        }else if(event.getAction()==MotionEvent.ACTION_UP){
            if(!start){
                return true;
            }
            moveButton.setVisibility(View.GONE);
            v.setAlpha(1f);
            float x = event.getRawX() - position[0];
            float y = event.getRawY() - position[1];
            //移动
            if(Math.abs(position[2])==1) {
                if(Math.abs(x)>=moveButton.getWidth()-50){
                    calc(buttonView,(int)position[2]);
                }
            }else {
                if(Math.abs(y)>=moveButton.getHeight()-50){
                    calc(buttonView,(int)position[2]);
                }
            }
        }else if(event.getAction()==MotionEvent.ACTION_MOVE){
            float x = event.getRawX() - position[0];
            float y = event.getRawY() - position[1];
            //移动
            if(Math.abs(x)>Math.abs(y)) {
                if(Math.abs(x)>moveButton.getWidth()){
                    x = moveButton.getWidth()*(x>0?1:-1);
                }
                if (event.getRawX() < position[0]) {
                    position[2] = -1;
                } else if (event.getRawX() > position[0]) {
                    position[2] = 1;
                }
            }else {
                if(Math.abs(y)>moveButton.getHeight()){
                    y = moveButton.getHeight()*(y>0?1:-1);
                }
                if (event.getRawY() > position[1]) {
                    position[2] = 2;
                } else if (event.getRawY() < position[1]) {
                    position[2] = -2;
                }
            }
            moveButton.setX(event.getRawX()-event.getX()+x);
            moveButton.setY(event.getRawY()-event.getY()-topHeight+y);
        }
        return false;
    }

    /***
     * 结果计算
     * @param button
     * @param direction
     */
    private void calc(Button button,int direction){
        int[] tag = (int[]) button.getTag();
        int x = tag[0];
        int y = tag[1];
        if(Math.abs(direction)==1){
            x+=direction;
        }else{
            y+=direction>0?1:-1;
        }
        int id = this.getResources().getIdentifier(String.format("button_%d_%d", y, x), "id", this.getPackageName());
        if(id==0){
            return;
        }
        Button buttonWithTag = layoutBox.findViewById(id);
        int[] source = (int[]) buttonWithTag.getTag();
        if(source[2]!=tag[2]){
            Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.VIBRATE.getValue());
            message.obj = GameActivity.this;
            message.getData().putInt("time",250);
            handleMessage.sendMessage(message);
            return;
        }
        Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.PLAY_SOUND_COMPOUND.getValue());
        message.obj = GameActivity.this;
        message.getData().putInt("sound",R.raw.compound);
        handleMessage.sendMessage(message);
        int sum = source[2]+tag[2];
        String text = String.valueOf(sum);
        buttonWithTag.setText(text);
        buttonWithTag.getBackground().setColorFilter(Color.BLACK,PorterDuff.Mode.CLEAR);
        buttonWithTag.getBackground().setColorFilter(ContextCompat.getColor(this,this.getResources().getIdentifier(String.format("grid_color_%s", text), "color", this.getPackageName())),PorterDuff.Mode.ADD);
        source[2] = sum;
        total+=sum;
        scoreNum.setText(String.valueOf(total));
        int val = numbs[Double.valueOf(Math.random()*numbs.length).intValue()];
        tag[2] = val;
        text = String.valueOf(val);
        button.setText(text);
        button.getBackground().setColorFilter(Color.BLACK,PorterDuff.Mode.CLEAR);
        button.getBackground().setColorFilter(ContextCompat.getColor(this,this.getResources().getIdentifier(String.format("grid_color_%s", text), "color", this.getPackageName())),PorterDuff.Mode.ADD);
    }

    /***
     * 倒计时处理
     * @param state
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void countDown(final Integer state) {
        final AtomicInteger atomicInteger = new AtomicInteger(state);
        final ProgressBar progressBar = findViewById(R.id.countDownProgressBar);
        final TextView textView = findViewById(R.id.countDownNumber);
        textView.setText(String.valueOf(progressBar.getProgress()));
        startTime(atomicInteger, progressBar, textView);
        findViewById(R.id.pause).setOnClickListener(v -> {
            if(CountDownStateEnum.PAUSE.getState().equals(atomicInteger.get())) {
                ((Button)v).setText("暂停");
                atomicInteger.set(CountDownStateEnum.START.getState());
                startTime(atomicInteger,progressBar,textView);
                start = true;
            }else{
                ((Button)v).setText("继续");
                atomicInteger.set(CountDownStateEnum.PAUSE.getState());
                start = false;
            }
        });
    }

    /***
     * 启动倒计时
     * @param atomicInteger
     * @param progressBar
     * @param textView
     */
    private void startTime(final AtomicInteger atomicInteger, final ProgressBar progressBar, final TextView textView) { ;
        countDownThread = new HandlerThread("CountDownThread");
        countDownThread.setPriority(Thread.MIN_PRIORITY);
        countDownThread.start();
        final Handler handler = new Handler(countDownThread.getLooper());
        final Runnable countDownWork = new Runnable() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void run() {
                if(CountDownStateEnum.PAUSE.getState().equals(atomicInteger.get())) {
                    countDownThread.quitSafely();
                    return;
                }
                int progress = progressBar.getProgress();
                if(start=false&&progress==progressBar.getMax()){
                    progress++;
                }
                start = true;
                progress = progress-1;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    progressBar.setProgress(progress,true);
                }else{
                    progressBar.setProgress(progress);
                }

                Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.UPDATE_NUMBER.getValue());
                message.getData().putString("number",String.valueOf(progress));
                message.obj = textView;
                handleMessage.sendMessage(message);
                handler.postDelayed(this,1000);
                if(progress<=0){
                    start = false;
                    message = Message.obtain(handleMessage, CountDownMsgTypeEnum.GAME_OVER.getValue());
                    message.getData().putInt("score",total);
                    message.obj = GameActivity.this;
                    handleMessage.sendMessage(message);
                    message = Message.obtain(handleMessage, CountDownMsgTypeEnum.PLAY_SOUND_COMPOUND.getValue());
                    message.obj = GameActivity.this;
                    message.getData().putInt("sound",R.raw.game_over);
                    handleMessage.sendMessage(message);
                    countDownThread.quitSafely();
                    return;
                }

            }
        };
        handler.post(countDownWork);
    }
    private long firstClickBack = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
//            long secondClickBack = System.currentTimeMillis();
//            if(secondClickBack - firstClickBack >1500){
//                Toast.makeText(this, "再一次确认并退出", Toast.LENGTH_SHORT).show();
//                firstClickBack = secondClickBack;
//                return true;
//            }else{
//                if(countDownThread!=null&&countDownThread.isAlive()) {
//                    start = false;
//                    countDownThread.quitSafely();
//                }
//                return super.onKeyDown(keyCode, event);
//            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * @do 返水主页
     * @author liuhua
     * @date 2020/3/16 8:43 PM
     */
    public void backHome(View view) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        sweetAlertDialog.setContentText("确定返回主页吗?");
        sweetAlertDialog.setCancelText("取消").setConfirmText("确定返回").setConfirmClickListener(sweetAlertDialog1 -> {
            countDownThread.quitSafely();
            sweetAlertDialog.cancel();
            finish();
        });
        AlertUtil.alertOther(sweetAlertDialog);
    }

    @Override
    public void recreate() {
        super.recreate();
        final ProgressBar progressBar = findViewById(R.id.countDownProgressBar);
        progressBar.setProgress(61);
        final TextView textView = findViewById(R.id.countDownNumber);
        textView.setText("60");
    }
}

