package bigleg.com.wifidirect;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jack on 2018/1/11.
 */

public class util {
    public static <T,V> ArrayList<Map<T,V>> sortListByAttr(ArrayList<Map<T,V>> lst, String attr){
        for(int i = 0; i < lst.size(); i++){
            for(int j = i + 1; j< lst.size(); j++){
                if(lst.get(i).get(attr).toString().compareToIgnoreCase(lst.get(j).get(attr).toString()) > 0){
                    Map<T,V> t = lst.get(i);
                    lst.set(i, lst.get(j));
                    lst.set(j, lst.get(i));
                }
            }
        }
        return lst;
    }
}
