package org.isorokoumov.pdiscovery.sequential

import java.io.File

import scala.io.Source

/**
 * Runs GSP algorithm to mine frequent phrases from reuters21578 dataset.
 * XXX My implementation of GSP is inefficient + GSP itself is quite slow, as a result I had to experiment only on a subset of data.
 * @author ilya40umov
 */
object GSPReuters21578 extends App {

  type Item = String

  val gsp = new GSP[Item]()

  import org.isorokoumov.pdiscovery.sequential.GSPReuters21578.gsp.{Event, Sequence}

  def readTransactions(): Seq[Sequence] = {
    val source = Source.fromFile(new File("./data/processed/reuters21578.words"))
    try {
      val transactions = source.getLines().toArray.foldLeft(Map.empty[Int, Map[Int, Event]]) { (seqById, line) =>
        val lineElements = line.split(" ")
        val seqId = lineElements(0).toInt
        val eventId = lineElements(1).toInt
        val event = Event(lineElements.drop(2): _*)
        val seq = seqById.get(seqId)
        if (seq.isEmpty) {
          seqById.updated(seqId, Map(eventId -> event))
        } else {
          seqById.updated(seqId, seq.get.updated(eventId, event))
        }
      }.map { case (_, events) => Sequence(events.toSeq.sortBy(_._1).map(_._2).toIndexedSeq)}.toSeq
      println("transactions count = " + transactions.size)
      transactions.take(500)
    } finally {
      source.close()
    }
  }

  val freqSeqs = gsp.mineFrequentSequences(readTransactions(), minimalSupport = 0.01f, maxGap = Some(1))
  freqSeqs.filter(_.sequence.events.size >= 4).foreach(println)
}
