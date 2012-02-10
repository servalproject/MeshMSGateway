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
package org.servalproject.meshms.gateway.services;

import org.servalproject.meshms.gateway.provider.GatewayItemsContract;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

/**
 * class to receive notifications from inReach about incoming messages 
 */
public class IncomingInReachMessages extends BroadcastReceiver {
	
	// private class level constants
	private boolean V_LOG = true;
	private final String TAG = "IncomingInReachMessages";
	
	// private class level variables
	private Context context;

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(V_LOG) {
			Log.v(TAG, "new intent from inReach received");
		}
		
		this.context = context;
		
		if(intent.getAction().equals("com.delorme.intent.action.MESSAGE_RECEIVED") == true) {
			// a new message has been received from the satellite
			processNewMessage(intent.getLongExtra("com.delorme.intent.extra.MESSAGE_ID", -1));
		} else if(intent.getAction().equals("com.delorme.intent.action.MESSAGE_SENT") == true) {
			// a message previously provided to the system was sent to satellite
			processSentMessage(intent.getLongExtra("com.delorme.intent.extra.MESSAGE_ID", -1));
		}
	}
	
	private void processNewMessage(long messageId) {
		if(messageId == -1) {
			Log.e(TAG, "missing message id in attempt to process new message");
			return;
		}
		if(V_LOG) {
			Log.v(TAG, "processing a new message from satellite with id: " + messageId);
		}
	}
	
	private void processSentMessage(long messageId) {
		if(messageId == -1) {
			Log.e(TAG, "missing message id in attempt to process sent message");
			return;
		}
		if(V_LOG) {
			Log.v(TAG, "processing a sent message with id: " + messageId);
		}
		
		/*
		 *  get the data from the message store
		 */
		String[] mProjection = {"address_id", "message"};
		
		ContentResolver mContentResolver = context.getContentResolver();
		Cursor mCursor = null;
		
		try{
			mCursor = mContentResolver.query(Uri.parse("content://com.delorme.provider.earthmate.messages/messages/" + messageId),
					mProjection,
					null,
					null,
					null);
		} catch(SQLException e) {
			Log.e(TAG, "unable to query messages content provider", e);
			return;
		}
		
		// check to make sure something was returned
		if(mCursor.getCount() == 0) {
			Log.e(TAG, "unable to retrieve message data with id: " + messageId);
			return;
		}
		
		mCursor.moveToFirst();
		
		long mAddressId = mCursor.getLong(mCursor.getColumnIndex("address_id"));
		String mContent = mCursor.getString(mCursor.getColumnIndex("message"));
		
		// debug code
		Log.v(TAG, "address id: " + mAddressId);
		Log.v(TAG, "message content: " + mContent);
		
		// play nice and tidy up
		mCursor.close();
		
		/*
		 *  get the address
		 */
		mProjection = new String[1];
		mProjection[0] = "address";
		
		try{
			mCursor = mContentResolver.query(Uri.parse("content://com.delorme.provider.earthmate.messages/recipients/" + mAddressId),
					mProjection,
					null,
					null,
					null);
		} catch(SQLException e) {
			Log.e(TAG, "unable to query addresses content provider", e);
			return;
		}
		
		if(mCursor.getCount() == 0) {
			Log.e(TAG, "unable to retrieve address data with id: " + mAddressId);
			return;
		}
		
		mCursor.moveToFirst();
		
		String mAddress = mCursor.getString(mCursor.getColumnIndex("address"));
		
		mCursor.close();
		
		//debug code
		Log.v(TAG, "addresses: " + mAddress);
		
		// debug code to munge the address
		mAddress = "555001001";
		
		/*
		 *  see if we can find a matching message
		 */
		mProjection = new String[1];
		mProjection[0] = GatewayItemsContract.Messages.Table._ID;
		
		String mSelection = GatewayItemsContract.Messages.Table.RECIPIENT + " = ? AND "
				+ GatewayItemsContract.Messages.Table.SENT_CONTENT + " = ?";
		
		String[] mSelectionArgs = new String[2];
		mSelectionArgs[0] = mAddress;
		mSelectionArgs[1] = mContent;
		
		try {
			mCursor = mContentResolver.query(GatewayItemsContract.Messages.CONTENT_URI,
					mProjection,
					mSelection, 
					mSelectionArgs, 
					null);
		} catch(SQLException e) {
			Log.e(TAG, "unable to query gateway messages content provider", e);
			return;
		}
		
		if(mCursor.getCount() == 0) {
			Log.e(TAG, "unable to retrieve gateway message id");
			return;
		}
		
		mCursor.moveToFirst();
		
		int mGatewayMessageId = mCursor.getInt(mCursor.getColumnIndex(GatewayItemsContract.Messages.Table._ID));
		
		// play nice and tidy up
		mCursor.close();
		
		Log.v(TAG, "gateway message id: " + mGatewayMessageId);
		
		/*
		 * update the gateway message with the sent flag
		 */
		
		ContentValues mContentValues = new ContentValues();
		
		mContentValues.put(GatewayItemsContract.Messages.Table.STATUS, GatewayItemsContract.Messages.IS_SENT_FLAG);
		
		try {
			mContentResolver.update(
					Uri.withAppendedPath(GatewayItemsContract.Messages.CONTENT_URI, Integer.toString(mGatewayMessageId)),
					mContentValues,
					null,
					null);
		} catch(SQLException e) {
			Log.e(TAG, "unable to update gateway message status", e);
			return;
		}
		
		Log.i(TAG, "message with id '" + mGatewayMessageId + "' has had its sent status updated");
	}
}
