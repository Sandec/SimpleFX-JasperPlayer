package AudioPlayer

import simplefx.core._
import simplefx.all._

class Behavior (val node:Node){ //println("BEHAVIOR> node: " + node)

	//type FUN = () => Unit
	//val dum:FUN = () => {}
	//private lazy val b01= :=(dum);	def onEnterFun   = b01;    def onEnterFun_=   (fun: => Unit) = {b01.v = () => fun; node.onEnter   --> {fun}}
	//private lazy val b02= :=(dum);	def onPressFun   = b02;    def onPressFun_=   (fun: => Unit) = {b02.v = () => fun; node.onPress   --> {fun}}
	//private lazy val b03= :=(dum);	def onReleaseFun = b03;    def onReleaseFun_= (fun: => Unit) = {b03.v = () => fun; node.onRelease --> {fun}}
	//private lazy val b04= :=(dum);	def onExitFun    = b04;    def onExitFun_=    (fun: => Unit) = {b04.v = () => fun; node.onExit    --> {fun}}
	//private lazy val b05= :=(dum);	def onClickFun   = b05;    def onClickFun_=	  (fun: => Unit) = {b05.v = () => fun; node.onClick   --> {fun}}

  val onEnter   = node.onMouseEntered
  val onPress   = node.onMousePressed
  val onRelease = node.onMouseReleased
  val onExit    = node.onMouseExited
  val onClick   = node.onMouseClicked
	
	//object onEnter   {def --> (fun: =>Unit) {onEnterFun   = fun}}
	//object onPress   {def --> (fun: =>Unit) {onPressFun   = fun}}
	//object onRelease {def --> (fun: =>Unit) {onReleaseFun = fun}}
	//object onExit    {def --> (fun: =>Unit) {onExitFun    = fun}}
	//object onClick   {def --> (fun: =>Unit) {onClickFun   = fun}}
}
 