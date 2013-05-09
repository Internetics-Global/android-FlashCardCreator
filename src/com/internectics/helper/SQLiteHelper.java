/*
 * we use AppContext.getAppContext() as all SQliteHelper method context
 */

package com.internectics.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.internectics.util.Global;

/*
 * Sqlite operation
 */
public class SQLiteHelper {

    private static DBOpenHelper dbOpenHelper = null;

    public static SQLiteDatabase defaultDatabase(Context context) {
        if (dbOpenHelper == null) {
            dbOpenHelper = new DBOpenHelper(context, Global.DATABASE_NAME,
                    null, Global.DATABASE_VERSION);
        }
        return dbOpenHelper.getWritableDatabase();
    }

    public static void closeDatabase() {
        dbOpenHelper.close();
    }

    public static Boolean checkIntegerValueExists(Context context, int value,
                                                  String columnName, String tableName) {
        String queryString = String.format("SELECT * FROM %s WHERE %s=%d",
                tableName, columnName, value);
        Cursor cursor = SQLiteHelper.defaultDatabase(context).rawQuery(
                queryString, null);
        Boolean isExistBoolean = (cursor.getCount()) > 0 ? true : false;
        return isExistBoolean;
    }

    public static int getMaxValueForColumn(Context context, String columnName,
                                           String tableName) {
        String queryString = String.format("SELECT max(%s) FROM %s",
                columnName, tableName);
        Cursor cursor = SQLiteHelper.defaultDatabase(context).rawQuery(
                queryString, null);
        int number = -1;
        while (cursor.moveToNext()) {
            number = cursor.getInt(0);
        }
        return number;
    }

    private static class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + "Users_Tables" + " (" + "user_id"
                    + " INTEGER PRIMARY KEY, " + "nick_name" + " TEXT)");

            // compare with iOS version, we made changes:
            // 1. delete is_public, language_name, 2. add question_title,
            // answer_title, logo_image and logo_url;3. change creator to
            // creator_id, logo to logo_image
            db.execSQL("CREATE TABLE " + "Packs_Tables" + " (" + "pack_id"
                    + " INTEGER PRIMARY KEY," + "pack_name" + " TEXT,"
                    + "sidebar_title" + " TEXT," + "user_id" + " INTEGER,"
                    + "question_title" + " TEXT," + "answer_title" + " TEXT,"
                    + "cover_image" + " TEXT," + "logo_image" + " TEXT,"
                    + "logo_url" + " TEXT," + "creator_id" + " TEXT,"
                    + "creator_nick_name" + " TEXT)");

            // compared with iOS version, we made changes:
            // 1. delete creator, card_name
            db.execSQL("CREATE TABLE " + "Cards_Tables" + " (" + "card_id"
                    + " INTEGER PRIMARY KEY," + "pack_id" + " INTEGER,"
                    + "cover_image" + " TEXT," + "template_background" + " TEXT,"
                    + "card_sn" + " INTEGER)");

            // compared with iOS version, we made changes:
            // 1. delete title, log and logo_url
            db.execSQL("CREATE TABLE " + "Question_Tables" + " (" + "question_id"
                    + " INTEGER PRIMARY KEY," + "card_id" + " INTEGER,"
                    + "subheading" + " TEXT," + "main" + " TEXT," + "sub"
                    + " TEXT," + "image" + " TEXT," + "css_id" + " INTEGER,"
                    + "template_id" + " INTEGER)");

            // compared with iOS version, we made changes:
            // 1. delete log and logo_url
            db.execSQL("CREATE TABLE " + "Answer_Tables" + " (" + "answer_id"
                    + " INTEGER PRIMARY KEY," + "card_id" + " INTEGER,"
                    + "subheading" + " TEXT," + "main" + " TEXT," + "sub"
                    + " TEXT," + "image" + " TEXT," + "css_id" + " INTEGER,"
                    + "template_id" + " INTEGER)");

            // compared with iOS version, we made changes:
            // 1.
            db.execSQL("CREATE TABLE " + "CSS_Tables" + " (" + "css_id"
                    + " INTEGER PRIMARY KEY," + "subheading_size" + " INTEGER,"
                    + "subheading_align" + " TEXT," + "subheading_color" + " TEXT,"
                    + "main_size" + " INTEGER," + "main_align" + " TEXT,"
                    + "main_color" + " TEXT," + "sub_size" + " INTEGER,"
                    + "sub_align" + " TEXT," + "sub_color" + " TEXT)");


            //Build default user table
            String queryString = String.format(
                    "SELECT * FROM Users_Tables WHERE user_id=%d", Global.USER_ID);
            Cursor cursor = db.rawQuery(queryString, null);
            if (cursor.moveToNext()) {
                Log.d(Global.debugTag, "default user has existed");
            } else {
                queryString = String
                        .format("INSERT INTO Users_Tables(user_id, nick_name) VALUES (%d,\"%s\")",
                                Global.USER_ID, Global.defaultUserStr);
                db.execSQL(
                        queryString);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

}
