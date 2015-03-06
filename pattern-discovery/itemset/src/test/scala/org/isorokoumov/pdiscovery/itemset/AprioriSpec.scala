package org.isorokoumov.pdiscovery.itemset

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.SortedSet

/**
 * @author ilya40umov
 */
class AprioriSpec extends FlatSpec with Matchers {

  "Apriori" should "return no associations if there are no frequent itemsets in the data" in {
    new Apriori[String](0.5f, 0.0f, 0.0f, 0, 10).mineRules(Seq(
      SortedSet("A"),
      SortedSet("B"),
      SortedSet("C"),
      SortedSet("D"),
      SortedSet("E")
    )) should have size 0
  }

  "Apriori" should "return correct associations based on frequent itemsets in the data (single-item itemsets)" in {
    new Apriori[String](0.3f, 0.0f, 0.0f, 0, 10).mineRules(Seq(
      SortedSet("A"),
      SortedSet("B"),
      SortedSet("C"),
      SortedSet("A"),
      SortedSet("E")
    )) should have size 1
  }

  "Apriori" should "return correct associations based on frequent itemsets in the data (multi-item itemsets)" in {
    new Apriori[String](0.3f, 0.0f, 0.0f, 0, 10).mineRules(Seq(
      SortedSet("A", "B"),
      SortedSet("A", "B"),
      SortedSet("C", "F", "G"),
      SortedSet("A", "G"),
      SortedSet("E", "D")
    )) should have size 5 // _ => A, _ => B, _ => G, A => B, B => A
  }

  "Apriori" should "correctly calculate support, confidence, kulczynski and imbalance ratio for association rules" in {
    val singleRules = new Apriori[String](0.5f, 0.0f, 0.0f, 0, 10).mineRules(Seq(
      SortedSet("A"),
      SortedSet("B"),
      SortedSet("A")
    ))
    singleRules should have size 1
    singleRules(0) should have(
      'lhs(SortedSet.empty[String]),
      'rhs(SortedSet("A"))
    )
    // XXX not sure if confidence+kulc+ir make any sense for single item rules(rules with nothing on lhs)
    singleRules(0).support should be(0.66f +- 0.01f)
    singleRules(0).confidence should be(0f +- 0.01f)
    singleRules(0).kulc should be(0.5f +- 0.01f)
    singleRules(0).ir should be(0f +- 0.01f)
  }

}
