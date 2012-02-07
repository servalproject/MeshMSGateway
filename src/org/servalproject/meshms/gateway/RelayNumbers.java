/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval MeshMS Gateway application
 *
 * Serval MeshMS Gateway application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
package org.servalproject.meshms.gateway;

import org.servalproject.meshms.gateway.provider.GatewayItemsContract;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * activity used to manage settings, such as contacts whose messages will be relayed
 */
public class RelayNumbers extends Activity implements OnClickListener {
	
	// private class level constants
	private final String TAG = "RelayNumbers";
	
	private final int INPUT_DIALOG = 0;
	
	// private class level variables
	private String contactToDelete = null;
	ListView contactList = null;
	
	/*
	 * layout the controls on the activity
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relay_numbers);
        
        // include the sample list of numbers
        contactList = (ListView) findViewById(R.id.relay_numbers_ui_list_numbers);
        
        // get the list of items
        ContentResolver mContentResolver = getContentResolver();
        
        Cursor mCursor = mContentResolver.query(GatewayItemsContract.Contacts.CONTENT_URI, null, null, null, null);
        startManagingCursor(mCursor);
        
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, 
        		//android.R.layout.simple_list_item_1,
        		android.R.layout.simple_list_item_single_choice,
        		mCursor,
        		new String[] {GatewayItemsContract.Contacts.Table.PHONE_NUMBER},
        		new int[] { android.R.id.text1});
        
        //contactList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, getResources().getStringArray(R.array.relay_numbers_ui_list_sample)));
        contactList.setAdapter(mAdapter);
        contactList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        // capture touches on the listview
        contactList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		// When clicked, show a toast with the TextView text
        		// store the selected contact for later
        		TextView textView = (TextView) view;
        		contactToDelete = textView.getText().toString();
        	}
        });
        
        // capture the touches on the buttons
        Button mButton = (Button) findViewById(R.id.relay_numbers_ui_btn_add);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.relay_numbers_ui_btn_delete);
        mButton.setOnClickListener(this);
    }

    /*
     * respond to touches on UI elements
     * 
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View view) {
		
		// work out which item was touched
		switch(view.getId()) {
		case R.id.relay_numbers_ui_btn_add:
			// add a new contact
			showDialog(INPUT_DIALOG);
			break;
		case R.id.relay_numbers_ui_btn_delete:
			// delete an existing contact
			if(contactToDelete != null) {
				// delete the contact
				ContentResolver mContentResolver = getContentResolver();
				String mWhere = GatewayItemsContract.Contacts.Table.PHONE_NUMBER + " = ?";
				String[] mSelectionArgs = {contactToDelete};
				if(mContentResolver.delete(GatewayItemsContract.Contacts.CONTENT_URI, mWhere, mSelectionArgs) > 0) {
					Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_delete_sucess, Toast.LENGTH_SHORT).show();
					contactToDelete = null;
					contactList.clearChoices(); // clear the choices so that the next in the list after the deleted item isn't automatically selected
				} else {
					Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_delete_failed, Toast.LENGTH_SHORT).show();
				}
			} else {
				// show a toast
				Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_select_before_delete, Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		default:
			Log.w(TAG, "unknown view fired a click event");
		}
		
	}
	
	/*
	 * callback method used to construct the required dialog
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
	
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;
			
		switch(id) {
		case INPUT_DIALOG:
			// seek input from the user
			final EditText mInput = new EditText(this);
			mInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE); // show the keyboard for entering phone numbers
			mInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE); // show the done key on the keyboard
			
			mBuilder.setMessage(R.string.relay_numbers_ui_dialog_new_contact_message)
			.setTitle(R.string.relay_numbers_ui_dialog_new_contact_title)
			.setCancelable(true)
			.setView(mInput)
			.setPositiveButton(R.string.misc_dialog_save_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// add the new contact number
					Log.v(TAG, "new contact number: " + mInput.getText().toString());
					
					if(TextUtils.isDigitsOnly(mInput.getText()) == false || TextUtils.isEmpty(mInput.getText())){
						Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_invalid, Toast.LENGTH_LONG).show();
						return;
					}
					
					// save the new phone number
					ContentResolver mContentResolver = getContentResolver();
					ContentValues mContentValues = new ContentValues();
					mContentValues.put(GatewayItemsContract.Contacts.Table.PHONE_NUMBER, mInput.getText().toString());
					
					try {
						mContentResolver.insert(GatewayItemsContract.Contacts.CONTENT_URI, mContentValues);
						Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_save_success, Toast.LENGTH_SHORT).show();
					}
					catch (SQLException e){
						Toast.makeText(getApplicationContext(), R.string.relay_numbers_ui_toast_save_failed, Toast.LENGTH_LONG).show();
						Log.e(TAG, "unable to save the new content phone number", e);
					}
					
				}
			})
			.setNegativeButton(R.string.misc_dialog_cancel_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			mDialog = mBuilder.create();
			break;
		default:
			Log.w(TAG, "unknown dialog identifier found in create dialog");
			super.onCreateDialog(id);
		}
		return mDialog;
	}
}
