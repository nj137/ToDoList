package com.app.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemLongClickListener,
		OnItemClickListener {

	private ListView ParentList;
	private SimpleCursorAdapter adapter;
	private SQLiteHelper db;
	private RelativeLayout AddLayout;
	private boolean AddVisible;
	private EditText nameParent;
	private ImageButton addParent;
	protected boolean toEdit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("oncreate", "check");
		setContentView(R.layout.activity_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xFFF54D70));
		db = new SQLiteHelper(this);
		// Adapter settings
		Cursor cursor = db.getAll();
		String[] from = new String[] { SQLiteHelper.PARENT_ITEM };
		int[] to = new int[] { R.id.text };
		adapter = new SimpleCursorAdapter(this, R.layout.listpopulate, cursor,
				from, to, 0);
		// List View Settings
		ParentList = (ListView)findViewById(R.id.parentList);
		ParentList.setAdapter(adapter);
		ParentList.setOnItemClickListener(this);
		ParentList.setOnItemLongClickListener(this);
		ParentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	protected void onResume() {
		Log.i("onResume()", "check");
		// visibility of add layout
		if (AddVisible) {
			AddLayout.setVisibility(View.GONE);
			AddVisible = false;
		}
		super.onResume();
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
		// Add new list name
		if (id == R.id.addParent) {
			// Create a dialog to take the list name
			AddVisible = true;
			AddLayout = (RelativeLayout) findViewById(R.id.AddLayout);
			AddLayout.setVisibility(View.VISIBLE);
			nameParent = (EditText) findViewById(R.id.ListName);
			addParent = (ImageButton) findViewById(R.id.addListName);
			addParent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String parent_to_db = nameParent.getText().toString()
							.trim();
					if (parent_to_db.length() == 0) {
						nameParent.setError("Listname is empty");
						AddLayout.setVisibility(View.VISIBLE);
					} else {
						AddLayout.setVisibility(View.GONE);
						String status = db.addParent(parent_to_db);
						Cursor new_cursor = db.getAll();
						adapter.changeCursor(new_cursor);
						adapter.notifyDataSetChanged();
						showToast(status);
						nameParent.setText(null);
					}

				}
			});

		}

		return super.onOptionsItemSelected(item);
	}

	// Toast for different instants
	private void showToast(String status) {
		Toast.makeText(this, status, Toast.LENGTH_LONG).show();

	}

	// Direct to the list of sub-tasks
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String value = db.getParent((int) id);
		Intent pass_parent = new Intent(MainActivity.this, ChildActivity.class);
		pass_parent.setType("text/plain");
		pass_parent.putExtra("ParentName", value);
		pass_parent.putExtra("Item", "");
		startActivity(pass_parent);

	}

	// Parent list options
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, final long parent_id) {
		// List of parent actions
		ListView parent_options = new ListView(this);
		String[] actions = new String[] { "Edit the Listname",
				"Share the List", "Delete the list" };
		ArrayAdapter<String> dialogAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, actions);
		parent_options.setAdapter(dialogAdapter);
		// Dialog to dispaly parent options
		final Dialog build = new Dialog(this,
				android.R.style.Theme_Holo_Light_Dialog_MinWidth);
		build.setTitle("Select action");
		build.setContentView(parent_options);
		build.show();
		parent_options.setOnItemClickListener(new OnItemClickListener() {
			// Trigger parent list actions
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					// Edit the list name
					build.dismiss();
					editParent(db.getParent((int) parent_id), (int) parent_id);
					break;
				case 1:
					// Share the list
					build.dismiss();
					String List = "ToDoList " + db.getParent((int) parent_id)
							+ "\n";
					String share = db.sendList(db.getParent((int) parent_id))
							.trim();
					if (share.length() != 0) {
						List += share;
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("text/plain");
						i.putExtra(Intent.EXTRA_TEXT, List);
						startActivity(Intent.createChooser(i, "Share List"));
					} else {
						showToast("List is empty!!");
					}
					break;
				case 2:
					// Delete the list
					build.dismiss();
					db.deleteParent((int) parent_id);
					Cursor c = db.getAll();
					adapter.changeCursor(c);
					adapter.notifyDataSetChanged();
					break;
				default:
					showToast("Invalid selection");
				}

			}
		});

		return true;
	}

	// Change parent name
	public void editParent(String value, final int parent_id) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final EditText getName = new EditText(this);
		getName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		getName.setText(value);
		dialog.setTitle("Edit the list name");
		dialog.setView(getName);
		dialog.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		dialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		// Override above dialog for error checking
		final AlertDialog checkName = dialog.create();
		checkName.show();
		checkName.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String new_value = getName.getText().toString().trim();
						if (new_value.length() == 0) {
							getName.setError("Taskname cannot be empty");
						} else {
							db.updatParent(new_value, parent_id);
							Cursor c = db.getAll();
							adapter.changeCursor(c);
							adapter.notifyDataSetChanged();
							checkName.cancel();
						}

					}
				});
	}

	@Override
	public void onBackPressed() {
		// Set the visibility of add layout
		if (AddVisible) {
			AddLayout.setVisibility(View.GONE);
			AddVisible = false;
		} else
			super.onBackPressed();
	}
}