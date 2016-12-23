package com.example.luweicheng.countdowntimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static TextView tv_time;
    private CountDownTimer countDownTimer;
    private static final String TAG = "MainActivity";
    private static Activity activity;
    private MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_time.setOnClickListener(this);
        myHandler = new MyHandler(MainActivity.this);
        countDownTimer = new CountDownTimer(6000, 1000) {
            int n = 6;

            @Override
            public void onTick(long l) {
                Log.e(TAG, "onTick: " + l);
                tv_time.setClickable(false);
                tv_time.setText("(" + --n + ")");
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "onFinish: " );
                tv_time.setClickable(true);
                tv_time.setText("重新获取");
                n = 6;
            }
        };
    }

    @Override
    public void onClick(View view) {
        //方式一
        countDownTimer.start();
        //方式二
        //startCountTime();
        //方式三
        //startCount(6);
    }

    /*
     * 创建一个计时的线程，利用handler发送计时的消息
     *
     */

    private void startCountTime() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 6; i >= 0; i--) {
                    Message mes = myHandler.obtainMessage();
                    mes.arg1 = i;
                    mes.what = 0x00;//计时中的标志
                    myHandler.sendMessage(mes);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message mes = myHandler.obtainMessage();
                mes.what = 0x11;//计时完成的标志
                myHandler.sendMessage(mes);

            }
        }.start();
    }

    /**
     * 自定义一个带有MainActivity弱引用的静态Handler
     * 防止内存泄漏
     */
    private static class MyHandler extends Handler {

        WeakReference<MainActivity> wr;

        public MyHandler(MainActivity activity) {
            //实例化弱引用
            wr = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "handleMessage: " + msg.arg1 + ":" + msg.what);
            MainActivity activity = wr.get();
            if (activity != null) {//判断该activity是否已经销毁
                switch (msg.what) {

                    case 0x00:
                        tv_time.setClickable(false);
                        tv_time.setText("(" + msg.arg1 + ")");
                        break;
                    case 0x11:
                        tv_time.setClickable(true);
                        tv_time.setText("重新获取");
                        break;
                }
            }

        }
    }

    /**
     * RxJava实现倒计时
     * @param times 需要计时的总时间（默认间隔为1s）
     */

    private void startCount(final int times) {

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Object>(){
                    @Override
                    public Object call(Long aLong) {
                        return times - aLong.intValue();
                    }
                  })
                .take(times+1) //过滤掉times+1后面的数字
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onNext(Object o) {
                        Log.e(TAG, "onNext: "+o.toString() );
                        tv_time.setClickable(false);
                        tv_time.setText("(" + o.toString() + ")");
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        Log.e(TAG, "onStart: 开始计时" );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: "+e.getMessage() );
                    }

                    @Override
                    public void onCompleted() {
                        tv_time.setClickable(true);
                        tv_time.setText("重新获取");
                    }
                });

    }
}
