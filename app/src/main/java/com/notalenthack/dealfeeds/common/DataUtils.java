package com.notalenthack.dealfeeds.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bweigand on 9/11/13.
 */
public class DataUtils {

    public static Set<String> setFromArray(String strArray[]) {
        List<String> stringList = Arrays.asList(strArray);
        Set<String> stringSet = new HashSet<String>(stringList);
        return stringSet;
    }
}
