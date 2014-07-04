package com.gino.rcw.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.DataOutputStream;
import java.io.IOException;

//only for debug
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
//            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        }catch (Exception e){
//            Log.e("mhy", e.getMessage());
//        }
//        isConnectWIFI(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean start(){
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        try {
            pro = runtime.exec("su");
            DataOutputStream dos = new DataOutputStream(pro.getOutputStream());
            dos.writeBytes("setprop service.adb.tcp.port 5555\n");
            dos.writeBytes("stop adbd\n");
            dos.writeBytes("start adbd\n");
            dos.flush();
            dos.close();
            if(pro.waitFor() == 0){
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean stop(){
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        try {
            pro = runtime.exec("su");
            DataOutputStream dos = new DataOutputStream(pro.getOutputStream());
            dos.writeBytes("setprop service.adb.tcp.port -1\n");
            dos.writeBytes("stop adbd\n");
            dos.writeBytes("start adbd\n");
            dos.flush();
            dos.close();
            if(pro.waitFor() == 0){
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean isConnectWIFI(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null) {
            return info.isConnected();
        }
        return false;
    }
}
