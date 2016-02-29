package org.CrossApp.lib;


import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;  
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;  
import org.apache.http.client.methods.HttpGet;  
import org.apache.http.client.methods.HttpPost;  
import org.apache.http.impl.client.DefaultHttpClient;  
import org.apache.http.message.BasicNameValuePair;  
import org.apache.http.protocol.HTTP;  
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject; 

public class PushService extends Service {
	private final static String TAG="PushService";
	
	private TimerTask task = null;
	private Timer timer = null;
	
	
	String url = "http://139.196.195.185:8080/sap";  
    HttpResponse httpResponse = null;
    
    private SQLiteDatabase database = null;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		checkServiceStatus();
		Log.i(TAG, "data : " + getFilesDir().getAbsolutePath() + "/common.db");
		database = openOrCreateDatabase(getFilesDir().getAbsolutePath() + "/common.db", Context.MODE_PRIVATE, null); 
		Log.i(TAG, "data" + database);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		doTask();
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void doTask() {
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				try{
						Log.i(TAG, "doTask");
						doTaskGetMessage();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		};
		timer.schedule(task, 6, 6*1000);
	}

	
	private void doTaskGetMessage(){
		HttpPost httpPost = new HttpPost(url);  
        List<NameValuePair> params = new ArrayList<NameValuePair>();  
        params.add(new BasicNameValuePair("tag", "M0"));
        params.add(new BasicNameValuePair("uid", "2"));  
        try {
        	//Log.i(TAG, "setEntity");
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
          
        try {
        	//Log.i(TAG, "httpResponse");
			httpResponse = new DefaultHttpClient().execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)  
        {  
        	String result = null;
            try {
            	//HttpEntity entity = httpResponse.getEntity();
				result = EntityUtils.toString(httpResponse.getEntity());
				//Log.i(TAG, "result : " + result);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
            JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(result);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            try {
            	JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("mg");
            	for(int i = 0; i<jsonArray.length(); i++)
                {
            		JSONObject jsonObject2 = (JSONObject)jsonArray.opt(i);
            		String title = jsonObject2.getString("MessageTitle");
            		String detail = jsonObject2.getString("MessageDetail");
            		String id = jsonObject2.getString("MessageTime");
            		int type = jsonObject2.getInt("MessageType");
            		
            		database.execSQL("CREATE TABLE IF NOT EXISTS notice(id int auto_increment, sid int, title VARCHAR(256), detail VARCHAR(256), readed int, start_time int64, end_time int64, type int);");
            		Cursor c = database.rawQuery("SELECT * FROM notice WHERE sid == ?", new String[]{id});
            		
            		//Log.i(TAG, title + c.getCount());
            		if(c.getCount() <= 0)
            		{
            			//Log.d(TAG, "insert--------" + title + detail + id + id);
            			//"INSERT INTO notice (sid, title, readed, start_time, end_time, type) VALUES (?,?,?,?,?,?);";
            			//sid, title, readed, start_time, end_time, type
            			database.execSQL("INSERT INTO notice (sid, title, detail, readed, start_time, end_time, type) VALUES (?,?,?,?,?,?,?)", new Object[]{id,title,detail,0,id,0,type}); 
            			AndroidNativeTool.sendRemoteNotification(this, title, detail, id, Integer.valueOf(id));
            		}
            		
                }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
	}
	
	public static boolean isServiceRunning(Context ctx,String filePath) {
	    ActivityManager manager = (ActivityManager) ctx.getSystemService(ctx.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (filePath.equalsIgnoreCase(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void checkServiceStatus() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					String filePath = "org.CrossApp.lib.PushService";
					boolean flag = isServiceRunning(PushService.this,filePath);
					if (flag == false) {
						PushService.this.startService(new Intent(PushService.this,PushService.class));
					}
				} catch (Exception e) {

				}
			}
		}.start();
		
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
