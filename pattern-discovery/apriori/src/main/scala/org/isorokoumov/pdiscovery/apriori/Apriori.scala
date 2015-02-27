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

  val minimalSupport = 0.003f
  val minConfidence = 0.5f
  val minKulczynski = 0.3f // in fact, most interesting rules are should have kulc > 0.6
  val minLength = 2
  val maxLength = 7

  type Item = String
  type Itemset = SortedSet[Item]
  type FrequentItemsets = Map[Itemset, Int]

  case class AssociationRule(lhs: Itemset, rhs: Itemset, support: Float, confidence: Float, kulc: Float, ir: Float)

  def readTransactions(): Seq[Itemset] = {
    val source = Source.fromFile(new File("./data/ner/reuters21578.ner"))
    try {
      source.getLines().toArray.map(line => SortedSet(line.split(","): _*))
    } finally {
      source.close()
    }
  }

  def findFrequentOneItemsets(transactions: Seq[Itemset], minimalFrequency: Int): FrequentItemsets = {
    transactions.foldLeft(Map.empty[Itemset, Int]) { (freqOneItemsets, transaction) =>
      transaction.foldLeft(freqOneItemsets) { (foundOneItemsets, item) =>
        val oneItemSet = SortedSet(item)
        foundOneItemsets.updated(oneItemSet, foundOneItemsets.getOrElse(oneItemSet, 0) + 1)
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
  def mineFreqItemsets(transactions: Seq[Itemset],
                       minimalFrequency: Int,
                       k: Int,
                       freqKMinusOneItemsets: FrequentItemsets,
                       allFoundFreqItemsets: FrequentItemsets): FrequentItemsets = {
    if (k > maxLength || freqKMinusOneItemsets.isEmpty) {
      allFoundFreqItemsets
    } else {
      println(s"k: $k, (k-1) freq itemsets: ${freqKMinusOneItemsets.size}")
      val aprioriCandidates = generateAprioriCandidates(freqKMinusOneItemsets)
      println(s"candidates: ${aprioriCandidates.size}")
      if (aprioriCandidates.isEmpty) {
        allFoundFreqItemsets
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
        mineFreqItemsets(transactions, minimalFrequency, k + 1, freqKItemsets, freqKItemsets ++ allFoundFreqItemsets)
      }
    }
  }

  def mineFreqItemsets(transactions: Seq[Itemset]): FrequentItemsets = {
    val minimalFrequency = (transactions.size * minimalSupport).toInt
    val freqOneItemsets = findFrequentOneItemsets(transactions, minimalFrequency)
    mineFreqItemsets(transactions, minimalFrequency, 2, freqOneItemsets, freqOneItemsets)
  }

  def generateAssociationRules(freqItemsets: FrequentItemsets, transactionsCount: Int): Seq[AssociationRule] = {
    freqItemsets.toSeq.filter(_._1.size >= minLength).sortBy(_._1.size).flatMap { case (itemset, freq) =>
      itemset.flatMap { item =>
        val lhs = itemset - item
        val rhs = SortedSet(item)
        val lhsFreq = freqItemsets(lhs)
        val rhsFreq = freqItemsets(rhs)
        val pLeftWhenRight = freq.toFloat / rhsFreq
        val pRightWhenLeft = freq.toFloat / lhsFreq
        val conf = pRightWhenLeft
        val kulc = (pLeftWhenRight + pRightWhenLeft) / 2
        val ir = math.abs(lhsFreq - rhsFreq) / (lhsFreq + rhsFreq - freq.toFloat)
        if (conf > minConfidence && kulc > minKulczynski) {
          Some(AssociationRule(lhs, rhs, freq.toFloat / transactionsCount, conf, kulc, ir))
        } else {
          None
        }
      }
    }
  }

  val transactions = readTransactions()
  val freqItemsets = mineFreqItemsets(transactions)
  generateAssociationRules(freqItemsets, transactions.size).foreach { rule =>
    val lhs = "{" + rule.lhs.mkString(", ") + "}"
    val rhs = "{" + rule.rhs.mkString(", ") + "}"
    println(f"$lhs%-60s => $rhs%-40s      ${rule.support}%1.7f       ${rule.confidence}%1.7f" +
      f"      ${rule.kulc}%1.7f       ${rule.ir}%1.7f")
  }

}
