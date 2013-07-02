package com.internectics.helper;

import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.util.StringUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PackBuildHelper {

    public static File createPackZipFile(Pack currentPack) {

        ArrayList<String> cardFiles = new ArrayList<String>();
        ArrayList<String> packFiles = new ArrayList<String>();

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
            cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.coverImageUriFormatStr));
            cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.question.imageUriFormatStr));
            if (!(card.question.imageUriFormatStr.equals(card.answer.imageUriFormatStr))) {
                //for history reason, in iOS version, this data could be same (answer_placeholder_content.jpg)
                cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(card.answer.imageUriFormatStr));
            }
            cardFiles.add(FileOperationHelper.deleteUriSchemeHeader(currentPack.logoImageUriFormatStr));
            File cardZipFile = new File(FileOperationHelper.uploadPackDirectory(), String.format("card%d.zip", i));
            try {
                ZipFileHelper.zipPackFiles(cardZipFile.toString(), cardFiles);
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //step2: add this new zip file to packFiles
            packFiles.add(cardZipFile.toString());

            //step3: reset
            cardFiles.clear();
        }

        //step3:
        packFiles.add(FileOperationHelper.deleteUriSchemeHeader(currentPack.coverImageUriFormatStr));
        String singleFile = PackBuildHelper.buildPackJsonFile(currentPack).toString();
        packFiles.add(singleFile);
        File packZipFile = FileOperationHelper.generateUniquePackZipFilePathForUploading();
        try {
            ZipFileHelper.zipPackFiles(packZipFile.toString(), packFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packZipFile;
    }

    /*
    * Result saved file path is: FileOperationHelper.getUploadPackJsonFile()
    */
    public static File buildPackJsonFile(Pack pack) {
        JSONObject summary = new JSONObject();
        summary.put("pack_name", pack.packName);
        summary.put("sidebar_title", pack.sidebarTitle);
        summary.put("cover_image", StringUtils.lastComponentOfPath(pack.coverImageUriFormatStr));
        summary.put("creator", pack.creatorID);
        summary.put("creator_nick_name", pack.creatorNickName);
        summary.put("platform", pack.platform);
        summary.put("logo_image", StringUtils.lastComponentOfPath(pack.logoImageUriFormatStr));
        summary.put("platform", "Android");//TODO

        FileWriter file;
        File savedPath = FileOperationHelper.getUploadPackJsonFile();
        try {
            file = new FileWriter(savedPath, false);
            file.write(summary.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + summary);
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

        obj.put("title", String.format("%d", card.cardSN));
        obj.put("template_background", card.templateBackground);
        obj.put("cover_image", StringUtils.lastComponentOfPath(card.coverImageUriFormatStr));

        obj.put("image", StringUtils.lastComponentOfPath(card.question.imageUriFormatStr));
        obj.put("template_id", String.format("%d", card.question.templateID));
        obj.put("subheading", card.question.subheading);
        obj.put("main", card.question.main);
        obj.put("sub", card.question.sub);
        obj.put("subheading_align", card.question.css.subheadingAlign);
        obj.put("subheading_color", card.question.css.subheadingColor);
        obj.put("subheading_size", card.question.css.subheadingSize);
        obj.put("main_align", card.question.css.mainAlign);
        obj.put("main_color", card.question.css.mainColor);
        obj.put("main_size", card.question.css.mainSize);
        obj.put("sub_align", card.question.css.subAlign);
        obj.put("sub_color", card.question.css.subColor);
        obj.put("sub_size", card.question.css.subSize);

        File savedPath = FileOperationHelper.getUploadCardQuestionJsonFile();
        FileWriter file;
        try {
            file = new FileWriter(savedPath, false);
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);
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
        obj.put("subheading", card.answer.subheading);
        obj.put("main", card.answer.main);
        obj.put("sub", card.answer.sub);
        obj.put("template_id", card.answer.templateID);
        obj.put("subheading_align", card.answer.css.subheadingAlign);
        obj.put("subheading_color", card.answer.css.subheadingColor);
        obj.put("subheading_size", card.answer.css.subheadingSize);
        obj.put("main_align", card.answer.css.mainAlign);
        obj.put("main_color", card.answer.css.mainColor);
        obj.put("main_size", card.answer.css.mainSize);
        obj.put("sub_align", card.answer.css.subAlign);
        obj.put("sub_color", card.answer.css.subColor);
        obj.put("sub_size", card.answer.css.subSize);

        File savedPath = FileOperationHelper.getUploadCardAnswerJsonFile();
        FileWriter file;
        try {
            file = new FileWriter(savedPath, false);
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedPath;
    }
}
