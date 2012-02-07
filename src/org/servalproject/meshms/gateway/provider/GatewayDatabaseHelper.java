package org.servalproject.meshms.gateway.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GatewayDatabaseHelper extends SQLiteOpenHelper {
	
	// declare public class constants
	public static final String DB_NAME = "gateway-items";
	public static final int DB_VERSION = 1;
	
	private final String CONTACTS_CREATE = "CREATE TABLE " 
			+ GatewayItemsContract.Contacts.Table.TABLE_NAME + " ("
			+ GatewayItemsContract.Contacts.Table._ID + " INTEGER PRIMARY KEY, "
			+ GatewayItemsContract.Contacts.Table.PHONE_NUMBER + " TEXT)";
	
	public GatewayDatabaseHelper(Context context) {
		// context, database name, factory, db version
		super(context, DB_NAME, null, DB_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the tables
		db.execSQL(CONTACTS_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
