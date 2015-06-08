package AudioPlayer

import simplefx.core._
import simplefx.all._

class Behavior (val node:Node){
  val onEnter   = node.onMouseEntered
  val onPress   = node.onMousePressed
  val onRelease = node.onMouseReleased
  val onExit    = node.onMouseExited
  val onClick   = node.onMouseClicked
}
 