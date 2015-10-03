package AudioPlayer

import javafx.application.Platform

import simplefx.all.Color._
import simplefx.all.EqualizerBand._
import simplefx.all.MediaPlayer.Status._
import simplefx.all._
import simplefx.core._
import simplefx.experimental._

/* === AudioPlayer ============================== START ============================================================= */
class AudioPlayer(stage:JStage) extends SimpleFXParent { THIS =>

  def mmss(x:  Time) = {
    val m = (x / minute).toInt
    val s = (x%minute / second).toInt
    val ms = "" + m
    val ss = if(s < 10) "0" + s else "" + s
    s"$ms:$ss"
  }
 
  def getChildrenUnprotected = getChildren  				                // Required for Parent ...

  def play = mpl.play
  def stop = mpl.stop

  private def pin  		  = this								                      // To be pinned to the scene-graph.
  private def NO_METERS = 10								                        // Number of vertical stacks of bars.
  private def NO_BARS 	= 20								                        // Bars per vertical meter/stack.
  
  private val PATH    	= "AudioPlayer/"
  private val PLAYLIST	= "/AudioPlayer/Playlist.xml"
  private val PLAYLIST1	= "http://www.archive.org/download/their_finest_hour_vol3/" +
		  				            "their_finest_hour_vol3_files.xml"

  private val CSS		    = PATH + "efxAudioPlayer.css"
  private val TTF_FONT	= PATH + "lcddot.ttf"						            // Font for the text displays.
  private val bgImage	  = PATH + "AudioPlayer.png"				          // Background Image.
  private val bk1Image	= PATH + "balance.png"						          // Image for the balance-knob.
  private val vk1Image	= PATH + "volume.png"						            // Image 1 for the volume-knob.
  private val vk2Image	= PATH + "volume-highlights.png"	          // Image 2 for the volume-knob.

  private val bimg		  = new ImageView(bgImage)			              // Background-image.
  private val sliders	  = new Array[Slider 	 ] (NO_METERS)			    // For the Magnitudes.
  private val vuMs		  = new Array[Rectangle] (NO_METERS*NO_BARS)	// vuMeters, used as magnitude-displays.
 /* ................................................................................................................. */
  

 /* Declare the EasyFXPlayer(extended MediaPlayer) ------------------------------------------------------------------ */
  lazy val mpl = new SimpleFXPlayer(PLAYLIST) {
    currentplayer ==> { pl =>
      if(pl != null) {
        THIS <++ (new MediaView(pl))
      }
    }
	  autoPlay	     	   	    = false // true					                // Autostart the play-mode.
	  audioSpectrumInterval   = 1d/30d					                      // Interval ratio.
    audioSpectrumNumBands 	= NO_METERS			  	                    // Number of Audio-bands.
    noMagnitudeIntervals  	= NO_BARS					                      // Number of Magnitude-intervals.
  	volume	 			 	      <-- volumeKnob.value	                    // Bind the knob to the volume-value.
	  balance 			 	      <-- balanceKnob.value                     // Bind the knob to the balance-value.
    leftVu	              <--	leftVuMeter				                    // Update VuMeter as values change.
    rightVu	              <--	rightVuMeter			                    // Binding the value to the meter.
  }  
 /* ................................................................................................................. */
   

 /* Declare the Buttons --------------------------------------------------------------------------------------------- */
  private class BtnBeh(b:Rectangle) extends Behavior(b){            // Behavior class for all buttons.
	  def pushIn(duration:Time, howDeep:Double, sw:Double){
		  b.scale 		   := 	 howDeep in duration                      // Animated a button-push-in.
		  b.strokeWidth	 :=     	  sw in duration		                  // Animated frame-width.
		  b.fill			   := BLACK^0.1  in duration  	                  // Animates Black down to 0.1 opac.
		  // TODO  makeShadow(b, BLACK^0.1)						                  // Shadow with Black 0.1 opac.
	  }
	  def pushOut(duration:Time) {
		  b.scale 		   := 	       1.0 in duration                    // Scales up again, in 1 second.
		  b.strokeWidth	 :=       	   0 in duration		                // Eliminates the frame-width.
		  b.fill			   := TRANSPARENT in duration		                  // Go from Back to transparent in 1 s.
		  b.effect		    =  null						                            // Eliminates all effects(like shadow).
	  }
	  b.onMouseEntered --> {pushIn (150 ms, 0.96, 3)}                 // When mouse enters, push-in.
    b.onMouseExited  --> {pushOut(150 ms         )}                 // When mouse exits, push-out.
    b.onClick        --> {
      pushIn (100 ms, 0.94, 4)                                      // When clicking, pushing, then
      in(100 ms) --> (pushOut(100 ms))                              // after 100 ms push out.
    }
  }
  
  private def newBtn(xp:Double, yp:Double, wp:Double, hp:Double, fun: =>Unit):Rectangle = {
    new Rectangle{										                              // Buttons are rectangles.
    	xy    	        =  (xp, yp)
      wh              =  (wp, hp) 		                              // Position and size the button.
    	fill   	        =  TRANSPARENT						                    // Use transparent color.
    	stroke 	        =  BLACK 								                      // Set the frame's paint/color.
    	/*behavior      <--*/ new BtnBeh(this)					              // Sets the Buttons behavior.
    	onClick       --> fun 								                        // Assign the onAction-function.
    }
  }	
  
  private val prevBtn = newBtn(106, 285, 74, 74, {mpl.playPrev  })	// Play previous.
  private val playBtn = newBtn(201, 285, 88, 74, {mpl.tooglePlay})	// Play/Pause Toggle.
  private val nextBtn = newBtn(310, 285, 74, 74, {mpl.playNext  })	// Play next item.
  private val powerBtn= newBtn(104, 532, 68, 86, {Platform.exit })	// Exit App.
  private val loadBtn = newBtn(413, 285, 77, 74, {new LoadDialog (stage, mpl.pls.load(_))})
 /* ................................................................................................................. */


 /* Declare the Gainsliders ----------------------------------------------------------------------------------------- */
  for (i <- 0 until NO_METERS) {
	  sliders(i) = new Slider(MIN_GAIN, MAX_GAIN, mpl.gains(i)) {
      orientation = Orientation.VERTICAL
	    laXY        =  (515+i*58, 228-20)					                    // Here all the vertical Sliders for
	    prefWH      =  (53		   , 181+40)					                  // the Magnitude are defined.
	    value     --> {mpl.gain = (i,value)}				                  // Their values set the gain-arrays.
	  }
  } 
 /* ................................................................................................................. */


 /* Declare the VU-Pointers ----------------------------------------------------------------------------------------- */
  @Bind var leftVu  = -40.0			                                    // Some magic values maybe ...
  @Bind var rightVu = -40.0			                                    // Initial values for the meter-displays.
         
  private def linTmp (line:Line, vu:B[Double]){	                    // Just a helper template-method for
    line.startEnd	   = ((0,0), (0,-83))		                          // the left- and right-VU-Pointers.
	  line.strokeWidth = 3
	  line.stroke  	   = linearGradient(DL2UL, 				                // Linear Gradient, Down- to Upper-Left, 3 stops.
		                    (0.33, TRANSPARENT), (0.34,BLACK), (1.0,"#7e7e7e"))
    line.effect 	   = new DropShadow (5, WHITE)
    line.rotate    <-- (-40 + 80 * vu)
	  //line.transforms = new Rotate {angle <-- {-40 + 80 * vu}} :: Nil // vu-changes set the angle.
  }  										
  
  private val leftVUPnt  = new Line {linTmp(this, leftVu ); translateXY = (375,615)}
  private val rightVUPnt = new Line {linTmp(this, rightVu); translateXY = (593,615)}
 /* ................................................................................................................. */

    
 /* Declare the VU-Meters ------------------------------------------------------------------------------------------- */
  private def pos(x:Double, y:Double) = (529+58*x,177-4*y)		      // Just a helper to position.
  private var ind = -1
  for (mt <- 0 until NO_METERS) {		                                // These loop set the visibility of the rectangles
	  for (bar <- 0 until NO_BARS) {	                                // representing the different frequency levels.
	    ind += 1							                                        // The magnitude-levels decide the visibility of
	    val myBarIx         = bar					                            // the vertical stacks of the rectangles.
	    val lowVis          = <-- (mpl.magnitudeLevels(mt) > myBarIx)
	    val highVis         = <-- (mpl.magnitudeLevels(mt)*mpl.gainLevel(mt) > myBarIx)
	    val opac	          = <-- (if(highVis) 1.0 else 0.5)
	    vuMs(ind)           = new Rectangle {laXY=pos(mt,bar); wh=(26,3); fill <-- (DARKRED^opac)}
	    vuMs(ind).visible <-- lowVis
	  }
  }		
 /* ................................................................................................................. */


 /* Declare the Track-Labels/Text-Displays on the top-left of the screen -------------------------------------------- */
  private val trackLb = new Label {layoutXY = (122, 95); prefWH = (389,26); text <-- l1; labTmp(this)}    // Line 1.
  private val timeLb  = new Label {layoutXY = (122,125); prefWH = (389,26); text <-- l2; labTmp(this)}    // Line 2.
  private val titleLb = new Label {layoutXY = (122,155); prefWH = (389,26); text <-- l3; labTmp(this)}    // Line 3.
  
  private def l1 = {
    println("playlist: " + mpl.pls)
    "Track: " + (mpl.pls.currentIndex+1) + "/" + mpl.pls.numItems + " " + pstatus
  }
  private def l2 = "Time: "  + mmss(mpl.currentTime  ) + "  Remaining: " + 
		  					               mmss(mpl.remainingTime) + " of " + mmss(mpl.totalDuration)
  private def l3 = mpl.pls.currentItem.title
  
  private def pstatus = mpl.status.v match {
	  case PAUSED  =>  "- Paused -"
	  case PLAYING =>  ""
	  case _ 		   =>  "Streaming ..."
  } 
  
  private def labTmp(n:Label) {n.font = new Font(20)}// TODO javafx.scene.text.Font.loadFont(TTF_FONT,20); 	n.textFill=RED}        // Template.
 /* ................................................................................................................. */
  

 /* Declare the Knobs ----------------------------------------------------------------------------------------------- */
  private lazy val balanceKnob = new Knob (-1, 1, 0  ) {	          // Handles the adjustment of
    laXY = (738,538); 	toRotate = List(bk1Image)					          // the balance.  It's value is
  } 														                                    // bound to the player(see above).
  
  private lazy val volumeKnob  = new Knob ( 0, 1, 0.5) {	          // This Knob has two rotateable
    laXY = (910,492); 	toRotate = List(vk1Image, vk2Image)		      // images.
  } 	
 /* ................................................................................................................. */


 /* Pin all nodes to the scene-graph. ------------------------------------------------------------------------------- */
  pin <++ (	  bimg			  ,  								                        // Background image.
			        prevBtn		  , 								                        // All the buttons.
			        nextBtn		  , playBtn	  , loadBtn, powerBtn,          // =="==
			        trackLb		  , timeLb	  , titleLb,			              // The text-displays.
			        leftVUPnt		, rightVUPnt,					                    // The VU-Displays(Meters)
			        balanceKnob	, volumeKnob)					                    // The Knobs.
  sliders.foreach { <++(_ )}
  vuMs   .foreach { <++(_ )}
			       // sliders		  , vuMs		  )					                  // Vertical sliders and vuMeters.
 /* ................................................................................................................. */
}
/* === AudioPlayer ============================== END =============================================================== */