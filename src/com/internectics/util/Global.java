package com.internectics.util;

public class Global {
    public static final String debugTag = "ccaa";
    public static final String DATABASE_NAME = "FlashCardCreator-Local.db";
    public static final int DATABASE_VERSION = 1;

    public static final int USER_ID = 314;
    public static final String defaultUserStr = "Default_User";

    //Broadcast action name
    public static final String BROADCAST_ACTION_UPDATE_MASTER_VIEW = "com.internectics.update_master_view";

    //Broadcast intent extra
    public static final String KEY_FROM = "from";
    public static final String BROADCAST_INTENT_EXTRA_FROM_NEW_CARD = "from_new_card";
    public static final String BROADCAST_INTENT_EXTRA_FROM_NEW_PACK = "from_new_pack";
    public static final String BROADCAST_INTENT_EXTRA_FROM_PACK_SELECTED = "from_pack_selected";
    public static final String BROADCAST_INTENT_EXTRA_FROM_PACK_DOWNLOADED = "from_pack_downloaded";

    //Used to judge which pack is to load during start-up
    public static final String mostRecentPackCreatedID_Property = "most_recent_pack_id";
    public static final String mostRecentPackCreatedDate_Property = "most_recent_pack_created_date";

    //Used to judge whether need to upload pack again for certain pack
    public static final String shareDate_Property = "share_date";
    public static final String shareLink_Property = "share_link";
    public static final String updateDate_Property = "updated_date";

    //Used to judge whether need to download example pack again
    public static final String isExamplePackDownloadedSBefore_Property = "is_example_pack_downloaded_before";


}




