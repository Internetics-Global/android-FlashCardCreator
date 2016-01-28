package com.flipflash.helper;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.flipflash.data.Card;
import com.flipflash.data.Pack;
import com.flipflash.util.StringUtils;
import com.flipflash.util.UIHelper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.flipflash.util.LogUtils.LOGE;


public class PackBuildHelper {

    private static final String TAG = PackBuildHelper.class.getSimpleName();

    private static Activity mActivity;

    /*
     * Generate a new file name if currentPack.fileNameOnAWS = null
     */
    public static File createPackZipFile(Activity activity,Pack currentPack, String password) {

        mActivity = activity;

        ArrayList<String> cardFiles = new ArrayList<String>();
        ArrayList<File> packFiles = new ArrayList<File>();

        int i = 0;

        //step1,delete current content
        FileOperationHelper.deleteFolder(FileOperationHelper.uploadPackDirectory());

        //step2
        for (Card card : currentPack.cards) {
            //step1: zip all the files in current card and come into a new zip file
            String singleFile = PackBuildHelper.buildCardQuestionJsonFile(card, currentPack).toString();
            cardFiles.add(singleFile);
            singleFile = PackBuildHelper.buildCardAnswerJsonFile(card, currentPack).toString();
            cardFiles.add(singleFile);

            if (card.coverImageUriFormatStr.length() > 0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.coverImageUriFormatStr));
            }

            if (currentPack.logoImageUriFormatStr.length() > 0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(currentPack.logoImageUriFormatStr));
            }



            if (card.question.imageUriFormatStr.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.imageUriFormatStr));
            }

            if (card.question.imageUriFormatStr2.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.imageUriFormatStr2));
            }

            if (card.question.backgroundImageUriFormatStr.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.backgroundImageUriFormatStr));
            }

            if ((card.question.movieUriFormatStr.length() >0) && (card.question.movieUriFormatStr.contains("http://") == false) && (card.question.movieUriFormatStr.contains("https://") == false)) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.movieUriFormatStr));
            }

            if ((card.question.movieUriFormatStr2.length() >0) && (card.question.movieUriFormatStr2.contains("http://") == false) && (card.question.movieUriFormatStr2.contains("https://") == false)) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.movieUriFormatStr2));
            }

            if (card.question.audioUriFormatStr.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.audioUriFormatStr));
            }




            if (!(card.question.imageUriFormatStr.equals(card.answer.imageUriFormatStr))) {
                //for history reason, in iOS version, this data could be same (answer_placeholder_content.jpg)
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.imageUriFormatStr));
            }

            if (!(card.question.imageUriFormatStr2.equals(card.answer.imageUriFormatStr2))) {
                //for history reason, in iOS version, this data could be same (answer_placeholder_content.jpg)
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.imageUriFormatStr2));
            }

            if (card.answer.backgroundImageUriFormatStr.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.backgroundImageUriFormatStr));
            }

            if ((card.answer.movieUriFormatStr.length() >0) && (card.answer.movieUriFormatStr.contains("http://") == false) && (card.answer.movieUriFormatStr.contains("https://") == false)) {  //有可能只是类似youtube的链接，而不是本地文件
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.movieUriFormatStr));
            }

            if ((card.answer.movieUriFormatStr2.length() >0) && (card.answer.movieUriFormatStr2.contains("http://") == false) && (card.answer.movieUriFormatStr2.contains("https://") == false)) {  //有可能只是类似youtube的链接，而不是本地文件
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.movieUriFormatStr2));
            }

            if (card.answer.audioUriFormatStr.length() >0) {
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.audioUriFormatStr));
            }

            //remove duplicated
            Set<String> hs = new HashSet<>();
            hs.addAll(cardFiles);
            cardFiles.clear();
            cardFiles.addAll(hs);

            File cardZipFile = new File(FileOperationHelper.uploadPackDirectory(), String.format("card%d.zip", i));
            try {
                ZipFileHelper.zipPackFiles(cardZipFile.toString(), cardFiles);
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            //step2: add this new zip file to packFiles
            packFiles.add(cardZipFile);

            //step3: reset
            cardFiles.clear();
        }

        //step3:
        packFiles.add(new File(FileOperationHelper.deleteUriSchemeHeader(currentPack.coverImageUriFormatStr)));
        File jsonPackFile = PackBuildHelper.buildPackJsonFile(currentPack);
        packFiles.add(jsonPackFile);

        File packZipFile;
        if (StringUtils.isEmpty(currentPack.fileNameOnAWS) || currentPack.fileNameOnAWS.toLowerCase().startsWith("pack")) {
            packZipFile = FileOperationHelper.generateUniquePackZipFilePathForUploading(currentPack);
            currentPack.fileNameOnAWS = packZipFile.getName();
            currentPack.save(mActivity);
        } else {
            packZipFile = new File(FileOperationHelper.uploadPackDirectory(),currentPack.fileNameOnAWS);
        }



        try {

            ZipFile zipFile = new ZipFile(packZipFile.toString());
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            if ((password != null) && (password.length() >0)) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                parameters.setPassword(password);
            } else {
                parameters.setEncryptFiles(false);
            }
            zipFile.addFiles(packFiles, parameters);
        } catch (ZipException e) {
            e.printStackTrace();
            LOGE(TAG, "createPackZipFile: zip pack file failure");
            return null;
        }
        return packZipFile;
    }

    /*
    * Result saved file path is: FileOperationHelper.getUploadPackJsonFile()
    */
    private static File buildPackJsonFile(Pack pack) {
        JSONObject summary = new JSONObject();
        summary.put("pack_id",String.format("%d",pack.packID));
        summary.put("pack_name", pack.packName);
        summary.put("sidebar_title", pack.sidebarTitle);
        summary.put("cover_image", StringUtils.lastComponentOfPath(pack.coverImageUriFormatStr));
        summary.put("creator", pack.creatorID);
        summary.put("creator_nick_name", pack.creatorNickName);
        summary.put("job_title", pack.jobTitle);
        summary.put("platform", pack.platform);
        summary.put("logo_image", StringUtils.lastComponentOfPath(pack.logoImageUriFormatStr));  //历史原因，在iOS中是存储在question/answer中的
        summary.put("platform", "Android");
        summary.put("auto_play_speed",String.format("%d",pack.autoPlaySpeed));
        summary.put("screen_width",String.format("%d", (int)(UIHelper.getScreenWidthDPUnit(mActivity))));

        summary.put("restore_password", pack.restorePassword);

        summary.put("share_link", pack.shareLink);
        summary.put("file_name_on_aws", pack.fileNameOnAWS);

        int versionCode = -1;
        try {
            versionCode = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        summary.put("language_name", String.format("%d",versionCode));  //language_name这个字段暂时不用,我们用来保存build信息,以debug用.与iOS不一样的是,这个不存到sqlite

        FileWriter file;
        File savedPath = FileOperationHelper.getUploadPackJsonFile();
        try {
            file = new FileWriter(savedPath, false);
            file.write(summary.toJSONString());
           // System.out.println("Successfully Copied JSON Object to File...");
           // System.out.println("\nJSON Object: " + summary);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return savedPath;
    }

    /*
     Result saved file path is: FileOperationHelper.getUploadCardQuestionJsonFile()
    */
    public static File buildCardQuestionJsonFile(Card card, Pack pack) {

        JSONObject obj = new JSONObject();

        obj.put("creator", pack.creatorID);
        obj.put("logo_url", pack.logoURL);
        obj.put("title", pack.questionTitle);
        obj.put("logo", StringUtils.lastComponentOfPath(pack.logoImageUriFormatStr));
        obj.put("template_background", card.templateBackground);
        obj.put("cover_image", StringUtils.lastComponentOfPath(card.coverImageUriFormatStr));

        obj.put("background_image", StringUtils.lastComponentOfPath(card.question.backgroundImageUriFormatStr));

        if (StringUtils.isYoutubeLinkage(card.question.movieUriFormatStr)) {
            obj.put("movie", card.question.movieUriFormatStr);
        } else {
            obj.put("movie", StringUtils.lastComponentOfPath(card.question.movieUriFormatStr));
        }

        if (StringUtils.isYoutubeLinkage(card.question.movieUriFormatStr2)) {
            obj.put("movie2", card.question.movieUriFormatStr2);
        } else {
            obj.put("movie2", StringUtils.lastComponentOfPath(card.question.movieUriFormatStr2));
        }


        obj.put("audio", StringUtils.lastComponentOfPath(card.question.audioUriFormatStr));

        obj.put("image", StringUtils.lastComponentOfPath(card.question.imageUriFormatStr));
        obj.put("image2", StringUtils.lastComponentOfPath(card.question.imageUriFormatStr2));
        obj.put("template_id", String.format("%d", card.question.templateID));
        obj.put("subheading", card.question.subheading);
        obj.put("main", card.question.main);
        obj.put("sub", card.question.sub);
        obj.put("line_number_subheading", String.valueOf(card.question.lineNoSubheading));
        obj.put("line_number_main", String.valueOf(card.question.lineNoMain));
        obj.put("line_number_sub", String.valueOf(card.question.lineNoSub));
        obj.put("subheading_align", card.question.css.subheadingAlign);
        obj.put("subheading_align_vertical", card.question.css.subheadingAlignVertical);
        obj.put("subheading_color", card.question.css.subheadingColor);
        obj.put("subheading_size", String.format("%d",(int)card.question.css.subheadingSize));
        obj.put("main_align", card.question.css.mainAlign);
        obj.put("main_align_vertical", card.question.css.mainAlignVertical);
        obj.put("main_color", card.question.css.mainColor);
        obj.put("main_size", String.format("%d",(int)card.question.css.mainSize));
        obj.put("sub_align", card.question.css.subAlign);
        obj.put("sub_align_vertical", card.question.css.subAlignVertical);
        obj.put("sub_color", card.question.css.subColor);
        obj.put("sub_size", String.format("%d",(int)card.question.css.subSize));

        obj.put("subheading_font", card.question.css.subheadingFont);
        obj.put("main_font", card.question.css.mainFont);
        obj.put("sub_font", card.question.css.subFont);

        File savedPath = FileOperationHelper.getUploadCardQuestionJsonFile();
        FileWriter file;
        try {
            file = new FileWriter(savedPath, false);
            file.write(obj.toJSONString());
            //System.out.println("Successfully Copied JSON Object to File...");
            //System.out.println("\nJSON Object: " + obj);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedPath;
    }

    /*
     Result saved file path is: FileOperationHelper.getUploadCardAnswerJsonFile()
    */
    public static File buildCardAnswerJsonFile(Card card, Pack pack) {

        JSONObject obj = new JSONObject();


        obj.put("title", pack.answerTitle);
        obj.put("logo", StringUtils.lastComponentOfPath(pack.logoImageUriFormatStr));
        obj.put("template_id", String.format("%d", card.answer.templateID));

        obj.put("image", StringUtils.lastComponentOfPath(card.answer.imageUriFormatStr));
        obj.put("image2", StringUtils.lastComponentOfPath(card.answer.imageUriFormatStr2));
        obj.put("subheading", card.answer.subheading);
        obj.put("main", card.answer.main);
        obj.put("sub", card.answer.sub);
        obj.put("line_number_subheading", String.valueOf(card.answer.lineNoSubheading));
        obj.put("line_number_main", String.valueOf(card.answer.lineNoMain));
        obj.put("line_number_sub", String.valueOf(card.answer.lineNoSub));
        obj.put("subheading_align", card.answer.css.subheadingAlign);
        obj.put("subheading_align_vertical", card.answer.css.subheadingAlignVertical);
        obj.put("subheading_color", card.answer.css.subheadingColor);
        obj.put("subheading_size", String.format("%d",(int)card.answer.css.subheadingSize));
        obj.put("main_align", card.answer.css.mainAlign);
        obj.put("main_align_vertical", card.answer.css.mainAlignVertical);
        obj.put("main_color", card.answer.css.mainColor);
        obj.put("main_size", String.format("%d",(int)card.answer.css.mainSize));
        obj.put("sub_align", card.answer.css.subAlign);
        obj.put("sub_align_vertical", card.answer.css.subAlignVertical);
        obj.put("sub_color", card.answer.css.subColor);
        obj.put("sub_size", String.format("%d",(int)card.answer.css.subSize));

        obj.put("background_image", StringUtils.lastComponentOfPath(card.answer.backgroundImageUriFormatStr));
        
        if (StringUtils.isYoutubeLinkage(card.answer.movieUriFormatStr)) {
            obj.put("movie", card.answer.movieUriFormatStr);
        } else {
            obj.put("movie", StringUtils.lastComponentOfPath(card.answer.movieUriFormatStr));
        }

        if (StringUtils.isYoutubeLinkage(card.answer.movieUriFormatStr2)) {
            obj.put("movie2", card.answer.movieUriFormatStr2);
        } else {
            obj.put("movie2", StringUtils.lastComponentOfPath(card.answer.movieUriFormatStr2));
        }

        obj.put("audio", StringUtils.lastComponentOfPath(card.answer.audioUriFormatStr));

        obj.put("subheading_font", card.answer.css.subheadingFont);
        obj.put("main_font", card.answer.css.mainFont);
        obj.put("sub_font", card.answer.css.subFont);

        File savedPath = FileOperationHelper.getUploadCardAnswerJsonFile();
        FileWriter file;
        try {
            file = new FileWriter(savedPath, false);
            file.write(obj.toJSONString());
            //System.out.println("Successfully Copied JSON Object to File...");
            //System.out.println("\nJSON Object: " + obj);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedPath;
    }
}
