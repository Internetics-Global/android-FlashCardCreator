package com.internectics.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.internectics.helper.FileOperationHelper;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Card {
	public int      cardID;
	public int      packID;
	public String coverImageUriFormatStr;
	public String   templateBackground;
	public int      cardSN;

	public Answer   answer;
	public Question question;
	
	public Card() {
		super();
		cardID = -1;
		cardSN = -1;
		packID = -1;
		templateBackground = "card_background_blue.png";
		coverImageUriFormatStr = FileOperationHelper.getCardCoverDefaultImagePath();
        question = new Question();
        answer = new Answer();
	}


	public Object initWithDictionary (HashMap<String, Object> dataDict) {
		cardID = (Integer) dataDict.get("card_id");
		packID = (Integer) dataDict.get("pack_id");
		coverImageUriFormatStr = (String) dataDict.get("cover_image");
		templateBackground = (String) dataDict.get("template_background");
		cardSN = (Integer) dataDict.get("card_sn");
		
		HashMap<String, Object> questionMap = (HashMap<String, Object>) dataDict.get("question");
        if (questionMap.size() == 0) {
            Log.d(Global.debugTag,"questionMap.size() is 0");
        } else {
            this.question = (new Question()).initWithDictionary(questionMap);
        }
		
		HashMap<String, Object> answerMap = (HashMap<String, Object>) dataDict.get("answer");
        if (answerMap.size() == 0) {
            Log.d(Global.debugTag,"answerMap.size() is 0");
        } else {
            this.answer = (new Answer()).initWithDictionary(answerMap);
        }
		
		return this;
	}
	
	
	public static ArrayList<HashMap<String, Object>> cardsForPackID (Context context,int packID) {
		ArrayList<HashMap<String, Object>> returnArray = new ArrayList<HashMap<String,Object>>();
		
		String queryString = String.format("SELECT * FROM Cards_Tables WHERE pack_id=%d", packID);
		Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
		while (cur.moveToNext()) {
			HashMap<String, Object> cardDict = new HashMap<String, Object>();
			cardDict.put("card_id", cur.getInt(0));
			cardDict.put("pack_id", cur.getInt(1));
			cardDict.put("cover_image", cur.getString(2));
			cardDict.put("template_background", cur.getString(3));
			cardDict.put("card_sn", cur.getInt(4));
			cardDict.put("question",Question.questionForCardID(context, cur.getInt(0)));
			cardDict.put("answer", Answer.answerForCardID(context, cur.getInt(0)));
			returnArray.add(cardDict);


		}
		return returnArray;
	}
	
	public void save(Context context) {
		if (cardID == -1) {
			insert(context);	
		} else {
		    if (SQLiteHelper.checkIntegerValueExists(context,cardID, "card_id", "Cards_Tables")) {
		    	update(context);
		    } else {
		    	insert(context);
		    }
		}
		
		this.question.cardID = this.cardID;
		this.answer.cardID = this.cardID;
		this.question.save(context);
		this.answer.save(context);
	}
	
	private void update(Context context) {
		String query = String.format("UPDATE Cards_Tables SET pack_id=%d, cover_image=\"%s\", template_background=\"%s\", card_sn=%d WHERE card_id=%d", packID, coverImageUriFormatStr, templateBackground, cardSN, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	private void insert(Context context) {
		if (cardID == -1) {
			cardID = (int)(System.currentTimeMillis()/1000L);
		}
       String query = String.format("INSERT INTO Cards_Tables(card_id, pack_id, cover_image, template_background, card_sn) VALUES (%d, %d, \"%s\", \"%s\", %d)",cardID, packID, coverImageUriFormatStr, templateBackground, cardSN);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
       
	}
	
	public void destroy(Context context) {
		//Step1: delete from database
		String query = String.format("DELETE FROM Cards_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);	
        
        //Step2: delete image resources
        if (!StringUtils.isNumeric(coverImageUriFormatStr)) {
        	File file = new File(this.coverImageUriFormatStr);
        	if (file.delete()) {
        		Log.d(Global.debugTag, "Successful to delete coverImageUriFormatStr file");
        	} else {
        		Log.d(Global.debugTag, "Fail to delete coverImageUriFormatStr file");
        	}
        }
        
        //Step3: We need to destroy all the data related in persistence
        this.answer.destroy(context);
        this.question.destroy(context);  
	}
}
