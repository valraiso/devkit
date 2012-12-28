package devkit.mvc;

import static play.libs.F.None;
import static play.libs.F.Some;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.libs.F.Option;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import devkit.utils.Binder;

public class Controller extends play.mvc.Controller{

	public static String params (String key){

		return params (key, String.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T params (String key, Class<T> clazz){

		String method = request().method();
		Map<String,String[]> params;
		
		if ("GET".equals(method)){
			params = request().queryString();
		} else if ("POST".equals(method)){
			
			if ("multipart/form-data".equals(request().getHeader("CONTENT-TYPE"))){
				
				MultipartFormData multipartFormData = request().body().asMultipartFormData();
				
				if (FilePart.class.equals(clazz)){
					
					return (T) multipartFormData.getFile(key);
					
				} else if (File.class.equals(clazz)){
					
					return (T) Option(
						multipartFormData.getFile(key)
					).getOrElse(null);
				}

				params = multipartFormData.asFormUrlEncoded();
				
			} else {
				params = request().body().asFormUrlEncoded();
			}
		} else {
			return null;
		}
		
		return params (params, key, clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] params_asArray(String key, Class<T> clazz){
		
		String method = request().method();
		Map<String,String[]> params;
		
		if ("GET".equals(method)){
			params = request().queryString();
		} else if ("POST".equals(method)){
			
			if ("multipart/form-data".equals(request().getHeader("CONTENT-TYPE"))){
				
				MultipartFormData multipartFormData = request().body().asMultipartFormData();
				
				if (FilePart.class.equals(clazz) || 
					File.class.equals(clazz)){
					
					List<Object> parts = new ArrayList<Object>();
					for (FilePart filePart : multipartFormData.getFiles()){
						if (key.equals(filePart.getKey())){
							parts.add(FilePart.class.equals(clazz) ? filePart : filePart.getFile());
						}
					}
					return (T[]) parts.toArray();
				}

				params = multipartFormData.asFormUrlEncoded();
				
			} else {
				params = request().body().asFormUrlEncoded();
			}
		} else {
			return null;
		}
		
		return params_asArray (params, key, clazz);
	}
	
	public static <T> T params (Map<String,String[]> params, String key, Class<T> clazz){
		
		String[] values = params.get(key);
		if (values != null && values.length > 0){

			String stringValue = values[0];
			return Binder.bind(stringValue, clazz);
		}
		
		return null;
	}
	
	public static <T> T[] params_asArray(Map<String,String[]> params, String key, Class<T> clazz){
		
		String[] values = params.get(key);
		if (values != null && values.length > 0){

			return Binder.bind(values, clazz);
		}
		
		return null;
	}
	
	public static <T> Option<T> Option(T value){
		
		if (value != null){
			return Some(value);
		}
		return None();
	}
}
