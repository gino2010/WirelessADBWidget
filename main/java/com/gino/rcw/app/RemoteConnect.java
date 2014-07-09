package com.gino.rcw.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementation of App Widget functionality.
 */
public class RemoteConnect extends AppWidgetProvider {

    private static final String REMOTE_CONNECT_ACTION =
            "com.gino.rcw.app.REMOTE_CONNECT_ACTION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, RemoteConnect.class);
            intent.setAction(REMOTE_CONNECT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.remote_connect);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

            //get status to set widget
            if (check()) {
                views.setImageViewResource(R.id.widget_button, R.drawable.on);
            } else {
                views.setImageViewResource(R.id.widget_button, R.drawable.off);
            }
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null && intent.getAction().equals(REMOTE_CONNECT_ACTION)) {
            //close
            if (check()) {
                close();
                Toast.makeText(context, R.string.closed, Toast.LENGTH_LONG).show();
            //not connect wifi to open port
            } else if (!isConnectWIFI(context) && !check()) {
                Toast.makeText(context, R.string.no_wifi, Toast.LENGTH_LONG).show();
            //connected wifi and open port
            } else if (isConnectWIFI(context) && !check()) {
                open();
                String ipInfo = getWIFIIP(context);
                Toast.makeText(context, context.getString(R.string.opened) + ipInfo, Toast.LENGTH_LONG).show();
            }

            //app widget update
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), RemoteConnect.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);

        }
    }

    private boolean check() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process pro = runtime.exec("getprop service.adb.tcp.port");
            if (pro.waitFor() == 0) {
                BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                String msg = in.readLine();
                in.close();
                return msg.contains("5555");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean open() {
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
            return pro.waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private boolean close() {
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
            return pro.waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public boolean isConnectWIFI(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    public String getWIFIIP(Context context) {
        WifiManager wifiManger = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManger.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return " IP:" + (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }
}


