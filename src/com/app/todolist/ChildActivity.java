package com.app.todolist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ChildActivity extends Activity implements OnItemLongClickListener, OnClickListener, OnItemClickListener{

	private SQLiteHelper db;
	private String ParentName;
	protected String setDate;
	private SimpleCursorAdapter Childadapter;
	private ActionMode mActionmode;
	//Child dialog fields
	private EditText childName;
	private EditText datefill;
	private EditText timefill;
	private Calendar calendar;
	protected long remindtime;
	protected SimpleDateFormat sdf;
	//on notification recieve
	private TextView userNote;
	private String review;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_child);
		//Set parent name as title
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xFFF54D70));
		//check notification
		userNote = (TextView) findViewById(R.id.reviewTask);
		Intent intent= getIntent();
		//review-diff between normal intent and notification intent
		review = intent.getExtras().getString("Item");
		ParentName=intent.getExtras().getString("ParentName");
		if(review.trim().length()!=0)
		{
			userNote.setText("Review the task:"+review);
			userNote.setVisibility(View.VISIBLE);
		}
		//title of sub-tasks
		setTitle(ParentName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		db=new SQLiteHelper(this);
		//Child Listview
		ListView ChildList = (ListView)findViewById(R.id.childList);
		Cursor cursor=db.getAllChild(ParentName);
		String[] from = new String[]{SQLiteHelper.CHILD_ITEM};
		int[] to = new int[]{R.id.text};
		Childadapter= new SimpleCursorAdapter(this,R.layout.listpopulate, cursor, from, to,0);
		ChildList.setAdapter(Childadapter);
		ChildList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		ChildList.setOnItemLongClickListener(this);
		ChildList.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.child, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id)
		{
		case android.R.id.home:
			onBackPressed();
			return true;
		case  R.id.action_settings:
			return true;
		case R.id.addChild:
			//Add the child item to parent list
			showDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
		
	}
    //set alarm and send notification on setting time
	protected void notifyUser(int id, String parentName2, String childItem,
			long remindtime2) {
		Intent note=new Intent(this,AlarmRecieve.class);
		note.putExtra("Parentname",parentName2);
		note.putExtra("ChildItem",childItem);
		note.putExtra("ID",id);
		PendingIntent notify=PendingIntent.getBroadcast(getBaseContext(),id,note,0);
		AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		manager.set(AlarmManager.RTC_WAKEUP,remindtime2,notify);
		
	}
    //Display toast messages for different instances
	private void showToast(String msg) {
		Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view,
			int position, final long id) {
		userNote.setVisibility(View.GONE);
		//Contextual Action Bar
		mActionmode = startActionMode(new ActionMode.Callback(){
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.contextual, menu);
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch(item.getItemId())
				{
				case R.id.edit:
			    //Edit the task
				String itemname=db.getChild((int) id);
				String reminder=db.getChildReminder((int)id);	
				 onEditDialog(itemname,Long.valueOf(reminder),(int)id);
			
					break;
				case R.id.delete:
					//Delete the task
					db.deleteChild((int)id);
					Cursor c = db.getAllChild(ParentName);
					Childadapter.changeCursor(c);
					Childadapter.notifyDataSetChanged();
					deletenotification((int) id);
					break;
				}
				mode.finish();
				
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
			}});
		return true;
	}

	
    //On click of calendar and alarm buttons
	@Override
	public void onClick(View v) {
		//set day and time
		calendar = Calendar.getInstance();
		final int current_year = calendar.get(Calendar.YEAR);
	    final int current_month = calendar.get(Calendar.MONTH);
	    final int current_day = calendar.get(Calendar.DAY_OF_MONTH);
	    int current_hour=calendar.get(Calendar.HOUR_OF_DAY);
	    int current_minute=calendar.get(Calendar.MINUTE);
		
		switch(v.getId())
		{
		case R.id.openCalendar:
				//Date picker
			    DatePickerDialog dpd = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
					calendar.set(year,( monthOfYear), dayOfMonth);
					Long time=calendar.getTimeInMillis();
					sdf= new SimpleDateFormat("dd/MM/yyyy");
					setDate=sdf.format(calendar.getTime());
					datefill.setText(setDate);
						
					}
				}, current_year, current_month,current_day);
			    dpd.show();
			break;
		case R.id.openTimePicker:
			//Set time of task
			TimePickerDialog tpd = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
					calendar.set(Calendar.MINUTE,minute);
					SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
					setDate=sdf.format(calendar.getTime());
					timefill.setText(setDate);
				}
			},current_hour,current_minute,true);
			tpd.show();
			break;
		}
		
	}
	//Show dialog on add action
	public void showDialog()
	{
		 AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			//Custom view of dialog
			View view = getLayoutInflater().inflate(R.layout.childdialog, null);
			//fields of dialog
			childName=(EditText)view.findViewById(R.id.childName);
			datefill=(EditText)view.findViewById(R.id.reminderDate);
			timefill=(EditText)view.findViewById(R.id.time);
			ImageButton date = (ImageButton)view.findViewById(R.id.openCalendar);
			date.setOnClickListener(this);
			ImageButton time = (ImageButton)view.findViewById(R.id.openTimePicker);
			time.setOnClickListener(this);
			dialog.setTitle("Enter the task ");
			dialog.setView(view);
			dialog.setPositiveButton("Submit",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					}
					
				});
			dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}});
			//Override old dialog for error checking
			final AlertDialog checkTask=dialog.create();
			checkTask.show();
			checkTask.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				
				private boolean validated=false;
				private boolean notify;

				@Override
				public void onClick(View v) {
					//in check with field data
					String childItem=childName.getText().toString().trim();
					if(childItem.length()==0)
					{
						
						childName.setError("Item name can't be empty");
						
					}
					else
					{
						//add the item
						if((datefill.getText().toString().trim().length()==0)&&(timefill.getText().toString().trim().length()==0))
						{
						   remindtime=0;
						   validated = true;
						}
						else
						{
							if(timefill.getText().toString().trim().length()==0)
							{
								calendar.set(Calendar.HOUR,0);
								calendar.set(Calendar.MINUTE,0);
							}
							remindtime=calendar.getTimeInMillis();
							if(System.currentTimeMillis()>remindtime)
							{
								if(timefill.getText().toString().trim().length()==0)
									datefill.setError("Elapsed time");
								else
									timefill.setError("Elapsed time");
							}
							else
							{
								showToast("alarm set");
								validated=true;
								notify=true;
								
							}
							
						}
						//all checked
						if(validated)
						{
						String status=db.addChild(ParentName,remindtime,childItem);
						if(status!=" ")
						{
							showToast("Item already exists");
						}
						Cursor c = db.getAllChild(ParentName);
						Childadapter.changeCursor(c);
						Childadapter.notifyDataSetChanged();
						checkTask.cancel();
						}
						if(notify==true)
						{
							int id=db.getCurrentId();
							notifyUser(id,ParentName,childItem,remindtime);
						}
					
				}
			}});
		
	}
	//Dialog for edit task
	void onEditDialog(String item, final long reminder,final int itemId)
	{
			//Custom view of dialog
			View view = getLayoutInflater().inflate(R.layout.childdialog, null);
			childName=(EditText)view.findViewById(R.id.childName);
			datefill=(EditText)view.findViewById(R.id.reminderDate);
			timefill=(EditText)view.findViewById(R.id.time);
			ImageButton date = (ImageButton)view.findViewById(R.id.openCalendar);
			ImageButton time = (ImageButton)view.findViewById(R.id.openTimePicker);
			//retrieve date
			Date Rdate = new Date(reminder);
			Calendar cal=Calendar.getInstance();
			cal.setTime(Rdate);
			int d=cal.get(Calendar.DAY_OF_MONTH);
			int m=cal.get(Calendar.MONTH);
			int y=cal.get(Calendar.YEAR);
			int h=cal.get(Calendar.HOUR_OF_DAY);
			int min=cal.get(Calendar.MINUTE);
			//set text
			childName.setText(item);
			if(reminder!=0)
			{
			datefill.setText(d+"/"+m+1+"/"+y);
			timefill.setText(h+":"+min);
			}
			//select date
			date.setOnClickListener(this);
			//select time
			time.setOnClickListener(this);
			//Dialog creation
			 AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Edit Task ");
			dialog.setView(view);
			dialog.setPositiveButton("Submit",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					}
					
				});
			dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}});
			//Overriding positive button to show errors
			final AlertDialog checkTask=dialog.create();
			checkTask.show();
			checkTask.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				
				private boolean validated=false;

				@Override
				public void onClick(View v) {
					String childItem=childName.getText().toString().trim();
					if(childItem.length()==0)
					{
						
						childName.setError("Can't be empty");
						
					}
					else
					{
						//add the item
						if((datefill.getText().toString().trim().length()==0)&&(timefill.getText().toString().trim().length()==0))
						{
						   remindtime=0;
						   validated = true;
						}
						else
						{
							if(timefill.getText().toString().trim().length()==0)
							{
								calendar.set(Calendar.HOUR,0);
								calendar.set(Calendar.MINUTE,0);
							}
							remindtime=calendar.getTimeInMillis();
							if(System.currentTimeMillis()>remindtime)
							{
								if(timefill.getText().toString().trim().length()==0)
									datefill.setError("Elapsed time");
								else
									timefill.setError("Elapsed time");
							}
							else
							{
								showToast("alarm set");
								validated=true;
								notifyUser(itemId,ParentName,childItem,remindtime);
							    
							}
						}
						if(validated)
						{
				
						db.updateChild(childItem,remindtime,itemId);
						Cursor c = db.getAllChild(ParentName);
						Childadapter.changeCursor(c);
						Childadapter.notifyDataSetChanged();
						checkTask.cancel();
						}
					
				}
			}});
	}
    //Cancel notification on sub-task's deletion 
	protected void deletenotification(int id) {
		Intent note=new Intent(this,AlarmRecieve.class);
		PendingIntent notify=PendingIntent.getBroadcast(getBaseContext(),id,note,0);
		AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		manager.cancel(notify);
	}
    //Hide review text
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		userNote.setVisibility(View.GONE);
		
	}
	
	
}

