package com.tromin.demo_0819;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity
{
    private Context mContext=null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        mContext = this ;
    }
    // 定義一個顯示 一按鍵對話盒 的方法，在Unity中調用此方法
    public void ShowDialog(final String _title, final String _content , final String _buttonName)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(_title).setMessage(_content).setPositiveButton(_buttonName, null);
                builder.show();
            }
        });
    }
    // 定義一個顯示Toast的方法，在Unity中調用此方法
    public void ShowToast(final String StringToast)
    {
        // 同樣需要在UI線程下執行
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(),StringToast, Toast.LENGTH_LONG).show();
            }
        });
    }
    //  定義一個手機振動的方法，在Unity中調用此方法
    public void SetVibrator()
    {
        Vibrator mVibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        //mVibrator.vibrate(new long[]{200, 2000, 2000, 200, 200, 200}, -1); //-1：表示不重復 0：循環的震動
        mVibrator.vibrate(2000);
    }
    //呼叫Android的Quit
    public void AndroidQuit()
    {
        finish() ;
    }
    // 定義一個打開Activity的方法，Unity中會調用這個方法，用於區分打開攝像機 開始本地相冊
    public void TakePhoto(String stringType)
    {
        Intent intent = new Intent(mContext,WebViewActivity.class);
        intent.putExtra("type", stringType);
        this.startActivity(intent);
    }
    //  定義一個呼叫Unity的方法
    // 第一個參數是unity中的對象名字，記住是對象名字，不是腳本類名
    // 第二個參數是函數名
    // 第三個參數是傳給函數的參數，目前只看到一個參數，並且是string的，自己傳進去轉吧
    public void AndroidCallUnity(String _objectName , String _functionName, String _content)
    {
        Log.i("Tromin.TAG","onActivityResult=UnitySendMessage="+_objectName+","+_functionName+","+_content);
        UnityPlayer.UnitySendMessage(_objectName, _functionName, _content);
    }
}