package com.cvte.www.cvte;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.total)
    TextView mTotal;
    @BindView(R.id.valid)
    TextView mValid;
    @BindView(R.id.cpu)
    TextView mCpu;

    public static final int REFRESH = 0x000001;

    long total = 0;
    long valid = 0;
    double cpu = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        new MyThread().start();
    }



    public void CPULoad( )
    {
        readUsage( );
    }

    public void readUsage( )
    {
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/stat" ) ), 1000 );
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);

            this.cpu =(currTotal - total) * 100.0f / (currTotal - total + currIdle - valid);
            this.total = currTotal;
            this.valid = currIdle;
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
        }

    }

    public Handler  mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MainActivity.REFRESH) {
                if(mTotal!=null){
                    mTotal.setText("总内存："+this.getTotalMemory());
                }
                if(mValid!=null){
                    mValid.setText("可使用内存："+this.getAvailMemory());
                }
                if(mCpu!=null){
                    mCpu.setText("CPU："+Math.round(this.getUsage())+"%");
                }

            }
        }

        public double getUsage( )
        {
            readUsage( );
            return cpu;
        }

        public String getAvailMemory() {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

            am.getMemoryInfo(mi);

            //mi.availMem; 当前系统的可用内存

            return Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化

        }

        public String getTotalMemory() {
            String str1 = "/proc/meminfo";// 系统内存信息文件
            String str2;

            int i = 0;

            String[] arrayOfString;

            long initial_memory = 0;

            try {
                FileReader localFileReader = new FileReader(str1);
                BufferedReader localBufferedReader = new BufferedReader(
                        localFileReader, 8192);
                str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
                arrayOfString = str2.split("\\s+");

                Log.e("TAG", arrayOfString[1]);// 2879016 KB
                i = Integer.parseInt(arrayOfString[1])/1024;

                Log.e("TAG", "转换后 的 int "+i);

                for (String num : arrayOfString) {
                    Log.i(str2, num + "\t");
                }
                //initial_memory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB，乘以1024转换为Byte
                localBufferedReader.close();

            } catch (IOException e) {

            }
            return i+" MB" ;// Byte转换为KB或者MB，内存大小规格化

        }
    };



    public class MyThread extends Thread {
        public void run() {
            while (true) {

                Message msg = new Message();
                msg.what = REFRESH;
                msg.obj = this;
                mHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
