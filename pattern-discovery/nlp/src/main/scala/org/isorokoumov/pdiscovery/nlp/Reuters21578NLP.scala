package org.isorokoumov.pdiscovery.nlp

import java.io.{File, PrintWriter}

import epic.preprocess.{MLSentenceSegmenter, TreebankTokenizer}

import scala.io.Source

/**
 * A base trait that contains generic code related to processing `reuters21578` dataset.
 * @author ilya40umov
 */
trait Reuters21578NLP extends App {

  val sentenceSplitter = MLSentenceSegmenter.bundled().get
  val treeBankTokenizer = new TreebankTokenizer

  val sgmlBodyRx = "(?s)<BODY>(.*?)</BODY>".r

  def readAndParseArticles[T](bodyParser: String => T): Seq[T] = {
    // got some encoding issues with #17, so ignoring it for now
    0.to(21).filter(_ != 17).flatMap { idx =>
      println(s"Processing reuters21578 sgm file #$idx")
      val source = Source.fromFile(new File(f"./data/reuters21578/reut2-$idx%03d.sgm"), "UTF-8")
      try {
        val rawArticles = sgmlBodyRx.findAllMatchIn(source.mkString).map(_.group(1)).toSeq.par
        val tidyArticles = rawArticles.map(
          _.replaceAll("Reuter|REUTER|reuter", "").
            replaceAll("\n&#3;", "").
            replaceAll("&#[0-9];", "").
            replaceAll("&", "").
            replaceAll("\n", " ")
        )
        tidyArticles.map(bodyParser)
      } finally {
        source.close()
      }
    }
  }

  def writeIntoFile(dir: String, fileName: String)(write: PrintWriter => Unit): Unit = {
    new File(dir).mkdir()
    val writer = new PrintWriter(new File(dir + File.separator + fileName))
    try {
      write(writer)
    } finally {
      writer.close()
    }
  }

}
