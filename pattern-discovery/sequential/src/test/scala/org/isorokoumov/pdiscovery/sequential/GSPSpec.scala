package org.isorokoumov.pdiscovery.sequential

import org.scalatest.{FlatSpec, Matchers}

/**
 * @author ilya40umov
 */
class GSPSpec extends FlatSpec with Matchers {

  "GSP" should "return no frequent sequences if there are none in the data" in {
    val gsp = new GSP[String]
    import gsp.{Event, Sequence}
    gsp.mineFrequentSequences(Seq(
      Sequence(Event("a")),
      Sequence(Event("b")),
      Sequence(Event("c")),
      Sequence(Event("de")),
      Sequence(Event("ghk")),
      Sequence(Event("x"), Event("y"), Event("z"))
    ), 0.5f) should have size 0
  }

  "GSP" should "return correct single-item frequent sequences when they are present in data" in {
    val gsp = new GSP[String]
    import gsp.{Event, Sequence}
    gsp.mineFrequentSequences(Seq(
      Sequence(Event("a")),
      Sequence(Event("b")),
      Sequence(Event("c")),
      Sequence(Event("de")),
      Sequence(Event("ghk")),
      Sequence(Event("a"), Event("b"), Event("z"))
    ), 0.35f) should have size 2
  }

  "GSP" should "return correct multi-item frequent sequences when they are present in data" in {
    val gsp = new GSP[String]
    import gsp.{Event, Sequence}
    gsp.mineFrequentSequences(Seq(
      //<(bd)cb(ac)>
      Sequence(Event("b", "d"), Event("c"), Event("b"), Event("a", "c")),
      //<(bf)(ce)b(fg)>
      Sequence(Event("b", "f"), Event("c", "e"), Event("b"), Event("f", "g")),
      //<(ah)(bf)abf>
      Sequence(Event("a", "h"), Event("b", "f"), Event("a"), Event("b"), Event("f")),
      //<(be)(ce)d>
      Sequence(Event("b", "e"), Event("c", "e"), Event("d")),
      //<a(bd)bcb(ade)>
      Sequence(Event("a"), Event("b", "d"), Event("b"), Event("c"), Event("b"), Event("a", "d", "e"))
    ), 0.4f) should have size (6 + 19 + 20 + 7 + 1)
  }

  "GPS" should "correctly handle maxGap limitation" in {
    val gsp = new GSP[String]
    import gsp.{Event, Sequence}
    val dataSequences = Seq(
      //<(bd)cb(ac)>
      Sequence(Event("b", "d"), Event("c"), Event("b"), Event("a", "c")),
      //<(bf)(ce)b(fg)>
      Sequence(Event("b", "f"), Event("c", "e"), Event("b"), Event("f", "g")),
      //<(ah)(bf)abf>
      Sequence(Event("a", "h"), Event("b", "f"), Event("a"), Event("b"), Event("f")),
      //<(be)(ce)d>
      Sequence(Event("b", "e"), Event("c", "e"), Event("d")),
      //<a(bd)bcb(ade)>
      Sequence(Event("a"), Event("b", "d"), Event("b"), Event("c"), Event("b"), Event("a", "d", "e"))
    )
    gsp.mineFrequentSequences(dataSequences, 0.4f, maxGap = Some(0)) should have size 9
    gsp.mineFrequentSequences(dataSequences, 0.4f, maxGap = Some(1)) should have size 19
    gsp.mineFrequentSequences(dataSequences, 0.4f, maxGap = Some(2)) should have size 45
    gsp.mineFrequentSequences(dataSequences, 0.4f, maxGap = Some(3)) should have size 50
    gsp.mineFrequentSequences(dataSequences, 0.4f, maxGap = Some(4)) should have size 52
  }

}
