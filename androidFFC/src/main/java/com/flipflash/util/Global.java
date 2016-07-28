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

    public static       boolean   IS_DOGFOOD_BUILD =  false;  //是否开启debug模式

    public static final String BucketPostfixAfterUserName = "55b5aa55673793805862";

    public static final String    SAMPLE_URL = "https://s3-us-west-1.amazonaws.com/ffcmaster/FlipFlashCards.zip?from=Flipflashcards&type=demo";
    //public static final String    SAMPLE_URL = "http://7o51o0.com1.z0.glb.clouddn.com/Pack1440729625-2043618070.zip";

    public static final String  DATABASE_NAME = "FlashCardCreator-Local.db";
    public static final int     DATABASE_VERSION = 1;  // you need to update this when changing

    public static final int USER_ID = 314;
    public static final String defaultUserStr = "Default_User";

    //名称必须与css_font（arrays.xml)保持一致，同iOS版本也一致
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


    /*
     * 字体放大系统（edit mode vs play mode），在AppStart中进行初始化。
     * 通过调整这个数值，可以用来验证triggerResizeTextToFitFrame的有效性
     */
    public static float   scaleInPlayMode = 1.2f;

    /*
     * 注意，这个值同play.xml中的soulwolf:widthRatio="1.45" 一致
     */
    public static float   ratioOfCardInPlayMode = 1.45f;

    //ie, image2, logoImage的tag，用在VGViewPager
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


    public static final String K_AppStore_Link = "https://play.google.com/store/apps/details?id=com.flipflashcards.FFC_Android";


    /*
     * latest downloaded pack
     */
    public static       int   maxDownloadableNoForCurrentDownloadingPack = 0;
    public static       String   fccURLForCurrentDownloadingPack = "";

    /*
     * 后门程序,用于是否关闭line number check
     */
    public static       boolean   checkLineNumberWhenResizeTextToFitFrame = true;


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




