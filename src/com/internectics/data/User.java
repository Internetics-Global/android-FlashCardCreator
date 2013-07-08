package com.internectics.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.internectics.helper.SQLiteHelper;
import com.internectics.util.Global;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    public int userID;
    public String nickName;
    public ArrayList<Pack> packs;
    public static Context gloalContext;

    private static User defaultUser = null;


    public User() {
        super();
        this.userID = -1;
        this.packs = new ArrayList<Pack>();
    }

    public static User defaultUser(Context context) {
        gloalContext = context;

        HashMap<String, Object> dataDict = new HashMap<String, Object>();
        SQLiteDatabase db = SQLiteHelper.defaultDatabase(context);
        String query = String.format("SELECT * FROM Users_Tables WHERE user_id=%d", Global.USER_ID);
        Cursor cur = db.rawQuery(query, null);
        try {
            while (cur.moveToNext()) {
                dataDict.put("user_id", cur.getInt(0));
                dataDict.put("nick_name", cur.getString(1));
                dataDict.put("packs", Pack.packsForUserID(context, cur.getInt(0)));
            }
        } finally {
            cur.close();
        }

        defaultUser = (new User()).initWithDictionary(dataDict);

        return defaultUser;
    }

    private User initWithDictionary(HashMap<String, Object> dataDict) {
        userID = (Integer) dataDict.get("user_id");
        nickName = (String) dataDict.get("nick_name");
        ArrayList<HashMap<String, Object>> packArray = (ArrayList<HashMap<String, Object>>) dataDict.get("packs");
        for (int i = 0; i < packArray.size(); i++) {

            Pack newPack = (Pack) (new Pack()).initWithDictionary(packArray.get(i));
            packs.add(newPack);
        }
        return this;
    }

    public void addPack(Pack pack) {
        Boolean isExist = false;
        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).packID == pack.packID) {
                isExist = true;
                Log.w(Global.debugTag, "addPack failure because already existence");
                break;
            }
        }

        pack.userID = this.userID;
        if (!isExist) {
            packs.add(pack);
            pack.save(gloalContext);
        }
    }

    public void removePack(Pack pack) {
        packs.remove(pack);
        pack.destroy(gloalContext);
    }
}
