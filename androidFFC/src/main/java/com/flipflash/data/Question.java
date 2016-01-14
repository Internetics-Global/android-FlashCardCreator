package com.flipflash.data;

import android.content.Context;
import android.database.Cursor;

import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.SQLiteHelper;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;

import java.io.File;
import java.util.HashMap;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

public class Question {
    private static final String TAG = Question.class.getSimpleName();

    public int questionID;
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

    public Question() {
        super();
        questionID = -1;
        cardID = -1;
        subheading = "";
        main = "";
        sub = "";
        imageUriFormatStr = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        imageUriFormatStr2 = FileOperationHelper.getQuestionImagePlaceholderImagePath();
        cssID = -1;
        templateID = 0;
        css = new CSS(true);

        lineNoSubheading = 0;
        lineNoMain = 0;
        lineNoSub = 0;

        backgroundImageUriFormatStr = "";

        movieUriFormatStr = "";
        movieUriFormatStr2 = "";

        audioUriFormatStr = "";
    }

    public Question initWithDictionary(HashMap<String, Object> dataDict) {
        questionID = (Integer) dataDict.get("question_id");
        cardID = (Integer) dataDict.get("card_id");
        subheading = (String) dataDict.get("subheading");
        main = (String) dataDict.get("main");
        sub = (String) dataDict.get("sub");
        imageUriFormatStr = (String) dataDict.get("image");
        if (imageUriFormatStr == null) {
            imageUriFormatStr = "";
        }
        imageUriFormatStr2 = (String) dataDict.get("image2");
        if (imageUriFormatStr2 == null) {
            imageUriFormatStr2 = "";
        }
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
                questionDict.put("image2", cur.getString(6));
                questionDict.put("css_id", cur.getInt(7));
                questionDict.put("template_id", cur.getInt(8));

                questionDict.put("line_number_subheading", cur.getInt(9));
                questionDict.put("line_number_main", cur.getInt(10));
                questionDict.put("line_number_sub", cur.getInt(11));

                questionDict.put("background_image", cur.getString(12));
                questionDict.put("movie", cur.getString(13));
                questionDict.put("movie2", cur.getString(14));
                questionDict.put("audio", cur.getString(15));

                questionDict.put("css", CSS.cssForCSSID(context, cur.getInt(7)));
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

        String query = String.format("UPDATE Question_Tables SET question_id=%d, subheading=?, main=?, sub=?, image=\"%s\",image2=\"%s\",css_id=%d, template_id=%d, line_number_subheading=%d, line_number_main=%d, line_number_sub=%d ,background_image=\"%s\",movie=\"%s\",movie2=\"%s\",audio=\"%s\" WHERE card_id=%d", questionID, imageUriFormatStr,imageUriFormatStr2, cssID, templateID,lineNoSubheading,lineNoMain,lineNoSub, backgroundImageUriFormatStr,movieUriFormatStr,movieUriFormatStr2,audioUriFormatStr, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});
    }

    private void insert(Context context) {
        if (questionID == -1) {
            questionID = Global.generateNoRepeatInt();
        }

        String decodedSubheading = StringUtils.stringDecodeForSQlite(subheading);
        String decodedMain = StringUtils.stringDecodeForSQlite(main);
        String decodedSub = StringUtils.stringDecodeForSQlite(sub);

        String query = String.format("INSERT INTO Question_Tables(question_id, card_id, subheading, main, sub, image,image2, css_id, template_id,line_number_subheading,line_number_main,line_number_sub,background_image,movie,movie2,audio) VALUES (%d,%d, ?, ?, ?, \"%s\", \"%s\", %d, %d,%d,%d,%d,\"%s\",\"%s\",\"%s\",\"%s\")", questionID, cardID, imageUriFormatStr, imageUriFormatStr2, cssID, templateID,lineNoSubheading,lineNoMain,lineNoSub,backgroundImageUriFormatStr,movieUriFormatStr,movieUriFormatStr2,audioUriFormatStr);
        SQLiteHelper.defaultDatabase(context).execSQL(query, new Object[]{decodedSubheading, decodedMain, decodedSub});
    }

    public void destroy(Context context) {
        String query = String.format("DELETE FROM Question_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);

        css.destroy(context);

        if ((imageUriFormatStr != null) && (!StringUtils.isNumeric(imageUriFormatStr)) && (!imageUriFormatStr.contains("placeholder"))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.imageUriFormatStr));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete imageUriFormatStr file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete imageUriFormatStr file in Question");
            }
        }

        if ((imageUriFormatStr2 != null) && (!StringUtils.isNumeric(imageUriFormatStr2)) && (!imageUriFormatStr2.contains("placeholder"))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.imageUriFormatStr2));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete imageUriFormatStr2 file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete imageUriFormatStr2 file in Question");
            }
        }

        if ((backgroundImageUriFormatStr != null) && (!StringUtils.isNumeric(backgroundImageUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.backgroundImageUriFormatStr));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete backgroundImageUriFormatStr file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete backgroundImageUriFormatStr file in Question");
            }
        }

        if ((movieUriFormatStr != null) && (!StringUtils.isNumeric(movieUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.movieUriFormatStr));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete movieUriFormatStr file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete movieUriFormatStr file in Question");
            }
        }

        if ((movieUriFormatStr2 != null) && (!StringUtils.isNumeric(movieUriFormatStr2))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.movieUriFormatStr2));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete movieUriFormatStr2 file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete movieUriFormatStr2 file in Question");
            }
        }

        if ((audioUriFormatStr != null) && (!StringUtils.isNumeric(audioUriFormatStr))) {
            File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.audioUriFormatStr));
            if (file.delete()) {
                LOGD(TAG, "destroy: Successful to delete audioUriFormatStr file in Question");
            } else {
                LOGE(TAG, "destroy: Fail to delete audioUriFormatStr file in Question");
            }
        }
    }
}
