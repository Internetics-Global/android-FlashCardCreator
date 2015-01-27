package com.internectics.model;

import android.net.Uri;

import com.internectics.data.Card;
import com.internectics.data.Pack;
import com.internectics.data.User;
import com.internectics.util.AppContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CardListModel {

    /**
     * Return card list for current pack
     * returned List is re-sorted by cardSN, rather than others
     * @param curentPack
     * @return non-null return value
     */
    public static List<HashMap<String, Object>> getCardList(Pack curentPack) {

        List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();

        if (curentPack == null) {
            return fillMaps;
        }

        ArrayList<Card> cardArrayList = curentPack.cards;

        Collections.sort(cardArrayList,new Comparator<Card>() {
            @Override
            public int compare(Card lhs, Card rhs) {
                return (lhs.cardSN - rhs.cardSN);  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        for (int i = 0; i < cardArrayList.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            Card card = cardArrayList.get(i);
            map.put("cardSN", card.cardSN);
            map.put("coverImageUriFormatStr", Uri.parse(card.coverImageUriFormatStr));
            fillMaps.add(map);
        }
        return fillMaps;
    }

    public static ArrayList<Pack> getAllPacks() {
        return User.defaultUser(AppContext.getAppContext()).packs;
    }

    public static Pack getLastPack() {

        Pack pack;
        int size = getAllPacks().size();
        if (size > 0) {
            pack = getAllPacks().get(size - 1);
            return pack;
        } else {
            return null;
        }
    }

    /**
     * if no existing pack, return null
     * we set most recently created pack as current pack
     */
    public static Pack getLatestCreatedPack() {
        Pack latestPack;

        ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).sortPacks(0);

        //case1: no pack
        if (packs.size() == 0)
            return null;

        latestPack = packs.get(0);

        return latestPack;
    }

    public static Pack updateCurrentPack(Pack currentPack) {
        Pack returnPack = null;
        ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;

        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).packID == currentPack.packID) {
                returnPack = packs.get(i);
                return returnPack;
            }
        }

        return returnPack;
    }

    public static Card updateCurrentCard(Card currentCard) {
        Card returnCard = null;
        ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;

        for (int i = 0; i < packs.size(); i++) {

            Pack pack = packs.get(i);

            for (int j = 0; j < pack.cards.size(); j++) {
              Card card =  pack.cards.get(j);
              if (card.cardID == currentCard.cardID) {
                  returnCard = card;
                  return returnCard;
              }
            }


        }

        return returnCard;
    }

    public static Pack getPack(int packID) {
        Pack returnPack = null;
        ArrayList<Pack> packs = User.defaultUser(AppContext.getAppContext()).packs;

        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).packID == packID) {
                returnPack = packs.get(i);
                return returnPack;
            }
        }
        return returnPack;
    }

}
