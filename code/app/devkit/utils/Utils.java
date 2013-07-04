package devkit.utils;

import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Utils {

    public static String slugify(String string) {
        return slugify(string, Boolean.TRUE);
    }

    public static String slugify(String string, Boolean lowercase) {
        string = noAccents(string);
        // Apostrophes.
        string = string.replaceAll("([a-z])'s([^a-z])", "$1s$2");
        string = string.replaceAll("[^\\w]", "-").replaceAll("-{2,}", "-");
        // Get rid of any - at the start and end.
        string = string.replaceAll("-+$", "").replaceAll("^-+", "");

        return (lowercase ? string.toLowerCase() : string);
    }

    public static String noAccents(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFKC).replaceAll("[àáâãäåāąă]", "a").replaceAll("[çćčĉċ]", "c").replaceAll("[ďđð]", "d").replaceAll("[èéêëēęěĕė]", "e").replaceAll("[ƒſ]", "f").replaceAll("[ĝğġģ]", "g").replaceAll("[ĥħ]", "h").replaceAll("[ìíîïīĩĭįı]", "i").replaceAll("[ĳĵ]", "j").replaceAll("[ķĸ]", "k").replaceAll("[łľĺļŀ]", "l").replaceAll("[ñńňņŉŋ]", "n").replaceAll("[òóôõöøōőŏœ]", "o").replaceAll("[Þþ]", "p").replaceAll("[ŕřŗ]", "r").replaceAll("[śšşŝș]", "s").replaceAll("[ťţŧț]", "t").replaceAll("[ùúûüūůűŭũų]", "u").replaceAll("[ŵ]", "w").replaceAll("[ýÿŷ]", "y").replaceAll("[žżź]", "z").replaceAll("[æ]", "ae").replaceAll("[ÀÁÂÃÄÅĀĄĂ]", "A").replaceAll("[ÇĆČĈĊ]", "C").replaceAll("[ĎĐÐ]", "D").replaceAll("[ÈÉÊËĒĘĚĔĖ]", "E").replaceAll("[ĜĞĠĢ]", "G").replaceAll("[ĤĦ]", "H").replaceAll("[ÌÍÎÏĪĨĬĮİ]", "I").replaceAll("[Ĵ]", "J").replaceAll("[Ķ]", "K").replaceAll("[ŁĽĹĻĿ]", "L").replaceAll("[ÑŃŇŅŊ]", "N").replaceAll("[ÒÓÔÕÖØŌŐŎ]", "O").replaceAll("[ŔŘŖ]", "R").replaceAll("[ŚŠŞŜȘ]", "S").replaceAll("[ÙÚÛÜŪŮŰŬŨŲ]", "U").replaceAll("[Ŵ]", "W").replaceAll("[ÝŶŸ]", "Y").replaceAll("[ŹŽŻ]", "Z").replaceAll("[ß]", "ss");
    }

    public static String mkString(Collection items, String separator) {
        if (items == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        Iterator ite = items.iterator();
        int i = 0;
        while (ite.hasNext()) {
            if (i++ > 0) {
                sb.append(separator);
            }
            sb.append(ite.next());
        }
        return sb.toString();
    }

    public static <T> List<String> pluck(String fieldName, List<T> list){
        List<String> result = new ArrayList<String>();
        for(T el : list){
            try{
                Field field = el.getClass().getField(fieldName);
                result.add(field.get(el).toString());

            }catch(Exception e){}
        }
        return result;
    }
}
