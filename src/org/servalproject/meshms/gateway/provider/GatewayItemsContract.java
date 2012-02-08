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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * a class that exposes the necessary constants for 
 * interacting with the content provider
 */
public class GatewayItemsContract {
	
	/**
	 * authority string for the content provider
	 */
	public static final String AUTHORITY = "org.servalproject.meshms.gateway.provider.items";
	
	/**
	 * meta data for the contacts table
	 */
	public static final class Contacts {
		
		/**
		 * path component of the URI
		 */
		public static final String CONTENT_URI_PATH = "contacts";
		
		/**
		 * content URI for the contacts data
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_URI_PATH);
		
		/**
		 * content type for a list of items
		 */
		public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_URI_PATH;
		
		/**
		 * content type for an individual item
		 */
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CONTENT_URI_PATH;
		
		/**
		 * table definition
		 */
		public static final class Table implements BaseColumns {
			
			/**
			 * table name
			 */
			public static final String TABLE_NAME = Contacts.CONTENT_URI_PATH;
			
			/**
			 * unique id column
			 */
			public static final String _ID = BaseColumns._ID;
			
			/**
			 * contact phone number
			 */
			public static final String PHONE_NUMBER = "phone_number";
			
			/**
			 * a list of all of the columns
			 */
			public static final String[] COLUMNS = {_ID, PHONE_NUMBER};
		}
	}
	
	/**
	 * meta data for the messages table
	 */
	public static final class Messages {
		
		/**
		 * path component of the URI
		 */
		public static final String CONTENT_URI_PATH = "messages";
		
		/**
		 * content URI for the contacts data
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_URI_PATH);
		
		/**
		 * content type for a list of items
		 */
		public static final String CONTENT_TYPE_LIST = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_URI_PATH;
		
		/**
		 * content type for an individual item
		 */
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CONTENT_URI_PATH;
		
		/**
		 * table definition
		 */
		public static final class Table implements BaseColumns {
			
			/**
			 * table name
			 */
			public static final String TABLE_NAME = Messages.CONTENT_URI_PATH;
			
			/**
			 * unique id column
			 */
			public static final String _ID = BaseColumns._ID;
			
			/**
			 * sender phone number 
			 */
			public static final String SENDER = "sender";
			
			/**
			 * recipient phone number
			 */
			public static final String RECIPIENT = "recipient";
			
			/**
			 * content of the message
			 */
			public static final String CONTENT = "content";
			
			/**
			 * timestamp of the message
			 */
			public static final String TIMESTAMP = "timestamp";
			
			/**
			 * connector used to send / receive the message
			 */
			public static final String CONNECTOR = "connector";
			
			/**
			 * connector specific status of the message
			 */
			public static final String STATUS = "status";
			
			/**
			 * md5 hash of the message content
			 */
			public static final String MD5_HASH = "md5_hash";
			
			/**
			 * a list of all of the columns
			 */
			public static final String[] COLUMNS = {_ID, SENDER, RECIPIENT, CONTENT, TIMESTAMP, STATUS, CONNECTOR, MD5_HASH};
		}	
	}
}
