package com.internectics.data;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class Pack {
	public int packID;
	public String packName;
	public String sidebarTitle;
	public int userID;
	public String questionTitle;
	public String answerTitle;
	public String coverImageURL;
	public String logoImageURL;
	public String logoURL;
	public int creatorID;
	public String creatorNickName;

	public ArrayList<Card> cards;
	
	public Pack() {
		super();
		packID = -1;
		userID = -1;
		creatorID = -1;
		cards = new ArrayList<Card>();
		questionTitle = "Question";
		answerTitle = "Answer";
		logoImageURL= "http://www.";
	}


	public Pack initWithDictionary (HashMap<String, Object> dataDict) {
		packID = (Integer) dataDict.get("pack_id");
		packName = (String) dataDict.get("pack_name");
		sidebarTitle = (String) dataDict.get("sidebar_title");
		userID = (Integer) dataDict.get("user_id");
		questionTitle = (String) dataDict.get("question_title");
		answerTitle = (String) dataDict.get("answer_title");
		coverImageURL = (String) dataDict.get("cover_image");
		logoImageURL = (String) dataDict.get("logo_image");
		logoURL = (String) dataDict.get("logo_url");
		creatorID = (Integer) dataDict.get("creator_id");
		creatorNickName = (String) dataDict.get("creator_nick_name");
		
		ArrayList<HashMap<String, Object>> cardArray = (ArrayList<HashMap<String, Object>>) dataDict.get("cards");
		for (int i = 0; i < cardArray.size(); i++) {
		    Card newCard = (Card) (new Card()).initWithDictionary(cardArray.get(i));
		    cards.add(newCard);
		}
		
		return this;
	}
	
	
	public static ArrayList<HashMap<String, Object>> packsForUserID (Context context,int userID) {
		ArrayList<HashMap<String, Object>> returnArray = new ArrayList<HashMap<String,Object>>();
		
		String queryString = String.format("SELECT * FROM Question_Tables WHERE user_id=%d", userID);
		Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
		while (cur.moveToNext()) {
			HashMap<String, Object> cardDict = new HashMap<String, Object>();
			cardDict.put("card_id", cur.getInt(0));
			cardDict.put("pack_id", cur.getInt(1));
			cardDict.put("cover_image", cur.getString(2));
			cardDict.put("template_background", cur.getString(3));
			cardDict.put("card_sn", cur.getString(4));
			cardDict.put("cards",Card.cardsForPackID(context,cur.getInt(0)));
			returnArray.add(cardDict);
		}
		return returnArray;
	}
    
	public void save(Context context) {
		if (packID == -1) {
		    update(context);	
		} else {
		    if (SQLiteHelper.checkIntegerValueExists(context, packID, "pack_id", "Packs_Tables")) {
		    	update(context);
		    } else {
		    	insert(context);
		    }
		}	
	}
	
	
	private void update(Context context) {
		String query = String.format("UPDATE Packs_Tables SET pack_name=\"%s\",sidebar_title=\"%s\",user_id= %d,question_title=\"%s\",answer_title=\"%s\",cover_image=\"%s\",logo_image=\"%s\",logo_url=\"%s\",creator_id= %d,creator_nick_name=\"%s\" WHERE pack_id=%d", packName, sidebarTitle, userID,questionTitle, answerTitle, coverImageURL, logoImageURL, logoURL, creatorID, creatorNickName,packID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	private void insert(Context context) {
		
		if (packID == -1) {
			packID = (int)(System.currentTimeMillis()/1000L);
		}
		
		String query = String.format("INSERT INTO Packs_Tables(pack_id, pack_name, sidebar_title, user_id, question_title, answer_title, cover_image,logo_image,logo_url,creator_id,creator_nick_name) VALUES (%d,%d, \"%s\", \"%s\", \"%s\", \"%s\", %d, %d)",packID,packName,sidebarTitle,userID,questionTitle,answerTitle,coverImageURL,logoImageURL,logoURL,creatorID,creatorNickName);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	public void destroy(Context context) {
		String query = String.format("DELETE FROM Packs_Tables WHERE pack_id=%d", packID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);	
        
        if (this.logoImageURL.indexOf("card_logo.jpg") == -1) {
        	File file = new File(this.logoImageURL);
        	if (file.delete()) {
        		Log.d(Global.debugTag, "Successful to delete logoImageURL file");
        	} else {
        		Log.d(Global.debugTag, "Fail to delete logoImageURL file");	
        	}
        }
        
        if (this.coverImageURL.indexOf("default_pack_cover_image.jpg") == -1) {
        	File file = new File(this.coverImageURL);
        	if (file.delete()) {
        		Log.d(Global.debugTag, "Successful to delete coverImageURL file in Pack");
        	} else {
        		Log.d(Global.debugTag, "Fail to delete coverImageURL file in Pack");	
        	}
        }
	}
}
