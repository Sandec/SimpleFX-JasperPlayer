package AudioPlayer



import AudioPlayer.LoadDialog._
import simplefx.all._
import simplefx.core._
import simplefx.experimental._
import simplefx.geometry.Pos._
import simplefx.scene.paint.Color._
import simplefx.stage.Modality._

/* == LoadDialog ================================ START ===================================== */
object LoadDialog{
  private def URLSTYLE  = "-fx-font-size: 1.2em"
  private def STYLE     = "-fx-base: #282828; -fx-background: #282828; -fx-font-size: 1.1em;"
  private def HEADLINER = "Enter a URL for a playlist(m3u or xml), flv or mp3 file."
  private def PLAYLIST = "http://www.archive.org/download/" +
                    "their_finest_hour_vol3/their_finest_hour_vol3_files.xml"
  private val PLAYLIST1  = "http://www.archive.org/download/their_finest_hour_vol3/" +
                          "their_finest_hour_vol3_files.xml"
 private val url     = :=(PLAYLIST)
}
 /* ......................................................................................... */

/* == LoadDialog ================================ START ===================================== */
class LoadDialog (st:JStage, loadFun:String=>Unit) {

  println("LOADDIALOG> ... ")

 private val pin      = new Group        // Pin for the Scene.
  private val headline = new Label(HEADLINER){textFill=RED}
 private val urlField = new TextField(url){prefWidthProp=900; style=URLSTYLE; text-->url}
 
 private val browBtn = new Button("Browse..."){onAction --> browse} // Open FileChooser.
 private val cancBtn = new Button("Cancel"   ){onAction --> cancel} // Cancel Dialog.
 private val loadBtn = new Button("Load..."  ){onAction --> load  } // Load new Playlist.
 
   private def cancel   = {dialog.hide}                          // The Cancel-Action.
   private def load     = {dialog.hide; loadFun(urlField.text)}        // The Load-Action.
 private def browse   = {show:=true}                         // The Browse-Action.

  private val line1 = headline
  private val line2 = new HBox{spacing=10; hgrowAll=true;    <++(urlField,browBtn); println("LOADDIALOG>HBOX ended ...")}
 private val line3 = new HBox{spacing=10; alignment=CENTER_RIGHT; <++(cancBtn ,loadBtn)}

 private def sceneFill = linearGradient(UL2DL, CycleMethod.REPEAT, (0.0, "#282828" : Color), (1.0,"#202020" : Color))
 private def newScene  = new Scene (pin, 1030, 200) {fill=sceneFill} // Pin to the Scene.
 private val dialog     = new Stage{owner=st; modality=APPLICATION_MODAL; scene=newScene}  
 
 private val show       = :=(false)
  println("LOADDIALOG> ... Filechooser ... " + url)
 private val chooser   = new FileChooser{
   if(show) {
     urlField.text = this.getDelegate.showOpenDialog(dialog).getAbsolutePath
   }
   //showOpenDialog()
   //(show, dialog, url){selectedFile-->urlField.text}
 }
  println("LOADDIALOG> ... Back from ... Filechooser ... " + url)

//  pin <++ new VBox{spacing=20; padding=10; style=STYLE; <++(line1, line2, line3)}//Pin lines.
  pin <++ new VBox{spacing=20; padding=Insets(10); style=STYLE; <++(line1, line2, line3); println("LOADDIALOG>VBOX ended ...") }//Pin lines.

  println("LOADDIALOG> ... show ... ")
  updated(dialog.show)
}
/* == LoadDialog ================================ END ======================================= */