package logic

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object UpdateHelper {
  private val CardsPositions: Map[Int, (Float, Float)] = Map(
    363 ->(49.3552172f, 16.0122428f),
    396 ->(49.7579297f, 16.6642572f),
    397 ->(49.6123714f, 16.6794683f),
    467 ->(49.4308331f, 17.2879547f),
    519 ->(49.5479639f, 17.7346917f),
    662 ->(50.6663342f, 15.5481647f),
    665 ->(50.6766433f, 15.7502317f),
    668 ->(50.6259625f, 15.8164817f),
    680 ->(49.7012106f, 17.0761486f),
    683 ->(49.5919617f, 18.1174406f),
    685 ->(48.6622f, 14.1664f)
  )

  private val MerchantsPositions: Map[Int, (Float, Float)] = Map(
    1280 ->(49.881499f, 12.980864f),
    1334 ->(50.421151f, 16.054801f),
    1348 ->(48.9894950f, 15.9870467f),
    1396 ->(49.4134222f, 14.6773347f),
    1411 ->(49.1857450f, 14.6999694f)
  )

  private val AddressReplacements = Map(
    "Ostarva" -> "Ostrava",
    "Masrykovo" -> "Masarykovo",

    "Plachého 35,Konstantinovy Lázně" -> "Náměstí U Fontány 208, Konstantinovy Lázně",
    "Babiččino údolí,Ratibořice" -> "Babiččino údolí, Ratibořice",
    "Na Kruhovém objezdu 8,Jevišovice" -> "Jevišovice 56, Jevišovice",
    "U Bechyňské dráhy 2926 (vlakové nádraží),Tábor" -> "Vlakové nádraží, U Bechyňské dráhy 2926, Tábor",
    "ČSA 582/1,Veselí nad Lužnicí" -> "Třída Čs. armády 582, Veselí nad Lužnicí"
  )

  def positionForCard(id: Int): Option[(Float, Float)] = CardsPositions.get(id)

  def positionForMerchant(id: Int): Option[(Float, Float)] = MerchantsPositions.get(id)

  def fixAddress(address: String): String = {
    var add = address.trim

    AddressReplacements.foreach { case (orig, fixed) =>
      add = add.replaceAll(orig, fixed)
    }

    add = add
      .stripPrefix(",")
      .stripSuffix(",")
      .replaceAll("\\.", ". ")
      .replaceAll(",", ", ")
      .replaceAll("-", " - ")
      .replaceAll("  ", " ")
      .replaceAll("č\\. p\\.", "č.p.")
      .replaceAll("\\. ,", ".,")
      .replaceAll("[nN]ám ", "nám. ")

    if (add.isEmpty)
      add
    else
      add.charAt(0).toUpper + add.substring(1) //first letter big
  }
}
