
import ol3.ol.source.MapQuest
import ol3u.olx.ViewOptions
import ol3u.olx.layer.TileOptions
import ol3u.olx.source.MapQuestOptions

import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.{JSName, JSExport}
import org.scalajs.dom
/**
 * Created by pappmar on 10/11/2015.
 */
object TestApp extends JSApp {
  import ol3._


//  implicit def imp(s: String) = s.asInstanceOf[UndefOr[ol3u.implicits.`olx.MapOptions#target`]]

  @JSExport
  override def main(): Unit = {
    import ol3u.implicits._

    new ol.Map(ol3u.olx.MapOptions(
      target = "map",
      layers = js.Array(
        new ol.layer.Tile(TileOptions(
          source = new MapQuest(MapQuestOptions(
            layer = "sat"
          ))
        ))
      ),
      view = new ol.View(ViewOptions(
        center = ol.proj.fromLonLat(js.Array(37, 8)),
        zoom = 4.asInstanceOf[js.Any]
      ))
    ))


  }
}


