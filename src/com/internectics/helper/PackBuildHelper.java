package com.internectics.helper;

import com.internectics.data.Card;
import com.internectics.data.Pack;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PackBuildHelper {

    public static File createPackZipFile(Pack currentPack) {
        ArrayList<String> cardFiles = new ArrayList<String>();
        ArrayList <String> packFiles = new ArrayList<String>();

        int numberOfCards = currentPack.cards.size();
        int i = 0;

        //step1
        for (Card card: currentPack.cards) {
            //step1: zip all the files in current card and come into a new zip file
            String singleFile = PackBuildHelper.buildCardJsonFile(card).toString();
            cardFiles.add(singleFile);
            cardFiles.add(card.coverImageURL);
            cardFiles.add(card.question.imageURL);
            cardFiles.add(card.answer.imageURL);
            File cardZipFile = new File(FileOperationHelper.uploadPackDirectory(),String.format("%d.zip",i));
            try {
                ZipFileHelper.zipFiles(cardZipFile.toString(),cardFiles);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //step2: add this new zip file to packFiles
            packFiles.add(cardZipFile.toString());

            //step3: reset
            cardFiles.clear();
        }

        //step2:
        packFiles.add(currentPack.coverImageUriStr);
        packFiles.add(currentPack.logoImageUriStr);
        String singleFile = PackBuildHelper.buildPackJsonFile(currentPack).toString();
        packFiles.add(singleFile);
        File packZipFile = new File(FileOperationHelper.uploadPackDirectory(),null);
        try {
            ZipFileHelper.zipFiles(packZipFile.toString(),packFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packZipFile;
    }

    /*
    * Result saved file path is: FileOperationHelper.getUploadPackJsonFile()
    */
    public static File buildPackJsonFile(Pack pack){
        JSONObject summary = new JSONObject();
        summary.put("pack_name", pack.packName);
        summary.put("sidebar_title", pack.sidebarTitle);
        summary.put("cover_image", pack.coverImageUriStr);
        summary.put("creator_id", pack.creatorID);
        summary.put("creator_nick_name", pack.creatorNickName);
        summary.put("logo_image", pack.logoImageUriStr);
        summary.put("logo_url", pack.logoURL);
        summary.put("question_title", pack.questionTitle);
        summary.put("answer_title", pack.answerTitle);

        FileWriter file;
        File savedPath =  FileOperationHelper.getUploadPackJsonFile();
        try {
            file = new FileWriter(savedPath,true);
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
     Result saved file path is: FileOperationHelper.getUploadCardJsonFile()
    */
    public static File buildCardJsonFile(Card card){

        JSONArray obj = new JSONArray();
        JSONObject summary = new JSONObject();
        JSONObject question = new JSONObject();
        JSONObject answer = new JSONObject();
        obj.add(summary);
        obj.add(question);
        obj.add(answer);

        summary.put("card_sn", card.cardSN);
        summary.put("cover_image", card.coverImageURL);
        summary.put("template_background", card.templateBackground);

        question.put("image", card.question.imageURL);
        question.put("subheading", card.question.subheading);
        question.put("main", card.question.main);
        question.put("sub", card.question.sub);
        question.put("template_id", card.question.templateID);
        question.put("subheading_align", card.question.css.subheadingAlign);
        question.put("subheading_color", card.question.css.subheadingColor);
        question.put("subheading_size", card.question.css.subheadingSize);
        question.put("main_align", card.question.css.mainAlign);
        question.put("main_color", card.question.css.mainColor);
        question.put("main_size", card.question.css.mainSize);
        question.put("sub_align", card.question.css.subAlign);
        question.put("sub_color", card.question.css.subColor);
        question.put("sub_size", card.question.css.subSize);

        answer.put("image", card.answer.imageURL);
        answer.put("subheading", card.answer.subheading);
        answer.put("main", card.answer.main);
        answer.put("sub", card.answer.sub);
        answer.put("template_id", card.answer.templateID);
        answer.put("subheading_align", card.answer.css.subheadingAlign);
        answer.put("subheading_color", card.answer.css.subheadingColor);
        answer.put("subheading_size", card.answer.css.subheadingSize);
        answer.put("main_align", card.answer.css.mainAlign);
        answer.put("main_color", card.answer.css.mainColor);
        answer.put("main_size", card.answer.css.mainSize);
        answer.put("sub_align", card.answer.css.subAlign);
        answer.put("sub_color", card.answer.css.subColor);
        answer.put("sub_size", card.answer.css.subSize);

        File savedPath = FileOperationHelper.getUploadCardJsonFile();
        FileWriter file;
        try {
            file = new FileWriter(savedPath,true);
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
