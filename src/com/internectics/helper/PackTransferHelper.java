package com.internectics.helper;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Interpolator;
import android.net.Uri;

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

public class PackTransferHelper {

    private static File packInformationFile;  // represent file of packInformation.json
    private static File answerCardFile; //represnet file of answerCard.json
    private static File questionCardFile; //represent file of questionCard.json


    /*
      will be automatically downloaded to /files/flashcard_downloaded_pack.zip
     */
    public static void download(Context mContext, String downloadUriStr) {
        DownloadManager downloadManager = (DownloadManager) (mContext.getSystemService(Context.DOWNLOAD_SERVICE));
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadUriStr));
        downloadRequest.setTitle("Download a pack");
        downloadRequest.setDescription("from somebody");
        downloadRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        downloadRequest.setVisibleInDownloadsUi(true);
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadRequest.setDestinationInExternalFilesDir(mContext, null, "flashcard_downloaded_pack.zip");
        downloadManager.enqueue(downloadRequest);
    }

    public static void upload(Context mContext, String uploadUriStr) {
        //step1: zip pack


    }


    public static Pack parsePackJsonFile(String packJsonFile) throws JSONException, IOException, ParseException {

        Pack pack = new Pack();

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new FileReader(packJsonFile));

        pack.packName = (String) obj.get("pack_name");
        pack.packName = (String) obj.get("sidebar_title");
        pack.packName = (String) obj.get("cover_image");
        pack.packName = (String) obj.get("creator_id");
        pack.packName = (String) obj.get("creator_nick_name");
        pack.packName = (String) obj.get("logo_image");
        pack.packName = (String) obj.get("logo_url");
        pack.packName = (String) obj.get("question_title");
        pack.packName = (String) obj.get("answer_title");

        return pack;

    }


    public static Card parseCardJsonFile(String cardJsonFile) throws JSONException, IOException, ParseException {
        Card card = new Card();
        JSONParser parser = new JSONParser();
        JSONArray obj = (JSONArray) parser.parse(new FileReader(cardJsonFile));

        JSONObject summaryObj = (JSONObject) obj.get(0);
        JSONObject questionObj = (JSONObject) obj.get(1);
        JSONObject answerObj = (JSONObject) obj.get(2);

        card.cardSN = ((Long)summaryObj.get("card_sn")).intValue();
        card.coverImageURL = (String)summaryObj.get("cover_image");
        card.templateBackground = (String)summaryObj.get("template_background");

        card.question.imageURL = (String)questionObj.get("image");
        card.question.subheading= (String)questionObj.get("subheading");
        card.question.main= (String)questionObj.get("main");
        card.question.sub= (String)questionObj.get("sub");
        card.question.templateID= ((Long)questionObj.get("template_id")).intValue();
        card.question.css.subheadingAlign= (String)questionObj.get("subheading_align");
        card.question.css.subheadingColor= (String)questionObj.get("subheading_color");
        card.question.css.subheadingSize= ((Long)questionObj.get("subheading_size")).intValue();
        card.question.css.mainAlign= (String)questionObj.get("main_align");
        card.question.css.mainColor= (String)questionObj.get("main_color");
        card.question.css.mainSize= ((Long)questionObj.get("main_size")).intValue();
        card.question.css.subAlign= (String)questionObj.get("sub_align");
        card.question.css.subColor= (String)questionObj.get("sub_color");
        card.question.css.subSize= ((Long)questionObj.get("sub_size")).intValue();

        card.answer.imageURL= (String)answerObj.get("image");
        card.answer.subheading= (String)answerObj.get("subheading");
        card.answer.main= (String)answerObj.get("main");
        card.answer.sub= (String)answerObj.get("sub");
        card.answer.templateID= ((Long)answerObj.get("template_id")).intValue();
        card.answer.css.subheadingAlign= (String)answerObj.get("subheading_align");
        card.answer.css.subheadingColor= (String)answerObj.get("subheading_color");
        card.answer.css.subheadingSize= ((Long)answerObj.get("subheading_size")).intValue();
        card.answer.css.mainAlign= (String)answerObj.get("main_align");
        card.answer.css.mainColor= (String)answerObj.get("main_color");
        card.answer.css.mainSize= ((Long)answerObj.get("main_size")).intValue();
        card.answer.css.subAlign= (String)answerObj.get("sub_align");
        card.answer.css.subColor= (String)answerObj.get("sub_color");
        card.answer.css.subSize= ((Long)answerObj.get("sub_size")).intValue();

        return card;
    }

    public static void buildPackJsonFile(Pack pack, String savedFileName) throws JSONException, IOException {
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

        FileWriter file = new FileWriter(savedFileName);
        try {
            file.write(summary.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + summary);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            file.flush();
            file.close();
        }

    }


    public static void buildCardJsonFile(Card card, String savedFileName) throws JSONException, IOException {

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

        FileWriter file = new FileWriter(savedFileName);
        try {
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            file.flush();
            file.close();
        }
    }
}
