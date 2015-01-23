package com.app.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper{
	//Main database
	private static final String DATABASE_NAME="ToDoList";
	private static final int DATABASE_VERSION=4;
	
	//Database tables
	private static final String PARENT_TABLE="ParentList";
	private static final String CHILD_TABLE="ChildList";
	
	//Parent fields
	public static final String PARENT_ITEM="parentItem";
	public static final String PARENT_ID="_id";
	
	//Child fields
	public static final String CHILD_ID="_id";
	public static final String CHILD_ITEM="childItem";
	public static final String PARENT="parent";
	public static final String REMINDER="date";
	
	public SQLiteHelper(Context context) {
		super(context,DATABASE_NAME,null,DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create all the databases
		db.execSQL("CREATE TABLE "+PARENT_TABLE+"("+PARENT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+PARENT_ITEM +" TEXT NOT NULL UNIQUE)");
		db.execSQL("CREATE TABLE "+CHILD_TABLE+"("+CHILD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+CHILD_ITEM+" TEXT NOT NULL,"+REMINDER+" INTEGER,"+PARENT+" TEXT REFERENCES "+PARENT_TABLE+"("+PARENT_ITEM+") ON DELETE CASCADE ON UPDATE CASCADE)");
		Log.i("onCreate()","Database created");
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		//enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");
		super.onOpen(db);
	}
	@Override
	public void onConfigure(SQLiteDatabase db) {
		db.setForeignKeyConstraintsEnabled(true);
		super.onConfigure(db);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+PARENT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+CHILD_TABLE);
		this.onCreate(db);
		
	}
	//Parent Methods
	
	//Add parent list name to db
  public String addParent(String add){
	  SQLiteDatabase db= this.getWritableDatabase();
	  String status="";
	  ContentValues values = new ContentValues();
	  values.put(PARENT_ITEM, add);
	  try{
	  db.insertOrThrow(PARENT_TABLE, null, values);
	  status="Successfully Added!!";
	  Log.i("Adding a parent",add);
	  db.close();
	  }
	  catch(SQLException e)
	  {
		  Log.e("Adding a parent","Error");
		  status="List Name already exists!!";
	  }
	 return status;
  }
  
//Retrieve parent onItemclick()
public String getParent(int id) {
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor c=db.query(PARENT_TABLE,new String[]{PARENT_ID,PARENT_ITEM},PARENT_ID+" =? ",new String[]{String.valueOf(id)}, null, null, null);
			if(c!=null)
				c.moveToFirst();
		    String value =c.getString(1);	
			Log.i("In getItem()",value);
     return value;
}
//Update parent list name
public void updatParent(String new_value, int parent_id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(PARENT_ITEM,new_value);
		db.update(PARENT_TABLE, values,PARENT_ID+"=?",new String[]{String.valueOf(parent_id)});
		db.close();
}

//Delete parent
public void deleteParent(int id)
{
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(PARENT_TABLE,PARENT_ID+"=?",new String[]{String.valueOf(id)});
	db.close();
	
}
//Retrieve all the listnames in PARENT_TABLE
public Cursor getAll()
	{
		String retrieve="SELECT * FROM "+PARENT_TABLE;
		 SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(retrieve,null);
		if(cursor!=null)
		{
			cursor.moveToFirst();
		}
		return cursor;
		//return pointer
	}

//Send the child list of parent to share
public String sendList(String parentName)
{
	String shareList=" ";
	Cursor cursor=getAllChild(parentName);
	if(cursor!=null)
	{
		cursor.moveToFirst();
	try
	{
	 if(cursor.getString(0) != null)
	{
		 do
			{
			shareList+="\u2610 "+cursor.getString(1)+"\n";
		    Log.i("sendList",shareList);
			}while(cursor.moveToNext());
	}
	}
	catch(CursorIndexOutOfBoundsException e){
		Log.e("Cursor Out of bound", "null");
	}
	}
	return shareList;
}

//Child Methods

//Check the presence of child in Parent List
public String checkChild(String childItem,String parentName)
{
SQLiteDatabase db = this.getReadableDatabase();
Cursor c = db.query(CHILD_TABLE,new String[]{CHILD_ID,CHILD_ITEM,REMINDER,PARENT},CHILD_ITEM+" =?"+" and "+PARENT+" =?",new String[]{childItem,parentName},null,null,null);
String status=" ";
if(c!=null)
{
	c.moveToFirst();
	try{
	if(c.getString(0)!=null)
	{
	status=childItem+"exists";
}}catch(CursorIndexOutOfBoundsException e)
{
	Log.e("checkchild","null");
}
}
return status;
}


//Add ChildItem to the DB by parent reference
public String addChild(String parent,long remindtime, String childItem) {
	String result=checkChild(childItem,parent);
	//empty status if child is not present
	if(result.trim().length()==0)
	{
	 SQLiteDatabase db = this.getWritableDatabase();
	 ContentValues values=new ContentValues();
	 values.put(CHILD_ITEM,childItem);
	 values.put(REMINDER,remindtime);
	 values.put(PARENT,parent);
	 long id=db.insert(CHILD_TABLE, null, values);
	 Log.i("inadd():item",id+" "+childItem+" "+remindtime+" "+parent);
	db.close();
	//null=item can be added
	return result;
	}
	else
	//status not null=item exists
      return result;
	
}
//Retrieve child item 
public String getChild(int id)
{
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor c = db.query(CHILD_TABLE,new String[]{CHILD_ID,CHILD_ITEM,REMINDER},CHILD_ID+"=?",new String[]{String.valueOf(id)},null,null,null);
    String value="";
	if(c!=null)
    {
    	c.moveToFirst();
        value=c.getString(1);
        Log.i("getChild()",value);
    }
    return value;
}

//delete the child item
public void deleteChild(int childId) {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(CHILD_TABLE, CHILD_ID+"=?",new String[]{String.valueOf(childId)});
	db.close();
}


//Update Child
public void updateChild(String new_value,Long remind,int id) {
	SQLiteDatabase db=this.getWritableDatabase();
	ContentValues values= new ContentValues();
	values.put(CHILD_ITEM,new_value);
	values.put(REMINDER, remind);
	db.update(CHILD_TABLE, values,CHILD_ID+"=?",new String[]{String.valueOf(id)});
	db.close();	
	
}

//Get the parent list of child items
public Cursor getAllChild(String parentName) {
	String retrieve="SELECT * FROM "+CHILD_TABLE+" WHERE "+PARENT+" = '"+parentName+"'";
	 SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(retrieve,null);
	if(cursor!= null)
	    cursor.moveToFirst();
	return cursor;
}

//send the child reminder date
public String getChildReminder(int id) {
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor c = db.query(CHILD_TABLE,new String[]{CHILD_ID,CHILD_ITEM,REMINDER},CHILD_ID+"=?",new String[]{String.valueOf(id)},null,null,null);
    String date1 = null;
	if(c!=null)
    {
    	c.moveToFirst();
        date1=c.getString(2);
        Log.i("getChild()","Date:"+date1);
    }
	return date1;
}

//send the child id to set alarm and notification
public int getCurrentId()
{
	SQLiteDatabase db = this.getReadableDatabase();
	String id ="SELECT * FROM "+CHILD_TABLE;
	Cursor c = db.rawQuery(id, null);
	if(c!=null)
	{
		c.moveToLast();
	}
	Log.i("getCurrentId()","ID:"+c.getString(0));
	return Integer.valueOf(c.getString(0));
}
}
