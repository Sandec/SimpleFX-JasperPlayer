package AudioPlayer

import simplefx.all.EqualizerBand._
import simplefx.all.MediaPlayer.Status._
import simplefx.all.Worker.State._
import simplefx.all._
import simplefx.core._

/* === EasyFXPlayer ============================= START =================================== */
object SimpleFXPlayer {
  implicit def toPlayer(x:SimpleFXPlayer) = x.currentplayer
}
/* ......................................................................................... */


/* == EasyFXPlayer ============================== START =================================== */
class SimpleFXPlayer {

  def this(mediaUrl:String) = {this(); url = mediaUrl}
  
/* General, player-undependant interface-variables, -properties and -methods --------------------------------------------------------- */ 
  var playlist:Playlist 	 	= _
  var currentplayer:MediaPlayer = _
  def pls  						= playlist

  type T2m = (Int, Double)

  @Bind var url: String              = "/AudioPlayer/Playlist.xml"
  @Bind var band                     = -1
  @Bind var leftVuMeter              = 0.0
  @Bind var rightVuMeter             = 0.0
  @Bind var noMagnitudeIntervals     = -1
  @Bind var minGain                  = MIN_GAIN
  @Bind var maxGain                  = MAX_GAIN
  @Bind var gain: (Int, Double)      = null
  @Bind var minGainLevel             = 0.0
  @Bind var maxGainLevel             = 1.0
  @Bind var gainLevel: (Int, Double) = null
  @Bind var defaultGainLevel         = 0.75
  @Bind var gainDistance             = <--(gDist)

  private def gDist = maxGain-minGain

  type ARD = Array[Double];		type BARD = B[ARD]		
  type ARI = Array[Int   ];		type BARI = B[ARI]		
  
  lazy val MAX_SIGNAL_LEVELS = 32
  lazy val MAX_METERS 		   = 32
  lazy val mag :ARD  = new Array[Double](MAX_SIGNAL_LEVELS)
  lazy val mags:ARD  = new Array[Double](MAX_SIGNAL_LEVELS)
  lazy val magl:ARI  = new Array[Int   ](MAX_SIGNAL_LEVELS)
  lazy val gais:ARD  = new Array[Double](MAX_METERS)
  lazy val gail:ARD  = new Array[Double](MAX_METERS)

  @Bind var magnitudes 			 = mag
  @Bind var magnitudeSignals = mags
  @Bind var magnitudeLevels	 = magl
  @Bind var gains					   = gais
  @Bind var gainLevels			 = gail
  
  def gain     (ind:Int) = if(currentplayer != null) currentplayer.getAudioEqualizer().getBands().get(ind).getGain else MAX_GAIN
  def gainLevel(ind:Int) = gainLevels(ind)   
  
  url 			       --> urlChanged
  gain 			      .onChange --> gainChanged
  gainLevel 	    .onChange --> gainLevelChanged
  defaultGainLevel.onChange --> defaultGainLevelChanged
/* ................................................................................................................................... */ 

/* Player-dependant Properties ------------------------------------------------------------------------------------------------------- */
  private val dumT = 0 s

  @Bind var autoPlay 				       = false
  @Bind var audioSpectrumInterval  = 1d/30d
  @Bind var audioSpectrumNumBands	 = 10
  @Bind var volume 				    	   = 1.0
  @Bind var balance 			    	   = 0.0
  @Bind var status 				    	   = UNKNOWN
  @Bind var currentTime		    		 = dumT
  @Bind var totalDuration	    		 = dumT
  @Bind val remainingTime	    		 = <--(remT)
  
  private def remT = totalDuration - currentTime
/* ................................................................................................................................... */ 

  
/* Our Interface Action-Methods ------------------------------------------------------------------------------------------------------ */ 
  def play		  = currentplayer.play   
  def pause		  = currentplayer.pause
  def stop		  = currentplayer.stop
  def tooglePlay  = if(status.v == PLAYING) pause else play
  def playNext	  = if(playlist != null) {playlist.useNextItem; playPlaylist}   
  def playPrev	  = if(playlist != null) {playlist.usePrevItem; playPlaylist}
  def skip		  = if(playlist != null) playlist.skip
  def back		  = if(playlist != null) playlist.back
/* ................................................................................................................................... */ 

  
/* Private variables and private helper methods -------------------------------------------------------------------------------------- */ 
  private lazy val THIS   = this
  private var initDone = false
  
  private def gain2level(g:Double) = (g - minGain)/gainDistance
  private def level2gain(l:Double) = minGain + l*gainDistance
  
  private def checkInit {
    if(!initDone) {
    	for(i <- 0 until MAX_METERS) gainLevels(i) = defaultGainLevel
    	for(i <- 0 until MAX_METERS) gains	   (i) = level2gain(defaultGainLevel)
    	initDone = true
    }
  }
  
  private def dispose(cpl:MediaPlayer){
    if(cpl != null) cpl.stop
  }
  
  private def playPlaylist {
	    val plsUrl = playlist.currentItem.url
	    println("PLAY PLAYLIST> initDone?: " + initDone + " " + plsUrl)
	    dispose(currentplayer)
      println("PLAY PLAYLIST> trying to play " + plsUrl)
      def classPath2Url = getClass.getResource(plsUrl).toString
      currentplayer = new E(new MediaPlayer(new Media(classPath2Url))) {
        println("PLAY PLAYLIST> played " + classPath2Url)
        this.setOnEndOfMedia 			 (new Runnable { def run: Unit = playPlaylist }) // TODO
        this.setAudioSpectrumListener(internalSpectrumListener)
        audioSpectrumListener    = internalSpectrumListener
        autoPlay				      <-> THIS.autoPlay// HHS 8.Jun.2015 <-> THIS.autoPlay
        audioSpectrumInterval	<-> THIS.audioSpectrumInterval

        audioSpectrumNumBands	<-> THIS.audioSpectrumNumBands
        volume			          <-> THIS.volume
        balance			          <-> THIS.balance
        THIS.status		        <--  status
        THIS.currentTime	    <--  currentTime
        THIS.totalDuration    <--  {if(totalDuration.v !=null) (totalDuration : Time) else 0 s}
      }.extended
      updated(if(autoPlay) play)
  }
  
  private def urlChanged {
    checkInit
    println("loading: " + url)
	playlist = new Playlist(url) {
      state.onChange --> {if(state.v == SUCCEEDED) {
     	  			playlist.useNextItem
    	  			playPlaylist
      		 	}
      }
    }
  }
  
  private def gainChanged {
    if (currentplayer != null){
    	currentplayer.getAudioEqualizer().getBands().get(gain._1).setGain(gain._2)
	  	gainLevels(gain._1) = gain2level(gain._2)
  	}    
  }

  private def gainLevelChanged {
    if (currentplayer != null){
		currentplayer.getAudioEqualizer().getBands().get(gainLevel._1).setGain(level2gain(gainLevel._2))
		gains(gainLevel._1) = level2gain(gainLevel._2)
  	}
  }
  
  private def defaultGainLevelChanged {
    for (i <- 0 until MAX_METERS) gainLevels(i) = defaultGainLevel
  }
/* ................................................................................................................................... */ 

  
/* Our standard SpectrumListener ----------------------------------------------------------------------------------------------------- */ 
  private def internalSpectrumListener = new AudioSpectrumListener() {	
  	override def spectrumDataUpdate(iTimestamp:Double, iDuration:Double, iMagnitudes:Array[Float], phases:Array[Float] ) {
	  var tempMagnitudes = new Array[Double](MAX_SIGNAL_LEVELS)
	  var tempSignals    = new Array[Double](MAX_SIGNAL_LEVELS)
	  var tempLevels     = new Array[Int   ](MAX_SIGNAL_LEVELS)
	  var average 		 = 0.0
	  for (i <- 0 until iMagnitudes.length ) { 
		  tempMagnitudes(i) = iMagnitudes(i)
		  tempSignals	(i) = ((60 + tempMagnitudes(i))/60)					// Maps to a signal of the range [0,1].
		  tempLevels 	(i) = (tempSignals(i)*noMagnitudeIntervals).toInt	// Maps to a value  of the range [0,noMagnitudeIntervals].
		  
		  if(average < 3) average += tempSignals(i)
	  }
	  
	  average  			= average / 3
	  magnitudes	 	= tempMagnitudes
	  magnitudeSignals 	= tempSignals
	  magnitudeLevels 	= tempLevels
		  
	  if (currentplayer.balance.v == 0.0) { leftVuMeter  = average; 			  				rightVuMeter = average } else
	  if (currentplayer.balance    > 0.0) { leftVuMeter  = average * (1-currentplayer.balance); rightVuMeter = average } else
	  					  				  { rightVuMeter = average * (1+currentplayer.balance); leftVuMeter  = average }
	}// override def
  }//private def  url  			   --> urlChanged
/* ................................................................................................................................... */     
}
/* === EasyFXPlayer ============================= END ===================================== */
