package org.isorokoumov.pdiscovery.itemset

import scala.annotation.tailrec
import scala.collection.immutable.SortedSet

/**
 * Simplistically implements Apriori algorithm.
 * http://rakesh.agrawal-family.com/papers/vldb94apriori.pdf
 * @author ilya40umov
 */
class Apriori[Item](val minimalSupport: Float,
                    val minConfidence: Float,
                    val minKulczynski: Float,
                    val minLength: Int,
                    val maxLength: Int)(implicit itemOrdering: Ordering[Item]) {

  import itemOrdering._

  type Itemset = SortedSet[Item]
  type FrequentItemsets = Map[Itemset, Int]

  case class AssociationRule(lhs: Itemset, rhs: Itemset, support: Float, confidence: Float, kulc: Float, ir: Float)

  def mineRules(transactions: Seq[Itemset]): Seq[AssociationRule] = {
    val freqItemsets = mineFreqItemsets(transactions)
    generateAssociationRules(freqItemsets, transactions.size)
  }

  private def findFrequentOneItemsets(transactions: Seq[Itemset], minimalFrequency: Int): FrequentItemsets = {
    transactions.foldLeft(Map.empty[Itemset, Int]) { (freqOneItemsets, transaction) =>
      transaction.foldLeft(freqOneItemsets) { (foundOneItemsets, item) =>
        val oneItemSet = SortedSet(item)(itemOrdering)
        foundOneItemsets.updated(oneItemSet, foundOneItemsets.getOrElse(oneItemSet, 0) + 1)
      }
    }.filter(_._2 >= minimalFrequency)
  }

  private def generateAprioriCandidates(freqKMinusOneItemsets: FrequentItemsets): FrequentItemsets = {
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
  private def mineFreqItemsets(transactions: Seq[Itemset],
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

  private def mineFreqItemsets(transactions: Seq[Itemset]): FrequentItemsets = {
    val minimalFrequency = (transactions.size * minimalSupport).toInt
    val freqOneItemsets = findFrequentOneItemsets(transactions, minimalFrequency)
    mineFreqItemsets(transactions, minimalFrequency, 2, freqOneItemsets, freqOneItemsets)
  }

  private def generateAssociationRules(freqItemsets: FrequentItemsets, transactionsCount: Int): Seq[AssociationRule] = {
    freqItemsets.toSeq.filter(_._1.size >= minLength).sortBy(_._1.size).flatMap { case (itemset, freq) =>
      itemset.flatMap { item =>
        val lhs = itemset - item
        val rhs = SortedSet(item)(itemOrdering)
        val lhsFreq = freqItemsets.getOrElse(lhs, 0)
        val rhsFreq = freqItemsets.getOrElse(rhs, 0)
        val pLeftWhenRight = if (rhsFreq != 0) freq.toFloat / rhsFreq else 0
        val pRightWhenLeft = if (lhsFreq != 0) freq.toFloat / lhsFreq else 0
        val sup = freq.toFloat / transactionsCount
        val conf = pRightWhenLeft
        val kulc = (pLeftWhenRight + pRightWhenLeft) / 2
        val irDenominator = lhsFreq + rhsFreq - freq.toFloat
        val ir = if (irDenominator != 0) math.abs(lhsFreq - rhsFreq) / irDenominator else 0
        if (sup >= minimalSupport && conf >= minConfidence && kulc >= minKulczynski) {
          Some(AssociationRule(lhs, rhs, sup, conf, kulc, ir))
        } else {
          None
        }
      }
    }
  }

}
