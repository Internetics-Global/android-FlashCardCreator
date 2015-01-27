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

import timber.log.Timber;

public class Answer {

    public int answerID;
    public int cardID;
    public String subheading;
    public String main;
    public String sub;
    public String imageUriFormatStr;
    public String imageUriFormatStr2;
    public int cssID;
    public int templateID;

    public int lineNoSubheading;
    public int lineNoMain;
    public int lineNoSub;

    public String backgroundImageUriFormatStr;
    public String movieUriFormatStr;
    public String movieUriFormatStr2;

    public String audioUriFormatStr;

    public CSS css;

    public Answer() {
        super();
        answerID = -1;
        cardID = -1;
        subheading = "";
        main = "";
        sub = "";
        imageUriFormatStr = FileOperationHelper.getAnswerImagePlaceholderImagePath();
        imageUriFormatStr2 = FileOperationHelper.getAnswerImagePlaceholderImagePath();
        cssID = -1;
        templateID = 0;
        css = new CSS(false);

        lineNoSubheading = 0;
        lineNoMain = 0;
        lineNoSub = 0;

        backgroundImageUriFormatStr = "";

        movieUriFormatStr = "";
        movieUriFormatStr2 = "";

        audioUriFormatStr = "";
    }

    public Answer initWithDictionary(HashMap<String, Object> dataDict) {
        answerID = (Integer) dataDict.get("answer_id");
        cardID = (Integer) dataDict.get("card_id");
        subheading = (String) dataDict.get("subheading");
        main = (String) dataDict.get("main");
        sub = (String) dataDict.get("sub");
        imageUriFormatStr = (String) dataDict.get("image");
        imageUriFormatStr2 = (String) dataDict.get("image2");
        cssID = (Integer) dataDict.get("css_id");
        templateID = (Integer) dataDict.get("template_id");

        lineNoSubheading = (Integer) dataDict.get("line_number_subheading");
        lineNoMain = (Integer) dataDict.get("line_number_main");
        lineNoSub = (Integer) dataDict.get("line_number_sub");

        backgroundImageUriFormatStr = (String) dataDict.get("background_image");
        movieUriFormatStr = (String) dataDict.get("movie");
        movieUriFormatStr2 = (String) dataDict.get("movie2");

        audioUriFormatStr = (String) dataDict.get("audio");

        HashMap<String, Object> cssArray = (HashMap<String, Object>) dataDict.get("css");
        this.css = (new CSS(false)).initWithDictionary(cssArray);

        return this;
    }

    public static HashMap<String, Object> answerForCardID(Context context, int cardID) {
        HashMap<String, Object> answerDict = new HashMap<String, Object>();

        String queryString = String.format("SELECT * FROM Answer_Tables WHERE card_id=%d", cardID);
        Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
        try {
            while (cur.moveToNext()) {
                answerDict.put("answer_id", cur.getInt(0));
                answerDict.put("card_id", cur.getInt(1));
                answerDict.put("subheading", cur.getString(2));
                answerDict.put("main", cur.getString(3));
                answerDict.put("sub", cur.getString(4));
                answerDict.put("image", cur.getString(5));
                answerDict.put("image2", cur.getString(6));
                answerDict.put("css_id", cur.getInt(7));
                answerDict.put("template_id", cur.getInt(8));

                answerDict.put("line_number_subheading", cur.getInt(9));
                answerDict.put("line_number_main", cur.getInt(10));
                answerDict.put("line_number_sub", cur.getInt(11));

                answerDict.put("background_image", cur.getString(12));
                answerDict.put("movie", cur.getString(13));
                answerDict.put("movie2", cur.getString(14));
                answerDict.put("audio", cur.getString(15));

                answerDict.put("css", CSS.cssForCSSID(context, cur.getInt(7)));
                break;
            }
        } finally {
            cur.close();
        }

        return answerDict;
    }

    public void save(Context context) {

        //CSS save first, since we need cssID for Answer
        css.save(context);
        this.cssID = css.cssID;

        if (answerID == -1) {
            insert(context);
        } else {
            if (SQLiteHelper.checkIntegerValueExists(context, answerID, "answer_id", "Answer_Tables")) {
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

        String query = String.format("UPDATE Answer_Tables SET answer_id=%d, subheading=?, main=?, sub=?, image=\"%s\",image2=\"%s\",css_id=%d, template_id=%d, line_number_subheading=%d, line_number_main=%d, line_number_sub=%d, background_image=\"%s\",movie=\"%s\",movie2=\"%s\",audio=\"%s\" WHERE card_id=%d", answerID, imageUriFormatStr,imageUriFormatStr2, cssID, templateID,lineNoSubheading,lineNoMain,lineNoSub,backgroundImageUriFormatStr,movieUriFormatStr,movieUriFormatStr2,audioUriFormatStr, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});
    }

    private void insert(Context context) {
        if (answerID == -1) {
            //it's not the best way to generate an random but not repeated, hope future to find a better way
            answerID = Global.generateNoRepeatInt();
        }

        String decodedSubheading = StringUtils.stringDecodeForSQlite(subheading);
        String decodedMain = StringUtils.stringDecodeForSQlite(main);
        String decodedSub = StringUtils.stringDecodeForSQlite(sub);

        String query = String.format("INSERT INTO Answer_Tables(answer_id, card_id, subheading, main, sub, image, image2, css_id, template_id,line_number_subheading,line_number_main,line_number_sub,background_image,movie, movie2, audio) VALUES (%d,%d, ?, ?, ?, \"%s\",\"%s\", %d, %d, %d, %d, %d,\"%s\",\"%s\",\"%s\",\"%s\")", answerID, cardID, imageUriFormatStr, imageUriFormatStr2, cssID, templateID,lineNoSubheading,lineNoMain,lineNoSub,backgroundImageUriFormatStr,movieUriFormatStr, movieUriFormatStr2, audioUriFormatStr);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});

    }

    public void destroy(Context context) {
        String query = String.format("DELETE FROM Answer_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);

        if ((imageUriFormatStr != null) && (!StringUtils.isNumeric(imageUriFormatStr)) && (!imageUriFormatStr.contains("placeholder"))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.imageUriFormatStr));
            if (file.delete()) {
                //Timber.d(Global.debugTag, "Successful to delete imageUriFormatStr file in Answer");
            } else {
                Timber.e(Global.debugTag, "Fail to delete imageUriFormatStr file in Answer:" + file);
            }
        }

        if ((imageUriFormatStr2 != null) && (!StringUtils.isNumeric(imageUriFormatStr2)) && (!imageUriFormatStr2.contains("placeholder"))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.imageUriFormatStr2));
            if (file.delete()) {
                //Timber.d(Global.debugTag, "Successful to delete imageUriFormatStr2 file in Answer");
            } else {
                Timber.e(Global.debugTag, "Fail to delete imageUriFormatStr2 file in Answer:" + file);
            }
        }

        if ((backgroundImageUriFormatStr != null) && (!StringUtils.isNumeric(backgroundImageUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.backgroundImageUriFormatStr));
            if (file.delete()) {
                Timber.d(Global.debugTag, "Successful to delete backgroundImageUriFormatStr file in Question");
            } else {
                Timber.w(Global.debugTag, "Fail to delete backgroundImageUriFormatStr file in Question");
            }
        }

        if ((movieUriFormatStr != null) && (!StringUtils.isNumeric(movieUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.movieUriFormatStr));
            if (file.delete()) {
                Timber.d(Global.debugTag, "Successful to delete movieUriFormatStr file in Question");
            } else {
                Timber.w(Global.debugTag, "Fail to delete movieUriFormatStr file in Question");
            }
        }

        if ((movieUriFormatStr2 != null) && (!StringUtils.isNumeric(movieUriFormatStr2))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.movieUriFormatStr2));
            if (file.delete()) {
                Timber.d(Global.debugTag, "Successful to delete movieUriFormatStr2 file in Question");
            } else {
                Timber.w(Global.debugTag, "Fail to delete movieUriFormatStr2 file in Question");
            }
        }

        if ((audioUriFormatStr != null) && (!StringUtils.isNumeric(audioUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.audioUriFormatStr));
            if (file.delete()) {
                Timber.d(Global.debugTag, "Successful to delete audioUriFormatStr file in Question");
            } else {
                Timber.w(Global.debugTag, "Fail to delete audioUriFormatStr file in Question");
            }
        }

    }
}
