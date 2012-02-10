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

import org.servalproject.meshms.gateway.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * core service class to manage the relay of messages
 */
public class CoreService extends Service {
	
	// class level constants
	private final int STATUS_NOTIFICATION = 0;
	
	private final boolean V_LOG = true;
	private final String  TAG = "CoreService";
	
	// class level variables
	private IncomingMeshMS incomingMeshMS;
	private IncomingInReachMessages incomingInReachMessages;
	
	/*
	 * called when the service is created
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		
		if(V_LOG) {
			Log.v(TAG, "Service Created");
		}
	}
	
	/*
	 * called when the service is started
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(V_LOG) {
			Log.v(TAG, "Service Started");
		}
		
		// add the notification icon
		addNotification();
		
		// capture the broadcast requests
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("org.servalproject.meshms.RECEIVE_BROADCASTS");
		incomingMeshMS = new IncomingMeshMS();
		registerReceiver(incomingMeshMS, mIntentFilter);
		
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("com.delorme.intent.action.MESSAGE_RECEIVED");
		mIntentFilter.addAction("com.delorme.intent.action.MESSAGE_SENT");
		incomingInReachMessages = new IncomingInReachMessages();
		registerReceiver(incomingInReachMessages, mIntentFilter);
		
		// If service gets killed, after returning from here, restart
	    return START_STICKY;	
	}
	
	// private method used to add the notification icon
	private void addNotification() {
		
		// add a notification icon
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		//TODO update this with a custom notification with stats
		int mNotificationIcon = R.drawable.ic_notification;
		
		CharSequence mTickerText = getString(R.string.system_notification_ticker_text);
		long mWhen = System.currentTimeMillis();
		
		// create the notification and set the flag so that it stays up
		Notification mNotification = new Notification(mNotificationIcon, mTickerText, mWhen);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		// get the content of the notification
		CharSequence mNotificationTitle = getString(R.string.system_notification_title);
		CharSequence mNotificationContent = getString(R.string.system_notification_content);
		
		// create the intent for the notification
		// set flags so that the user returns to this activity and not a new one
		Intent mNotificationIntent = new Intent(this, org.servalproject.meshms.gateway.MainActivity.class);
		mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		// create a pending intent so that the system can use the above intent at a later time.
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, mNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// complete the setup of the notification
		mNotification.setLatestEventInfo(getApplicationContext(), mNotificationTitle, mNotificationContent, mPendingIntent);
		
		// add the notification
		mNotificationManager.notify(STATUS_NOTIFICATION, mNotification);
	}
	
	/*
	 * called when the service kills the service
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// tidy up any used resources etc.
		// clear the notification
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(STATUS_NOTIFICATION);
		
		// unregister the receiver
		unregisterReceiver(incomingMeshMS);
		unregisterReceiver(incomingInReachMessages);
		
		super.onDestroy();
		
		if(V_LOG) {
			Log.v(TAG, "Service Destroyed");
		}
	}

	/*
	 * this isn't a bound service so we can safely return null here
	 * 
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
