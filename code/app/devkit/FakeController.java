package devkit;

import play.*;
import play.mvc.*;

public class FakeController extends Controller {

	public static Result fakeAction(){
		return notFound();
	}
}