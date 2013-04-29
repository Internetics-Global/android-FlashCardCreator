package com.internectics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.net.Uri;
import android.util.Log;

import com.internectics.android_flashcardcreator.R;
import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppConfig;
import com.internectics.util.AppContext;
import com.internectics.util.Global;
import com.internectics.util.StringUtils;

public class CardListModel {

	public static List<HashMap<String, Object>> getCardList(Pack curentPack) {

		ArrayList<Card> cardArrayList = new ArrayList<Card>();

		// Use simulator data only for test purpose
		 Card tempCard = new Card();
		 tempCard.cardSN = 1;
		 tempCard.coverImageURL =
		 "file:///data/data/com.internectics.android_flashcardcreator/cache/Images/53837dc1-4633-4e5d-9126-c178426ac5e3.jpg";
		 cardArrayList.add(tempCard);
		 Card tempCard2 = new Card();
		 tempCard2.cardSN = 2;
		 tempCard2.coverImageURL = String.format("%d",
		 R.drawable.card_cover_image_placeholder);
		 cardArrayList.add(tempCard2);
		 Card tempCard3 = new Card();
		 tempCard3.cardSN = 3;
		 tempCard3.coverImageURL =
		 "file:///data/data/com.internectics.android_flashcardcreator/cache/Images/53837dc1-4633-4e5d-9126-c178426ac5e3.jpg";
		 cardArrayList.add(tempCard3);

		//cardArrayList = curentPack.cards;

		// Build data for adapter
		List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < cardArrayList.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			Card card = cardArrayList.get(i);
			map.put("cardSN", card.cardSN);
			if (!StringUtils.isNumeric(card.coverImageURL)) {
				// standard Uri
				map.put("coverImageURL", Uri.parse(card.coverImageURL));
			} else {
				// local resources
				String localResourceUriStr = "android.resource://"
						+ AppContext.getAppContext().getPackageName() + "/"
						+ card.coverImageURL;
				Log.d(Global.debugTag, localResourceUriStr);
				map.put("coverImageURL", Uri.parse(localResourceUriStr));
			}
			fillMaps.add(map);
		}
		return fillMaps;
	}

	public static Pack getCurrentPack() {
		Pack currentPack = null;
		String packIDString = AppConfig.getAppConfigInstance(
				AppContext.getAppContext()).get(Global.packID_Property);

		ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;
		for (int i = 0; i < packs.size(); i++) {
			if (packs.get(i).packID == Integer.parseInt(packIDString)) {
				currentPack = packs.get(i);
				Log.d(Global.debugTag, "latest Pack's ID is:" + packIDString);
				break;
			}
		}

		return currentPack;
	}

}
