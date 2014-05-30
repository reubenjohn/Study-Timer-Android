package com.reubenjohn.studytimer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.reubenjohn.studytimer.timming.Time;

public class StudyTimerDBManager {

	private final Context context;

	private DBHelper helper;
	private SQLiteDatabase DB;
	private LapDBManager lapDB;

	public StudyTimerDBManager(Context context) {
		this.context = context;
	}

	private static class properties {
		public final static String DATABASE_NAME = "StudyTimer.db";
		public final static int DATABASE_VERSION = 2;
	}

	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, properties.DATABASE_NAME, null,
					properties.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("StudyTimerDB",
					"execSQL(" + LapDBProperties.commands.createTable() + ")");
			db.execSQL(LapDBProperties.commands.createTable());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				if (newVersion == 1)
					break;
			case 2:
				reset();
				if (newVersion == 2)
					break;
			case 3:
				if (newVersion == 3)
					break;
			case 4:
				if (newVersion == 4)
					break;
			case 5:
				if (newVersion == 5)
					break;
			case 6:
				if (newVersion == 6)
					break;
			}
			reset();
		}

		protected void destroyTable(SQLiteDatabase db) {
			db.execSQL(LapDBProperties.commands.dropTable());
			onCreate(db);
		}

		public void reset() {
			SQLiteDatabase db = getWritableDatabase();
			Log.d("StudyTimerDB",
					"execSQL(" + LapDBProperties.commands.createTable() + ")");
			destroyTable(db);
		}

	}

	public static class LapDBProperties {
		public static String tableName(int DBVersion) {
			switch (DBVersion) {
			case 1:
			case 2:
			case 3:
				return "laps";

			default:
				return "laps";
			}
		}

		public static String tableName() {
			return tableName(properties.DATABASE_VERSION);
		}

		public static final class keys {
			public static final String ROWID = "_id";
			public static final String DURATION = "duration";
			public static final String ELAPSE = "elapse_duration";
		}

		public static final class columns {

			public static String[] all(int DBVersion) {
				switch (DBVersion) {
				case 1:
				case 2:
					return new String[] { keys.ROWID, keys.DURATION,
							keys.ELAPSE };
				case 3:
					return new String[] { keys.ROWID, keys.DURATION };

				default:
					return new String[] { keys.ROWID, keys.DURATION };
				}
			}

			public static String[] all() {
				return all(properties.DATABASE_VERSION);
			}

			public static String[] listViewColumns(int DBVersion) {
				switch (DBVersion) {
				case 1:
				case 2:
					return new String[] { LapDBProperties.keys.ROWID,
							LapDBProperties.keys.DURATION,
							LapDBProperties.keys.ELAPSE };
				case 3:
					return new String[] { LapDBProperties.keys.ROWID,
							LapDBProperties.keys.ELAPSE };

				default:
					return new String[] { keys.ROWID, keys.DURATION };
				}
			}

			public static String[] listViewColumns() {
				return listViewColumns(properties.DATABASE_VERSION);
			}
		}

		public static final class commands {
			public static String createTable(int DBVersion) {
				String result = "CREATE TABLE if not exists ";
				switch (DBVersion) {
				case 1:
				case 2:
					result += tableName(DBVersion) + "(" + keys.ROWID
							+ " integer PRIMARY KEY autoincrement,"
							+ keys.DURATION + " TEXT NOT NULL," + keys.ELAPSE
							+ " integer" + ");";
					break;
				case 3:
					break;

				default:
					result += tableName(DBVersion) + "(" + keys.ROWID
							+ " integer PRIMARY KEY autoincrement,"
							+ keys.DURATION + " TEXT NOT NULL," + keys.ELAPSE
							+ " integer" + ");";
				}
				return result;
			}

			public static String createTable() {
				return createTable(properties.DATABASE_VERSION);
			}

			public static String dropTable(int DBVersion) {
				return "DROP TABLE IF EXISTS " + tableName(DBVersion);
			}

			public static String dropTable() {
				return dropTable(properties.DATABASE_VERSION);
			}

			public static String insertInto(int destinationTableversion,
					String fromTable) {
				String insertIntoPrefix = null;
				switch (destinationTableversion) {
				case 3:
					insertIntoPrefix = "insert into "
							+ LapDBProperties.tableName(3)
							+ getFormattedStringArrayElements("(",
									LapDBProperties.columns.all(), ")")
							+ " select "
							+ getFormattedStringArrayElements("",
									LapDBProperties.columns.all(), "")
							+ " from " + fromTable;
					break;
				default:
					return null;
				}
				return insertIntoPrefix;
			}

			private static final String addToEachPrefix = "update "
					+ tableName() + " set " + keys.ELAPSE + "=" + keys.ELAPSE
					+ "+";
		}

		private static final String countQuery = "SELECT  * FROM "
				+ tableName();

	}

	private class LapDBManager {

	}

	public StudyTimerDBManager open() {
		helper = new DBHelper(context);
		DB = helper.getWritableDatabase();
		lapDB = new LapDBManager();
		return StudyTimerDBManager.this;
	}

	public void close() {
		if (helper != null) {
			helper.close();
		}
	}

	public long addLap(String duration, int elapse_duration) {
		ContentValues val = new ContentValues();
		val.put(LapDBProperties.keys.DURATION, duration);
		val.put(LapDBProperties.keys.ELAPSE, elapse_duration);

		Log.d("StudyTimerDB", "insert(" + LapDBProperties.tableName()
				+ ",null, " + val + ")");
		return DB.insert(LapDBProperties.tableName(), null, val);
	}

	public int getAverage() {
		Log.d("StudyTimerDB",
				"rawQuery(\"SELECT CAST(avg(" + LapDBProperties.keys.ELAPSE
						+ ") AS INTEGER) AS " + LapDBProperties.keys.ELAPSE
						+ " from " + LapDBProperties.tableName() + ", null)\"");
		Cursor cursor = DB.rawQuery(
				"SELECT CAST(avg(" + LapDBProperties.keys.ELAPSE
						+ ") AS INTEGER) AS " + LapDBProperties.keys.ELAPSE
						+ " from " + LapDBProperties.tableName(), null);
		cursor.moveToFirst();
		return (int) cursor.getLong(0);
	}

	public String getFormattedAverage() {
		int average = getAverage();
		return Time.getFormattedTime(average);
	}

	public Cursor fetchAllLaps() {
		Log.d("StudyTimerDB",
				"query("
						+ LapDBProperties.tableName()
						+ ", "
						+ getFormattedStringArrayElements("{",
								LapDBProperties.columns.listViewColumns(), "}")
						+ " , null, null, null, null, "
						+ LapDBProperties.keys.ROWID + " DESC)");
		Cursor cursor = DB.query(LapDBProperties.tableName(),
				LapDBProperties.columns.listViewColumns(), null, null, null,
				null, LapDBProperties.keys.ROWID + " DESC");
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public int getLapCount() {
		if (DB != null) {
			Log.d("StudyTimerDB", "rawQuery(\"" + LapDBProperties.countQuery
					+ "\", null)");
			Cursor cursor = DB.rawQuery(LapDBProperties.countQuery, null);
			int cnt = cursor.getCount();
			cursor.close();
			return cnt;
		} else
			return -1;
	}

	protected static String getFormattedStringArrayElements(String prefix,
			String[] array, String postFix) {
		String result = prefix;
		for (String s : array) {
			result += s + ", ";
		}
		result = result.substring(0, (result.length() - 2));
		return result += postFix;
	}

	public void reset() {
		Log.d("StudyTimer", "Database reset");
		helper.reset();
	}

	public void distributeToLaps(long elapse) {
		long induvidualContribution = elapse / getAverage();
		addToEachLap(induvidualContribution);
	}

	public void addToEachLap(long induvidualContribution) {
		if (DB != null) {
			try {
				Log.d("StudyTimerDB", LapDBProperties.commands.addToEachPrefix
						+ induvidualContribution);
				DB.execSQL(LapDBProperties.commands.addToEachPrefix
						+ induvidualContribution);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
