package com.flipflash.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.flipflash.data.Pack;

public class Global {

    /**
     *  In order to submit a version as soon as possible, we prepare this version and remove Amazon for client uploading
     */
    public static final boolean  FFC_WITHOUT_SUBSCRIPTION = true;

    public static       boolean   IS_DOGFOOD_BUILD =  false;

    public static final String BucketPostfixAfterUserName = "55b5aa55673793805862";

    public static final String    SAMPLE_URL = "https://s3.amazonaws.com/kimflipflashcardscom-55b5aa55673793805862/SAMPLECARDES1508045661293390394.zip?from=Flipflashcards&type=demo";
    //public static final String    SAMPLE_URL = "http://7o51o0.com1.z0.glb.clouddn.com/Pack1440729625-2043618070.zip";

    public static final String  DATABASE_NAME = "FlashCardCreator-Local.db";
    public static final int     DATABASE_VERSION = 3;  // you need to update this when changing

    public static final int USER_ID = 314;
    public static final String defaultUserStr = "Default_User";

    public static final String fontName_Default      = "DejaVuSans.ttf";
    public static final String fontName_ArialBoldMT  = "Arial-BoldMT.ttf";
    public static final String fontName_Chalkduster  = "Chalkduster.ttf";
    public static final String fontName_Courier      = "Courier.ttf";
    public static final String fontName_Papyrus      = "Papyrus.ttf";
    public static final String fontName_Zapfino      = "Zapfino.ttf";
    public static final String fontName_Chalkboard   = "ChalkboardSE-Bold.ttf";
    public static final String fontName_Futura       = "Futura-Medium.ttf";

    //Broadcast action name
    public static final String BROADCAST_ACTION_UPDATE_MASTER_VIEW = "com.internectics.update_master_view";

    //Broadcast intent extra
    public static final String KEY_FROM = "from";
    public static final String KEY_CARD_INDEX = "card_index";
    public static final String BROADCAST_EXTRA_FROM_NEW_CARD = "from_new_card";
    public static final String BROADCAST_EXTRA_FROM_NEW_PACK = "from_new_pack";
    public static final String BROADCAST_EXTRA_FROM_EDIT_PACK = "from_edit_pack";
    public static final String BROADCAST_EXTRA_FROM_CURRENT_PACK_UPDATE = "from_current_pack_update";
    public static final String BROADCAST_EXTRA_FROM_PACK_SELECTED = "from_pack_selected";
    public static final String BROADCAST_EXTRA_FROM_PACK_DOWNLOADED = "from_pack_downloaded";
    public static final String BROADCAST_EXTRA_FROM_SNAPSHOT_ALL = "snapshot_all";

    //Used to judge whether need to upload pack again for certain pack
    public static final String shareDate_Property = "share_date";
    public static final String updateDate_Property = "updated_date";

    //Used to highlight current selected pack in pack list
    public static final String lastSelectedPackID = "lastSelectedPackID";

    //Used to judge whether need to download example pack again
    public static final String isExamplePackDownloadedSBefore_Property = "is_example_pack_downloaded_before";

    //Used to judge whether need to copy local resource to files to "Reserved folder"
    public static final String isFirstStartUp = "is_first_startup";

    //Used to judge whether to play random
    public static final String isRandomPlay = "is_random_play";

    public static final String isFunctionPromptOff = "isFunctionPromptOff";

    public static final String isSoundRecording = "isSoundRecording";

    public static final String isTextToSpeech = "is_Text_To_Speech";

    public static final String PLAY_OPTION  = "PLAY_OPTION";

    //used to generate a redirected URL
    public static final String URL_REDIRECT_API = "http://tinyurl.com/api-create.php?url=";
    public static final String TINYURL_SHORTED_BASE_URL = "http://tinyurl.com/";


    // true only hideEverything is executed
    public static  boolean     isAllowToShowTooltips = true;

    //has to show tip on the help button firstly, then others
    public static final String isHelpTipHasBeenShowedFirst = "isHelpTipHasBeenShowedFirst";

    //download sample pack related
    public static  boolean   isNotAllowDownloadSamplePack = false;

    //Amazon SimpleDB
    public static final String amazon_sdb_domain_name = "flashcardcreator";
    public static int currentAmazonSimpleDBItemDownloadCount = -1;
    public static String currentAmazonSimpleDBItemName = "";

    //
    public static final String sortType = "sort_Type";

    /*
     * For preview function. In order to avoid serialization for activity intent, we put previewPack here
     */
    public static Pack         previewPack;


    public static float   scaleInPlayMode = 1.2f;


    public static float   ratioOfCardInPlayMode = 1.45f;

    public static final String mImage_Showing = "mImage_Showing";
    public static final String mImage2_Showing = "mImage2_Showing";
    public static final String mLogoImage_Showing = "mLogoImage_Showing";
    public static final String mImages_Not_Showing = "mImages_Not_Showing";


    public static int     k_MAX_Auto_Play_Speed  = 60;
    public static int     k_MIN_Auto_Play_Speed  = 4;
    public static int     kDefault_Auto_Play_Speed = k_MIN_Auto_Play_Speed;



    //Setting related
    public static final int kDEFAULT_CountDown_Slider_Value = 3;

    //Dropbox folder to save Flip Flash Cards
    public static final String DROPBOX_FOLDER = "/FlipFlashCardsPacks/";
    //Google Drive folder to save Flip Flash Cards
    public static final String GOOGLE_DRIVE_FOLDER_NAME = "FlipFlashCardsPacks";


    public static final String K_AppStore_Link = "https://play.google.com/store/apps/details?id=com.flipflashcards.FFC_Android";


    /*
     * latest downloaded pack
     */
    public static       int   maxDownloadableNoForCurrentDownloadingPack = 0;
    public static       String   fccURLForCurrentDownloadingPack = "";

    public static       boolean   checkLineNumberWhenResizeTextToFitFrame = true;


    //show share action list again after a share action
    public static       boolean   showActionListAgain = false;  //only for Email and Twitter. For Facebook, use FacebookShareFinishEvent
    public static       int       activeShareStorage= -1;   // 0: Google Drive; 1: Dropbox; 2: AWS


    /*
     * for startActivityForResult
     */
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_CODE_FROM_LOGO  = 314;
    public static final int REQUEST_CODE_FROM_IMAGE  = 315;
    public static final int REQUEST_CODE_FROM_BACKGROUND  = 316;
    public static final int REQUEST_CODE_FROM_BACKGROUND_AFTER_CROPPED  = 317;
    public static final int REQUEST_ACTION_MANAGE_OVERLAY_PERMISSION    = 319;

    public static final int REQUEST_CODE_GOOGLE_ACCOUNT_PICKER = 321;
    public static final int REQUEST_CODE_GOOGLE_DRIVE_REQUEST_PERMISSION = 322;


    /**
     * detect network
     *
     * @return
     */
    public static boolean apiReachable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    /**
     * detect network with Dialog
     *
     * @return
     */
    public static boolean apiReachableWithAlert(Context context) {
        boolean result = apiReachable(context);
        return result;
    }


    /**
     * Strictly, it could be repeated
     *
     * @return
     */
    public static int generateNoRepeatInt() {
        int result = (int) (System.currentTimeMillis() & 0x7FFFFFFF);   //0xFFFFFFF is the max number of int, 4 bytes (not long);
        return result;
    }

    /*
     * History reason: our table in SQLite is int format, so I have to do this
     */
    public static int currentTimeSeconds() {
        long millSeconds = System.currentTimeMillis();
        return (int)(millSeconds/1000);
    }


}




