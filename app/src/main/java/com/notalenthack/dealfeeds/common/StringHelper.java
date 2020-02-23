package com.notalenthack.dealfeeds.common;

import java.util.Locale;

/**
 * String helpers
 * Use https://www.regextester.com/
 * https://www.journaldev.com/634/regular-expression-in-java-regex-example
 */
public class StringHelper {

    private static final String WORDSPATTERNTOREMOVE =
            "\\:(.*)|"+ "(.*)\\!|" + "\\'(.*)\\'|" +      // match everything after : and in '
            "[.!?\\\\-]|" + "\\d*%|$\\d*|" + // match special char, %num and $num
            "\\bget\\b[\\s.!?\\\\-]|" +
            "\\bthe\\b[\\s.!?\\\\-]|" +
            "\\ba\\b[\\s.!?\\\\-]|" +
            "\\ban\\b[\\s.!?\\\\-]|" +
            "\\bto\\b[\\s.!?\\\\-]|" +
            "\\bthe\\b[\\s.!?\\\\-]|" +
            "\\bup\\b[\\s.!?\\\\-]|" +
            "\\bsave\\b[\\s.!?\\\\-]|" +
            "\\bsale\\b[\\s.!?\\\\-]|" +
            "\\bprice\\b[\\s.!?\\\\-]|" +
            "\\bat\\b[\\s.!?\\\\-]|" +
            "\\bfor\\b[\\s.!?\\\\-]|" +
            "\\bonly\\b[\\s.!?\\\\-]|" +
            "\\bon\\b[\\s.!?\\\\-]|" +
            "\\bfrom\\b[\\s.!?\\\\-]|" +
            "\\band\\b[\\s.!?\\\\-]|" +
            "\\bprice\\b[\\s.!?\\\\-]|" +
            "\\bpremium\\b[\\s.!?\\\\-]|" +
            "\\bsuper\\b[\\s.!?\\\\-]|" +
            "\\bsupream\\b[\\s.!?\\\\-]|" +
            "\\bfree\\b[\\s.!?\\\\-]|" +
            "\\bmonday\\b[\\s.!?\\\\-]|" +
            "\\btuesday\\b[\\s.!?\\\\-]|" +
            "\\bwednesday\\b[\\s.!?\\\\-]|" +
            "\\bthursday\\b[\\s.!?\\\\-]|" +
            "\\bfriday\\b[\\s.!?\\\\-]|" +
            "\\bsaturday\\b[\\s.!?\\\\-]|" +
            "\\bsunday\\b[\\s.!?\\\\-]|" +
            "\\btoday\\b[\\s.!?\\\\-]|" +
            "\\btomorrow\\b[\\s.!?\\\\-]|" +
            "\\bhello\\b[\\s.!?\\\\-]|" +
            "\\bsay\\b[\\s.!?\\\\-]|" +
            "\\bbye\\b[\\s.!?\\\\-]|" +
            "\\byour\\b[\\s.!?\\\\-]|" +
            "\\bnew\\b[\\s.!?\\\\-]|" +
            "\\bweek[\\s.!?\\\\-]|" +
            "\\bmomth[\\s.!?\\\\-]|" +
            "\\byear[\\s.!?\\\\-]|" +
            "\\bblack[\\s.!?\\\\-]|" +
            "\\bwhite[\\s.!?\\\\-]|" +
            "\\bred[\\s.!?\\\\-]|" +
            "\\byellow[\\s.!?\\\\-]|" +
            "\\bblue[\\s.!?\\\\-]|" +
            "\\bgreen[\\s.!?\\\\-]|" +
            "\\bbrown[\\s.!?\\\\-]|" +
            "\\borange[\\s.!?\\\\-]|" +
            "\\bis\\b[\\s.!?\\\\-]|" +
            "\\bhere[\\s.!?\\\\-]|" +
            "\\bamazon[\\s.!?\\\\-]|" +
            "\\bwalmart[\\s.!?\\\\-]|" +
            "\\bbest\\sbuy[\\s.!?\\\\-]|" +
            "\\btarget[\\s.!?\\\\-]|" +
            "\\bcostco\\b[\\s.!?\\\\-]|" +
            "\\bstaples\\b[\\s.!?\\\\-]|" +
            "\\bhome\\sdepot\\b[\\s.!?\\\\-]|" +
            "\\blowes\\s.!?\\\\-]|" +
            "\\bhere[\\s.!?\\\\-]|" +
            "\\bnow\\b[\\s.!?\\\\-]"
            ;

    public static String[] buildStringList(String str) {
        // remove un-wanted words
        str = str.replaceAll(WORDSPATTERNTOREMOVE, "").trim();

        String results[] = null;
        int words = str.isEmpty() ? 0 : str.split("\\s+").length;
        int i = 0;
        if (words > 2) {
            results = new String[words-2];

            while (words > 2) {
                int idx = str.lastIndexOf(" ");
                str = str.substring(0, idx).trim();
                results[i++] = str;
                words--;
            }

            return results;
        } else {
            if (str.isEmpty()) {
                return null;
            } else {
                return new String[]{str};
            }
        }
    }

    // guess if the word is a model number
    // model number has digits, letters, ..etc
    public static boolean isModelNumber(String s) {
        int digits = 0;
        int letters = 0;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                digits++;
            } else if (Character.isLetter(c)) {
                letters++;
            }
        }
        return ((digits >= 3) && (letters >= 2)) ?  true : false;
    }

    // get string without model number
    public static String getStringNoModelNumber(String str) {
        boolean foundModel = false;
        StringBuilder sb = new StringBuilder();
        for (String s : str.split("\\s")) {
            if (!isModelNumber(s)) {
                sb.append(s);
                sb.append(" ");
            } else {
                foundModel = true;
            }
        }
        return foundModel==true ? sb.toString().trim() : null;
    }

    /*
     * Remove special chars, useless words, price. Also remove all after "at "
     *
     * Need to remove model number if we can't find anything.
     * Then walk backward and start remove words
     */
    public static String buildSearchString(String str) {
        int i = str.indexOf('(');
        if (i == -1) {
            i = str.indexOf('+');
            if (i == -1) {
                i = str.indexOf('$');
                if (i == -1) {
                    i = str.indexOf("at");
                }
            }
        }
        if (i != -1) {
            return str.substring(0, i);
        } else {
            return str;
        }
    }

    public static String getTimeStr(long timeOnce) {
        long hour = timeOnce / Constant.MILLISECS_IN_HOUR;
        long minute = (timeOnce - (hour * Constant.MILLISECS_IN_HOUR)) / Constant.MILLISECS_IN_MINUTE;
        String timeStr = String.format(Locale.getDefault(), "%02d", hour)
                        + ":" + String.format(Locale.getDefault(),"%02d", minute);
        return timeStr;
    }
}
