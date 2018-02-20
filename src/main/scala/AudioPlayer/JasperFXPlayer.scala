package AudioPlayer

import simplefx.core._
import simplefx.all._
import simplefx.scene.paint.Color._

/* === JasperFXPlayer =========================== START ===================================== */
/*
 * 		This sample shows a Player which was written by Jasper Potts, Oracle.
 * 		The original source code and description can be found here:
 * 				
 *  			 http://fxexperience.com/2012/01/fun-javafx-2-0-audio-player/
 * 
 * 		and for some more background information, look at the link here:
 *   
 *   			https://www.java.net/story/jasper-potts-fun-javafx-20-audio-player
 *     
 *		This project is an implementation of the same player (we here call it the 
 *  	"JasperFXPlayer"), basically with identical functionality (we have made a couple of 
 *   	small additions, but basically the running version is identical); just with one 
 *    	significant difference:
 *      
 *      		This player is written in EasyFX, and therefore about 20% of 
 *        		the size of the original java-code written by Jasper Potts.
 *	  
 *  	I hope the reader of this code also finds EasyFX remarkably easier to read and to
 *   	maintain than a similar project written in pure Java. 
 *     
 *     	Note:
 *      (a) This implementation contains some magic values, which we just imported from Jasper
 *      AudioPLayer.scalaPotts' original.
 *      (b) Some of the classes used here have turned pretty big according to my taste.  Some
 *      further abstraction would not have hurt.  But, the purpose of this excercise was  
 *      the comparison, so it made sense to leave it that way.
 *      And (c) ... if you like music, there is a Volume-Knob with which you 
 *      can adjust the "music" to a level of almost zero; if you do that, the program should 
 *      be fun to operate ... (or ... even better: find some other playlists out there ... ) 
 *      
 *          							HAVE FUN     
 * .......................................................................................... */

object JasperFXPlayer extends App
@SimpleFXApp class JasperFXPlayer {

  val audioPlayer = new AudioPlayer(stage) {
    val wh = (1204, 763)
    def scaleX = scene.width / 1204
    def scaleY = scene.height / 763
    def scale = scaleX min scaleY
    transforms <-- List(Translate(scene.width/2, scene.height/2), Scale(scale), Translate(-wh._1/2,-wh._2/2))
  }
  
  scene = new Scene (audioPlayer, 1204, 763, Color.TRANSPARENT) {
	 css = "/AudioPlayer/AudioPlayer.css" :: Nil
  }

  stage.title = "JasperFXPlayer V.2.0"
  stage.xy    = (1400,100)
}
/* === JasperFXPlayer =========================== END ======================================= */
 