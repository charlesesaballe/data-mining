package org.isorokoumov.pdiscovery.apriori

import java.io.File

import scala.annotation.tailrec
import scala.collection.immutable.SortedSet
import scala.io.Source

/**
 * Implements Apriori algorithm for mining association rules between named entities in reuters21578 dataset.
 * @author ilya40umov
 */
object Apriori extends App {

  val minimalSupport = 0.003
  val minConfidence = 0.5
  // not used yet
  val minLength = 2
  val maxLength = 7

  type Itemset = SortedSet[String]
  type FrequentItemsets = Map[Itemset, Int]

  def readTransactions(): Seq[Itemset] = {
    val source = Source.fromFile(new File("./data/ner/reuters21578.ner"))
    try {
      source.getLines().toArray.map(line => SortedSet(line.split(","): _*))
    } finally {
      source.close()
    }
  }

  val transactions = readTransactions()
  val minimalFrequency = (transactions.size * minimalSupport).toInt

  def findFrequentOneItemsets(): FrequentItemsets = {
    transactions.foldLeft(Map.empty[Itemset, Int]) { (freqOneItemsets, transaction) =>
      transaction.foldLeft(freqOneItemsets) { (foundOneItemsets, item) =>
        val itemSet = SortedSet(item)
        foundOneItemsets.updated(itemSet, foundOneItemsets.getOrElse(itemSet, 0) + 1)
      }
    }.filter(_._2 >= minimalFrequency)
  }

  def generateAprioriCandidates(freqKMinusOneItemsets: FrequentItemsets): FrequentItemsets = {
    def hasNoInfrequentSubsets(itemset: Itemset): Boolean = {
      itemset.subsets(itemset.size - 1).forall(freqKMinusOneItemsets.contains)
    }
    val kMinusOneItemsets = freqKMinusOneItemsets.map(_._1)
    val kMinusOne = kMinusOneItemsets.head.size
    val kItemsets = for {
      l1 <- kMinusOneItemsets
      l2 <- kMinusOneItemsets if l1.take(kMinusOne - 1) == l2.take(kMinusOne - 1) && l1.last < l2.last
    } yield l1 + l2.last
    kItemsets.filter(hasNoInfrequentSubsets).map(_ -> 0).toMap
  }

  @tailrec
  def mineFreqItemsets(k: Int,
                       freqKMinusOneItemsets: FrequentItemsets,
                       foundFreqItemsets: List[FrequentItemsets]): List[FrequentItemsets] = {
    if (freqKMinusOneItemsets.isEmpty) {
      foundFreqItemsets.tail
    } else if (k > maxLength) {
      foundFreqItemsets
    } else {
      println(s"k: $k, (k-1) freq itemsets: ${freqKMinusOneItemsets.size}")
      val aprioriCandidates = generateAprioriCandidates(freqKMinusOneItemsets)
      println(s"candidates: ${aprioriCandidates.size}")
      if (aprioriCandidates.isEmpty) {
        foundFreqItemsets
      } else {
        val freqItems = aprioriCandidates.map(_._1).flatten.toSet
        val freqKItemsets = transactions.foldLeft(aprioriCandidates) { (aprioriKItemsets, transaction) =>
          transaction.intersect(freqItems).subsets(k).foldLeft(aprioriKItemsets) { (candidateKItemsets, tSubItemset) =>
            val freq = candidateKItemsets.get(tSubItemset)
            if (freq.isDefined) {
              candidateKItemsets.updated(tSubItemset, freq.get + 1)
            } else {
              candidateKItemsets
            }
          }
        }.filter(_._2 >= minimalFrequency)
        mineFreqItemsets(k + 1, freqKItemsets, freqKItemsets :: foundFreqItemsets)
      }
    }
  }

  val freqOneItemsets = findFrequentOneItemsets()
  mineFreqItemsets(2, freqOneItemsets, List(freqOneItemsets)).flatten.
    filter(_._1.size >= minLength).sortBy(_._1.size).foreach {
    case (itemset, freq) => println(itemset.mkString(" - ") + " : " + freq.toDouble / transactions.size)
  }

}
