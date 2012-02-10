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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GatewayDatabaseHelper extends SQLiteOpenHelper {
	
	// declare public class constants
	public static final String DB_NAME = "gateway-items.db";
	public static final int DB_VERSION = 1;
	
	private final String CONTACTS_CREATE = "CREATE TABLE " 
			+ GatewayItemsContract.Contacts.Table.TABLE_NAME + " ("
			+ GatewayItemsContract.Contacts.Table._ID + " INTEGER PRIMARY KEY, "
			+ GatewayItemsContract.Contacts.Table.PHONE_NUMBER + " TEXT)";
	
	private final String MESSAGES_CREATE = "CREATE TABLE "
			+ GatewayItemsContract.Messages.Table.TABLE_NAME + " ("
			+ GatewayItemsContract.Messages.Table._ID + " INTEGER PRIMARY KEY, "
			+ GatewayItemsContract.Messages.Table.SENDER + " TEXT, "
			+ GatewayItemsContract.Messages.Table.RECIPIENT + " TEXT, "
			+ GatewayItemsContract.Messages.Table.CONTENT + " TEXT, "
			+ GatewayItemsContract.Messages.Table.TRUNCATED + " INTEGER DEFAULT " + GatewayItemsContract.Messages.IS_NOT_TRUNCATED_FLAG + ", "
			+ GatewayItemsContract.Messages.Table.SENT_CONTENT + " TEXT, "
			+ GatewayItemsContract.Messages.Table.TIMESTAMP + " INTEGER, "
			+ GatewayItemsContract.Messages.Table.CONNECTOR + " INTEGER, "
			+ GatewayItemsContract.Messages.Table.STATUS + " INTEGER DEFAULT " + GatewayItemsContract.Messages.IS_NOT_SENT_FLAG + ", "
			+ GatewayItemsContract.Messages.Table.MD5_HASH + " TEXT)";
	
	private final String MESSAGES_MD5_INDEX = "CREATE INDEX messages_md5hash ON "
			+ GatewayItemsContract.Messages.Table.TABLE_NAME + " ("
			+ GatewayItemsContract.Messages.Table.MD5_HASH + ")";
	
	private final String MESSAGES_CONTENT_INDEX = "CREATE INDEX content_index ON "
			+ GatewayItemsContract.Messages.Table.TABLE_NAME + " ("
			+ GatewayItemsContract.Messages.Table.RECIPIENT + ", "
			+ GatewayItemsContract.Messages.Table.SENT_CONTENT + ")";
	
	public GatewayDatabaseHelper(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the tables
		db.execSQL(CONTACTS_CREATE);
		db.execSQL(MESSAGES_CREATE);
		
		// create additional indexes
		db.execSQL(MESSAGES_MD5_INDEX);
		db.execSQL(MESSAGES_CONTENT_INDEX);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
