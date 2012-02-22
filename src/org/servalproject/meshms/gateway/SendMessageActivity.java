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

import org.servalproject.meshms.SimpleMeshMS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * an activity to send messages via the Delorme inReach API
 */
public class SendMessageActivity extends Activity {

	// private class level constants
	private final String TAG = "SendMessageActivity";

	private final int ACTIVITY_RESULT_CODE = 0;
	
	final boolean ALLOW_EDIT = true;
	
	// declare delorme constants
	private final int DELORME_MESSAGE_SENT = 0;

	/*
	 * layout the controls on the activity
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_message);

		//get the message data to send
		Bundle mBundle = this.getIntent().getExtras();
		
		if(mBundle == null) {
			Log.e(TAG, "activity started without a bundle");
			finish();
		}
		
		SimpleMeshMS mMessage = (SimpleMeshMS) mBundle.getParcelable("message");
		int mMessageId = mBundle.getInt("id", -1);

		// check to see that the message was sent with the intent
		if(mMessage == null) {
			Log.e(TAG, "activity started without a message");
			finish();
		}

		if(mMessageId == -1) {
			Log.e(TAG, "activity started without a message id");
			finish();
		}

		// process the message

		// prepare the inReach Intent
		Intent mOutgoingIntent = new Intent("com.delorme.intent.action.SEND_MESSAGE"); // action
		mOutgoingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// override the recipient
		mOutgoingIntent.putExtra("com.delorme.intent.extra.MESSAGE_RECIPIENTS", mMessage.getRecipient()); // add recipient address
		mOutgoingIntent.putExtra("com.delorme.intent.extra.MESSAGE_TEXT", mMessage.getContent()); // message content
		mOutgoingIntent.putExtra("com.delorme.intent.extra.MESSAGE_EDIT_ONERROR", ALLOW_EDIT); // allow user to edit if delorme app detects and error

		startActivityForResult(mOutgoingIntent, ACTIVITY_RESULT_CODE);  
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// check to see if this is the same result code as specified in the original call
		if(requestCode != ACTIVITY_RESULT_CODE) {
			//nothing to do
			finish();
		}

		// get the delorme intent message id
		// TODO report bug in Delorme earthmate missing intent. 
		long mDelormeId = -1;
		if(data != null) {
			mDelormeId = data.getLongExtra("com.delorme.intent.extra.MESSAGE_ID", -1);
		}
		
		//debug code
//		if(data != null) {
//			if(data.getExtras() != null) {
//				if(data.getExtras().keySet() == null) {
//					String[] keys = data.getExtras().keySet().toArray(new String[0]);
//					
//					for(String key : keys) {
//						Log.v(TAG, "bundle key: " + key);
//					}
//				} else {
//					Log.v(TAG, "no extras keys found attached to the intent");
//				}
//			} else {
//				Log.v(TAG, "no extras found attached to the intent");
//			}
//		} else {
//			Log.v(TAG, "no intent found in method call");
//		}

		// determine what action to take
		switch(resultCode) {
		case DELORME_MESSAGE_SENT:
			// TODO?
			Log.i(TAG, "the relay of a MeshMS message via in reach succeeded with id '" + mDelormeId + "'");
		default:
			// message was not sent
			Log.w(TAG, "the relay of the MeshMS via inReach has resulted in code'" + resultCode + "'");
		}
		
		finish();
	}
}
