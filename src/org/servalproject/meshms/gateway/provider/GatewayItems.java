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
package org.servalproject.meshms.gateway.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * main content provider for the gateway application
 */
public class GatewayItems extends ContentProvider {
	
	// class level constants
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	private final int CONTACTS_LIST_URI = 0;
	private final int CONTACTS_ITEM_URI = 1;
	
	private final int MESSAGES_LIST_URI = 2;
	private final int MESSAGES_ITEM_URI = 3;
	
	private final String TAG = "GatewayItems";
	
	// class level variables
	private GatewayDatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	
	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		
		// define the URIs that we'll match against
		uriMatcher.addURI(GatewayItemsContract.AUTHORITY, GatewayItemsContract.Contacts.CONTENT_URI_PATH, CONTACTS_LIST_URI);
		uriMatcher.addURI(GatewayItemsContract.AUTHORITY, GatewayItemsContract.Contacts.CONTENT_URI_PATH + "/#", CONTACTS_ITEM_URI);
		
		uriMatcher.addURI(GatewayItemsContract.AUTHORITY, GatewayItemsContract.Messages.CONTENT_URI_PATH, MESSAGES_LIST_URI);
		uriMatcher.addURI(GatewayItemsContract.AUTHORITY, GatewayItemsContract.Messages.CONTENT_URI_PATH + "/#", MESSAGES_ITEM_URI);
		
		databaseHelper = new GatewayDatabaseHelper(getContext());
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		String mTable = null;
		int mCount = -1;
		
		// chose the table name
		switch(uriMatcher.match(uri)) {
		case CONTACTS_LIST_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			break;
		case CONTACTS_ITEM_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			
			// ammend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		case MESSAGES_LIST_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			break;
		case MESSAGES_ITEM_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			
			// ammend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		default:
			// unknown uri found
			Log.e(TAG, "invalid URI detected for delete: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		
		// delete the required item(s)
		mCount = database.delete(mTable, selection, selectionArgs);
		
		// notify anyone watching of the change to the data
		getContext().getContentResolver().notifyChange(uri, null);

		// return the number of delete records
		return mCount;
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// choose the mime type
		switch(uriMatcher.match(uri)) {
		case CONTACTS_LIST_URI:
			return GatewayItemsContract.Contacts.CONTENT_TYPE_LIST;
		case CONTACTS_ITEM_URI:
			return GatewayItemsContract.Contacts.CONTENT_TYPE_ITEM;
		case MESSAGES_LIST_URI:
			return GatewayItemsContract.Messages.CONTENT_TYPE_LIST;
		case MESSAGES_ITEM_URI:
			return GatewayItemsContract.Messages.CONTENT_TYPE_ITEM;
		default:
			// unknown uri found
			Log.e(TAG, "unknown URI detected on getType: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		Uri mResults = null;
		String mTable = null;
		Uri mContentUri = null;
		
		// chose the table name
		switch(uriMatcher.match(uri)) {
		case CONTACTS_LIST_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			mContentUri = GatewayItemsContract.Contacts.CONTENT_URI;
			break;
		case MESSAGES_LIST_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			mContentUri = GatewayItemsContract.Messages.CONTENT_URI;
			break;
		default:
			// unknown uri found
			Log.e(TAG, "invalid URI detected for insert: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getWritableDatabase();
		
		long mId = database.insertOrThrow(mTable, null, values);
		
		mResults = ContentUris.withAppendedId(mContentUri, mId);
		getContext().getContentResolver().notifyChange(mResults, null);
		
		return mResults;
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		String mTable = null;
		Cursor mResults = null;
		
		// chose the table name
		switch(uriMatcher.match(uri)) {
		case CONTACTS_LIST_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			break;
		case CONTACTS_ITEM_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			
			// amend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		case MESSAGES_LIST_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			break;
		case MESSAGES_ITEM_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			
			// ammend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		default:
			// unknown uri found
			Log.e(TAG, "invalid URI detected for query: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		// get a connection to the database
		database = databaseHelper.getReadableDatabase();
		
		mResults = database.query(mTable, projection, selection, selectionArgs, null, null, sortOrder);
		
		mResults.setNotificationUri(getContext().getContentResolver(), uri);
		
		return mResults;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		String mTable = null;
		int mResults = -1;
		
		// chose the table name
		switch(uriMatcher.match(uri)) {
		case CONTACTS_LIST_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			break;
		case CONTACTS_ITEM_URI:
			mTable = GatewayItemsContract.Contacts.Table.TABLE_NAME;
			
			// amend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Contacts.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		case MESSAGES_LIST_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			break;
		case MESSAGES_ITEM_URI:
			mTable = GatewayItemsContract.Messages.Table.TABLE_NAME;
			
			// ammend the where clause if required
			if(TextUtils.isEmpty(selection) == true) {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1);
			} else {
				selection = GatewayItemsContract.Messages.Table._ID + " = " + uri.getPathSegments().get(1) + " AND (" + selection + ")";
			}
			break;
		default:
			// unknown uri found
			Log.e(TAG, "invalid URI detected for update: " + uri.toString());
			throw new IllegalArgumentException("unknwon URI detected");
		}
		
		database = databaseHelper.getWritableDatabase();
		
		mResults = database.update(mTable, values, selection, selectionArgs);
		
		return mResults;	
	}
}
