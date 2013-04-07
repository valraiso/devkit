package devkit.mvc;

import devkit.utils.Binder;
import play.api.templates.Html;
import play.libs.F.Option;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.libs.F.None;
import static play.libs.F.Some;

public class Controller extends play.mvc.Controller{

    private static final String HTTP_PARAMS_KEY = "http.params";

    public static String param (String key){

        return param (key, String.class);
    }

    public static <T> T param (String key, Class<T> clazz){
        return param(key, clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T param (String key, Class<T> clazz, T defVal){

        if (File.class.equals(clazz) || FilePart.class.equals(clazz)){
            MultipartFormData multipartFormData = request().body().asMultipartFormData();
            if (multipartFormData != null){
                FilePart filePart = multipartFormData.getFile(key);
                if (FilePart.class.equals(clazz)){
                    return (T) filePart;
                } else if (filePart != null){
                    return (T) filePart.getFile();
                }
            }
            return null;
        }

        return param (getParams(), key, clazz, defVal);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] params(String key, Class<T> clazz){

        if (File.class.equals(clazz) || FilePart.class.equals(clazz)){
            MultipartFormData multipartFormData = request().body().asMultipartFormData();
            if (multipartFormData != null){

                if (FilePart.class.equals(clazz)){
                    List<FilePart> parts = new ArrayList<FilePart>();
                    for (FilePart filePart : multipartFormData.getFiles()){
                        if (key.equals(filePart.getKey())){
                            parts.add(filePart);
                        }
                    }
                    return (T[]) parts.toArray();
                }
                if (File.class.equals(clazz)){
                    List<File> parts = new ArrayList<File>();
                    for (FilePart filePart : multipartFormData.getFiles()){
                        if (key.equals(filePart.getKey())){
                            parts.add(filePart.getFile());
                        }
                    }
                    return (T[]) parts.toArray();
                }
            }
            return null;
        }

        return params(getParams(), key, clazz);
    }

    public static <T> T param (Map<String,String[]> params, String key, Class<T> clazz){
        return param(params, key , clazz, null);
    }

    public static <T> T param (Map<String,String[]> params, String key, Class<T> clazz, T defVal){

        String[] values = params.get(key);
        if (values != null && values.length > 0){

            String stringValue = values[0];
            T val = Binder.bind(stringValue, clazz);
            if( val != null){
                return val;
            }
        }
        return defVal;
    }

    public static <T> T[] params(Map<String,String[]> params, String key, Class<T> clazz){

        String[] values = params.get(key);
        if (values != null && values.length > 0){

            return Binder.bind(values, clazz);
        }

        return null;
    }

    private static Map<String,String[]> getParams(){

        String method = request().method();
        Map<String,String[]> params = (Map<String,String[]>) ctx().args.get(HTTP_PARAMS_KEY);

        if (params == null){

            if ("GET".equals(method)){
                params = request().queryString();
            } else if ("POST".equals(method)){
                MultipartFormData multipartFormData = request().body().asMultipartFormData();
                if(multipartFormData != null) {
                    params = multipartFormData.asFormUrlEncoded();
                } else {
                    params = request().body().asFormUrlEncoded();
                }
            }
            if (params == null){
                params = new HashMap<String, String[]>();
            }
            ctx().args.put(HTTP_PARAMS_KEY, params);
        }
        return params;
    }

    public static <T> Option<T> Option(T value){

        if (value != null){
            return Some(value);
        }
        return None();
    }

    public static Html trim(Html html){
        return new play.api.templates.Html(
            new scala.collection.mutable.StringBuilder(html.body().replaceAll("^\\r?\\n?", ""))
        );
    }
}