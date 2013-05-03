package com.internectics.model;

import android.net.Uri;
import android.util.Log;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardListModel {

	public static List<HashMap<String, Object>> getCardList(Pack curentPack) {

		ArrayList<Card> cardArrayList = new ArrayList<Card>();

		// Use simulator data only for test purpose
//		for (int i = 0; i < 5; i++) {
//			Card tempCard = new Card();
//			 tempCard.cardSN = 1;
//			 tempCard.coverImageURL =
//			 "file:///data/data/com.internectics.android_flashcardcreator/cache/Images/af9e10f9-a1e8-47e0-ba1c-e0685c29f602.jpg";
//			 cardArrayList.add(tempCard);
//			 Card tempCard2 = new Card();
//			 tempCard2.cardSN = 2;
//			 tempCard2.coverImageURL = String.format("%d",R.drawable.card_cover_image_placeholder);
//			 cardArrayList.add(tempCard2);
//		}

		cardArrayList = curentPack.cards;

		// Build data for adapter
		List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < cardArrayList.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			Card card = cardArrayList.get(i);
			map.put("cardSN", card.cardSN);
			if (!StringUtils.isNumeric(card.coverImageURL)) {
				// standard Uri
				map.put("coverImageUriStr", Uri.parse(card.coverImageURL));
			} else {
				String localResourceUriStr = StringUtils.convertToUriStr(card.coverImageURL);
				map.put("coverImageUriStr", Uri.parse(localResourceUriStr));
			}
			fillMaps.add(map);
		}
		return fillMaps;
	}

    public  static ArrayList<Pack> getAllPacks() {
        return  User.defaultUser(AppContext.getAppContext()).packs;
    }

    /**
     * if no existing pack, return null
     */
    public static Pack getCurrentPack() {
		Pack currentPack = null;
		String packIDString = AppConfig.getInstance(
                AppContext.getAppContext()).get(Global.packID_Property);

		ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;

        //case1: no pack
        if (packs.size() == 0)
            return null;

        //case2: existing last saved pack
        for (int i = 0; i < packs.size(); i++) {
			if (packs.get(i).packID == Integer.parseInt(packIDString)) {
				currentPack = packs.get(i);
				Log.d(Global.debugTag, "latest Pack's ID is:" + packIDString);
				return currentPack;
			}
		}

        //case3: return first pack
        currentPack = packs.get(0);

		return currentPack;
	}

}
