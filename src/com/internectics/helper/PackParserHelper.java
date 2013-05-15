package com.internectics.helper;

import android.net.Uri;
import android.util.Log;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PackParserHelper {

    /**
     * after finshing pack download and unzip, this methold will be called to build pack and add to current user
     */
    public static void parse() {

        //step1: save pack
        File packJsonFile = new File(FileOperationHelper.downloadedPackDirectory(), "packInformation.json");
        Pack resultPack = parsePackJsonFile(packJsonFile.toString());

        FileOperationHelper.copyImageToImagesFolder(getPackImageFullPath(resultPack.coverImageUriFormatStr));
        FileOperationHelper.copyImageToImagesFolder(getPackImageFullPath(resultPack.logoImageUriFormatStr));

        resultPack.save(AppContext.getAppContext());

        //step2: save cards
        for (int i = 0; ; i++) {
            File cardDirectory = new File(FileOperationHelper.downloadedPackDirectory(), String.format("card%d", i));
            if (!cardDirectory.exists()) {
                break;
            }
            File cardJsonFile = new File(cardDirectory, "cardTextContent.json");
            Card resultCard = parseCardJsonFile(cardJsonFile.toString());

            FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.coverImageUriFormatStr, i));
            FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.question.imageUriFormatStr, i));
            FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.answer.imageUriFormatStr, i));

            resultCard.packID = resultPack.packID; //this is necessary

            resultCard.save(AppContext.getAppContext());

            try {
                Thread.sleep(1000);
                //TODO it's a temporary solution,
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Special purpose for get card related image under unzipped downloaded pack folder
     * Images include: cover image of card, image of question card, image of answer card
     */
    private static File getCardImageFullPath(String uriFormatStr, int indexOfCard) {
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
     * parse downloaded pack JSON file into Pack
     */
    private static Pack parsePackJsonFile(String packJsonFile) {

        Pack pack = new Pack();

        JSONParser parser = new JSONParser();
        FileReader reader;
        try {
            reader = new FileReader(packJsonFile);
            JSONObject obj = (JSONObject) parser.parse(reader);
            pack.userID = ((Long) obj.get("user_id")).intValue();
            pack.packName = (String) obj.get("pack_name");
            pack.sidebarTitle = (String) obj.get("sidebar_title");
            pack.coverImageUriFormatStr = (String) obj.get("cover_image");
            pack.creatorID = (String) obj.get("creator_id");
            pack.creatorNickName = (String) obj.get("creator_nick_name");
            pack.logoImageUriFormatStr = (String) obj.get("logo_image");
            pack.logoURL = (String) obj.get("logo_url");
            pack.questionTitle = (String) obj.get("question_title");
            pack.answerTitle = (String) obj.get("answer_title");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return pack;
    }

    /**
     * parse downloaded card JSON file into Card
     */
    private static Card parseCardJsonFile(String cardJsonFile) {
        Card card = new Card();
        JSONParser parser = new JSONParser();
        try {
            FileReader fileReader = new FileReader(cardJsonFile);
            JSONArray obj = (JSONArray) parser.parse(fileReader);

            JSONObject summaryObj = (JSONObject) obj.get(0);
            JSONObject questionObj = (JSONObject) obj.get(1);
            JSONObject answerObj = (JSONObject) obj.get(2);

            card.cardSN = ((Long) summaryObj.get("card_sn")).intValue();
            card.coverImageUriFormatStr = (String) summaryObj.get("cover_image");
            card.templateBackground = (String) summaryObj.get("template_background");

            card.question.imageUriFormatStr = (String) questionObj.get("image");
            card.question.subheading = (String) questionObj.get("subheading");
            card.question.main = (String) questionObj.get("main");
            card.question.sub = (String) questionObj.get("sub");
            card.question.templateID = ((Long) questionObj.get("template_id")).intValue();
            card.question.css.subheadingAlign = (String) questionObj.get("subheading_align");
            card.question.css.subheadingColor = (String) questionObj.get("subheading_color");
            card.question.css.subheadingSize = ((Long) questionObj.get("subheading_size")).intValue();
            card.question.css.mainAlign = (String) questionObj.get("main_align");
            card.question.css.mainColor = (String) questionObj.get("main_color");
            card.question.css.mainSize = ((Long) questionObj.get("main_size")).intValue();
            card.question.css.subAlign = (String) questionObj.get("sub_align");
            card.question.css.subColor = (String) questionObj.get("sub_color");
            card.question.css.subSize = ((Long) questionObj.get("sub_size")).intValue();

            card.answer.imageUriFormatStr = (String) answerObj.get("image");
            card.answer.subheading = (String) answerObj.get("subheading");
            card.answer.main = (String) answerObj.get("main");
            card.answer.sub = (String) answerObj.get("sub");
            card.answer.templateID = ((Long) answerObj.get("template_id")).intValue();
            card.answer.css.subheadingAlign = (String) answerObj.get("subheading_align");
            card.answer.css.subheadingColor = (String) answerObj.get("subheading_color");
            card.answer.css.subheadingSize = ((Long) answerObj.get("subheading_size")).intValue();
            card.answer.css.mainAlign = (String) answerObj.get("main_align");
            card.answer.css.mainColor = (String) answerObj.get("main_color");
            card.answer.css.mainSize = ((Long) answerObj.get("main_size")).intValue();
            card.answer.css.subAlign = (String) answerObj.get("sub_align");
            card.answer.css.subColor = (String) answerObj.get("sub_color");
            card.answer.css.subSize = ((Long) answerObj.get("sub_size")).intValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return card;
    }
}
