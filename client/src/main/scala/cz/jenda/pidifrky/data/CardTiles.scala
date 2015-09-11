package cz.jenda.pidifrky.data

import android.app.Activity
import cz.jenda.pidifrky.data.pojo.Card

/**
 * Created <b>30.9.13</b><br>
 *
 * @author Jenda Kolena, jendakolena@gmail.com
 * @version 0.1
 * @since 0.2
 */
class CardTiles(context: Activity, card: Card) {
  //  private final val table: Array[Array[Int]] = new Array[Array[Int]](5, 5)
  //  private final val cardsDao: Nothing = null
  //  private final val showNumbers: Boolean = false
  //  private final val showFound: Boolean = false
  //
  //  def this(context: Activity, card: Card) {
  //    this()
  //    cardsDao = CardsDao.getInstance(context)
  //    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext)
  //    showNumbers = prefs.getBoolean(PidifrkyConstants.PREF_SHOW_TILES_NUMBERS, false)
  //    showFound = prefs.getBoolean(PidifrkyConstants.PREF_SHOW_TILES_FOUND, false)
  //    val nums: Array[String] = card.getNeighbours.split(",")
  //    var t: Int = 0
  //    {
  //      var c: Int = 0
  //      while (c <= 11) {
  //        {
  //          t = nums(c).toInt
  //          table(c / 5)(c % 5) = t
  //          val c2: Int = 13 + c
  //          t = nums(c2 - 1).toInt
  //          table(c2 / 5)(c2 % 5) = t
  //        }
  //        ({
  //          c += 1; c - 1
  //        })
  //      }
  //    }
  //    table(2)(2) = card.getNumber
  //  }
  //
  //  def getCode: String = {
  //    val recs: Int = 3
  //    val pics: Array[AnyRef] = new Array[String](25 * recs)
  //    {
  //      var i: Int = 0
  //      var c: Int = 0
  //      while (c < 25) {
  //        {
  //          try {
  //            val n: Int = table(c / 5)(c % 5)
  //            val path: String = Utils.getFullImageUri(n).getEncodedPath
  //            pics(i) = n + ""
  //            pics(i + 1) = path
  //            pics(i + 2) = (if (showNumbers) n + "" else "") + (if (showFound && cardsDao.isOwner(n)) "<img src=\"file:///android_res/drawable/smiley.png\" />" else "")
  //          }
  //          catch {
  //            case e: Exception => {
  //              pics(i) = ""
  //              pics(i + 1) = "file:///android_res/drawable/tile_empty.png"
  //              pics(i + 2) = ""
  //            }
  //          }
  //        }
  //        i += recs
  //        ({
  //          c += 1; c - 1
  //        })
  //      }
  //    }
  //    return String.format(Utils.getContext.getString(R.string.tiles_code).replaceAll("url", "url('%s')").replaceAll("href=\"\"", "href=\"%s\"").replaceAll("></div", "><span>%s</span></div"), pics)
  //  }
}