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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.servalproject.meshms.SimpleMeshMS;
import org.servalproject.meshms.gateway.R;
import org.servalproject.meshms.gateway.connectors.InReachConnector;
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
 * a broadcast receiver to pick up the incoming SimpleMeshMS messages
 */
public class IncomingMeshMS extends BroadcastReceiver {
	
	// class level constants
	private final boolean V_LOG = true;
	private final String TAG = "IncomingMeshMS";
	private final boolean SKIP_DUPLICATE_CHECK = true;
	
	// class level variables
	private ContentResolver contentResolver;
	
	private Context context;

	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// get the simpleMeshMS object
		SimpleMeshMS mMessage = (SimpleMeshMS) intent.getParcelableExtra("simple");

		// check to see if a message was attached
		if(mMessage != null) {
			
			// get a content resolver for all of the DB work ahead
			contentResolver = context.getContentResolver();
			
			this.context = context;
			
			// check to see if relaying is allowed for this recipient
			if(isRelayAllowed(mMessage.getRecipient()) == false) {
				// stop processing the message
				Log.i(TAG, "message for '" + mMessage.getRecipient() + "' received but is not in relay allowed list");
				return;
			} else {
				Log.i(TAG, "message for '" + mMessage.getRecipient() + "' received for relay");
			}
			
			// check to see if this message has been seen before
			String mHash = getMd5Hash(mMessage.getSender(), mMessage.getRecipient(), mMessage.getContent());
			
			if(mHash != null) {
				if(isDuplicate(mHash) == true) {
					// stop processing
					Log.w(TAG, "message with hash '" + mHash + "' is a duplicate");
					return;
				}
				
				if(V_LOG) {
					Log.v(TAG, "message is not a duplicate");
				}
			} else {
				Log.w(TAG, "unable to check for duplicate messages");
			}
			
			// log the incoming message
			int mId = logMessage(mMessage, mHash);
			if(mId == -1) {
				Log.e(TAG, "unable to save the message to the log");
				return;
			}
			
			if(V_LOG) {
				Log.v(TAG, "message logged with id " + mId);
			}
			
			// reformat the message
			String mOldMessageContent = mMessage.getContent();
			mMessage.setContent(reformatContent(mMessage));
			
			// truncate the message if required
			if(truncateMessage(mMessage, InReachConnector.MAX_MESSAGE_LENGTH, InReachConnector.TRUNCATED_MESSAGE_INDICATOR) == true) {
	
				Log.w(TAG, "message with id '" + mId + "' exceeded length limit of '" + InReachConnector.MAX_MESSAGE_LENGTH + "' and was truncated");
				
				// send a reply
				sendReply(mMessage.getSender(), mMessage.getRecipient(), mOldMessageContent, InReachConnector.MAX_MESSAGE_LENGTH);
				
				// update the record with the truncated flag
				updateRecordTruncated(mId);
			}
			
			// update the record with the sent content
			updateRecordSentContent(mId, mMessage.getContent());
			
			// send the message
			// TODO move this to the connector class
			sendOutboundMessage(mMessage, mId);
			
			// process a simple message
//			Log.d(TAG, "simple message found");
//			Log.d(TAG, "sender: " + mMessage.getSender());
//			Log.d(TAG, "recipient: " + mMessage.getRecipient());
//			Log.d(TAG, "content: " + mMessage.getContent());
//			Log.d(TAG, "timestamp: " + mMessage.getTimestamp());
			
		} else {
			// no message found
			Log.e(TAG, "no message found");
		}
	}
	
	// method to determine if we can relay for this recipient
	private boolean isRelayAllowed(String recipient) {
		
		boolean mReturn = false;
		
		// check for the start recipient
		if(recipient.equals("*") == true) {
			return false;
		}
		
		// prepare the query
		// TODO use a cached form of this data
		String[] mProjection = {GatewayItemsContract.Contacts.Table._ID};
		String mSelection = GatewayItemsContract.Contacts.Table.PHONE_NUMBER + " = ?";
		String[] mSelectionArgs = {recipient};
		
		// execute the query
		try {
			Cursor mCursor = contentResolver.query(GatewayItemsContract.Contacts.CONTENT_URI, mProjection, mSelection, mSelectionArgs, null);
			
			if(mCursor.getCount() > 0) {
				mReturn = true;
			} 
			
			mCursor.close();
			
		} catch (SQLException e) {
			Log.e(TAG, "error in executing SQL in isRelayAllowed", e);
			return false;
		}
		
		return mReturn;
	}

	// calculate the md5 hash of a message as a string
	/*
	 * the following method is based on code found here:
	 * http://p-xr.com/android-snippet-making-a-md5-hash-from-a-string-in-java/
	 * which is considered to be in the public domain
	 */
	// method to build the md5 hash of from the simpleMesgMS content
	private String getMd5Hash(String sender, String recipient, String content) {
		
		String mResult = null;
		
		try {
			
			// instantiate and configure the digest class for use with the MD5 algorithm
			MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.reset();
			
			// compile the string and digest it
			String mInput = sender + recipient + content;
			mDigest.update(mInput.getBytes());
			
			// convert the byte array of the digest into a string
			byte[] mBytes = mDigest.digest();
			int mLength = mBytes.length;
			
			StringBuilder mBuilder = new StringBuilder(mLength << 1);
			
			for (int i = 0; i < mLength; i++) {
				mBuilder.append(Character.forDigit((mBytes[i] & 0xf0) >> 4, 16));
				mBuilder.append(Character.forDigit(mBytes[i] & 0x0f, 16));
			}

			return mBuilder.toString();

		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "unable to use md5 for hashing", e);
		}

		return mResult;
	}


	// check to see if this message is a duplicate
	private boolean isDuplicate(String md5hash) {
		
		if(SKIP_DUPLICATE_CHECK) {
			return false;
		}
		
		boolean mReturn = false;
		
		// prepare the query
		String[] mProjection = {GatewayItemsContract.Messages.Table._ID};
		String mSelection = GatewayItemsContract.Messages.Table.MD5_HASH + " = ?";
		String[] mSelectionArgs = {md5hash};
		
		// execute the query
		try {
			Cursor mCursor = contentResolver.query(GatewayItemsContract.Messages.CONTENT_URI, mProjection, mSelection, mSelectionArgs, null);
			
			if(mCursor.getCount() > 0) {
				mReturn = true;
			} 
			
			mCursor.close();
			
		} catch (SQLException e) {
			Log.e(TAG, "error in executing SQL in isDuplicate", e);
			return false;
		}
		
		return mReturn;
	}

	// save a log of this message
	private int logMessage(SimpleMeshMS message, String md5hash) {
		
		int mReturn = -1;
		
		// compile the list of things to put into the table
		ContentValues mValues = new ContentValues();
		mValues.put(GatewayItemsContract.Messages.Table.SENDER, message.getSender());
		mValues.put(GatewayItemsContract.Messages.Table.RECIPIENT, message.getRecipient());
		mValues.put(GatewayItemsContract.Messages.Table.CONTENT, message.getContent());
		mValues.put(GatewayItemsContract.Messages.Table.TIMESTAMP, message.getTimestamp());
		mValues.put(GatewayItemsContract.Messages.Table.CONNECTOR, InReachConnector.CONNECTOR_NAME); //TODO update to use constants class
		mValues.put(GatewayItemsContract.Messages.Table.MD5_HASH, md5hash);
		
		// save the new record
		try {
			Uri mUri = contentResolver.insert(GatewayItemsContract.Messages.CONTENT_URI, mValues);
		
			mReturn = Integer.parseInt(mUri.getLastPathSegment());
		} catch (SQLException e) {
			Log.e(TAG, "error in executing SQL in logMessage", e);
			return -1;
		} catch (NumberFormatException e) {
			Log.e(TAG, "error in determining record id in logMessage", e);
			return -1;
		}
		
		return mReturn;
		
	}

	// reformat the message
	private String reformatContent(SimpleMeshMS message) {
		
		String newContent = "";
		
		newContent = message.getSender() + "\n" + message.getContent();
		
		return newContent;
		
	}

	// truncate the message if required
	private boolean truncateMessage(SimpleMeshMS message, int maxLength, String indicator) {
		
		if(message.getContent().length() > maxLength) {
			String mContent = message.getContent();
			
			int newLength = maxLength - indicator.length();
			mContent = mContent.substring(0, newLength);
			mContent = mContent + indicator;
			
			message.setContent(mContent);
			
			return true;
		} else {
			return false;
		}
	}

	// send a message indicating that the original message was truncated
	private void sendReply(String sender, String recipient, String content, int maxLength) {
		
		// build the content of the reply
		String mContent = String.format(context.getString(R.string.core_service_truncation_message), 
				recipient,
				content.substring(0, 15),
				maxLength);
		
		SimpleMeshMS mReply = new SimpleMeshMS(sender, mContent);
		
		// create the intent
		Intent mMeshMSIntent = new Intent("org.servalproject.meshms.SEND_MESHMS");

		// add the SimpleMeshMS parcelable to the intent as an extra
		mMeshMSIntent.putExtra("simple", mReply);

		// send the intent
		context.startService(mMeshMSIntent);
	}

	// private method to update a message log
	private void updateRecordTruncated(int id) {
		
		ContentValues mValues = new ContentValues();
		mValues.put(GatewayItemsContract.Messages.Table.TRUNCATED, GatewayItemsContract.Messages.IS_TRUNCATED_FLAG);
		
		Uri mUri = Uri.withAppendedPath(GatewayItemsContract.Messages.CONTENT_URI, Integer.toString(id)); 
		
		contentResolver.update(mUri, mValues, null, null);
	}
	
	// private method to update the record with the sent content
	private void updateRecordSentContent(int id, String content) {
		
		ContentValues mValues = new ContentValues();
		mValues.put(GatewayItemsContract.Messages.Table.SENT_CONTENT, content);
		
		Uri mUri = Uri.withAppendedPath(GatewayItemsContract.Messages.CONTENT_URI, Integer.toString(id)); 
		
		contentResolver.update(mUri, mValues, null, null);
	}

	// private method to send an outbound message
	private void sendOutboundMessage(SimpleMeshMS message, int id) {
		
		// prepare the inReach Intent
		Intent mOutgoingIntent = new Intent(context, org.servalproject.meshms.gateway.SendMessageActivity.class); // action
		mOutgoingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mOutgoingIntent.putExtra("message", message);
		mOutgoingIntent.putExtra("id", id);
		
		context.startActivity(mOutgoingIntent);
	}
}