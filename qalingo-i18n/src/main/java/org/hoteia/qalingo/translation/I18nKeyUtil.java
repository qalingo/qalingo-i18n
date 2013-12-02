package org.hoteia.qalingo.translation;

public class I18nKeyUtil {

    static public String handleKey(String key){
        key = key.replaceAll("\\.", "_");
        key = key.replaceAll("\\-", "_");
        return key;
    }
    
}
