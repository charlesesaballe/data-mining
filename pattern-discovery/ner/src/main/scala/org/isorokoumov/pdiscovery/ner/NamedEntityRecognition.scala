package org.isorokoumov.pdiscovery.ner

import java.io.{PrintWriter, File}

import epic.models.NerSelector
import epic.preprocess.{MLSentenceSegmenter, TreebankTokenizer}

import scala.io.Source

/**
 * Performs named entity recognitions on reuters21578 dataset.
 * Results are saved into a new file(./data/ner/reuters21578.ner).
 * @author ilya40umov
 */
object NamedEntityRecognition extends App {

  val sentenceSplitter = MLSentenceSegmenter.bundled().get
  val tokenizer = new TreebankTokenizer
  val ner = NerSelector.loadNer("en").get

  val sgmlBodyRx = "(?s)<BODY>(.*?)</BODY>".r

  // got some encoding issues with #17, so ignoring it for now
  val articleEntities = 0.to(21).filter(_ != 17).flatMap { idx =>
    println(s"Processing reuters21578 sgm file #$idx")
    val source = Source.fromFile(new File(f"./data/reuters21578/reut2-$idx%03d.sgm"), "UTF-8")
    try {
      val rawArticles = sgmlBodyRx.findAllMatchIn(source.mkString).map(_.group(1))
      val tidyArticles = rawArticles.map(
        _.replaceAll("Reuter|REUTER", "").
          replaceAll("\n&#3;", "").
          replaceAll("&#[0-9];", "").
          replaceAll("&", "").
          replaceAll("\n", " ")
      ).toSeq.par
      tidyArticles.map { article =>
        val sentences = sentenceSplitter(article).map(tokenizer)
        val namedEntities = sentences.flatMap { sentence =>
          val nerSequence = ner.bestSequence(sentence)
          nerSequence.label.map {
            case (label, span) => label + ": " + sentence.view(span.begin, span.end).mkString(" ")
          }.filter(!_.startsWith("MISC:"))
        }.toSet
        namedEntities
      }
    } finally {
      source.close()
    }
  }

  new File("./data/ner").mkdir()
  val writer = new PrintWriter(new File("./data/ner/reuters21578.ner"))
  try {
    articleEntities.foreach { entities =>
      writer.println(entities.mkString(","))
    }
  } finally {
    writer.close()
  }

  println("Finished.")
}
