package com.fanfou.app.db;

import java.util.Iterator;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.fanfou.app.App;
import com.fanfou.app.R.string;
import com.fanfou.app.api.Status;
import com.fanfou.app.api.User;
import com.fanfou.app.config.Commons;
import com.fanfou.app.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.05.24
 * @version 1.5 2011.05.25
 * @version 1.6 2011.06.04
 * @version 1.7 2011.07.14
 * @version 1.8 2011.07.22
 * 
 */
public class FanFouProvider extends ContentProvider implements Contents {

	private static final String TAG = FanFouProvider.class.getSimpleName();

	private void log(String message) {
		Log.d(TAG, message);
	}

	private SQLiteHelper dbHelper;

	public static final int USERS = 1;
	public static final int USER_SEARCH = 2;
	public static final int USER_ITEM = 3;
	public static final int USER_TYPE = 4;
	public static final int USER_ID = 5;

	public static final int USER_FRIENDS = 6;
	public static final int USER_FOLLOWERS = 7;

	public static final int STATUSES = 21;
	public static final int STATUS_SEARCH_LOCAL = 22;
	public static final int STATUS_USER = 23;
	public static final int STATUS_ITEM = 24;

	public static final int STATUS_ACTION_CLEAN = 25;

	// public timeline cache, individual table
	// public static final int PUBLIC = 27;

	public static final int STATUS_SEARCH = 28;

	public static final int STATUS_TYPE = 30;
	public static final int STATUS_ACTION_COUNT = 31;
	public static final int STATUS_ID = 32;

	public static final int MESSAGES = 41;
	public static final int MESSAGE_ITEM = 42;
	public static final int MESSAGE_ID = 43;

	private static final UriMatcher sUriMatcher;
	// private static HashMap<String, String> sUserProjectionMap;
	// private static HashMap<String, String> sStatusProjectionMap;
	// private static HashMap<String, String> sMessageProjectionMap;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(AUTHORITY, UserInfo.URI_PATH, USERS);
		sUriMatcher.addURI(AUTHORITY, UserInfo.URI_PATH + "/search/*",
				USER_SEARCH);
		sUriMatcher.addURI(AUTHORITY, UserInfo.URI_PATH + "/item/*", USER_ITEM);
		sUriMatcher.addURI(AUTHORITY, UserInfo.URI_PATH + "/type/#", USER_TYPE);
		sUriMatcher.addURI(AUTHORITY, UserInfo.URI_PATH + "/id/#", USER_ID);

		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH, STATUSES);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/local/*",
				STATUS_SEARCH_LOCAL);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/user/*",
				STATUS_USER);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/item/*",
				STATUS_ITEM);

		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/search/*",
				STATUS_SEARCH);

		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/type/#",
				STATUS_TYPE);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/action/count/#",
				STATUS_ACTION_COUNT);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/action/clean",
				STATUS_ACTION_CLEAN);
		sUriMatcher.addURI(AUTHORITY, StatusInfo.URI_PATH + "/id/#", STATUS_ID);

		sUriMatcher.addURI(AUTHORITY, DirectMessageInfo.URI_PATH, MESSAGES);
		sUriMatcher.addURI(AUTHORITY, DirectMessageInfo.URI_PATH + "/item/*",
				MESSAGE_ITEM);
		sUriMatcher.addURI(AUTHORITY, DirectMessageInfo.URI_PATH + "/id/#",
				MESSAGE_ID);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new SQLiteHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case USERS:
			return UserInfo.CONTENT_TYPE;
		case USER_ITEM:
		case USER_ID:
			return UserInfo.CONTENT_ITEM_TYPE;
		case STATUSES:
		case STATUS_SEARCH_LOCAL:
		case STATUS_SEARCH:
		case STATUS_USER:
			// case PUBLIC:
			return StatusInfo.CONTENT_TYPE;
		case STATUS_ITEM:
		case STATUS_ID:
		case STATUS_ACTION_COUNT:
		case STATUS_ACTION_CLEAN:
			return StatusInfo.CONTENT_ITEM_TYPE;
		case MESSAGES:
			return DirectMessageInfo.CONTENT_TYPE;
		case MESSAGE_ITEM:
		case MESSAGE_ID:
			return DirectMessageInfo.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("getType() Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String orderBy) {
		// log("query() uri = " + uri + " where= (" + where + ") whereArgs = "
		// + StringHelper.toString(whereArgs));
		// SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String order = null;
		// Cursor c;
		switch (sUriMatcher.match(uri)) {
		case USERS:
			// c=db.query(UserInfo.TABLE_NAME, columns, where, whereArgs, null,
			// null, orderBy);
			qb.setTables(UserInfo.TABLE_NAME);
			break;
		case USER_ITEM:
			// {
			// String userId=uri.getPathSegments().get(1);
			// String whereClause=UserInfo.ID + "=" + userId +
			// (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			// c=db.query(UserInfo.TABLE_NAME, columns, whereClause, whereArgs,
			// null, null, orderBy);
			// }
			qb.setTables(UserInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case USER_ID:
			qb.setTables(UserInfo.TABLE_NAME);
			qb.appendWhere(BaseColumns._ID + "=");
			qb.appendWhere(uri.getPathSegments().get(2));
			break;
		case STATUSES:
			// c=db.query(StatusInfo.TABLE_NAME, columns, where, whereArgs,
			// null, null, orderBy);
			qb.setTables(StatusInfo.TABLE_NAME);
			order = ORDERBY_DATE_DESC;
			break;
		// case PUBLIC:
		// qb.setTables(StatusInfo.PUBLIC_TABLE_NAME);
		// break;
		// case STATUS_COUNT:
		// break;
		case STATUS_SEARCH:
			order = ORDERBY_DATE_DESC;
			break;
		case STATUS_ITEM:
			// {
			// String statusId=uri.getPathSegments().get(1);
			// String whereClause=StatusInfo.ID + "=" + statusId +
			// (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			// c=db.query(StatusInfo.TABLE_NAME, columns, whereClause,
			// whereArgs, null, null, orderBy);
			// }
			qb.setTables(StatusInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case STATUS_ID:
			qb.setTables(StatusInfo.TABLE_NAME);
			qb.appendWhere(BaseColumns._ID + "=");
			qb.appendWhere(uri.getPathSegments().get(2));
			break;
		case STATUS_ACTION_COUNT:
			return countStatus(uri);
			// qb.setTables(StatusInfo.TABLE_NAME);
			// qb.appendWhere(StatusInfo.TYPE+"=");
			// qb.appendWhere(uri.getPathSegments().get(3));
			// break;
		case MESSAGES:
			// c=db.query(DirectMessageInfo.TABLE_NAME, columns, where,
			// whereArgs, null, null, orderBy);
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			order = ORDERBY_DATE_DESC;
			break;
		case MESSAGE_ITEM:
			// {
			// String messageId=uri.getPathSegments().get(1);
			// String whereClause=DirectMessageInfo.ID + "=" + messageId +
			// (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
			// c=db.query(DirectMessageInfo.TABLE_NAME, columns, whereClause,
			// whereArgs, null, null, orderBy);
			// }
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			qb.appendWhere(BasicColumns.ID + "=");
			qb.appendWhere("'" + uri.getPathSegments().get(2) + "'");
			break;
		case MESSAGE_ID:
			qb.setTables(DirectMessageInfo.TABLE_NAME);
			qb.appendWhere(BaseColumns._ID + "=");
			qb.appendWhere(uri.getPathSegments().get(2));
			break;
		default:
			throw new IllegalArgumentException("query() Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = qb.query(db, columns, where, whereArgs, null, null, order);

		if (c == null) {
			// log("query() uri " + uri + " failed.");
		} else {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return c;
	}

	/**
	 * 计算某种类型的消息数量
	 * 
	 * @param uri
	 * @return
	 */
	private Cursor countStatus(Uri uri) {
		int type = Integer.parseInt(uri.getPathSegments().get(3));
		String sql = "SELECT COUNT(" + BasicColumns.ID + ") FROM "
				+ StatusInfo.TABLE_NAME;
		if (type == Status.TYPE_NONE) {
			sql += " ;";
		} else {
			sql += " WHERE " + BasicColumns.TYPE + "=" + type + ";";
		}
		// log("countStatus() status count, uri=" + uri + " ");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(sql, null);
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (values == null || values.size() == 0) {
			throw new IllegalArgumentException("插入数据不能为空.");
		}

		// log("insert() uri=" + uri.toString() + " id="
		// + values.getAsString(BasicColumns.ID));

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String table;
		Uri contentUri;
		switch (sUriMatcher.match(uri)) {
		case USERS:
			table = UserInfo.TABLE_NAME;
			contentUri = UserInfo.CONTENT_URI;
			break;
		case STATUSES:
			table = StatusInfo.TABLE_NAME;
			contentUri = StatusInfo.CONTENT_URI;
			break;
		// case PUBLIC:
		// table = StatusInfo.PUBLIC_TABLE_NAME;
		// contentUri = StatusInfo.PUBLIC_URI;
		// break;
		case MESSAGES:
			table = DirectMessageInfo.TABLE_NAME;
			contentUri = DirectMessageInfo.CONTENT_URI;
			break;
		case USER_ITEM:
		case STATUS_ITEM:
		case MESSAGE_ITEM:
			throw new UnsupportedOperationException("Cannot insert URI: " + uri);
		default:
			throw new IllegalArgumentException("insert() Unknown URI " + uri);
		}

		// log(" insert() table=" + table + " values=" + values);
		long rowId = db.insert(table, null, values);
		// long rowId=db.insert(table, null, values);

		if (rowId < 0) {
			// log("insert failed. ");
			// String where=BasicColumns.ID+"=?";
			// String[] whereArgs=new
			// String[]{values.getAsString(BasicColumns.ID)};
			// db.update(table, values, where,whereArgs);
			return uri;
		}
		Uri resultUri = ContentUris.withAppendedId(contentUri, rowId);

		// log("insert() resultUri=" + resultUri);
		getContext().getContentResolver().notifyChange(resultUri, null);
		return resultUri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final int match = sUriMatcher.match(uri);
		int result = 0;
		switch (match) {
		case STATUSES:
			result = bulkInsertStatuses(values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case USERS:
			result = bulkInsertUsers(values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case MESSAGES:
			result = bulkInsertData(DirectMessageInfo.TABLE_NAME, values);
			break;
		default:
			if (App.DEBUG) {
				throw new UnsupportedOperationException("unsupported uri: "
						+ uri);
			}
			break;
		}
		return result;
	}

	private int bulkInsertData(String table, ContentValues[] values) {
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for (ContentValues value : values) {
				db.insert(table, null, value);
			}
			db.setTransactionSuccessful();
			numInserted += values.length;
		} catch (Exception e) {
			if (App.DEBUG) {
				e.printStackTrace();
			}
		} finally {
			db.endTransaction();
		}
		return numInserted;

	}
	
	private int bulkInsertUsers(ContentValues[] values){
		if (App.DEBUG) {
			log("bulkInsertUsers()");
		}
		
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		InsertHelper ih = new InsertHelper(db, UserInfo.TABLE_NAME);

		int id = ih.getColumnIndex(UserInfo.ID);
		int ownerId = ih.getColumnIndex(UserInfo.OWNER_ID);
		int name=ih.getColumnIndex(UserInfo.NAME);
		
		int screenName=ih.getColumnIndex(UserInfo.SCREEN_NAME);
		int location=ih.getColumnIndex(UserInfo.LOCATION);
		int gender=ih.getColumnIndex(UserInfo.GENDER);
		int birthday=ih.getColumnIndex(UserInfo.BIRTHDAY);
		
		int description=ih.getColumnIndex(UserInfo.DESCRIPTION);
		int profileImageUrl=ih.getColumnIndex(UserInfo.PROFILE_IMAGE_URL);
		int url=ih.getColumnIndex(UserInfo.URL);
		int protect=ih.getColumnIndex(UserInfo.PROTECTED);
		
		int followersCount=ih.getColumnIndex(UserInfo.FOLLOWERS_COUNT);
		int friendsCount=ih.getColumnIndex(UserInfo.FRIENDS_COUNT);
		int favoritesCount=ih.getColumnIndex(UserInfo.FAVORITES_COUNT);
		int statusesCount=ih.getColumnIndex(UserInfo.STATUSES_COUNT);
		
		int following=ih.getColumnIndex(UserInfo.FOLLOWING);
		int notifications=ih.getColumnIndex(UserInfo.NOTIFICATIONS);
		int createdAt = ih.getColumnIndex(UserInfo.CREATED_AT);
		int utcOffset=ih.getColumnIndex(UserInfo.UTC_OFFSET);
		
		int lastStatusId=ih.getColumnIndex(UserInfo.LAST_STATUS_ID);
		int lastStatusText=ih.getColumnIndex(UserInfo.LAST_STATUS_TEXT);
		int lastStatusCreatedAt=ih.getColumnIndex(UserInfo.LAST_STATUS_CREATED_AT);
		
		int type=ih.getColumnIndex(UserInfo.TYPE);
		int timestamp=ih.getColumnIndex(UserInfo.TIMESTAMP);
		
		try {
			db.beginTransaction();
			for (ContentValues value : values) {
				ih.prepareForInsert();

				ih.bind(id, value.getAsString(UserInfo.ID));
				ih.bind(ownerId, value.getAsString(UserInfo.OWNER_ID));
				
				ih.bind(screenName, value.getAsString(UserInfo.SCREEN_NAME));
				ih.bind(location, value.getAsString(UserInfo.LOCATION));
				ih.bind(gender, value.getAsString(UserInfo.GENDER));
				ih.bind(birthday, value.getAsString(UserInfo.BIRTHDAY));
				
				ih.bind(description, value.getAsString(UserInfo.DESCRIPTION));
				ih.bind(profileImageUrl, value.getAsString(UserInfo.PROFILE_IMAGE_URL));
				ih.bind(url, value.getAsString(UserInfo.URL));
				ih.bind(protect, value.getAsBoolean(UserInfo.PROTECTED));
				
				ih.bind(followersCount, value.getAsInteger(UserInfo.FOLLOWERS_COUNT));
				ih.bind(friendsCount, value.getAsInteger(UserInfo.FRIENDS_COUNT));
				ih.bind(favoritesCount, value.getAsInteger(UserInfo.FAVORITES_COUNT));
				ih.bind(statusesCount, value.getAsInteger(UserInfo.STATUSES_COUNT));
				
				ih.bind(following, value.getAsBoolean(UserInfo.FOLLOWING));
				ih.bind(notifications, value.getAsBoolean(UserInfo.NOTIFICATIONS));
				ih.bind(createdAt, value.getAsLong(UserInfo.CREATED_AT));
				ih.bind(utcOffset, value.getAsInteger(UserInfo.UTC_OFFSET));
				
				if(value.containsKey(UserInfo.LAST_STATUS_CREATED_AT)){
					ih.bind(lastStatusId, value.getAsString(UserInfo.LAST_STATUS_ID));
					ih.bind(lastStatusText, value.getAsString(UserInfo.LAST_STATUS_TEXT));
					ih.bind(lastStatusCreatedAt, value.getAsLong(UserInfo.LAST_STATUS_CREATED_AT));
				}

				ih.bind(type, value.getAsInteger(UserInfo.TYPE));
				ih.bind(timestamp, value.getAsLong(UserInfo.TIMESTAMP));
				ih.bind(name, value.getAsString(UserInfo.NAME));

				long result = ih.execute();
				if (App.DEBUG) {
					log("bulkInsertUsers insert: user.id= "
							+ value.getAsString(UserInfo.ID) + " result="
							+ result);
				}
			}
			db.setTransactionSuccessful();
			numInserted = values.length;
		} catch (Exception e) {
			if(App.DEBUG){
				e.printStackTrace();
			}
		} finally {
			db.endTransaction();
		}
		return numInserted;
	}

	private int bulkInsertStatuses(ContentValues[] values) {
		if (App.DEBUG) {
			log("bulkInsertStatuses()");
		}
		int numInserted = 0;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		InsertHelper ih = new InsertHelper(db, StatusInfo.TABLE_NAME);

		int id = ih.getColumnIndex(StatusInfo.ID);
		int ownerId = ih.getColumnIndex(StatusInfo.OWNER_ID);
		int createdAt = ih.getColumnIndex(StatusInfo.CREATED_AT);

		int text = ih.getColumnIndex(StatusInfo.TEXT);
		int source = ih.getColumnIndex(StatusInfo.SOURCE);

		int inReplyToStatusId = ih
				.getColumnIndex(StatusInfo.IN_REPLY_TO_STATUS_ID);
		int inReplyToUserId = ih.getColumnIndex(StatusInfo.IN_REPLY_TO_USER_ID);
		int inReplyToScreenName = ih
				.getColumnIndex(StatusInfo.IN_REPLY_TO_SCREEN_NAME);

		int truncated = ih.getColumnIndex(StatusInfo.TRUNCATED);
		int favorited = ih.getColumnIndex(StatusInfo.FAVORITED);

		int photoImageUrl = ih.getColumnIndex(StatusInfo.PHOTO_IMAGE_URL);
		int photoThumbUrl = ih.getColumnIndex(StatusInfo.PHOTO_THUMB_URL);
		int photoLargeUrl = ih.getColumnIndex(StatusInfo.PHOTO_LARGE_URL);

		int userId = ih.getColumnIndex(StatusInfo.USER_ID);
		int userScreenName = ih.getColumnIndex(StatusInfo.USER_SCREEN_NAME);
		int userProfileImageUrl = ih
				.getColumnIndex(StatusInfo.USER_PROFILE_IMAGE_URL);

		int type = ih.getColumnIndex(StatusInfo.TYPE);
		int isRead = ih.getColumnIndex(StatusInfo.IS_READ);
		int timestamp = ih.getColumnIndex(StatusInfo.TIMESTAMP);

		try {
			db.beginTransaction();
			for (ContentValues value : values) {
				ih.prepareForInsert();

				ih.bind(id, value.getAsString(StatusInfo.ID));
				ih.bind(ownerId, value.getAsString(StatusInfo.OWNER_ID));
				ih.bind(createdAt, value.getAsLong(StatusInfo.CREATED_AT));

				ih.bind(text, value.getAsString(StatusInfo.TEXT));
				ih.bind(source, value.getAsString(StatusInfo.SOURCE));

				ih.bind(inReplyToStatusId,
						value.getAsString(StatusInfo.IN_REPLY_TO_STATUS_ID));
				ih.bind(inReplyToUserId,
						value.getAsString(StatusInfo.IN_REPLY_TO_USER_ID));
				ih.bind(inReplyToScreenName,
						value.getAsString(StatusInfo.IN_REPLY_TO_SCREEN_NAME));

				ih.bind(truncated, value.getAsBoolean(StatusInfo.TRUNCATED));
				ih.bind(favorited, value.getAsBoolean(StatusInfo.FAVORITED));

				ih.bind(photoImageUrl,
						value.getAsString(StatusInfo.PHOTO_IMAGE_URL));
				ih.bind(photoThumbUrl,
						value.getAsString(StatusInfo.PHOTO_THUMB_URL));
				ih.bind(photoLargeUrl,
						value.getAsString(StatusInfo.PHOTO_LARGE_URL));

				ih.bind(userId, value.getAsString(StatusInfo.USER_ID));
				ih.bind(userScreenName,
						value.getAsString(StatusInfo.USER_SCREEN_NAME));
				ih.bind(userProfileImageUrl,
						value.getAsString(StatusInfo.USER_PROFILE_IMAGE_URL));

				ih.bind(type, value.getAsInteger(StatusInfo.TYPE));
				ih.bind(isRead, value.getAsBoolean(StatusInfo.IS_READ));

				ih.bind(timestamp, value.getAsLong(StatusInfo.TIMESTAMP));

				long result = ih.execute();
				if (App.DEBUG) {
					log("bulkInsertStatuses insert: status.id= "
							+ value.getAsString(StatusInfo.ID) + " result="
							+ result);
				}
			}
			db.setTransactionSuccessful();
			numInserted = values.length;
		} catch (Exception e) {
			if(App.DEBUG){
				e.printStackTrace();
			}
		} finally {
			db.endTransaction();

		}
		return numInserted;

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// log("delete() uri = " + uri + " where= (" + where + ") whereArgs = "
		// + StringHelper.toString(whereArgs));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		String id;
		String _id;
		// String where;
		// int type;
		switch (sUriMatcher.match(uri)) {
		case USERS:
			count = db.delete(UserInfo.TABLE_NAME, where, whereArgs);
			break;
		case USER_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.delete(UserInfo.TABLE_NAME, BasicColumns.ID + "=?",
					new String[] { id });
			break;
		case USER_ID:
			_id = uri.getPathSegments().get(2);
			count = db.delete(UserInfo.TABLE_NAME, BaseColumns._ID + "=?",
					new String[] { _id });
			break;
		case STATUSES:
			count = db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
			break;
		case STATUS_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.delete(StatusInfo.TABLE_NAME, BasicColumns.ID + "=?",
					new String[] { id });
			break;
		case STATUS_ID:
			_id = uri.getPathSegments().get(2);
			count = db.delete(StatusInfo.TABLE_NAME, StatusInfo._ID + "=?",
					new String[] { _id });
			break;
		case STATUS_ACTION_CLEAN:
			// count = cleanDatabase(uri, where, whereArgs);
			count = cleanAll();
			break;
		case MESSAGES:
			count = db.delete(DirectMessageInfo.TABLE_NAME, where, whereArgs);
			break;
		case MESSAGE_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.delete(DirectMessageInfo.TABLE_NAME,
					DirectMessageInfo.ID + "=?", new String[] { id });
			break;
		case MESSAGE_ID:
			_id = uri.getPathSegments().get(2);
			count = db.delete(DirectMessageInfo.TABLE_NAME,
					DirectMessageInfo._ID + "=?", new String[] { _id });
			break;
		default:
			throw new IllegalArgumentException("delete() Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private int cleanAll() {
		int result = 0;
		result += cleanHome();
		result += cleanMentions();
		result += cleanPublic();
		result += cleanUser();
		result += cleanFavorites();
		result += cleanOthers();
		return result;
	}

	/**
	 * 压缩数据库，删除旧消息
	 * 
	 * @param uri
	 * @param db
	 * @return
	 */
	private int cleanDatabase(Uri uri, String where, String[] whereArgs) {
		int type = Integer.parseInt(uri.getPathSegments().get(3));
		int result = -1;
		switch (type) {
		case Status.TYPE_HOME:
			result = cleanHome();
			break;
		case Status.TYPE_MENTION:
			result = cleanMentions();
			break;
		case Status.TYPE_PUBLIC:
			result = cleanPublic();
			break;
		case Status.TYPE_USER:
		case Status.TYPE_FAVORITES:
			if (whereArgs != null && whereArgs.length == 1
					&& !StringHelper.isEmpty(whereArgs[0])) {
				if (type == Status.TYPE_USER) {
					// user id
					result = cleanTimeline(whereArgs[0]);
				} else {
					// own id
					result = cleanFavorites(whereArgs[0]);
				}
			}
			break;
		default:
			break;
		}

		return result;
	}

	private int cleanUser() {
		String where = StatusInfo.TYPE + "=?";
		String[] whereArgs = new String[] { String.valueOf(Status.TYPE_USER), };
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanFavorites() {
		String where = StatusInfo.TYPE + "=?";
		String[] whereArgs = new String[] { String
				.valueOf(Status.TYPE_FAVORITES), };
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanPublic() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
		// + StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;
		//
		// where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_PUBLIC;
		//
		// where += " ORDER BY " + StatusInfo.CREATED_AT +
		// " DESC LIMIT 1 OFFSET "
		// + Commons.DATA_NORMAL_MAX + ")";
		//
		// where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_PUBLIC +
		// " ";
		//
		// // log("cleanPublic where: [" + where + "]");
		//
		//
		// return db.delete(StatusInfo.TABLE_NAME, where, null);
		String where = StatusInfo.TYPE + "=?";
		String[] whereArgs = new String[] { String.valueOf(Status.TYPE_PUBLIC) };
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanOthers() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = StatusInfo.TYPE + "=?";
		String[] whereArgs = new String[] { String.valueOf(Status.TYPE_NONE) };
		return db.delete(StatusInfo.TABLE_NAME, where, whereArgs);
	}

	private int cleanTimeline(String userId) {

		String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
				+ StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_USER;
		where += " AND " + StatusInfo.USER_ID + " = " + " '" + userId + "' ";

		where += " ORDER BY " + StatusInfo.CREATED_AT + " DESC LIMIT 1 OFFSET "
				+ Commons.DATA_NORMAL_MAX + ")";

		where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_USER + " ";
		where += " AND " + StatusInfo.USER_ID + " = " + " '" + userId + "' ";

		// log("cleanTimeline where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	private int cleanFavorites(String ownerId) {

		String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
				+ StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_FAVORITES;
		where += " AND " + StatusInfo.OWNER_ID + " = " + " '" + ownerId + "' ";

		where += " ORDER BY " + StatusInfo.CREATED_AT + " DESC LIMIT 1 OFFSET "
				+ Commons.DATA_NORMAL_MAX + ")";

		where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_FAVORITES
				+ " ";
		where += " AND " + StatusInfo.OWNER_ID + " = " + " '" + ownerId + "' ";

		// log("cleanFavorites where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);
	}

	private int cleanHome() {
		String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
				+ StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_HOME;

		where += " ORDER BY " + StatusInfo.CREATED_AT + " DESC LIMIT 1 OFFSET "
				+ Commons.DATA_NORMAL_MAX + ")";

		where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_HOME + " ";

		// log("cleanHome where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	private int cleanMentions() {

		String where = " " + StatusInfo.CREATED_AT + " < " + " (SELECT "
				+ StatusInfo.CREATED_AT + " FROM " + StatusInfo.TABLE_NAME;

		where += " WHERE " + StatusInfo.TYPE + " = " + Status.TYPE_MENTION;

		where += " ORDER BY " + StatusInfo.CREATED_AT + " DESC LIMIT 1 OFFSET "
				+ Commons.DATA_NORMAL_MAX + ")";

		where += " AND " + StatusInfo.TYPE + " = " + Status.TYPE_MENTION + " ";

		// log("cleanMentions where: [" + where + "]");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(StatusInfo.TABLE_NAME, where, null);

	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		// log("update() uri = " + uri + " where= (" + where + ") whereArgs = "
		// + StringHelper.toString(whereArgs));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		String id;
		String _id;
		switch (sUriMatcher.match(uri)) {
		case USER_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.update(UserInfo.TABLE_NAME, values, UserInfo.ID + "=?",
					new String[] { id });
			// count = db.update(UserInfo.TABLE_NAME, values,
			// UserInfo.ID
			// + "="
			// + userId
			// + (!TextUtils.isEmpty(where) ? " AND (" + where
			// + ')' : ""), whereArgs);
			break;
		case USER_ID:
			_id = uri.getPathSegments().get(2);
			id = _id;
			count = db.update(UserInfo.TABLE_NAME, values, UserInfo._ID + "=?",
					new String[] { _id });
			break;
		case STATUS_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.update(StatusInfo.TABLE_NAME, values, StatusInfo.ID
					+ "=?", new String[] { id });
			// count = db.update(StatusInfo.TABLE_NAME, values,
			// StatusInfo.ID
			// + "="
			// + statusId
			// + (!TextUtils.isEmpty(where) ? " AND (" + where
			// + ')' : ""), whereArgs);
			break;
		case STATUS_ID:
			_id = uri.getPathSegments().get(2);
			id = _id;
			count = db.update(StatusInfo.TABLE_NAME, values, StatusInfo._ID
					+ "=?", new String[] { _id });
			break;
		case MESSAGE_ITEM:
			id = uri.getPathSegments().get(2);
			count = db.update(DirectMessageInfo.TABLE_NAME, values,
					DirectMessageInfo.ID + "=?", new String[] { id });
			// count = db.update(
			// DirectMessageInfo.TABLE_NAME,
			// values,
			// DirectMessageInfo.ID
			// + "="
			// + messageId
			// + (!TextUtils.isEmpty(where) ? " AND (" + where
			// + ')' : ""), whereArgs);
			break;
		case MESSAGE_ID:
			_id = uri.getPathSegments().get(2);
			id = _id;
			count = db.update(DirectMessageInfo.TABLE_NAME, values,
					DirectMessageInfo._ID + "=?", new String[] { _id });
			break;
		case USERS:
			id = "";
			count = db.update(UserInfo.TABLE_NAME, values, where, whereArgs);
			break;
		case STATUSES:
			id = "";
			count = db.update(StatusInfo.TABLE_NAME, values, where, whereArgs);
			break;
		// case PUBLIC:
		// id = "";
		// count = db.update(StatusInfo.PUBLIC_TABLE_NAME, values, where,
		// whereArgs);
		// break;
		case MESSAGES:
			id = "";
			count = db.update(DirectMessageInfo.TABLE_NAME, values, where,
					whereArgs);
			break;
		default:
			throw new IllegalArgumentException("update() Unknown URI " + uri);
		}

		// log("update() notifyChange() rowId: " + id + " uri: " + uri);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}