// @SOURCE:/Users/benoit/dev/apps/devkit/code/conf/devkit.routes
// @HASH:365abef0222ac04516c74e274718c48d484b5cda
// @DATE:Fri Dec 28 14:44:45 CET 2012

import devkit.Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString


// @LINE:6
package devkit {

// @LINE:6
class ReverseFakeController {
    

// @LINE:6
def fakeAction(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "fake/route")
}
                                                
    
}
                          
}
                  


// @LINE:6
package devkit.javascript {

// @LINE:6
class ReverseFakeController {
    

// @LINE:6
def fakeAction : JavascriptReverseRoute = JavascriptReverseRoute(
   "devkit.FakeController.fakeAction",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "fake/route"})
      }
   """
)
                        
    
}
              
}
        


// @LINE:6
package devkit.ref {

// @LINE:6
class ReverseFakeController {
    

// @LINE:6
def fakeAction(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   devkit.FakeController.fakeAction(), HandlerDef(this, "devkit.FakeController", "fakeAction", Seq(), "GET", """ Home page""", _prefix + """fake/route""")
)
                      
    
}
                          
}
                  
      