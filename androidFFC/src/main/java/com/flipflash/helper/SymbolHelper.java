package com.flipflash.helper;

/**
 * Created with IntelliJ IDEA.
 * User: bournewang
 * Date: 13-7-2
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class SymbolHelper {

    private static final String TAG = SymbolHelper.class.getName();

    /**
     * 这是3个特殊的symbol，占用了普通的两倍距离，且有不同的含义
     */
    public final static String    K_Space_Bar_Lowcase   = "space bar";
    public final static String    K_Line_Break_Lowcase  = "line break";
    public final static String    K_Delete_Lowcase      = "delete";

    public static String[] mUnicodeArray = {
            "0","1","2","3","4","5","6","7","8","9","Space Bar",
            "⨯","+","÷","−","=","π","mb","%","r","d","Line Break",
            ">","<","m³","m²","ft²","ft³","°C","°F","°K","°R","Delete",
            "O₂","r²","±",
            "½","⅓","⅔","¼","¾","⅕","⅖","⅗","⅘","⅙","⅚","⅛","⅜","⅝","⅞",
            "©","R²","O₃","cm³","cm²","mm³","mm²","in²","in³","CO₂","N₂",
            "(",")",".",
            "...", "★", "►", "✓", "✗", "←", "→", "↑", "↓", "µ","λ", "♢",
            "∀", "∁", "∂", "∃", "∄", "∅", "∆", "∇", "∈", "∉","∊", "∋", "∌", "∍", "∎", "∏",
            "∐", "∑", "∓", "∔", "∕", "∖", "∘", "∙","√", "∛", "∜", "∝", "∞", "∟",
            "∠", "∡", "∢", "∣", "∤", "∥", "∦", "∧", "∨", "∩","∪", "∫", "∬", "∭", "∮", "∯",
            "∰", "∱", "∲", "∳", "∴", "∵", "∶", "∷", "∸", "∹","∺", "∻", "∼", "∽", "∾", "∿",
            "≀", "≁", "≂", "≃", "≄", "≅", "≆", "≇", "≈", "≉","≊", "≋", "≌", "≍", "≎", "≏",
            "≐", "≑", "≒", "≓", "≔", "≕", "≖", "≗", "≘", "≙","≚", "≛", "≜", "≝", "≞", "≟",
            "≠", "≡", "≢", "≣", "≤", "≥", "≦", "≧", "≨", "≩","≪", "≫", "≬", "≭", "≮", "≯",
            "≰", "≱", "≲", "≳", "≴", "≵", "≶", "≷", "≸", "≹","≺", "≻", "≼", "≽", "≾", "≿",
            "⊀", "⊁", "⊂", "⊃", "⊄", "⊅", "⊆", "⊇", "⊈", "⊉","⊊", "⊋", "⊌", "⊍", "⊎", "⊏",
            "⊐", "⊑", "⊒", "⊓", "⊔", "⊕", "⊖", "⊗", "⊘", "⊙","⊚", "⊛", "⊜", "⊝", "⊞", "⊟",
            "⊠", "⊡", "⊢", "⊣", "⊤", "⊥", "⊦", "⊧", "⊨", "⊩","⊪", "⊫", "⊬", "⊭", "⊮", "⊯",
            "⊰", "⊱", "⊲", "⊳", "⊴", "⊵", "⊶", "⊷", "⊸", "⊹","⊺", "⊻", "⊼", "⊽", "⊾", "⊿",
            "⋀", "⋁", "⋂", "⋃", "⋄", "⋅", "⋆", "⋇", "⋈", "⋉","⋊", "⋋", "⋌", "⋍", "⋎", "⋏",
            "⋐", "⋑", "⋒", "⋓", "⋔", "⋕", "⋖", "⋗", "⋘", "⋙","⋚", "⋛", "⋜", "⋝", "⋞", "⋟",
            "⋠", "⋡", "⋢", "⋣", "⋤", "⋥", "⋦", "⋧", "⋨", "⋩","⋪", "⋫", "⋬", "⋭", "⋮", "⋯",
            "⋰", "⋱"
    };


    public static boolean isSymbolIncluded (String str) {

        if ((str == null) || str.length() ==0) {
            return false;
        }

        for (int i = 0; i < mUnicodeArray.length; i++) {
            if (str.contains(mUnicodeArray[i])) {
                return true;
            }
        }

        return false;
    }


    public static int getSymbolCount() {
        int count = mUnicodeArray.length;
        return count;
    }

}
