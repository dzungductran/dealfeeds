package com.notalenthack.dealfeeds.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ProductCategories {
    public static final Integer CAT_TELEVISION  = 0;
	public static final Integer CAT_COMPUTER    = 1;
    public static final Integer CAT_ELECTRONICS = 2;
    public static final Integer CAT_TOYS        = 3;
	public static final Integer CAT_CLOTHING    = 4;
	public static final Integer CAT_HOME        = 5;
	public static final Integer CAT_OTHER       = 6;

	public static final Map<Integer, String> categoryMap;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(CAT_TELEVISION,  " TV & Video");
        aMap.put(CAT_COMPUTER,    " Computers");
        aMap.put(CAT_ELECTRONICS, " Electronics");
        aMap.put(CAT_TOYS,        " Games, Toys");
        aMap.put(CAT_CLOTHING,    " Clothing");
        aMap.put(CAT_HOME,        " Home, Garden");
        aMap.put(CAT_OTHER,       " Others...");
        categoryMap = Collections.unmodifiableMap(aMap);
    }

    // We could use a simple keyword map to search the title for
    // a single word that *strongly* identifies a given category
    public static final Map<String, Integer> categoryKeywordMap;
    static {
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put("laptop", CAT_COMPUTER);
        aMap.put("desktop", CAT_COMPUTER);
        aMap.put("notebook", CAT_COMPUTER);
        aMap.put("computer", CAT_COMPUTER);
        aMap.put("hdd", CAT_COMPUTER);
        aMap.put("hard drive", CAT_COMPUTER);
        aMap.put("pc", CAT_COMPUTER);
        aMap.put("cpu", CAT_COMPUTER);
        aMap.put("processor", CAT_COMPUTER);
        aMap.put("ultrabook", CAT_COMPUTER);
        aMap.put("tablet", CAT_COMPUTER);
        aMap.put("memory", CAT_COMPUTER);
        aMap.put("ssd", CAT_COMPUTER);
        aMap.put("ghz", CAT_COMPUTER);
        aMap.put("sata", CAT_COMPUTER);
        aMap.put("intel", CAT_COMPUTER);
        aMap.put("mac", CAT_COMPUTER);
        aMap.put("power", CAT_COMPUTER);
        aMap.put("macbook", CAT_COMPUTER);
        aMap.put("laptop", CAT_COMPUTER);
        aMap.put("computer", CAT_COMPUTER);
        aMap.put("chromebook", CAT_COMPUTER);

        aMap.put("television", CAT_TELEVISION);
        aMap.put("theater", CAT_TELEVISION);
        aMap.put("plasma", CAT_TELEVISION);
        aMap.put("surround", CAT_TELEVISION);
        aMap.put("hdtv", CAT_TELEVISION);
        aMap.put("tv", CAT_TELEVISION);

        aMap.put("tool", CAT_HOME);
        aMap.put("garden", CAT_HOME);
        aMap.put("home", CAT_HOME);
        aMap.put("office", CAT_HOME);
        aMap.put("watch", CAT_HOME);
        aMap.put("shower", CAT_HOME);
        aMap.put("appliance", CAT_HOME);
        aMap.put("soap", CAT_HOME);
        aMap.put("bath", CAT_HOME);
        aMap.put("light", CAT_HOME);
        aMap.put("cook", CAT_HOME);
        aMap.put("bake", CAT_HOME);
        aMap.put("gift", CAT_HOME);
        aMap.put("photo", CAT_HOME);
        aMap.put("clean", CAT_HOME);
        aMap.put("saw", CAT_HOME);
        aMap.put("drill", CAT_HOME);
        aMap.put("lowes", CAT_HOME);
        aMap.put("craftsman", CAT_HOME);
        aMap.put("hammer", CAT_HOME);
        aMap.put("wrench", CAT_HOME);
        aMap.put("refrigerator", CAT_HOME);
        aMap.put("blender", CAT_HOME);
        aMap.put("tent", CAT_HOME);
        aMap.put("cabinet", CAT_HOME);
        aMap.put("ladder", CAT_HOME);
        aMap.put("toaster", CAT_HOME);
        aMap.put("grill", CAT_HOME);
        aMap.put("shovel", CAT_HOME);

        aMap.put("shirt", CAT_CLOTHING);
        aMap.put("t-shirt", CAT_CLOTHING);
        aMap.put("pant", CAT_CLOTHING);
        aMap.put("shoe", CAT_CLOTHING);
        aMap.put("jean", CAT_CLOTHING);
        aMap.put("sock", CAT_CLOTHING);
        aMap.put("jacket", CAT_CLOTHING);
        aMap.put("costume", CAT_CLOTHING);
        aMap.put("apparel", CAT_CLOTHING);
        aMap.put("nike", CAT_CLOTHING);
        aMap.put("adidas", CAT_CLOTHING);
        aMap.put("reebok", CAT_CLOTHING);
        aMap.put("men", CAT_CLOTHING);
        aMap.put("women", CAT_CLOTHING);
        aMap.put("glasses", CAT_CLOTHING);
        aMap.put("sunglasses", CAT_CLOTHING);
        aMap.put("suit", CAT_CLOTHING);
        aMap.put("tote", CAT_CLOTHING);
        aMap.put("cloth", CAT_CLOTHING);
        aMap.put("leather", CAT_CLOTHING);
        aMap.put("sweatshirt", CAT_CLOTHING);

        aMap.put("headphone", CAT_ELECTRONICS);
        aMap.put("stereo", CAT_ELECTRONICS);
        aMap.put("gadget", CAT_ELECTRONICS);
        aMap.put("phone", CAT_ELECTRONICS);
        aMap.put("gps", CAT_ELECTRONICS);
        aMap.put("multimedia", CAT_ELECTRONICS);
        aMap.put("dslr", CAT_ELECTRONICS);
        aMap.put("camera", CAT_ELECTRONICS);
        aMap.put("mobile", CAT_ELECTRONICS);
        aMap.put("video", CAT_ELECTRONICS);
        aMap.put("bluetooth", CAT_ELECTRONICS);
        aMap.put("mouse", CAT_ELECTRONICS);
        aMap.put("monitor", CAT_ELECTRONICS);
        aMap.put("display", CAT_ELECTRONICS);
        aMap.put("xbox", CAT_ELECTRONICS);
        aMap.put("playstation", CAT_ELECTRONICS);
        aMap.put("ps3", CAT_ELECTRONICS);
        aMap.put("mp3", CAT_ELECTRONICS);
        aMap.put("usb", CAT_ELECTRONICS);
        aMap.put("dlp", CAT_ELECTRONICS);
        aMap.put("wireless", CAT_ELECTRONICS);
        aMap.put("wifi", CAT_ELECTRONICS);
        aMap.put("camcorder", CAT_ELECTRONICS);
        aMap.put("dvd", CAT_ELECTRONICS);
        aMap.put("nintendo", CAT_ELECTRONICS);
        aMap.put("speaker", CAT_ELECTRONICS);
        aMap.put("printer", CAT_ELECTRONICS);
        aMap.put("movie", CAT_ELECTRONICS);
        aMap.put("audio", CAT_ELECTRONICS);
        aMap.put("flash", CAT_ELECTRONICS);
        aMap.put("disc", CAT_ELECTRONICS);
        aMap.put("charger", CAT_ELECTRONICS);
        aMap.put("network", CAT_ELECTRONICS);
        aMap.put("software", CAT_ELECTRONICS);
        aMap.put("e-reader", CAT_ELECTRONICS);
        aMap.put("ipad", CAT_ELECTRONICS);
        aMap.put("ipod", CAT_ELECTRONICS);
        aMap.put("iphone", CAT_ELECTRONICS);
        aMap.put("blue-ray", CAT_ELECTRONICS);
        aMap.put("blu-ray", CAT_ELECTRONICS);
        aMap.put("microsoft", CAT_ELECTRONICS);
        aMap.put("router", CAT_ELECTRONICS);
        aMap.put("drive", CAT_ELECTRONICS);
        aMap.put("microphone", CAT_ELECTRONICS);
        aMap.put("len", CAT_ELECTRONICS);

        aMap.put("lego", CAT_TOYS);
        aMap.put("game", CAT_TOYS);
        aMap.put("toy", CAT_TOYS);
        aMap.put("gym", CAT_TOYS);
        aMap.put("bike", CAT_TOYS);
        aMap.put("hoop", CAT_TOYS);
        aMap.put("doll", CAT_TOYS);
        aMap.put("airplane", CAT_TOYS);
        aMap.put("plane", CAT_TOYS);
        aMap.put("nerf", CAT_TOYS);
        aMap.put("disney", CAT_TOYS);
        aMap.put("star", CAT_TOYS);
        aMap.put("crayola", CAT_TOYS);
        aMap.put("train", CAT_TOYS);


        categoryKeywordMap = Collections.unmodifiableMap(aMap);
    }

    private static final int MIN_WORD_LENGTH = 2; // Discard words shorter
    public static Integer getCatIdFromTitle(String productTitle) {
        // This assumes a title is just a mix of words and spaces
        // If the titles contain punctuation, we need to strip that
        String[] wordsInTitle = productTitle.split("\\s");

        for (String titleWord : wordsInTitle) {
            if (titleWord.length() >= MIN_WORD_LENGTH) {
                String word = titleWord.toLowerCase(Locale.getDefault());
                if (categoryKeywordMap.containsKey(word)) {
                    return categoryKeywordMap.get(word);
                } else {
                    // remove plural
                    int l = word.lastIndexOf("s");
                    if (l != -1 && l == word.length()-1) {
                        String subWord = word.substring(0, word.length()-1);
                        if (categoryKeywordMap.containsKey(subWord)) {
                            return categoryKeywordMap.get(subWord);
                        }
                    }

                    l = word.lastIndexOf("es");
                    if (l != -1 && l == word.length()-2) {
                        String subWord = word.substring(0, word.length()-2);
                        if (categoryKeywordMap.containsKey(subWord)) {
                            return categoryKeywordMap.get(subWord);
                        }
                    }
                }
            }
        }
        return CAT_OTHER;
    }

}
