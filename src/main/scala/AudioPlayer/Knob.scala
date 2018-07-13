package AudioPlayer

import simplefx.all._
import simplefx.core._
import simplefx.experimental._

/* === Knob ===================================== START ===================================== */
class Knob(val minValue:Double, maxValue:Double, initValue:Double) extends SimpleFXParent {
  
/* Declare Properties and enable convenient access to them --------------------------------- */
  type Point = Double2
  type URLs  = B[List[String]]					           // List of URLs, as Bindable-Properties.
  @Bind var value = initValue
  @Bind var toRotate: List[String] = Nil

  private val rotateables = new SimpleFXParent {				     // Create a Group for the rotatable images.
    rotate <-- -currDragAngle
    /*transform <-- Rotate(-currDragAngle)new Rotate {					// Assign a Rotation-effect to it.
      angle  <-- (-currDragAngle)					// Rotate with the currentDragAngle-value.
      axis	  =  Rotate.Z_AXIS						// Rotate around the Z-axis.
      pivotXY <-- (centerX, centerY)				// Set & bind the pivotation-center.
    }*/
  }
  def newValue(angle:Double):Double = {				     // Calculate the result.
    val normalizedAngle = (angle + 180) % 360		   // Start is the vertical-up position.
    val (min,max)       =  (40, 320)				       // Lowest, highest possible angle.
    val newVal = (normalizedAngle-(360-max+min)/2)/(max - min)
    val clockDirectionValue = (maxValue - newVal * (maxValue - minValue))		// Change direction to clockwise.
    return clockDirectionValue
  }
/* ......................................................................................... */
  
/* Set some triggers and bindings for the properties -------------------------------------- */
  value    <-- newValue(currDragAngle)				// Export the result, as a binding.
  toRotate --> toRotate.foreach(url => {			// Put all rotateable images into 
    rotateables <++ (new ImageView(url))			// the Group.
  }) 				
   
  this <++ rotateables								// Pin the Group-Node to the Scene.
/* ......................................................................................... */
  
/* Let the dragging of the rotateable trigger the angle-calculation ------------------------ */
  //def leftDown =
  def leftDown = mousePressed(MouseButton.PRIMARY)
  when(leftDown) ==> {
    initDrag(mousePositionTarget)
    //every(0.001 s) --> {
      currDragAngle <-- adjustKnobValue(mousePositionTarget)
    //}
    //
    // currDragAngle <-- adjustKnobValue(mousePosition)
  }
  when(!leftDown) --> { lastAnglePosition = currDragAngle }
  onDoubleClick   --> { toggleMute }

  @Bind var muted = false; muted --> {currDragAngle = {if(muted) 220 else 0}}
  private def toggleMute = {muted := !muted; lastAnglePosition = currDragAngle}  

  onMousePressed  --> {(rotateables.scale) := 0.93 in (200 ms); ()}
  onMouseReleased --> {(rotateables.scale) := 1.0  in (150 ms); ()}
/* ......................................................................................... */
  
  
/* Variables and methods to carry through the calculation of a new angle/value ------------- */
  private var 	   lastAnglePosition = 0.0   			       // To be added to the new angle.
  private var 	   startDragQuadrant = 0				         // We define the quadrants from 1-4 in
  														                           // the opposite clockwise direction.
  private var 	   startDragAngle 	 = 0.0 				       // Angle when Drag starts.
  private var 	   currDragQuadrant	 = 0				         // The quadrant and the angle in which
  @Bind private var currDragAngle 	 = 0.0   		         // we currently are during the drag.
  
  @Bind private var centerX	 = labW/2	   // TODO what does <:-?	// The center of the Knob.  It is bound
  @Bind private var centerY	 = labH/2	   // TODO what does <:-?	// for one single calculation, only.

  nextFrame --> (nextFrame --> { // TODO ... less workarounds please ...
    centerX = labW/2
    centerY = labH/2
  })
  
  private def XoY(p:Point) 	 = abs(p.x)/abs(p.y)		      // The Angle defined through X/Y.
  private def YoX(p:Point) 	 = abs(p.y)/abs(p.x)		      // The Angle defined through Y/X.
  
  private def relX (p:Point) = p.x     - centerX		      // X-value relative to the center.
  private def relY (p:Point) = centerY - p.y			        // Relative Y, inverted.
  private def relXY(p:Point) = (relX(p), relY(p))		      // The Point relative to the center.
  
  private def quadrant(p:Point):Int = {					          // Returns the quadrant of a point.
    if(relX(p) >= 0) {if(relY(p) >= 0) 1 else 4} else     // We define the quadrants from 1-4
                     {if(relY(p) >= 0) 2 else 3}		      // stepping with 90 degrees, starting with the range 0 to 90.
  }
  
  private def quadDistance(p:Point) =  					          // Returns the number of quadrants
		  		quadrant(p) - startDragQuadrant + 1		          // between start and current drag-position.

  private def initDrag(p:Point) {						              // Defines Quadrant and Point for
    startDragQuadrant = quadrant(p)						            // the start of a drag-operation.
    startDragAngle    = startAngle(startDragQuadrant, relXY(p))
  }

  private def startAngle(q:Int, p:Point):Double = {		    // Returns the arc-tangent of the
    atan2degrees (if(q==1 || q==3) YoX(p) else 	          // start-angle converted from
    					    if(q==2 || q==4) XoY(p) else 	          // radians to degrees.
    					    0.0)
  }														

  private def adjustKnobValue(p:Point):Double = {		      // Returns angle of drag-position.
    val dragged	   = 90 * quadDistance(p) - startDragAngle - thirdAngle(quadrant(p), relXY(p))
    val normalized = (lastAnglePosition + dragged) % 360  // Normalizes, validates and filters
    validatedAngleValue(normalized, 140, 220)	            // out undefined angle-range (from
  }														                            // 140 to 220 after transformation).
  
  def validatedAngleValue (angleValue:Double, lim1:Double, lim2:Double):Double = {
    if(angleValue <= lim1 || angleValue >= lim2) angleValue  else
    if(abs(angleValue - lim1) < abs(angleValue - lim2)) lim1 else lim2  
  }
  
  private def thirdAngle(q:Int, p:Point):Double = {		    // Calculates the "third angle",
    atan2degrees (if(q==1 || q==3) XoY(p) else 	          // which is used to fill up a
    					    if(q==2 || q==4) YoX(p) else 	          // 90-degrees-Quadrant.
    					    0.0)
  }														
}
/* === Knob ===================================== END ======================================= */