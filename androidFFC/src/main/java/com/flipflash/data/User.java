package com.flipflash.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.flipflash.helper.SQLiteHelper;
import com.flipflash.util.AppContext;
import com.flipflash.util.Global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.flipflash.util.LogUtils.LOGD;
import static com.flipflash.util.LogUtils.LOGE;

public class User {
    private static final String TAG = User.class.getName();

    public int userID;
    public String nickName;
    public ArrayList<Pack> packs;
    public static Context gloalContext;

    private static User defaultUser;

    private static boolean mIsReset;


    public User() {
        super();
        this.userID = -1;
        this.packs = new ArrayList<Pack>();
    }

    /*
     * 用于重置内存中的数据
     */
    public static void reset(Context context,boolean isReset) {

        if (isReset) {
            mIsReset = true;
            defaultUser(context);
            mIsReset = false; //一旦reset后，置false
        }


    }


    public static User defaultUser(Context context) {
        gloalContext = context;

        if ((defaultUser == null)||(defaultUser.packs.size() == 0) || mIsReset)  {
            HashMap<String, Object> dataDict = new HashMap<String, Object>();
            SQLiteDatabase db = SQLiteHelper.defaultDatabase(context);
            String query = String.format("SELECT * FROM Users_Tables WHERE user_id=%d", Global.USER_ID);
            Cursor cur = db.rawQuery(query, null);
            try {
                while (cur.moveToNext()) {
                    dataDict.put("user_id", cur.getInt(0));
                    dataDict.put("nick_name", cur.getString(1));
                    dataDict.put("packs", Pack.packsForUserID(context, cur.getInt(0),false));
                    break;
                }
            } finally {
                cur.close();
            }

            defaultUser = (new User()).initWithDictionary(dataDict);
        }


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
        int     indexExit = -1;
        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).packID == pack.packID) {
                isExist = true;
                indexExit = i;
                LOGE(TAG, "addPack: addPack failure because already existence");
                break;
            }
        }

        pack.userID = this.userID;
        if (isExist) {
            packs.remove(indexExit);
        }

        packs.add(pack);
        pack.save(gloalContext);
    }

    public void removePack(Pack pack) {

        int i = 0;
        for (Pack item: packs) {
            if (item.packID == pack.packID) {
                packs.remove(i);
                break;
            }

            i++;
        }


        pack.destroy(gloalContext);
    }

    public ArrayList<Pack> sortPacks(int sortType) {

        switch (sortType) {
            case 0: { //created ascend
                Collections.sort(packs, new Comparator<Pack>() {
                    @Override
                    public int compare(Pack lhs, Pack rhs) {
                        return (rhs.createDate - lhs.createDate);
                    }
                });
                break;
            }
            case 1: { //created descend
                Collections.sort(packs, new Comparator<Pack>() {
                    @Override
                    public int compare(Pack lhs, Pack rhs) {
                        return (lhs.createDate - rhs.createDate);
                    }
                });
                break;
            }
            case 2: { //visited ascend
                Collections.sort(packs, new Comparator<Pack>() {
                    @Override
                    public int compare(Pack lhs, Pack rhs) {
                        return (rhs.lastVistDate - lhs.lastVistDate);
                    }
                });
                break;
            }
            case 3: { //visited descend
                Collections.sort(packs, new Comparator<Pack>() {
                    @Override
                    public int compare(Pack lhs, Pack rhs) {
                        return (lhs.lastVistDate - rhs.lastVistDate);
                    }
                });
                break;
            }
        }

        if ((sortType == 0) || (sortType == 1)) {
            for (int i =0;i<packs.size();i++) {
                //LOGD(TAG, "sortPacks: " + "Create date: " + packs.get(i).createDate);
            }
        } else {
            for (int i =0;i<packs.size();i++) {
                //LOGD(TAG, "sortPacks: "+ "Last Visit date: " + packs.get(i).lastVistDate);
            }
        }


        return packs;

    }


    /*
     * 重新到数据库取
     */
    public static Pack getPack(Context context,int packID) {

        long startTime = System.currentTimeMillis();

        reset(context,true);
        Pack returnPack = null;
        ArrayList<Pack> packs = User.defaultUser(context).packs;

        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).packID == packID) {
                returnPack = packs.get(i);

                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                Log.d("SQLite","getPack: execute time(mill) is " + elapsedTime);

                return returnPack;
            }
        }




        return returnPack;
    }
}
