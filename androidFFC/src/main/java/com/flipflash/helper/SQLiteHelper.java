/*
 * we use AppContext.getAppContext() as all SQliteHelper method context
 */

package com.flipflash.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.flipflash.util.Global;

import static com.flipflash.util.LogUtils.LOGD;

/*
 * Sqlite operation
 */
public class SQLiteHelper {

    private static final String TAG = SQLiteHelper.class.getSimpleName();

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
        cursor.close();
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
        cursor.close();
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
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on Users_Tables(user_id)");

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
                    + "platform" + " TEXT,"
                    + "create_date" + " INTEGER,"
                    + "last_visit_date" + " INTEGER,"
                    + "creator_nick_name" + " TEXT,"
                    + "job_title" + " TEXT,"
                    + "auto_play_speed" + " INTEGER,"
                    + "restore_password" + " TEXT,"
                    + "share_link" + " TEXT,"
                    + "file_name_on_aws" + " TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on Packs_Tables(pack_id)");

            // compared with iOS version, we made changes:
            // 1. delete creator, card_name
            db.execSQL("CREATE TABLE " + "Cards_Tables" + " (" + "card_id"
                    + " INTEGER PRIMARY KEY," + "pack_id" + " INTEGER,"
                    + "cover_image" + " TEXT," + "template_background" + " TEXT,"
                    + "card_sn" + " INTEGER)");
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on Cards_Tables(card_id)");

            // compared with iOS version, we made changes:
            // 1. delete title, log and logo_url
            db.execSQL("CREATE TABLE " + "Question_Tables" + " (" + "question_id"
                    + " INTEGER PRIMARY KEY," + "card_id" + " INTEGER,"
                    + "subheading" + " TEXT," + "main" + " TEXT," + "sub"
                    + " TEXT," + "image" + " TEXT," + "image2" + " TEXT," + "css_id" + " INTEGER,"
                    + "template_id" + " INTEGER,"
                    + "line_number_subheading" + " INTEGER," + "line_number_main" + " INTEGER," + "line_number_sub" + " INTEGER,"
                    + "background_image" + " TEXT," + "movie" + " TEXT," + "movie2" + " TEXT," + "audio" + " TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on Question_Tables(question_id)");

            // compared with iOS version, we made changes:
            // 1. delete log and logo_url
            db.execSQL("CREATE TABLE " + "Answer_Tables" + " (" + "answer_id"
                    + " INTEGER PRIMARY KEY," + "card_id" + " INTEGER,"
                    + "subheading" + " TEXT," + "main" + " TEXT," + "sub"
                    + " TEXT," + "image" + " TEXT," + "image2" + " TEXT," + "css_id" + " INTEGER,"
                    + "template_id" + " INTEGER,"
                    + "line_number_subheading" + " INTEGER," + "line_number_main" + " INTEGER," + "line_number_sub" + " INTEGER,"
                    + "background_image" + " TEXT," + "movie" + " TEXT," + "movie2" + " TEXT," + "audio" + " TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on Answer_Tables(answer_id)");

            // compared with iOS version, we made changes:
            // 1.
            db.execSQL("CREATE TABLE " + "CSS_Tables" + " (" + "css_id"
                    + " INTEGER PRIMARY KEY," + "subheading_size" + " INTEGER,"
                    + "subheading_align" + " TEXT," + "subheading_color" + " TEXT,"
                    + "main_size" + " INTEGER," + "main_align" + " TEXT,"
                    + "main_color" + " TEXT," + "sub_size" + " INTEGER,"
                    + "sub_align" + " TEXT," + "sub_color" + " TEXT," + "subheading_font" + " TEXT," + "main_font" + " TEXT,"+ "sub_font" + " TEXT,"+ "subheading_align_vertical" + " TEXT,"+ "main_align_vertical" + " TEXT," + "sub_align_vertical" + " TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS IA on CSS_Tables(css_id)");


            //Build default user table
            String queryString = String.format(
                    "SELECT * FROM Users_Tables WHERE user_id=%d", Global.USER_ID);
            Cursor cursor = db.rawQuery(queryString, null);
            if (cursor.moveToNext()) {
                LOGD(TAG, "onCreate: default user has existed");
            } else {
                queryString = String
                        .format("INSERT INTO Users_Tables(user_id, nick_name) VALUES (%d,\"%s\")",
                                Global.USER_ID, Global.defaultUserStr);
                db.execSQL(
                        queryString);
            }
            cursor.close();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

}
