package devkit.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import play.Play;
import play.api.Application;
import play.api.i18n.Messages;
import play.api.mvc.Codec;
import play.libs.Crypto;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.Map;

public class JsMessages {

    private static Map<String, Result> cached = new HashMap<String,Result>();

    public synchronized static Result expose(String... exposedKeys){

        String      lang = Http.Context.current().lang().code();
        Application app  = Play.application().getWrappedApplication();

        String cachedKey = Crypto.sign(
            lang + exposedKeys.toString()
        );

        Result result = cached.get(cachedKey);
        if (result == null){

            Map<String,String> messages = new HashMap<String, String>();
            messages.putAll(JavaConversions.mapAsJavaMap(
                Messages.messages(app).get("default").get()
            ));
            messages.putAll(JavaConversions.mapAsJavaMap(
                Messages.messages(app).get(lang).get()
            ));

            String content = "var __messages__ = __messages__ || {};var jsMessages=function(c){var a=__messages__[c];if('undefined'==typeof a)return c;for(var b=1;b<arguments.length;b++)a=a.replace(RegExp('\\\\{'+(b-1)+'\\\\}','g'),arguments[b]);return a};";
            content+="(function (__messages__){";

            String jsKeys = "";
            for (String exposeKey : exposedKeys){

                if (exposeKey.endsWith(".*")){
                    String pattern = exposeKey.substring(0, exposeKey.length()-2);
                    for (String key : messages.keySet()){
                        if (key.startsWith(pattern)){
                             jsKeys += "'" + key + "':'"+ StringEscapeUtils.escapeEcmaScript(messages.get(key)) + "',";
                        }
                    }
                } else {
                    String trad = messages.get(exposeKey);
                    if (trad != null){
                        jsKeys += "'" + exposeKey + "':'"+ StringEscapeUtils.escapeEcmaScript(trad) + "',";
                    }
                }
            }
            if (jsKeys.length()>0){jsKeys = jsKeys.substring(0, jsKeys.length()-1);}
            content+="var _exposed = {" + jsKeys+"};";
            content+="var _extend=function(b,c){var b=b||{},a;for(a in c)b[a]='object'===typeof c[a]?extend(b[a],c[a]):c[a];return b};_extend(__messages__, _exposed);})(__messages__)";

            result = new Results.Status(
                play.core.j.JavaResults.Ok(), content, Codec.javaSupported("utf-8")
            ).as("application/javascript");

            cached.put(cachedKey,result);
        }

        return result;
    }
}
