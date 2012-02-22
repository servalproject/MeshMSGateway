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
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	// private class level constants
	private final String TAG = "MainActivity";
	
	// private class level variables
	private Handler updateHandler = new Handler();
	private int updateDelay = 10 * 1000;
	
	private TextView mReceivedCountView;
	private TextView mSentCountView;
    
	/*
	 * layout the controls on the activity
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button mButton = (Button) findViewById(R.id.main_ui_btn_settings);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_ui_btn_start_stop);
        mButton.setOnClickListener(this);
        
        mReceivedCountView = (TextView) findViewById(R.id.main_ui_lbl_status_in_count);
        mSentCountView = (TextView) findViewById(R.id.main_ui_lbl_status_out_count);
        
        // adjust the text of the button if required
        if(isMyServiceRunning() == true) {
        	mButton.setText(R.string.main_ui_btn_stop);
        } else {
        	mButton.setText(R.string.main_ui_btn_start);
        }
        
     // update the map without delay
     updateHandler.post(updateTask);
     
    }
    
    // runnable used to update the count
    private Runnable updateTask = new Runnable() {
    	
    	public void run() {
    	
	    	// resolve the content uri
	    	ContentResolver mContentResolver = getApplicationContext().getContentResolver();
	    	
	    	String[] mProjection = new String[1];
	    	mProjection[0] = GatewayItemsContract.Messages.Table._ID;
	    	
	    	String   mSelection = GatewayItemsContract.Messages.Table.STATUS + " = ?";
	    	String[] mSelectionArgs = new String[1];
	    	
	    	// get the received count
	    	mSelectionArgs[0] = Integer.toString(GatewayItemsContract.Messages.IS_RECEIVED_FLAG);
	    	
	    	Cursor mCursor = mContentResolver.query(
	    			GatewayItemsContract.Messages.CONTENT_URI, 
	    			mProjection, 
	    			mSelection, 
	    			mSelectionArgs,
	    			null);
	    	
	    	mReceivedCountView.setText(Integer.toString(mCursor.getCount()));
	    	
	    	mCursor.close();
	    	
	    	mSelectionArgs[0] = Integer.toString(GatewayItemsContract.Messages.IS_SENT_FLAG);
	    	
	    	mCursor = mContentResolver.query(
	    			GatewayItemsContract.Messages.CONTENT_URI, 
	    			mProjection, 
	    			mSelection, 
	    			mSelectionArgs,
	    			null);
	    	
	    	mSentCountView.setText(Integer.toString(mCursor.getCount()));
	    	
	    	mCursor.close();
	    	
	    	updateHandler.postDelayed(updateTask, updateDelay);
    	}
    };
    
    /*
     * determine what was clicked on
     * 
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View view) {
		
		Intent mIntent;
		
		// determine which view was touched
		switch(view.getId()) {
		case R.id.main_ui_btn_settings:
			// the settings button was clicked
			mIntent = new Intent(this, org.servalproject.meshms.gateway.RelayNumbers.class);
			startActivityForResult(mIntent, 0);
			break;
		case R.id.main_ui_btn_start_stop:
			mIntent = new Intent(this, org.servalproject.meshms.gateway.services.CoreService.class);
			
			if(isMyServiceRunning() == false) {
				startService(mIntent);
				
				Button mButton = (Button) findViewById(R.id.main_ui_btn_start_stop);
		        mButton.setText(R.string.main_ui_btn_stop);
			} else {
				stopService(mIntent);
				
				Button mButton = (Button) findViewById(R.id.main_ui_btn_start_stop);
		        mButton.setText(R.string.main_ui_btn_start);
			}
			
			break;
		default:
			Log.w(TAG, "unknown view fired a click event");
		}
	}
	
	/*
	 * check to see if the service is running
	 * this code is based on the code available at the URL below
	 * which is considered to be in the public domain
	 * http://stackoverflow.com/questions/600207/android-check-if-a-service-is-running
	 */
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (org.servalproject.meshms.gateway.services.CoreService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}