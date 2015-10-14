package AudioPlayer

import javafx.concurrent.Worker.State._

import simplefx.all._
import simplefx.core._

import scala.xml._

/* == Playlist ================================== START ===================================== */
class Playlist (url:String){ 

/* Declare some needed types and some local variables -------------------------------------- */
  private val THIS 		  = this
  type IL = List[Item]
  @Bind private var items: List[Item] = Nil 							// List of Items, as Bindable.
  
  case class  Item (title: String, url:String)      				        // The Item-Class.
/* ......................................................................................... */

/* Declare Interface-Properties and enable convenient access to them ----------------------- */
  @Bind var currentIndex = -1
  @Bind var numItems     = <-- (items.length)
  @Bind var state        = READY
  @Bind private var exception    = new Throwable
/* ......................................................................................... */
  
/* A set of convenience methods, to be from outside as well as inside ---------------------- */
  def currentItem  = if(currentIndex.v == -1) new Item("loading", "") else items(currentIndex)
  def useFirstItem = {currentIndex := FIRST; currentItem}
  def useNextItem  = {currentIndex := NEXT ; currentItem}
  def usePrevItem  = {currentIndex := PREV ; currentItem}
  def skip	  	   =  currentIndex := NEXT
  def back	  	   =  currentIndex := PREV
/* ......................................................................................... */
  
/* A set of private convenience methods ---------------------------------------------------- */
  private def EMPTY = items.length == 0
  private def FIRST = 0
  private def LAST  = items.length-1
  private def NEXT  = if(currentIndex.v==LAST ) FIRST else currentIndex + 1  
  private def PREV  = if(currentIndex.v==FIRST) LAST  else currentIndex - 1  
  
  private def add(list:B[List[Item]], t:String, u:String) = {list := list ::: Item(t, u) :: Nil}

  private def newUrl (u:String, s:String) = {u.lastIndexOf("/") + s}
/* ......................................................................................... */
   
  
  
/* The Interface-method to load a new Playlist --------------------------------------------- */
  def hasExt(elem: String, ext: String*) = !ext.forall(!elem.endsWith(_))
  def load(u:String) {
    println("PLAYLIST> Load ... " + u)
  	if (hasExt(u, "mp3", "mp4", "flv" )) {add     (items, u, u)} else
  	if (hasExt(u, "xml"			          )) {loadXml (          u)} else
 	  if (hasExt(u, "m3u"			          )) {loadM3u (          u)} else
 		throw new Exception ("Wrong file-extension for " + u)
   }
/* ......................................................................................... */
 	

  
/* Private Task-Instantiation with which we let the Xml-loading run in a separate Thread --- */
  private val loadXmlTask = new Task[IL] {
    println("LOADXMLTASK ...")
    @Bind var toRun: (() => IL) = () => (null)
    def call = toRun.apply()

    value.onChange  --> {value.foreach(s=> add(items, s.title, s.url))} // Insert all Songs
    THIS.state      <-- this.state //stateValue 		  --> THIS.state 										                  // into the Items.
    THIS.exception  <-- exception //exceptionValue 	--> THIS.exception									                // Bind all outputs.
  } 
  
  private def runnable(u:String):IL = {						// The actual execution-Logic
    println("RUNNABLE ... " + u)
 	  @Bind var songs:IL = Nil 								        // to run in the Task.
    def classPath2Url = getClass.getResource(u)
    val doc 	  = { println("STARTING TO XML-LOAD " + u + " ... " + classPath2Url); val doc = XML.load(classPath2Url); println("PLAYLIST.SCALA ... " + u + " ...loaded ..."); doc	}						      // Loads the Xml-file/Playlist.
    doc match { 											            // Parses the Xml-Document.
    	case <files>{files @ _*}</files>  => {
        val resourcePath = (doc \"@resourcePath"	).text; println("resourcePath: "   + resourcePath)
        for (file <- files) {
        		val name  = (file \"@name").text			// Extracts the Name-Attribute.
        		val title = (file \"title").text			// Extracts the Title-Attribute.
        		if(hasExt(name, "mp3", "mp4", "mpg", "flv")) {
        			val use = if(title > " ") title else name.substring(0,name.lastIndexOf(".")) // TODO prüfen trim the name from "."
        			simplefx.core.inFX(add(songs, use, resourcePath + name/*newUrl(u, resourcePath + name)*/))		// Adds the Song to the Songs.
        		}
        	}//for
        }//case
        case _  => {throw new Exception ("No <files>-tag in the playlist!" + doc)}
    }// match 	    
    return songs       
  }// runnable
 	  
  private def loadXml(u:String) {
    println("PLAYLIST.SCALA ... loading xml ... " + u)
    //parallel(runnable(u)) // what will be done with the result?
	  loadXmlTask.toRun = () => runnable(u)							// Associate the Execution-logic
 	  new Thread(loadXmlTask).start()							// with the Task and start the
  }															              // Thread with this Task.
/* ......................................................................................... */

  
/* Private Task-Instantiation with which we let the M3U-loading run in a separate Thread --- */
  private def loadM3u(u:String) { 						// Later ... basically a copy
  }															              // of the loadXml, just with
  															              // a different syntax tro parse.
/* ......................................................................................... */
  
 															
  load(url)													          // LOAD Playlist.
}
/* == Playlist ================================== END ======================================= */