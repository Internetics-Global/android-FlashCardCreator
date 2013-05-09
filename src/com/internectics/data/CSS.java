package com.internectics.data;

import android.content.Context;
import android.database.Cursor;
import com.internectics.helper.SQLiteHelper;

import java.util.HashMap;

public class CSS {
	
	public int      cssID;
	public String   subheadingAlign;
	public String   subheadingColor;
	public int      subheadingSize;
	public String   mainAlign;
	public String   mainColor;
	public int      mainSize;
	public String   subAlign;
	public String   subColor;
	public int      subSize;
	
	public CSS() {
		super();
		cssID = -1;
		subheadingAlign = "Center";
		subheadingColor = "Black";
		mainAlign = "Center";
		mainColor = "Black";
		subAlign = "Center";
		subColor = "Black";
	}

	public CSS initWithDictionary (HashMap<String, Object> dataDict) {
		cssID = (Integer) dataDict.get("css_id");
		subheadingSize = (Integer) dataDict.get("subheading_size");
		subheadingAlign = (String) dataDict.get("subheading_align");
		subheadingColor = (String) dataDict.get("subheading_color");
		mainSize = (Integer) dataDict.get("main_size");
		mainAlign = (String) dataDict.get("main_align");
		mainColor = (String) dataDict.get("main_color");
		subSize = (Integer) dataDict.get("sub_size");
		subAlign = (String) dataDict.get("sub_align");
		subColor = (String) dataDict.get("sub_color");
		
		return this;
	}
	
	public static HashMap<String, Object> cssForCSSID (Context context,int cssID) {
		HashMap<String, Object> cssDict = new HashMap<String, Object>();
		String queryString = String.format("SELECT * FROM CSS_Tables WHERE css_id=%d",cssID);
		Cursor cur = SQLiteHelper.defaultDatabase(context).rawQuery(queryString, null);
		while (cur.moveToNext()) {
			cssDict.put("css_id", cur.getInt(0));
			cssDict.put("subheading_size", cur.getInt(1));
			cssDict.put("subheading_align", cur.getString(2));
			cssDict.put("subheading_color", cur.getString(3));
			cssDict.put("main_size", cur.getInt(4));
			cssDict.put("main_align", cur.getString(5));
			cssDict.put("main_color", cur.getString(6));
			cssDict.put("sub_size", cur.getInt(7));
			cssDict.put("sub_align", cur.getString(8));
			cssDict.put("sub_color", cur.getString(9));
		}
		return cssDict;
	}
	
	public void save(Context context) {
		if (cssID == -1) {
			insert(context);	
		} else {
		    if (SQLiteHelper.checkIntegerValueExists(context,cssID, "css_id", "CSS_Tables")) {
		    	update(context);
		    } else {
		    	insert(context);
		    }
		}	
	}
	
	private void update(Context context) {
		String query = String.format("UPDATE CSS_Tables SET subheading_size=%d, subheading_align=\"%s\", subheading_color=\"%s\", main_size=%d, main_align=\"%s\", main_color=\"%s\",sub_size=%d, sub_align=\"%s\", sub_color=\"%s\" WHERE css_id=%d", subheadingSize, subheadingAlign, subheadingColor,mainSize, mainAlign, mainColor, subSize, subAlign, subColor, cssID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);
	}
	
	private void insert(Context context) {
		
		if (cssID == -1) {
			cssID = SQLiteHelper.getMaxValueForColumn(context,"css_id", "CSS_Tables") + 1;
		}
		
		String query = String.format("INSERT INTO CSS_Tables(css_id, subheading_size, subheading_align, subheading_color, main_size, main_align, main_color, sub_size, sub_align, sub_color) VALUES (%d,%d, \"%s\", \"%s\", %d, \"%s\", \"%s\", %d, \"%s\", \"%s\")",cssID, subheadingSize, subheadingAlign, subheadingColor, mainSize, mainAlign, mainColor, subSize, subAlign, subColor);
        SQLiteHelper.defaultDatabase(context).execSQL(query);

    }
	
	public void destroy(Context context) {
		String query = String.format("DELETE FROM CSS_Tables WHERE css_id=%d", cssID);
        SQLiteHelper.defaultDatabase(context).execSQL(query);	
	}
}
