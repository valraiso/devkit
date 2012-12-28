// @SOURCE:/Users/benoit/dev/apps/devkit/code/conf/devkit.routes
// @HASH:365abef0222ac04516c74e274718c48d484b5cda
// @DATE:Fri Dec 28 14:44:45 CET 2012
package devkit

import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString

object Routes extends Router.Routes {

private var _prefix = "/"

def setPrefix(prefix: String) {
  _prefix = prefix  
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" } 


// @LINE:6
private[this] lazy val devkit_FakeController_fakeAction0 = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("fake/route"))))
        
def documentation = List(("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """fake/route""","""devkit.FakeController.fakeAction()""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]] 
}}
       
    
def routes:PartialFunction[RequestHeader,Handler] = {        

// @LINE:6
case devkit_FakeController_fakeAction0(params) => {
   call { 
        invokeHandler(devkit.FakeController.fakeAction(), HandlerDef(this, "devkit.FakeController", "fakeAction", Nil,"GET", """ Home page""", Routes.prefix + """fake/route"""))
   }
}
        
}
    
}
        