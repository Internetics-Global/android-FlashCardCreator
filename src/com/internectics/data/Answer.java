package com.internectics.data;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;

public class Answer {

	public int       answerID;
	public int       cardID;
	public String    subheading;
	public String    main;
	public String    sub;
	public String    imageURL;
	public int       cssID;
	public int       templateID;
	
	public CSS       css;
	
	public Answer() {
		super();
		answerID = -1;
		cardID = -1;
		cssID = -1;
		templateID = -1;
		css = new CSS();
	}

	public Answer initWithDictionary (HashMap<String, Object> dataDict) {
		answerID = (Integer) dataDict.get("answer_id");
		cardID = (Integer) dataDict.get("card_id");
		subheading = (String) dataDict.get("subheading");
		main = (String) dataDict.get("main");
		sub = (String) dataDict.get("sub");
		imageURL = (String) dataDict.get("image");
		cssID = (Integer) dataDict.get("css_id");
		templateID = (Integer) dataDict.get("template_id");
		
		HashMap<String, Object> cssArray = (HashMap<String, Object>) dataDict.get("css");
		this.css = (new CSS()).initWithDictionary(cssArray);
	
		return this;
	}
	
	public static HashMap<String, Object> answerForCardID (Context context,int cardID) {
		HashMap<String, Object> answerDict = new HashMap<String, Object>();
		
		String queryString = String.format("SELECT * FROM Answer_Tables WHERE card_id=%d", cardID);
		Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
		while (cur.moveToNext()) {
			answerDict.put("answer_id", cur.getInt(0));
			answerDict.put("card_id", cur.getInt(1));
			answerDict.put("subheading", cur.getString(2));
			answerDict.put("main", cur.getString(3));
			answerDict.put("sub", cur.getString(4));
			answerDict.put("image", cur.getString(5));
			answerDict.put("css_id", cur.getInt(6));
			answerDict.put("template_id", cur.getInt(7));
			answerDict.put("css", CSS.cssForCSSID(context, cur.getInt(6)));
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
		    if (SQLiteHelper.checkIntegerValueExists(context,answerID, "answer_id", "Answer_Tables")) {
		    	update(context);
		    } else {
		    	insert(context);
		    }
		}
		
	}
	
	private void update(Context context) {
		String query = String.format("UPDATE Answer_Tables SET answer_id=%d, subheading=\"%s\", main=\"%s\", sub=\"%s\", image=\"%s\",css_id=%d, template_id=%d WHERE card_id=%d", answerID, subheading, main,sub, imageURL, cssID, templateID, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	private void insert(Context context) {
		if (answerID == -1) {
			//it's not the best way to generate an random but not repeated, hope future to find a better way
			answerID = (int)(System.currentTimeMillis()/1000L);
		}
		String query = String.format("INSERT INTO Answer_Tables(answer_id, card_id, subheading, main, sub, image, css_id, template_id) VALUES (%d,%d, \"%s\", \"%s\", \"%s\", \"%s\", %d, %d)",answerID, cardID, subheading, main, sub, imageURL, cssID, templateID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
       
	}
	
	public void destroy(Context context) {
		String query = String.format("DELETE FROM Answer_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);	
        
        if (this.imageURL.indexOf("answer_placeholder_content.jpg") == -1) {
        	File file = new File(this.imageURL);
        	if (file.delete()) {
        		Log.d(Global.debugTag, "Successful to delete imageURL file in Answer");
        	} else {
        		Log.d(Global.debugTag, "Fail to delete coverImageURL file in Answer");	
        	}
        }
	}
}
