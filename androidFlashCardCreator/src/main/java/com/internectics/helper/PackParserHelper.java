package com.internectics.helper;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.amazonaws.auth.NoSessionSupportCredentials;
import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
import com.internectics.util.UIHelper;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;

import timber.log.Timber;

public class PackParserHelper {

    /*
      iPhone ＝ 640
      iPad   = 1024
      其他的，根据json数据screen_width进行获取
      如果json中screen_width字段不存在，则 ＝ 0 （进行默认逻辑处理）
     */
    private static float mScreenWidthFromSharedDevice; // the screen width whose device has shared the pack

    /**
     * after finshing pack download and unzip, this methold will be called to build pack and add to current user
     */
    public static Pack parse(Context context) {

        File newFile;


        //step1: save pack
        File packJsonFile = new File(FileOperationHelper.downloadedPackDirectory(), "packInformation.json");
        Pack resultPack = parsePackJsonFile(context,packJsonFile.toString());

        //step2: save cards
        for (int i = 0; ; i++) {
            File cardDirectory = new File(FileOperationHelper.downloadedPackDirectory(), String.format("card%d", i));
            if (!cardDirectory.exists()) {
                break;
            }
            Card resultCard = parseCardJsonFiles(cardDirectory, resultPack);
            resultCard.cardSN = i + 1;

            //***************再次加工,重要
            //从json获取到的filepath，只有文件名，没有路径，所以需要做如下操作：1. 定位到下载的地方；2. 拷贝到Images folder；3.组成uri格式的完整路径

            //coverImageUriFormatStr
            if (StringUtils.isCorrectImageName(resultCard.coverImageUriFormatStr)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.coverImageUriFormatStr, i));
                resultCard.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.coverImageUriFormatStr = "";
            }


            //imageUriFormatStr
            if (StringUtils.isCorrectImageName(resultCard.question.imageUriFormatStr)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.imageUriFormatStr, i));
                resultCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.question.imageUriFormatStr = "";
            }
            if (StringUtils.isCorrectImageName(resultCard.answer.imageUriFormatStr)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.imageUriFormatStr, i));
                resultCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.answer.imageUriFormatStr = "";
            }

            //imageUriFormatStr2
            if (StringUtils.isCorrectImageName(resultCard.question.imageUriFormatStr2)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.imageUriFormatStr2, i));
                resultCard.question.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.question.imageUriFormatStr2 = "";
            }
            if (StringUtils.isCorrectImageName(resultCard.answer.imageUriFormatStr2)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.imageUriFormatStr2, i));
                resultCard.answer.imageUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.answer.imageUriFormatStr2 = "";
            }


            //backgroundImageUriFormatStr
            if (StringUtils.isCorrectImageName(resultCard.question.backgroundImageUriFormatStr)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.backgroundImageUriFormatStr, i));
                resultCard.question.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.question.backgroundImageUriFormatStr = "";

            }
            if (StringUtils.isCorrectImageName(resultCard.answer.backgroundImageUriFormatStr)) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.backgroundImageUriFormatStr, i));
                resultCard.answer.backgroundImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                resultCard.answer.backgroundImageUriFormatStr = "";
            }



            //movieUriFormatStr

            if (resultCard.question.movieUriFormatStr.contains("http")) {
                //do nothing
            } else if ((resultCard.question.movieUriFormatStr == null) || (resultCard.question.movieUriFormatStr.length() == 0)) {
                resultCard.question.movieUriFormatStr = "";

            } else {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.movieUriFormatStr, i));
                resultCard.question.movieUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            }

            //movieUriFormatStr2

            if (resultCard.question.movieUriFormatStr2.contains("http")) {
                //do nothing
            } else if ((resultCard.question.movieUriFormatStr2 == null) || (resultCard.question.movieUriFormatStr2.length() == 0)) {
                resultCard.question.movieUriFormatStr2 = "";

            } else {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.movieUriFormatStr2, i));
                resultCard.question.movieUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(newFile);
            }


            if (resultCard.answer.movieUriFormatStr.contains("http")) {
                //do nothing
            } else if ((resultCard.answer.movieUriFormatStr == null) || (resultCard.answer.movieUriFormatStr.length() == 0)) {
                resultCard.answer.movieUriFormatStr = "";

            } else {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.movieUriFormatStr, i));
                resultCard.answer.movieUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            }

            if (resultCard.answer.movieUriFormatStr2.contains("http")) {
                //do nothing
            } else if ((resultCard.answer.movieUriFormatStr2 == null) || (resultCard.answer.movieUriFormatStr2.length() == 0)) {
                resultCard.answer.movieUriFormatStr2 = "";

            } else {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.movieUriFormatStr2, i));
                resultCard.answer.movieUriFormatStr2 = FileOperationHelper.convertToUriFormatFile(newFile);
            }

            //audioUriFormatStr

            if (resultCard.question.audioUriFormatStr.length() >0) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.question.audioUriFormatStr, i));
                resultCard.question.audioUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            } else {
                Timber.tag(Global.debugTag).d( "resultCard.question.audioUriFormatStr is empty");
            }

            if (resultCard.answer.audioUriFormatStr.length() >0) {
                newFile = FileOperationHelper.copyImageVideoToImagesFolder(getCardImageFullPath(resultCard.answer.audioUriFormatStr, i));
                resultCard.answer.audioUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);

            } else {
                Timber.tag(Global.debugTag).d( "resultCard.answer.audioUriFormatStr is empty");
            }

            //***************再次加工,结束


            resultCard.packID = resultPack.packID; //this is necessary

            resultPack.addCard(AppContext.getAppContext(),resultCard);

        }

        newFile = FileOperationHelper.copyImageVideoToImagesFolder(getPackImageFullPath(resultPack.coverImageUriFormatStr));
        resultPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
        newFile = FileOperationHelper.copyImageVideoToImagesFolder(getLogoImageFullPath(resultPack.logoImageUriFormatStr, resultPack.cards.size() - 1));//be careful, we set logoImageUriFormatStr from last card in parseCardJsonFiles
        resultPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
        User.defaultUser(AppContext.getAppContext()).addPack(resultPack);

        return resultPack;

    }

    /**
     * Special purpose for get card related image under unzipped downloaded pack folder
     * Images include: cover image of card, image of question card, image of answer card
     */
    private static File getCardImageFullPath(String uriFormatStr, int indexOfCard) {

        if (StringUtils.isEmpty(uriFormatStr)) {
            return null;
        }

        String fileName = StringUtils.lastComponentOfPath(Uri.parse(uriFormatStr));
        File fullFilePath = new File(FileOperationHelper.downloadedPackDirectory(), String.format("card%d/%s", indexOfCard, fileName));
        return fullFilePath;
    }

    /**
     * Special purpose for get pack related image under unzipped downloaded pack folder
     * Image include: cover image of pack, logo of pack
     */
    private static File getPackImageFullPath(String uriFormatStr) {
        String fileName = StringUtils.lastComponentOfPath(Uri.parse(uriFormatStr));
        File fullFilePath = new File(FileOperationHelper.downloadedPackDirectory(), fileName);
        return fullFilePath;
    }


    /**
     * Because of history reason, the card logo image belong to card, rather than to pack.
     * All the log images are same under same package, so we only need to take one
     */
    private static File getLogoImageFullPath(String uriFormatStr,int index) {
        return getCardImageFullPath(uriFormatStr, index);
    }


    /**
     * parse downloaded pack JSON file into Pack
     */
    private static Pack parsePackJsonFile(Context context,String packJsonFile) {

        Pack pack = new Pack();

        JSONParser parser = new JSONParser();
        FileReader reader;
        try {
            reader = new FileReader(packJsonFile);
            JSONObject obj = (JSONObject) parser.parse(reader);

            if (obj.containsKey("pack_id")) {
                pack.packID = Integer.parseInt((String)(obj.get("pack_id")));
                User.defaultUser(context).removePack(pack);
            } else {
                pack.packID = -1;
            }


            pack.packName = (String) obj.get("pack_name");
            pack.sidebarTitle = (String) obj.get("sidebar_title");
            pack.coverImageUriFormatStr = (String) obj.get("cover_image");
            pack.creatorID = (String) obj.get("creator");
            pack.creatorNickName = (String) obj.get("creator_nick_name");
            if (obj.containsKey("job_title")) {
                pack.jobTitle = (String) obj.get("job_title");
            } else {
                pack.jobTitle = "";
            }

            if (obj.containsKey("share_link")) {
                pack.shareLink = (String) obj.get("share_link");
            } else {
                pack.shareLink = "";
            }

            if (obj.containsKey("file_name_on_aws")) {
                pack.fileNameOnAWS = (String) obj.get("file_name_on_aws");
            } else {
                pack.fileNameOnAWS = "";
            }


            if (obj.containsKey("restore_password")) {
                pack.restorePassword = (String) obj.get("restore_password");
            } else {
                byte[] encodedVal = Base64.encode("".getBytes(), 0);
                pack.restorePassword = new String(encodedVal);

            }

            String temp = (String) obj.get("auto_play_speed");
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                pack.autoPlaySpeed =  Integer.parseInt(temp);
            } else {
                pack.autoPlaySpeed = Global.k_Default_Auto_Play_Dwell_Time;
            }


            pack.platform = (String) obj.get("platform");
            pack.userID = Global.USER_ID; // there's no this information in json file, so we have to add manually

            pack.createDate = Global.currentTimeSeconds();
            pack.lastVistDate = Global.currentTimeSeconds();

            if (pack.platform.contains("iPhone") == true) {
                mScreenWidthFromSharedDevice = 640;
            } else if (pack.platform.contains("iPad") == true) {
                mScreenWidthFromSharedDevice = 1024;
            } else {
                temp = (String) obj.get("screen_width");
                if ((temp != null) && (StringUtils.isNumeric(temp))) {
                    mScreenWidthFromSharedDevice =  Integer.parseInt(temp);
                } else {
                    Timber.w("mScreenWidthFromSharedDevice is 0");
                    mScreenWidthFromSharedDevice = 0;
                }
            }
            Timber.tag(Global.debugTag2).d("screenWith from shared device is:" + mScreenWidthFromSharedDevice);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pack;
    }

    /**
     * parse downloaded card JSON file into Card
     */
    private static Card parseCardJsonFiles(File cardDirectory, Pack currentPack) {
        Card card = new Card();
        JSONParser parser = new JSONParser();

        File questionJsonFile = new File(cardDirectory, "questionTextContent.json");
        File answerJsonFile = new File(cardDirectory, "answerTextContent.json");

        //Question
        try {

            //step1: 获取原始数据

            FileReader fileReader = new FileReader(questionJsonFile);
            JSONObject questionObj = (JSONObject) parser.parse(fileReader);

            String temp = "";

            //this is a history issue(to compatibible with iOS version), i have to get logoImageUriFormatStr from question
            //Better way is to put them in pack

            if (questionObj.containsKey("logo")) {
                temp = (String) questionObj.get("logo");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                currentPack.logoImageUriFormatStr = temp;
            }

            temp = null;
            if (questionObj.containsKey("logo_url")) {
                temp = (String) questionObj.get("logo_url");
            }
            if ((temp != null) && (temp.contains("http"))) {
                currentPack.logoURL = temp;
            } else {
                currentPack.logoURL = "http://novalidurl.com";
            }


            if (questionObj.containsKey("title")) {
                currentPack.questionTitle = (String) questionObj.get("title");
            }

            temp = "";
            if (questionObj.containsKey("cover_image")) {
                temp = (String) questionObj.get("cover_image");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.coverImageUriFormatStr = temp;
            }

            temp = "";
            if (questionObj.containsKey("background_image")) {
                temp = (String) questionObj.get("background_image");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.question.backgroundImageUriFormatStr = temp;
            }

            temp = "";
            if (questionObj.containsKey("movie")) {
                temp = (String) questionObj.get("movie");
            }
            if ((StringUtils.isCorrectMov3GPName(temp)) || (temp.contains("http"))) {   //video or video linkage
                card.question.movieUriFormatStr = temp;
            }

            temp = "";
            if (questionObj.containsKey("movie2")) {
                temp = (String) questionObj.get("movie2");
            }
            if ((StringUtils.isCorrectMov3GPName(temp)) || (temp.contains("http"))) {   //video or video linkage
                card.question.movieUriFormatStr2 = temp;
            }


            temp = "";
            if (questionObj.containsKey("audio")) {
                temp = (String) questionObj.get("audio");
            }
            if (StringUtils.isCorrect3GPOrAACName(temp)) {
                card.question.audioUriFormatStr = temp;
            }

            temp = "";
            if (questionObj.containsKey("template_background")) {
                temp = (String) questionObj.get("template_background");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.templateBackground = temp;
            }

            temp = null;
            if (questionObj.containsKey("template_id")) {
                temp = (String) questionObj.get("template_id");
            }
            if (temp != null) {
                card.question.templateID = Integer.parseInt(temp);
            } else {
                card.question.templateID = 0;
            }

            temp = null;
            if (questionObj.containsKey("line_number_subheading")) {
                temp = (String) questionObj.get("line_number_subheading");
            }
            if (temp != null) {
                card.question.lineNoSubheading = Integer.parseInt(temp);
            } else {
                card.question.lineNoSubheading = 0;
            }

            temp = null;
            if (questionObj.containsKey("line_number_main")) {
                temp = (String) questionObj.get("line_number_main");
            }
            if (temp != null) {
                card.question.lineNoMain = Integer.parseInt(temp);
            } else {
                card.question.lineNoMain = 0;
            }

            temp = null;
            if (questionObj.containsKey("line_number_sub")) {
                temp = (String) questionObj.get("line_number_sub");
            }
            if (temp != null) {
                card.question.lineNoSub = Integer.parseInt(temp);
            } else {
                card.question.lineNoSub = 0;
            }

            temp = "";
            if (questionObj.containsKey("image")) {
                temp = (String) questionObj.get("image");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.question.imageUriFormatStr = temp;
            }

            temp = "";
            if (questionObj.containsKey("image2")) {
                temp = (String) questionObj.get("image2");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.question.imageUriFormatStr2 = temp;
            }

            if (questionObj.containsKey("subheading")) {
                card.question.subheading = removeTrailingSpaceAndUnexpectedCharacters((String) questionObj.get("subheading"));
            }

            if (questionObj.containsKey("main")) {
                card.question.main =  removeTrailingSpaceAndUnexpectedCharacters((String) questionObj.get("main"));
            }


            if (questionObj.containsKey("sub")) {
                card.question.sub =  removeTrailingSpaceAndUnexpectedCharacters((String) questionObj.get("sub"));
            }



            if (questionObj.containsKey("subheading_align"))  {
                card.question.css.subheadingAlign = (String) questionObj.get("subheading_align");
            }

            if (questionObj.containsKey("subheading_align_vertical"))  {
                card.question.css.subheadingAlignVertical = (String) questionObj.get("subheading_align_vertical");
            }

            if (questionObj.containsKey("subheading_color")) {
                card.question.css.subheadingColor = (String) questionObj.get("subheading_color");
            }

            if (questionObj.containsKey("main_align"))  {
                card.question.css.mainAlign = (String) questionObj.get("main_align");
            }

            if (questionObj.containsKey("main_align_vertical"))  {
                card.question.css.mainAlignVertical = (String) questionObj.get("main_align_vertical");
            }

            if (questionObj.containsKey("main_color")) {
                card.question.css.mainColor = (String) questionObj.get("main_color");
            }

            if (questionObj.containsKey("sub_align"))  {
                card.question.css.subAlign = (String) questionObj.get("sub_align");
            }

            if (questionObj.containsKey("sub_align_vertical"))  {
                card.question.css.subAlignVertical = (String) questionObj.get("sub_align_vertical");
            }

            if (questionObj.containsKey("sub_color")) {
                card.question.css.subColor = (String) questionObj.get("sub_color");
            }

            if (questionObj.containsKey("subheading_font")) {

                String str = (String) questionObj.get("subheading_font");
                card.question.css.subheadingFont = str;
            }

            if (questionObj.containsKey("main_font")) {

                String str = (String) questionObj.get("main_font");
                card.question.css.mainFont = str;
            }

            if (questionObj.containsKey("sub_font")) {

                String str = (String) questionObj.get("sub_font");
                card.question.css.subFont = str;
            }

            int subheadingSize;
            temp = null;
            if (questionObj.containsKey("subheading_size"))  {
                temp = (String) questionObj.get("subheading_size");
            }
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                subheadingSize =  Integer.parseInt(temp);
            } else {
                subheadingSize = 0;
            }


            int mainSize;
            temp = null;
            temp = (String) questionObj.get("main_size");
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                mainSize =  Integer.parseInt(temp);
            } else {
                mainSize = 0;
            }

            int subSize;
            temp = null;
            temp = (String) questionObj.get("sub_size");
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                subSize =  Integer.parseInt(temp);
            } else {
                subSize = 0;
            }

            int[] standardCSSArrary = AppContext.getAppContext().getResources().getIntArray(R.array.css_size_int);

            //step2: 根据平台不同进行初次缩放

            if (currentPack.platform.equals(UIHelper.getCurrentPlatform()) == true) {
                if (subheadingSize >0) {
                    card.question.css.subheadingSize = subheadingSize;
                } else {
                    card.question.css.subheadingSize = standardCSSArrary[0];
                }

                if (subheadingSize >0) {
                    card.question.css.subSize = subSize;
                } else {
                    card.question.css.mainSize = standardCSSArrary[1];
                }

                if (subheadingSize >0) {
                    card.question.css.subSize = subSize;
                } else {
                    card.question.css.subSize = standardCSSArrary[2];
                }


            } else {
                if (mScreenWidthFromSharedDevice == 0) { // mean no this field in pack json file  （兼容之前的版本）

                    //-----begin scale down policy with error protection
                    //step1: get which is trustworthy
                    int baseSize = 0;
                    int whichAsBase = 0;  //0, subheading; 1, main; 2. sub

                    if ((subheadingSize == 0) ||(card.question.subheading.length() == 0)) {
                        card.question.css.subheadingSize = standardCSSArrary[0];
                        Timber.tag(Global.debugTag).w( "subheadingSize = 0 or subheading.length = 0");
                    } else {
                        baseSize =  subheadingSize;
                        whichAsBase = 0;
                    }


                    if ((subSize == 0) ||(card.question.sub.length() == 0)) {
                        card.question.css.subSize = standardCSSArrary[2];
                        Timber.tag(Global.debugTag).w( "subSize = 0 or sub.length = 0");
                    } else {
                        baseSize =  subSize;
                        whichAsBase =2;
                    }

                    //put main at last is very important, because it's the most trustworthy value
                    if ((mainSize == 0) ||(card.question.main.length() == 0)) {
                        card.question.css.mainSize = standardCSSArrary[1];
                        Timber.tag(Global.debugTag).w( "mainSize = 0 or main.length = 0");
                    } else {
                        baseSize =  mainSize;
                        whichAsBase = 1;
                    }

                    if (baseSize == 0) {
                        baseSize =  standardCSSArrary[1];
                        whichAsBase = 0;
                    }


                    //step2: scale down
                    //size interchangeable with iOS, and other android devices

                    switch (whichAsBase) {
                        case    0: {

                            //subheadingSize is trustworthy
                            //card.question.css.subheadingSize will be base value
                            //baseSize is same as subHeadingSize

                            card.question.css.subheadingSize = standardCSSArrary[0];

                            if ((mainSize == 0) || (card.question.main.length() == 0)) {
                                card.question.css.mainSize = standardCSSArrary[1];
                            } else {
                                card.question.css.mainSize = (int)(card.question.css.subheadingSize * ((float)mainSize/baseSize));
                            }

                            if ((subSize == 0) || (card.question.sub.length() == 0)) {
                                card.question.css.subSize = standardCSSArrary[2];
                            } else {
                                card.question.css.subSize = (int)(card.question.css.subheadingSize * ((float)subSize/baseSize));
                            }

                            break;
                        }

                        case    1: {

                            //mainSize is trustworthy
                            //card.question.css.mainSize will be base value
                            //baseSize is same as mainSize

                            //when the mainSize is quite big
                            if (mainSize > standardCSSArrary[6]) {
                                card.question.css.mainSize = mainSize;
                            }  else {
                                card.question.css.mainSize = standardCSSArrary[1];
                            }

                            if ((subheadingSize == 0) || (card.question.subheading.length() == 0)) {
                                card.question.css.subheadingSize = standardCSSArrary[0];
                            } else {
                                card.question.css.subheadingSize = (int)(card.question.css.mainSize * ((float)subheadingSize/baseSize));
                            }

                            if ((subSize == 0) || (card.question.sub.length() == 0)) {
                                card.question.css.subSize = standardCSSArrary[2];
                            } else {
                                card.question.css.subSize = (int)(card.question.css.mainSize * ((float)subSize/baseSize));
                            }

                            break;
                        }

                        case    2: {

                            //subSize is trustworthy
                            //card.question.css.subSize will be base value
                            //baseSize is same as subSize

                            card.question.css.subSize = standardCSSArrary[1];

                            if ((subheadingSize == 0) || (card.question.subheading.length() == 0)) {
                                card.question.css.subheadingSize = standardCSSArrary[0];
                            } else {
                                card.question.css.subheadingSize = (int)(card.question.css.subSize * ((float)subheadingSize/baseSize));
                            }

                            if ((mainSize == 0) || (card.question.main.length() == 0)) {
                                card.question.css.mainSize = standardCSSArrary[1];
                            } else {
                                card.question.css.mainSize = (int)(card.question.css.subSize * ((float)mainSize/baseSize));
                            }

                            break;
                        }

                        default:
                            break;
                    }

                    //-----end scale down policy with error protection
                } else {

                    //字体根据mScreenWidthFromSharedDevice和当前平台的width进行一定比例的缩放
                    float bestFontSizeFromSharedDevice = UIHelper.getBestFontSize(mScreenWidthFromSharedDevice);
                    int baseFontSizeOnCurrentDevice = standardCSSArrary[1];
                    float factor = baseFontSizeOnCurrentDevice/bestFontSizeFromSharedDevice;

                    if (subheadingSize == 0) {
                        subheadingSize = standardCSSArrary[0];
                    }
                    card.question.css.subheadingSize = (int)(subheadingSize * factor);

                    if (mainSize == 0) {
                       mainSize = standardCSSArrary[1];
                    }
                    card.question.css.mainSize = (int)(mainSize * factor);

                    if (subSize == 0) {
                        subSize = standardCSSArrary[2];
                    }
                    card.question.css.subSize = (int)(subSize * factor);

                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        } catch (IOException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        }

        //Answer
        try {
            String temp = null;

            FileReader fileReader = new FileReader(answerJsonFile);
            JSONObject answerObj = (JSONObject) parser.parse(fileReader);

            currentPack.answerTitle = (String) answerObj.get("title");

            if (answerObj.containsKey("template_id")) {
                temp = (String) answerObj.get("template_id");
            }
            if (temp != null)  {
                card.answer.templateID = Integer.parseInt(temp);
            } else {
                card.answer.templateID = 0;
            }

            temp = null;
            if (answerObj.containsKey("line_number_subheading")) {
                temp = (String) answerObj.get("line_number_subheading");
            }
            if (temp != null) {
                card.answer.lineNoSubheading = Integer.parseInt(temp);
            } else {
                card.answer.lineNoSubheading = 0;
            }

            temp = null;
            if (answerObj.containsKey("line_number_main")) {
                temp = (String) answerObj.get("line_number_main");
            }
            if (temp != null) {
                card.answer.lineNoMain = Integer.parseInt(temp);
            } else {
                card.answer.lineNoMain = 0;
            }

            temp = null;
            if (answerObj.containsKey("line_number_sub")) {
                temp = (String) answerObj.get("line_number_sub");
            }
            if (temp != null) {
                card.answer.lineNoSub = Integer.parseInt(temp);
            } else {
                card.answer.lineNoSub = 0;
            }

            temp = "";
            if (answerObj.containsKey("image")) {
                temp = (String) answerObj.get("image");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.answer.imageUriFormatStr = temp;
            }

            temp = "";
            if (answerObj.containsKey("image2")) {
                temp = (String) answerObj.get("image2");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.answer.imageUriFormatStr2 = temp;
            }

            if (answerObj.containsKey("subheading"))  {
                card.answer.subheading = removeTrailingSpaceAndUnexpectedCharacters((String) answerObj.get("subheading"));
            }

            if (answerObj.containsKey("main"))  {
                card.answer.main = removeTrailingSpaceAndUnexpectedCharacters((String) answerObj.get("main"));
            }


            if (answerObj.containsKey("sub")) {
                card.answer.sub = removeTrailingSpaceAndUnexpectedCharacters((String) answerObj.get("sub"));
            }

            if (answerObj.containsKey("subheading_align")) {
                card.answer.css.subheadingAlign = (String) answerObj.get("subheading_align");
            }

            if (answerObj.containsKey("subheading_align_vertical")) {
                card.answer.css.subheadingAlignVertical = (String) answerObj.get("subheading_align_vertical");
            }

            if (answerObj.containsKey("subheading_color")) {
                card.answer.css.subheadingColor = (String) answerObj.get("subheading_color");
            }

            if (answerObj.containsKey("main_align")) {
                card.answer.css.mainAlign = (String) answerObj.get("main_align");
            }

            if (answerObj.containsKey("main_align_vertical")) {
                card.answer.css.mainAlignVertical = (String) answerObj.get("main_align_vertical");
            }

            if (answerObj.containsKey("main_color")) {
                card.answer.css.mainColor = (String) answerObj.get("main_color");
            }

            if (answerObj.containsKey("sub_align")) {
                card.answer.css.subAlign = (String) answerObj.get("sub_align");
            }

            if (answerObj.containsKey("sub_align_vertical")) {
                card.answer.css.subAlignVertical = (String) answerObj.get("sub_align_vertical");
            }

            if (answerObj.containsKey("sub_color")) {
                card.answer.css.subColor = (String) answerObj.get("sub_color");
            }

            if (answerObj.containsKey("subheading_font")) {

                String str = (String) answerObj.get("subheading_font");
                card.answer.css.subheadingFont = str;
            }

            if (answerObj.containsKey("main_font")) {

                String str = (String) answerObj.get("main_font");
                card.answer.css.mainFont = str;
            }

            if (answerObj.containsKey("sub_font")) {

                String str = (String) answerObj.get("sub_font");
                card.answer.css.subFont = str;
            }

            temp = "";
            if (answerObj.containsKey("background_image")) {
                temp = (String) answerObj.get("background_image");
            }
            if (StringUtils.isCorrectImageName(temp)) {
                card.answer.backgroundImageUriFormatStr = temp;
            }

            temp = "";
            if (answerObj.containsKey("movie")) {
                temp = (String) answerObj.get("movie");
            }
            if ((StringUtils.isCorrectMov3GPName(temp)) || (temp.contains("http"))) { //video or video linkage
                card.answer.movieUriFormatStr = temp;
            }

            temp = "";
            if (answerObj.containsKey("movie2")) {
                temp = (String) answerObj.get("movie2");
            }
            if ((StringUtils.isCorrectMov3GPName(temp)) || (temp.contains("http"))) { //video or video linkage
                card.answer.movieUriFormatStr2 = temp;
            }

            temp = "";
            if (answerObj.containsKey("audio")) {
                temp = (String) answerObj.get("audio");
            }
            if (StringUtils.isCorrect3GPOrAACName(temp)) {
                card.answer.audioUriFormatStr = temp;
            }

            int subheadingSize;
            temp = null;
            if (answerObj.containsKey("subheading_size")) {
                temp = (String) answerObj.get("subheading_size");
            }
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                subheadingSize =  Integer.parseInt(temp);

            } else {
                subheadingSize = 0;
            }


            int mainSize;
            temp = null;
            if (answerObj.containsKey("main_size")) {
                temp = (String) answerObj.get("main_size");
            }
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                mainSize =  Integer.parseInt(temp);
            } else {
                mainSize = 0;
            }

            int subSize;
            temp = null;
            if (answerObj.containsKey("sub_size"))  {
                temp = (String) answerObj.get("sub_size");
            }
            if ((temp != null) && (StringUtils.isNumeric(temp))) {
                subSize =  Integer.parseInt(temp);
            } else {
                subSize = 0;
            }



            int[] standardCSSArrary = AppContext.getAppContext().getResources().getIntArray(R.array.css_size_int);


            if (currentPack.platform.equals(UIHelper.getCurrentPlatform()) == true) {
                if (subheadingSize >0) {
                    card.answer.css.subheadingSize = subheadingSize;
                } else {
                    card.answer.css.subheadingSize = standardCSSArrary[3];
                }

                if (subheadingSize >0) {
                    card.answer.css.subSize = subSize;
                } else {
                    card.answer.css.mainSize = standardCSSArrary[4];
                }

                if (subheadingSize >0) {
                    card.answer.css.subSize = subSize;
                } else {
                    card.answer.css.subSize = standardCSSArrary[5];
                }


            } else {
                if (mScreenWidthFromSharedDevice == 0) {  // mean no this field in pack json file
                    //-----begin scale down policy with error protection

                    //step1: get which is trustworthy
                    int baseSize = 0;
                    int whichAsBase = 0;  //0, subheading; 1, main; 2. sub

                    if ((subheadingSize == 0) ||(card.answer.subheading.length() == 0)) {
                        card.answer.css.subheadingSize = standardCSSArrary[3];
                        Timber.tag(Global.debugTag).w( "subheadingSize = 0 or subheading.length = 0");
                    } else {
                        baseSize =  subheadingSize;
                        whichAsBase = 0;
                    }


                    if ((subSize == 0) ||(card.answer.sub.length() == 0)) {
                        card.answer.css.subSize = standardCSSArrary[5];
                        Timber.tag(Global.debugTag).w( "subSize = 0 or sub.length = 0");
                    } else {
                        baseSize =  subSize;
                        whichAsBase =2;
                    }

                    //put main at last is very important, because it's the most trustworthy value
                    if ((mainSize == 0) ||(card.answer.main.length() == 0)) {
                        card.answer.css.mainSize = standardCSSArrary[4];
                        Timber.tag(Global.debugTag).w( "mainSize = 0 or main.length = 0");
                    } else {
                        baseSize =  mainSize;
                        whichAsBase = 1;
                    }

                    if (baseSize == 0) {
                        baseSize =  standardCSSArrary[4];
                        whichAsBase = 0;
                    }


                    //step2: scale down
                    //size interchangeable with iOS, and other android devices

                    switch (whichAsBase) {
                        case    0: {

                            //subheadingSize is trustworthy
                            //card.answer.css.subheadingSize will be base value
                            //baseSize is same as subHeadingSize

                            card.answer.css.subheadingSize = standardCSSArrary[3];

                            if ((mainSize == 0) || (card.answer.main.length() == 0)) {
                                card.answer.css.mainSize = standardCSSArrary[4];
                            } else {
                                card.answer.css.mainSize = (int)(card.answer.css.subheadingSize * ((float)mainSize/baseSize));
                            }

                            if ((subSize == 0) || (card.answer.sub.length() == 0)) {
                                card.answer.css.subSize = standardCSSArrary[5];
                            } else {
                                card.answer.css.subSize = (int)(card.answer.css.subheadingSize * ((float)subSize/baseSize));
                            }

                            break;
                        }

                        case    1: {

                            //mainSize is trustworthy
                            //card.answer.css.mainSize will be base value
                            //baseSize is same as mainSize


                            //when the mainSize is quite big
                            if (mainSize > standardCSSArrary[6]) {
                                card.answer.css.mainSize = mainSize;
                            }  else {
                                card.answer.css.mainSize = standardCSSArrary[4];
                            }


                            if ((subheadingSize == 0) || (card.answer.subheading.length() == 0)) {
                                card.answer.css.subheadingSize = standardCSSArrary[3];
                            } else {
                                card.answer.css.subheadingSize = (int)(card.answer.css.mainSize * ((float)subheadingSize/baseSize));
                            }

                            if ((subSize == 0) || (card.answer.sub.length() == 0)) {
                                card.answer.css.subSize = standardCSSArrary[5];
                            } else {
                                card.answer.css.subSize = (int)(card.answer.css.mainSize * ((float)subSize/baseSize));
                            }

                            break;
                        }

                        case    2: {

                            //subSize is trustworthy
                            //card.answer.css.subSize will be base value
                            //baseSize is same as subSize

                            card.answer.css.subSize = standardCSSArrary[4];

                            if ((subheadingSize == 0) || (card.answer.subheading.length() == 0)) {
                                card.answer.css.subheadingSize = standardCSSArrary[3];
                            } else {
                                card.answer.css.subheadingSize = (int)(card.answer.css.subSize * ((float)subheadingSize/baseSize));
                            }

                            if ((mainSize == 0) || (card.answer.main.length() == 0)) {
                                card.answer.css.mainSize = standardCSSArrary[4];
                            } else {
                                card.answer.css.mainSize = (int)(card.answer.css.subSize * ((float)mainSize/baseSize));
                            }

                            break;
                        }

                        default:
                            break;
                    }

                    //-----end scale down policy with error protection
                } else {

                    float bestFontSizeFromSharedDevice = UIHelper.getBestFontSize(mScreenWidthFromSharedDevice);
                    int baseFontSizeOnCurrentDevice = standardCSSArrary[4];
                    float factor = baseFontSizeOnCurrentDevice/bestFontSizeFromSharedDevice;


                    if (subheadingSize  == 0) {
                      subheadingSize = standardCSSArrary[3];
                    }
                    card.answer.css.subheadingSize = (int)(subheadingSize * factor);

                    if (mainSize == 0) {
                        mainSize = standardCSSArrary[4];
                    }
                    card.answer.css.mainSize = (int)(mainSize * factor);

                    if (subSize == 0) {
                       subSize = standardCSSArrary[5];
                    }
                    card.answer.css.subSize = (int)(subSize * factor);

                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        } catch (IOException e) {
            e.printStackTrace();
            Timber.tag(Global.debugTag).e("Error during parse questionTextContent.json, reason:" + e.getCause());
        }

        return card;
    }


    /*
     *\r\n , \r , \n what is the difference between them: http://stackoverflow.com/questions/15433188/r-n-r-n-what-is-the-difference-between-them
     */
    private static String removeTrailingSpaceAndUnexpectedCharacters(String str) {

        String returnStr = str.replace("\r\n","\n");

        returnStr = returnStr.replace("\r","\n");  //it's strange,but it does here in real case

        returnStr = StringUtils.removeAllLinesTrailingSpace(returnStr);

        return returnStr;

    }
}
