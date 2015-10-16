package logic

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.specs2.runner.JUnitRunner

/**
  * @author Jenda Kolena, kolena@avast.com
  */
@RunWith(classOf[JUnitRunner])
class UpdateHelperTest extends FunSuite {
  test("fixing address") {
    val data = Map(
      "," -> "",
      "Babiččino údolí,Ratibořice" -> "Babiččino údolí, Ratibořice",
      "Bedřichov 218,Bedřichov v Jizerských horách" -> "Bedřichov 218, Bedřichov v Jizerských horách",
      "Bezručova ul.,Trhové Sviny" -> "Bezručova ul., Trhové Sviny",
      ",Křtiny č.p.6" -> "Křtiny č.p. 6",
      ",Hřensko  č.p.82" -> "Hřensko č.p. 82",
      ",Brumov-Bylnice" -> "Brumov - Bylnice",
      "nad hřebčínem,Slatiňany" -> "Nad hřebčínem, Slatiňany",
      "nám Míru 73,Česká Kamenice" -> "Nám. Míru 73, Česká Kamenice",
      "Masrykovo nám. 140,Jilemnice" -> "Masarykovo nám. 140, Jilemnice",
      "Masarykovo nám.71,Bystřice pod Hostýnem" -> "Masarykovo nám. 71, Bystřice pod Hostýnem"
    )

    data.foreach { case (orig, expected) =>
      assertResult(expected)(logic.UpdateHelper.fixAddress(orig))
    }
  }
}
