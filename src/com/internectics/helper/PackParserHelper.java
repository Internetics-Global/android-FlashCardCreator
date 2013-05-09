package com.internectics.helper;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PackParserHelper {

    /*
     * parse downloaded pack JSON file into Pack
     */
    public static Pack parsePackJsonFile(String packJsonFile){

        Pack pack = new Pack();

        JSONParser parser = new JSONParser();
        FileReader reader;
        try {
            reader = new FileReader(packJsonFile);
            JSONObject obj = (JSONObject) parser.parse(reader);

            pack.packName = (String) obj.get("pack_name");
            pack.packName = (String) obj.get("sidebar_title");
            pack.packName = (String) obj.get("cover_image");
            pack.packName = (String) obj.get("creator_id");
            pack.packName = (String) obj.get("creator_nick_name");
            pack.packName = (String) obj.get("logo_image");
            pack.packName = (String) obj.get("logo_url");
            pack.packName = (String) obj.get("question_title");
            pack.packName = (String) obj.get("answer_title");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return pack;
    }

    /*
     * parse downloaded card JSON file into Card
     */
    public static Card parseCardJsonFile(String cardJsonFile){
        Card card = new Card();
        JSONParser parser = new JSONParser();
        try {
            FileReader fileReader = new FileReader(cardJsonFile);
            JSONArray obj = (JSONArray) parser.parse(fileReader);

            JSONObject summaryObj = (JSONObject) obj.get(0);
            JSONObject questionObj = (JSONObject) obj.get(1);
            JSONObject answerObj = (JSONObject) obj.get(2);

            card.cardSN = ((Long) summaryObj.get("card_sn")).intValue();
            card.coverImageURL = (String) summaryObj.get("cover_image");
            card.templateBackground = (String) summaryObj.get("template_background");

            card.question.imageURL = (String) questionObj.get("image");
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

            card.answer.imageURL = (String) answerObj.get("image");
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return card;
    }
}
