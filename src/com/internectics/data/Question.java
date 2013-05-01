package com.internectics.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.internectics.android_flashcardcreator.R;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;

import java.io.File;
import java.util.HashMap;

public class Question {
	
	public int       questionID;
	public int       cardID;
	public String    subheading;
	public String    main;
	public String    sub;
	public String    imageURL;
	public int       cssID;
	public int       templateID;
	
	public CSS       css;

	public Question() {
		super();
		questionID = -1;
		cardID = -1;
        subheading = "";
        main = "";
        sub = "";
        imageURL = String.format("%d", R.drawable.image_placeholder);
		cssID = -1;
		templateID = 0;
		css = new CSS();
	}

	public Question initWithDictionary (HashMap<String, Object> dataDict) {
		questionID = (Integer) dataDict.get("question_id");
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
	
	public static HashMap<String, Object> questionForCardID (Context context,int cardID) {
		HashMap<String, Object> questionDict = new HashMap<String, Object>();
		
		String queryString = String.format("SELECT * FROM Question_Tables WHERE card_id=%d", cardID);
		Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
		while (cur.moveToNext()) {
			questionDict.put("question_id", cur.getInt(0));
			questionDict.put("card_id", cur.getInt(1));
			questionDict.put("subheading", cur.getString(2));
			questionDict.put("main", cur.getString(3));
			questionDict.put("sub", cur.getString(4));
			questionDict.put("image", cur.getString(5));
			questionDict.put("css_id", cur.getInt(6));
			questionDict.put("template_id", cur.getInt(7));
			questionDict.put("css", CSS.cssForCSSID(context, cur.getInt(6)));
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
		String query = String.format("UPDATE Question_Tables SET question_id=%d, subheading=\"%s\", main=\"%s\", sub=\"%s\", image=\"%s\",css_id=%d, template_id=%d WHERE card_id=%d", questionID, subheading, main,sub, imageURL, cssID, templateID, cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	private void insert(Context context) {
		if (questionID == -1) {
			questionID = (int)(System.currentTimeMillis()/1000L);
		}
		String query = String.format("INSERT INTO Question_Tables(question_id, card_id, subheading, main, sub, image, css_id, template_id) VALUES (%d,%d, \"%s\", \"%s\", \"%s\", \"%s\", %d, %d)",questionID, cardID, subheading, main, sub, imageURL, cssID, templateID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	public void destroy(Context context) {
		String query = String.format("DELETE FROM Question_Tables WHERE card_id=%d", cardID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);	
        
        if (this.imageURL.indexOf("question_placeholder_logo.jpg") == -1) {
        	File file = new File(this.imageURL);
        	if (file.delete()) {
        		Log.d(Global.debugTag, "Successful to delete imageURL file in Question");
        	} else {
        		Log.d(Global.debugTag, "Fail to delete coverImageUriStr file in Question");
        	}
        }   
	}
}
