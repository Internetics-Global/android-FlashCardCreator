package com.internectics.helper;

import android.net.Uri;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;
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

        File newFile;

        //step1: save pack
        File packJsonFile = new File(FileOperationHelper.downloadedPackDirectory(), "packInformation.json");
        Pack resultPack = parsePackJsonFile(packJsonFile.toString());

        //step2: save cards
        for (int i = 0; ; i++) {
            File cardDirectory = new File(FileOperationHelper.downloadedPackDirectory(), String.format("card%d", i));
            if (!cardDirectory.exists()) {
                break;
            }
            Card resultCard = parseCardJsonFiles(cardDirectory,resultPack);


            newFile = FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.coverImageUriFormatStr, i));
            resultCard.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);

            newFile = FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.question.imageUriFormatStr, i));
            resultCard.question.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
            newFile = FileOperationHelper.copyImageToImagesFolder(getCardImageFullPath(resultCard.answer.imageUriFormatStr, i));
            resultCard.answer.imageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);

            resultCard.packID = resultPack.packID; //this is necessary

            resultCard.save(AppContext.getAppContext());

        }

        newFile = FileOperationHelper.copyImageToImagesFolder(getPackImageFullPath(resultPack.coverImageUriFormatStr));
        resultPack.coverImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
        newFile = FileOperationHelper.copyImageToImagesFolder(getLogoImageFullPath(resultPack.logoImageUriFormatStr));
        resultPack.logoImageUriFormatStr = FileOperationHelper.convertToUriFormatFile(newFile);
        resultPack.save(AppContext.getAppContext());
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
     * Because of history reason, the card logo image belong to card, rather than to pack.
     * All the log images are same under same package, so we only need to take one
     */
    private static File getLogoImageFullPath(String uriFormatStr) {
        return getCardImageFullPath(uriFormatStr,0);
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
            pack.packName = (String) obj.get("pack_name");
            pack.sidebarTitle = (String) obj.get("sidebar_title");
            pack.coverImageUriFormatStr = (String) obj.get("cover_image");
            pack.creatorID = (String) obj.get("creator");
            pack.creatorNickName = (String) obj.get("creator_nick_name");
            pack.userID = Global.USER_ID; // there's no this information in json file, so we have to add manually
            pack.packID = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
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
            FileReader fileReader = new FileReader(questionJsonFile);
            JSONObject questionObj = (JSONObject) parser.parse(fileReader);

            //this is a history issue(to compatibible with iOS version), i have to get logoImageUriFormatStr from question
            //Better way is to put them in pack
            currentPack.logoImageUriFormatStr = (String) questionObj.get("logo");
            currentPack.logoURL = (String) questionObj.get("logo_url");
            currentPack.questionTitle = (String) questionObj.get("title");

            card.cardSN = Integer.parseInt((String) questionObj.get("cardSN"));
            card.coverImageUriFormatStr = (String) questionObj.get("cover_image");
            card.templateBackground = (String) questionObj.get("template_background");

            card.question.templateID = Integer.parseInt((String) questionObj.get("template_id"));
            card.question.imageUriFormatStr = (String) questionObj.get("image");
            card.question.subheading = (String) questionObj.get("subheading");
            card.question.main = (String) questionObj.get("main");
            card.question.sub = (String) questionObj.get("sub");
            card.question.css.subheadingAlign = (String) questionObj.get("subheading_align");
            card.question.css.subheadingColor = (String) questionObj.get("subheading_color");
            card.question.css.subheadingSize = Integer.parseInt((String) questionObj.get("subheading_size"));
            card.question.css.mainAlign = (String) questionObj.get("main_align");
            card.question.css.mainColor = (String) questionObj.get("main_color");
            card.question.css.mainSize = Integer.parseInt((String) questionObj.get("main_size"));
            card.question.css.subAlign = (String) questionObj.get("sub_align");
            card.question.css.subColor = (String) questionObj.get("sub_color");
            card.question.css.subSize = Integer.parseInt((String) questionObj.get("sub_size"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Answer
        try {
            FileReader fileReader = new FileReader(answerJsonFile);
            JSONObject answerObj = (JSONObject) parser.parse(fileReader);

            currentPack.answerTitle= (String) answerObj.get("title");

            card.answer.templateID = Integer.parseInt((String) answerObj.get("template_id"));
            card.answer.imageUriFormatStr = (String) answerObj.get("image");
            card.answer.subheading = (String) answerObj.get("subheading");
            card.answer.main = (String) answerObj.get("main");
            card.answer.sub = (String) answerObj.get("sub");
            card.answer.css.subheadingAlign = (String) answerObj.get("subheading_align");
            card.answer.css.subheadingColor = (String) answerObj.get("subheading_color");
            card.answer.css.subheadingSize = Integer.parseInt((String) answerObj.get("subheading_size"));
            card.answer.css.mainAlign = (String) answerObj.get("main_align");
            card.answer.css.mainColor = (String) answerObj.get("main_color");
            card.answer.css.mainSize = Integer.parseInt((String) answerObj.get("main_size"));
            card.answer.css.subAlign = (String) answerObj.get("sub_align");
            card.answer.css.subColor = (String) answerObj.get("sub_color");
            card.answer.css.subSize = Integer.parseInt((String) answerObj.get("sub_size"));
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
