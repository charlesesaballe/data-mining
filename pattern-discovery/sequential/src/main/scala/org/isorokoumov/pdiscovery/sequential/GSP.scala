package org.isorokoumov.pdiscovery.sequential

import scala.annotation.tailrec
import scala.collection.immutable.SortedSet

/**
 * Simplistically implements GSP(Generalized Sequential Patterns) algorithm.
 * Very very inefficient implementation.
 * http://dm.kaist.ac.kr/kse525/resources/papers/edbt96gsp.pdf
 * http://www.cin.ufpe.br/~jtalr/Mestrado/Mineracao/ALGORITMOPatternerSrikant96mining.pdf
 * @author ilya40umov
 */
class GSP[Item]()(implicit itemOrdering: Ordering[Item]) {

  import itemOrdering._

  object Event {
    def apply(items: Item*): Event = SortedSet(items: _*)(itemOrdering)
  }

  type Event = SortedSet[Item]

  def mkString(events: TraversableOnce[Event]): String = {
    events.foldLeft(new StringBuilder("<")) { (str, event) =>
      str.append("{").append(event.mkString(",")).append("}")
    }.append(">").toString()
  }

  object Sequence {

    def apply(item: Item): Sequence = Sequence(IndexedSeq(SortedSet(item)(itemOrdering)))

    def apply(events: Event*): Sequence = Sequence(IndexedSeq(events: _*))
  }

  case class Sequence(events: IndexedSeq[Event]) {
    private val eventsAsList = events.toList
    private val itemset = events.flatten.toSet

    def minusOneSubSequences(contiguous: Boolean): Seq[Sequence] = {
      for {
        (event, eventIndex) <- events.zipWithIndex
        (item, itemIndex) <- event.zipWithIndex
        if !contiguous || event.size > 1 || (eventIndex == 0 && itemIndex == 0) || (eventIndex == events.size && itemIndex == event.size)
      } yield Sequence(events.patch(eventIndex, if (event.size > 1) Seq(event - item) else Seq.empty, 1))
    }

    def flatten(): Seq[Item] = events.flatten

    def withoutFirst(): Sequence = {
      if (events.isEmpty) {
        throw new IllegalStateException("sequence is empty!")
      } else {
        val firstEvent = events.head
        if (firstEvent.isEmpty) {
          throw new IllegalStateException("first event in sequence is empty!")
        } else if (firstEvent.size == 1) {
          Sequence(events.tail)
        } else {
          Sequence(firstEvent.tail +: events.tail)
        }
      }
    }

    def withoutLast(): Sequence = {
      if (events.isEmpty) {
        throw new IllegalStateException("sequence is empty!")
      } else {
        val lastEvent = events.last
        if (lastEvent.isEmpty) {
          throw new IllegalStateException("last event in sequence is empty!")
        } else if (lastEvent.size == 1) {
          Sequence(events.take(events.length - 1))
        } else {
          Sequence(events.take(events.length - 1) :+ lastEvent.take(lastEvent.size - 1))
        }
      }
    }

    def last(): Item = {
      if (events.isEmpty) {
        throw new IllegalStateException("sequence is empty!")
      } else {
        val lastEvent = events.last
        if (lastEvent.isEmpty) {
          throw new IllegalStateException("last event in sequence is empty!")
        } else {
          lastEvent.last
        }
      }
    }

    def join(item: Item): List[Sequence] = {
      if (events.isEmpty) {
        List(Sequence(Event(item)))
      } else {
        val lastItem = last()
        if (lastItem < item) {
          List(Sequence(events :+ Event(item)), Sequence(events.take(events.length - 1) :+ (events.last + item)))
        } else {
          List(Sequence(events :+ Event(item)))
        }
      }
    }

    private def mayContain(sequence: Sequence): Boolean = {
      sequence.itemset.forall(this.itemset.contains)
    }

    def contains(maxGap: Option[Int], sequence: Sequence): Boolean = {
      // this one is for search when there is no cap on max gap size
      @tailrec
      def simpleSearch(remnant: Seq[Event], suffix: Seq[Event]): Boolean = {
        if (suffix.nonEmpty) {
          if (remnant.nonEmpty) {
            if (suffix.head.forall(remnant.head.contains)) {
              simpleSearch(remnant.tail, suffix.tail)
            } else {
              simpleSearch(remnant.tail, suffix)
            }
          } else {
            false
          }
        } else {
          true
        }
      }
      // this search takes into account maxGap constraint
      @tailrec
      def backForthSearch(reversedPath: List[Event], remnant: List[Event],
                          reversedPrefix: List[(Event, Int)], suffix: List[Event],
                          currentGap: Int, forward: Boolean): Boolean = {
        if (suffix.nonEmpty) {
          if (forward) {
            if (remnant.nonEmpty) {
              if (currentGap <= maxGap.get) {
                if (suffix.head.forall(remnant.head.contains)) {
                  backForthSearch(remnant.head :: reversedPath, remnant.tail, (suffix.head, currentGap) :: reversedPrefix, suffix.tail, 1, forward = true)
                } else {
                  backForthSearch(remnant.head :: reversedPath, remnant.tail, reversedPrefix, suffix, if (currentGap == 0) currentGap else currentGap + 1, forward = true)
                }
              } else {
                backForthSearch(reversedPath, remnant, reversedPrefix, suffix, currentGap, forward = false)
              }
            } else {
              false
            }
          } else {
            if (currentGap > 1) {
              backForthSearch(reversedPath.tail, reversedPath.head +: remnant, reversedPrefix, suffix, currentGap - 1, forward = false)
            } else {
              if (reversedPrefix.nonEmpty) {
                val (event, gap) = reversedPrefix.head
                backForthSearch(reversedPath, remnant, reversedPrefix.tail, event +: suffix, if (gap == 0) gap else gap + 1, forward = true)
              } else {
                false
              }
            }
          }
        } else {
          true
        }
      }
      if (mayContain(sequence)) {
        if (maxGap.isEmpty) {
          simpleSearch(events, sequence.events)
        } else {
          simpleSearch(events, sequence.events) && backForthSearch(Nil, eventsAsList, Nil, sequence.eventsAsList, 0, forward = true)
        }
      } else {
        false
      }
    }

    override def toString: String = mkString(events)
  }

  type FreqBySequence = Map[Sequence, Int]

  case class FrequentSequence(sequence: Sequence, support: Float) {
    override def toString: String = s"[ $sequence support = $support]"
  }

  def mineFrequentSequences(dataSequences: Seq[Sequence],
                            minimalSupport: Float,
                            maxLength: Int = 10,
                            maxGap: Option[Int] = None): Seq[FrequentSequence] = {
    val dataSetSize = dataSequences.size
    val minimalFrequency = (dataSetSize * minimalSupport).toInt
    assert(minimalFrequency > 0, "Minimal frequency can't be 0!")
    val oneItemFreqSequences = findOneItemFreqSequences(dataSequences, minimalFrequency)
    mineFreqSequences(dataSequences, minimalFrequency, maxLength, maxGap,
      k = 2, kMinusOneFreqSequences = oneItemFreqSequences, allFreqSequences = frequentSequences(oneItemFreqSequences, dataSetSize))
  }

  private def findOneItemFreqSequences(dataSequences: Seq[Sequence], minimalFrequency: Int): FreqBySequence = {
    val freqByItem = dataSequences.foldLeft(Map.empty[Item, Int]) { (freqByItem, dataSequence) =>
      dataSequence.flatten().toSet.foldLeft(freqByItem) { (innerFreqByItem, item) =>
        innerFreqByItem.updated(item, innerFreqByItem.getOrElse(item, 0) + 1)
      }
    }
    freqByItem.filter(_._2 >= minimalFrequency).map {
      case (item, freq) => Sequence(item) -> freq
    }.toMap
  }

  private def frequentSequences(freqSequences: FreqBySequence, dataSetSize: Int): Seq[FrequentSequence] = {
    freqSequences.map { case (sequence, freq) => FrequentSequence(sequence, freq.toFloat / dataSetSize)}.toSeq
  }

  @tailrec
  private def mineFreqSequences(dataSequences: Seq[Sequence],
                                minimalFrequency: Int,
                                maxLength: Int,
                                maxGap: Option[Int],
                                k: Int,
                                kMinusOneFreqSequences: FreqBySequence,
                                allFreqSequences: Seq[FrequentSequence]): Seq[FrequentSequence] = {
    println(k - 1 + " freq pattern count " + kMinusOneFreqSequences.size)
    if (k > maxLength || kMinusOneFreqSequences.isEmpty) {
      allFreqSequences
    } else {
      val kLengthCandidateSequences = generateKLengthCandidates(k, maxGap, kMinusOneFreqSequences)
      println(k + " candidate count " + kLengthCandidateSequences.size)
      //kLengthCandidateSequences.foreach(candidate => println(k + " candidate " + candidate))
      if (kLengthCandidateSequences.isEmpty) {
        allFreqSequences
      } else {
        // XXX here original GSP suggests using a hash-tree structure to check only against a fraction of sequences
        // I've simplified this for now with much less efficient checking each data-seq against every candidate-seq
        val freqKLenSequences: FreqBySequence = dataSequences.par.aggregate(kLengthCandidateSequences.map(_ -> 0).toMap)(
          seqop = { (freqByCandidateSeq, dataSeq) =>
            freqByCandidateSeq.map { case (candidateSeq, freq) =>
              if (dataSeq.contains(maxGap, candidateSeq)) {
                candidateSeq -> (freq + 1)
              } else {
                candidateSeq -> freq
              }
            }
          },
          combop = (freqBySeq1, freqBySeq2) =>
            freqBySeq1 ++ freqBySeq2.map { case (seq, freq) => seq -> (freq + freqBySeq1.getOrElse(seq, 0))}
        ).filter(_._2 >= minimalFrequency)
        mineFreqSequences(dataSequences, minimalFrequency, maxLength, maxGap, k + 1, freqKLenSequences,
          allFreqSequences ++: frequentSequences(freqKLenSequences, dataSequences.size))
      }
    }
  }

  private def generateKLengthCandidates(k: Int, maxGap: Option[Int], kMinusOneFreqSequences: FreqBySequence): Seq[Sequence] = {
    val seedSequences = kMinusOneFreqSequences.keySet
    def hasNoInfrequentSubSequences(sequence: Sequence): Boolean = {
      // any contiguous sub-seq should be frequent AND if maxGap is not set, any sub-seq must be contiguous
      sequence.minusOneSubSequences(contiguous = maxGap.isDefined).forall(seedSequences.contains)
    }
    seedSequences.flatMap { seq1 =>
      seedSequences.flatMap { seq2 =>
        if (k == 2 || seq1.withoutFirst() == seq2.withoutLast()) {
          seq1.join(seq2.last())
        } else {
          Nil
        }
      }
    }.filter(hasNoInfrequentSubSequences).toSeq
  }

}
