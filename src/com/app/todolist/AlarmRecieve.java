package com.app.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class AlarmRecieve extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		//on recieving the intent from alarm manager
		Log.i("onRecieve","notification");
		String parent=intent.getExtras().getString("Parentname");
		String child=intent.getExtras().getString("ChildItem");
		int ID = (int) intent.getExtras().getLong("ID");
		//notification intent-to child containing list
		Intent open=new Intent(context,ChildActivity.class);
		open.putExtra("ParentName",parent);
		open.putExtra("Item",child);
		PendingIntent pend = PendingIntent.getActivity(context,ID,open,0);
		//vibrate for two seconds 
		Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrate.vibrate(2000);
		//notification build
		Notification notification= new Notification.Builder(context)
		.setContentTitle("MyList")
		.setContentText("ToDoList:"+parent+"\n Task:"+child)
		.setContentIntent(pend)
		.setSmallIcon(R.drawable.ic_launcher)
		.build();
		NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
		notification.flags |=Notification.FLAG_AUTO_CANCEL;
		manager.notify(ID,notification);
		
	}

}
