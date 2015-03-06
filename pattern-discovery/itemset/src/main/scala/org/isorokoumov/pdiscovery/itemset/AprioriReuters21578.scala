package org.isorokoumov.pdiscovery.itemset

import java.io.File

import scala.collection.immutable.SortedSet
import scala.io.Source

/**
 * Runs Apriori algorithm to mine association rules between named entities extracted from reuters21578 dataset.
 * @author ilya40umov
 */
object AprioriReuters21578 extends App {

  type Item = String

  val apriori = new Apriori[Item](minimalSupport = 0.003f, minConfidence = 0.5f, minKulczynski = 0.3f,
    minLength = 2, maxLength = 7)

  def readTransactions(): Seq[apriori.Itemset] = {
    val source = Source.fromFile(new File("./data/processed/reuters21578.ner"))
    try {
      source.getLines().toArray.map(line => SortedSet(line.split(","): _*))
    } finally {
      source.close()
    }
  }

  val associationRules = apriori.mineRules(readTransactions())

  associationRules.foreach { rule =>
    val lhs = "{" + rule.lhs.mkString(", ") + "}"
    val rhs = "{" + rule.rhs.mkString(", ") + "}"
    println(f"$lhs%-60s => $rhs%-40s      ${rule.support}%1.7f       ${rule.confidence}%1.7f" +
      f"      ${rule.kulc}%1.7f       ${rule.ir}%1.7f")
  }

}
