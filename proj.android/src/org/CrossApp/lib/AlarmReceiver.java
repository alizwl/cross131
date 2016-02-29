package org.CrossApp.lib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;


import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.os.Bundle;
import android.util.Log;
/**
 * IntentService responsible for handling GCM messages.
 */


public class AlarmReceiver extends BroadcastReceiver {

	private final static String SYSTEM_BROADCAST_ACTION="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("receiver", "receive");
    	Bundle bundle = intent.getExtras();  
    	if(bundle != null)
    	{
    		
    		String name=bundle.getString("id"); 
    		 
    		Log.d("receive", "intent : " + name);//intent.getStringExtra("id")
    		
        	//context.startActivity();
    		Intent i = null;
			try {
				i = new Intent(context,Class.forName("com.FApp.dkom.dkom"));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		//Intent i = new Intent(context, org.CrossApp.lib.Cocos2dxActivity.class);
    		
    		PendingIntent pi = PendingIntent.getActivity(context, Integer.valueOf(bundle.getString("id")), i, PendingIntent.FLAG_UPDATE_CURRENT);
        	Notification.Builder builder = new Notification.Builder(context)
        			.setContentIntent(pi)
        			.setSmallIcon(R.drawable.icon)
        			.setWhen(System.currentTimeMillis())
        			.setContentTitle(bundle.getString("title"))
        			.setContentText(bundle.getString("content"))//intent.getStringExtra("title")
        			.setAutoCancel(true)
        			.setDefaults(Notification.DEFAULT_SOUND);
        			

        			NotificationManager mn = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        			mn.notify(Integer.valueOf(bundle.getString("id")), builder.build());
    	}
    }
}