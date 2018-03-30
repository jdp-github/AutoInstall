package com.jd.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import com.jd.activity.MainActivity;
import com.jd.constant.TimeConstant;
import com.jd.util.DateUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CheckTimeService extends Service {

    /** 8 */
    private static final int WORK_HOUR = 8;

    /** 检测时间间隔 */
    private static final int CHECK_TIME_INTERVAL = 1 * 60 * 1000;
    private static final int CHECK_TIME_INTERVAL_IDLE = 60 * 60 * 1000;

    private Timer mTimer;
    private TimerTask mTask;
    private MyHandler mHandler;
    private boolean mHaveChangedNormal;
    private boolean mHaveChangedIdle;

    public CheckTimeService() {
        cancelTask();
        newTask();
        mHandler = new MyHandler(this);
    }

    private boolean isRightTime() {
        int hour = DateUtil.getHour();
        // 8点之前1小时一检测，8点之后，1分钟一检测
        if (hour == WORK_HOUR) {
            if (!mHaveChangedNormal) {
                cancelTask();
                newTask();
                mTimer.schedule(mTask, 1000, CHECK_TIME_INTERVAL);
                mHaveChangedNormal = true;
                mHaveChangedIdle = false;
            }
        } else {
            if (!mHaveChangedIdle) {
                cancelTask();
                newTask();
                mTimer.schedule(mTask, 1000, CHECK_TIME_INTERVAL_IDLE);
                mHaveChangedIdle = true;
                mHaveChangedNormal = false;
            }
        }

        boolean isRightTime = false;
        int workDay = DateUtil.getWeekDay() - 1;
        int minute = DateUtil.getMinute();
        if (TimeConstant.s_WorkDayList.indexOf(workDay) > 0 &&
                hour == WORK_HOUR && minute == TimeConstant.s_Punch_Minute) {
            isRightTime = true;
        }
//      TODO  return isRightTime;
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTimer.schedule(mTask, 1000, DateUtil.getHour() != WORK_HOUR ? CHECK_TIME_INTERVAL_IDLE : CHECK_TIME_INTERVAL);
        return Service.START_STICKY;
    }

    private void newTask() {
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                if (isRightTime()) {
                    try {
                        Thread.sleep(TimeConstant.s_Punch_Second);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    jumpToPunch();
                }
            }
        };
    }

    private void cancelTask() {
        if (mTask != null) {
            mTask.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    private void jumpToPunch() {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesseList = activityManager.getRunningAppProcesses();
        if (runningAppProcesseList != null && !runningAppProcesseList.isEmpty()) {
            String processName = runningAppProcesseList.get(0).processName;
            if (TextUtils.equals(processName, "com.jd")) {
                mHandler.sendEmptyMessage(0);
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    static class MyHandler extends Handler {
        private WeakReference<CheckTimeService> reference;

        public MyHandler(CheckTimeService service) {
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            CheckTimeService checkTimeService = reference.get();
            if (checkTimeService == null) {
                return;
            }
            switch (msg.what) {
                // 跳到京me
                case 0:
                    gotoJingMe(checkTimeService);
                    break;
                // 跳到主页面
                case 1:
                    Intent intent = new Intent(checkTimeService.getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    checkTimeService.getApplicationContext().startActivity(intent);
                    break;
            }
        }

        private void gotoJingMe(CheckTimeService checkTimeService) {
            String packName = "com.jd.oa";
            String className = "com.jd.oa.StartupActivity";

            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = new ComponentName(packName, className);
            intent.setComponent(componentName);

            checkTimeService.getApplicationContext().startActivity(intent);
        }
    }
}
