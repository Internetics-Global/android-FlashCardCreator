package com.flipflash.data;

import android.content.Context;
import android.database.Cursor;

import com.flipflash.helper.FileOperationHelper;
import com.flipflash.helper.SQLiteHelper;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;
import com.flipflash.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;
import static com.flipflash.util.LogUtils.LOGW;

public class Pack {
    private static final String TAG = Pack.class.getName();

    public int packID;
    public String packName;
    public String sidebarTitle;
    public int userID;
    public String questionTitle;
    public String answerTitle;
    public String coverImageUriFormatStr;
    public String logoImageUriFormatStr;
    public String logoURL;
    public String creatorID;
    public String creatorNickName;
    public String jobTitle;
    public String platform;

    public String restorePassword; //used to re-clain the ownership

    public String shareLink;
    public String fileNameOnAWS;

    public int    lastVistDate;
    public int    createDate;
    public int    autoPlaySpeed;

    public boolean isAllowShare;

    public ArrayList<Card> cards;

    public Pack() {
        super();
        packName = "";
        sidebarTitle = "";
        packID = -1;
        userID = -1;
        creatorID = "";
        creatorNickName = "";
        jobTitle = "";
        platform = "";
        cards = new ArrayList<Card>();
        questionTitle = "Question";
        answerTitle = "Answer";
        logoURL = "http://www.";
        restorePassword = "";
        shareLink = "";
        fileNameOnAWS = "";
        logoImageUriFormatStr = FileOperationHelper.getLogoPlaceholderImagePath();
        coverImageUriFormatStr = FileOperationHelper.getPackCoverDefaultImagePath();

        isAllowShare = true;
    }


    public Pack initWithDictionary(HashMap<String, Object> dataDict) {
        packID = (Integer) dataDict.get("pack_id");
        packName = (String) dataDict.get("pack_name");
        sidebarTitle = (String) dataDict.get("sidebar_title");
        userID = (Integer) dataDict.get("user_id");
        questionTitle = (String) dataDict.get("question_title");
        answerTitle = (String) dataDict.get("answer_title");
        coverImageUriFormatStr = (String) dataDict.get("cover_image");
        logoImageUriFormatStr = (String) dataDict.get("logo_image");
        logoURL = (String) dataDict.get("logo_url");
        creatorID = (String) dataDict.get("creator_id");
        creatorNickName = (String) dataDict.get("creator_nick_name");
        jobTitle = (String) dataDict.get("job_title");
        platform = (String) dataDict.get("platform");

        autoPlaySpeed = (Integer) dataDict.get("auto_play_speed");

        createDate = (Integer) dataDict.get("create_date");
        lastVistDate = (Integer) dataDict.get("last_visit_date");

        restorePassword = (String) dataDict.get("restore_password");

        shareLink =  (String) dataDict.get("share_link");
        fileNameOnAWS =  (String) dataDict.get("file_name_on_aws");

        ArrayList<HashMap<String, Object>> cardArray = (ArrayList<HashMap<String, Object>>) dataDict.get("cards");
        if (cardArray != null) {
            for (int i = 0; i < cardArray.size(); i++) {
                Card newCard = (Card) (new Card()).initWithDictionary(cardArray.get(i));
                cards.add(newCard);
            }

            Collections.sort(cards, new Comparator<Card>() {
                @Override
                public int compare(Card lhs, Card rhs) {
                    return (lhs.cardSN - rhs.cardSN);
                }
            });

            if ((cards.size() > 0) && (cards.get(0).cardSN != 1)) {
                LOGE(TAG, "initWithDictionary: Something is not right, cardSN should begin from 1");
            }
        } else {
            LOGW(TAG, "initWithDictionary: cardArray is null. You may have used the parameter of isSummary = true in User.defaultUser");
        }

        return this;
    }


    public static ArrayList<HashMap<String, Object>> packsForUserID(Context context, int userID, boolean isSummary) {
        ArrayList<HashMap<String, Object>> returnArray = new ArrayList<HashMap<String, Object>>();

        String queryString = String.format("SELECT * FROM Packs_Tables WHERE user_id=%d", userID);
        Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
        try {
            while (cur.moveToNext()) {
                HashMap<String, Object> cardDict = new HashMap<String, Object>();
                cardDict.put("pack_id", cur.getInt(0));
                cardDict.put("pack_name", cur.getString(1));
                cardDict.put("sidebar_title", cur.getString(2));
                cardDict.put("user_id", cur.getInt(3));
                cardDict.put("question_title", cur.getString(4));
                cardDict.put("answer_title", cur.getString(5));
                cardDict.put("cover_image", cur.getString(6));
                cardDict.put("logo_image", cur.getString(7));
                cardDict.put("logo_url", cur.getString(8));
                cardDict.put("creator_id", cur.getString(9));
                cardDict.put("platform", cur.getString(10));
                cardDict.put("create_date", cur.getInt(11));
                cardDict.put("last_visit_date", cur.getInt(12));
                cardDict.put("creator_nick_name", cur.getString(13));
                cardDict.put("job_title", cur.getString(14));
                cardDict.put("auto_play_speed", cur.getInt(15));
                cardDict.put("restore_password", cur.getString(16));
                cardDict.put("share_link", cur.getString(17));
                cardDict.put("file_name_on_aws", cur.getString(18));
                if (isSummary) {

                } else {
                    cardDict.put("cards", Card.cardsForPackID(context, cur.getInt(0)));
                }

                returnArray.add(cardDict);
            }
        } finally {
            cur.close();
        }
        return returnArray;
    }

    /**
     * Only save pack info
     * @param context
     */
    public void save(Context context) {
        if (packID == -1) {
            insert(context);
        } else {
            if (SQLiteHelper.checkIntegerValueExists(context, packID, "pack_id", "Packs_Tables")) {
                update(context);
            } else {
                insert(context);
            }
        }
    }


/*
No new card included
 */
    public void saveAllCards(Context context) {
        try {
            SQLiteHelper.defaultDatabase(context).beginTransaction();

            for (Card card:cards) {
                card.save(context);
            }

            SQLiteHelper.defaultDatabase(context).setTransactionSuccessful();
        } finally {
            SQLiteHelper.defaultDatabase(context).endTransaction();
        }
    }


    private void update(Context context) {
        String query = String.format("UPDATE Packs_Tables SET pack_name=\"%s\",sidebar_title=\"%s\",user_id= %d,question_title=\"%s\",answer_title=\"%s\",cover_image=\"%s\",logo_image=\"%s\",logo_url=\"%s\",creator_id= \"%s\",creator_nick_name=\"%s\",platform=\"%s\",create_date= %d,last_visit_date= %d,job_title=\"%s\",auto_play_speed= %d,restore_password=\"%s\",share_link=\"%s\",file_name_on_aws=\"%s\" WHERE pack_id=%d", packName, sidebarTitle, userID, questionTitle, answerTitle, coverImageUriFormatStr, logoImageUriFormatStr, logoURL, creatorID, creatorNickName, platform,createDate,lastVistDate, jobTitle, autoPlaySpeed,restorePassword,shareLink,fileNameOnAWS,packID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
    }

    private void insert(Context context) {

        if (packID == -1) {
            packID = Global.generateNoRepeatInt();
        }

        String query = String.format("INSERT INTO Packs_Tables(pack_id, pack_name, sidebar_title, user_id, question_title, answer_title, cover_image,logo_image,logo_url,creator_id,creator_nick_name,platform,create_date,last_visit_date,job_title,auto_play_speed,restore_password,share_link,file_name_on_aws) VALUES (%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",%d,\"%s\",\"%s\",\"%s\")", packID, packName, sidebarTitle, userID, questionTitle, answerTitle, coverImageUriFormatStr, logoImageUriFormatStr, logoURL, creatorID, creatorNickName, platform,createDate,lastVistDate,jobTitle,autoPlaySpeed,restorePassword,shareLink,fileNameOnAWS);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
    }

    public void destroy(Context context) {

        try {
            SQLiteHelper.defaultDatabase(context).beginTransaction();

            String query = String.format("DELETE FROM Packs_Tables WHERE pack_id=%d", packID);
            SQLiteHelper.defaultDatabase(context).execSQL(query);

            if ((!StringUtils.isNumeric(logoImageUriFormatStr))&&(!logoImageUriFormatStr.contains("placeholder"))) {
                File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.logoImageUriFormatStr));
                if (file.delete()) {
                    LOGD(TAG, "destroy: Successful to delete logoImageUriFormatStr file");
                } else {
                    LOGE(TAG, "destroy: Fail to delete logoImageUriFormatStr file");
                }
            }

            if ((!StringUtils.isNumeric(coverImageUriFormatStr))&&(!logoImageUriFormatStr.contains("placeholder"))) {
                File file = new File(FileOperationHelper.deleteUriSchemeHeader(this.coverImageUriFormatStr));
                if (file.delete()) {
                    LOGD(TAG, "destroy: Successful to delete coverImageUriFormatStr file in Pack");
                } else {
                    LOGE(TAG, "destroy: Fail to delete coverImageUriFormatStr file in Pack");
                }
            }

            for (Card card : cards) {
                card.destroy(AppContext.getAppContext());
            }

            SQLiteHelper.defaultDatabase(context).setTransactionSuccessful();
        } finally {
            SQLiteHelper.defaultDatabase(context).endTransaction();
        }

    }

    public void removeCard (Context context,Card card) {
        cards.remove(card);
        card.destroy(context);
    }

    public void addCard (Context context,Card card) {
        card.packID = packID;
        cards.add(card);
        card.save(context);
    }
}
