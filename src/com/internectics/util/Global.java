package com.internectics.util;

public class Global {
    public static final String debugTag = "ccaa";
    public static final String DATABASE_NAME = "FlashCardCreator-Local.db";
    public static final int DATABASE_VERSION = 1;

    public static final int USER_ID = 314;
    public static final String defaultUserStr = "Default_User";

    //Broadcast action name
    public static final String BROADCAST_ACTION_SAVE_NEW_CARD = "com.internectics.save_new_card";
    public static final String BROADCAST_ACTION_UPDATE_MASTER_VIEW = "com.internectics.update_master_view";

    //Broadcast intent extra
    public static final String KEY_FROM = "from";
    public static final String BROADCAST_INTENT_EXTRA_FROM_NEW_CARD = "from_new_card";
    public static final String BROADCAST_INTENT_EXTRA_FROM_NEW_PACK = "from_new_pack";
    public static final String BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED = "from_pack_selected";

    //Property name
    public static final String packID_Property = "packID";
    public static final String latestPackCreatedDate_Property = "latestPackCreatedDate";


}




