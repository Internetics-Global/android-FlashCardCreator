package com.internectics.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;

import java.io.File;
import java.util.HashMap;

public class Question {

    public int questionID;
    public int cardID;
    public String subheading;
    public String main;
    public String sub;
    public String imageUriFormatStr;
    public int cssID;
    public int templateID;

    public String backgroundImageUriFormatStr;
    public String movieUriFormatStr;
    public String audioUriFormatStr;

    public CSS css;

    public Question() {
        super();
        questionID = -1;
        cardID = -1;
        subheading = "";
        main = "";
        sub = "";
        imageUriFormatStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        cssID = -1;
        templateID = 0;
        css = new CSS(true);

        backgroundImageUriFormatStr = "";
        movieUriFormatStr = "";
        audioUriFormatStr = "";
    }

    public Question initWithDictionary(HashMap<String, Object> dataDict) {
        questionID = (Integer) dataDict.get("question_id");
        cardID = (Integer) dataDict.get("card_id");
        subheading = (String) dataDict.get("subheading");
        main = (String) dataDict.get("main");
        sub = (String) dataDict.get("sub");
        imageUriFormatStr = (String) dataDict.get("image");
        cssID = (Integer) dataDict.get("css_id");
        templateID = (Integer) dataDict.get("template_id");

        backgroundImageUriFormatStr = (String) dataDict.get("background_image");
        movieUriFormatStr = (String) dataDict.get("movie");
        audioUriFormatStr = (String) dataDict.get("audio");

        HashMap<String, Object> cssArray = (HashMap<String, Object>) dataDict.get("css");
        this.css = (new CSS(true)).initWithDictionary(cssArray);

        return this;
    }

    public static HashMap<String, Object> questionForCardID(Context context, int cardID) {
        HashMap<String, Object> questionDict = new HashMap<String, Object>();

        String queryString = String.format("SELECT * FROM Question_Tables WHERE card_id=%d", cardID);
        Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
        try {
            while (cur.moveToNext()) {
                questionDict.put("question_id", cur.getInt(0));
                questionDict.put("card_id", cur.getInt(1));
                questionDict.put("subheading", cur.getString(2));
                questionDict.put("main", cur.getString(3));
                questionDict.put("sub", cur.getString(4));
                questionDict.put("image", cur.getString(5));
                questionDict.put("css_id", cur.getInt(6));
                questionDict.put("template_id", cur.getInt(7));

                questionDict.put("background_image", cur.getString(8));
                questionDict.put("movie", cur.getString(9));
                questionDict.put("audio", cur.getString(10));

                questionDict.put("css", CSS.cssForCSSID(context, cur.getInt(6)));
                break;
            }
        }  finally {
            cur.close();
        }

        return questionDict;
    }

    public void save(Context context) {
        //CSS save first, since we need cssID for Answer
        css.save(context);
        this.cssID = css.cssID;

        if (questionID == -1) {
            insert(context);
        } else {
            if (SQLiteHelper.checkIntegerValueExists(context, questionID, "question_id", "Question_Tables")) {
                update(context);
            } else {
                insert(context);
            }
        }
    }

    private void update(Context context) {

        String decodedSubheading = StringUtils.stringDecodeForSQlite(subheading);
        String decodedMain = StringUtils.stringDecodeForSQlite(main);
        String decodedSub = StringUtils.stringDecodeForSQlite(sub);

        String query = String.format("UPDATE Question_Tables SET question_id=%d, subheading=?, main=?, sub=?, image=\"%s\",css_id=%d, template_id=%d,background_image=\"%s\",movie=\"%s\",audio=\"%s\" WHERE card_id=%d", questionID, imageUriFormatStr, cssID, templateID, backgroundImageUriFormatStr,movieUriFormatStr,audioUriFormatStr, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});
    }

    private void insert(Context context) {
        if (questionID == -1) {
            questionID = Global.generateNoRepeatInt();
        }

        String decodedSubheading = StringUtils.stringDecodeForSQlite(subheading);
        String decodedMain = StringUtils.stringDecodeForSQlite(main);
        String decodedSub = StringUtils.stringDecodeForSQlite(sub);

        String query = String.format("INSERT INTO Question_Tables(question_id, card_id, subheading, main, sub, image, css_id, template_id,background_image,movie,audio) VALUES (%d,%d, ?, ?, ?, \"%s\", %d, %d,\"%s\",\"%s\",\"%s\")", questionID, cardID, imageUriFormatStr, cssID, templateID,backgroundImageUriFormatStr,movieUriFormatStr,audioUriFormatStr);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});
    }

    public void destroy(Context context) {
        String query = String.format("DELETE FROM Question_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);

        if ((imageUriFormatStr != null) && (!StringUtils.isNumeric(imageUriFormatStr)) && (!imageUriFormatStr.contains("placeholder"))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.imageUriFormatStr));
            if (file.delete()) {
                Log.d(Global.debugTag, "Successful to delete imageUriFormatStr file in Question");
            } else {
                Log.w(Global.debugTag, "Fail to delete coverImageUriFormatStr file in Question");
            }
        }

        if ((backgroundImageUriFormatStr != null) && (!StringUtils.isNumeric(backgroundImageUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.backgroundImageUriFormatStr));
            if (file.delete()) {
                Log.d(Global.debugTag, "Successful to delete backgroundImageUriFormatStr file in Question");
            } else {
                Log.w(Global.debugTag, "Fail to delete backgroundImageUriFormatStr file in Question");
            }
        }

        if ((movieUriFormatStr != null) && (!StringUtils.isNumeric(movieUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.movieUriFormatStr));
            if (file.delete()) {
                Log.d(Global.debugTag, "Successful to delete movieUriFormatStr file in Question");
            } else {
                Log.w(Global.debugTag, "Fail to delete movieUriFormatStr file in Question");
            }
        }

        if ((audioUriFormatStr != null) && (!StringUtils.isNumeric(audioUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.audioUriFormatStr));
            if (file.delete()) {
                Log.d(Global.debugTag, "Successful to delete audioUriFormatStr file in Question");
            } else {
                Log.w(Global.debugTag, "Fail to delete audioUriFormatStr file in Question");
            }
        }
    }
}
