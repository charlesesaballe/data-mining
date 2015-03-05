package org.isorokoumov.pdiscovery.nlp

/**
 * Parses reuters21578 dataset into sentences/words.
 * Results are saved into a new file(./data/ner/reuters21578.words).
 * @author ilya40umov
 */
object Reuters21578Words extends Reuters21578NLP {

  println("Started tokenizing Reuters21578 into words ...")

  val articleSentences = readAndParseArticles { articleBody =>
    val sentences = sentenceSplitter(articleBody).map(treeBankTokenizer)
    sentences.map { sentence =>
      sentence.flatMap { word =>
        if (!word.matches("\\p{Punct}|``|''|\\.\\.\\.|--")) {
          Some(word.trim())
        } else {
          None
        }
      }
    }
  }

  writeIntoFile("./data/processed", "reuters21578.words") { writer =>
    articleSentences.flatten.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
      val words = sentence.foldLeft(List[String]()) { (words, word) =>
        if (words.nonEmpty && (word.startsWith("'") || words.head.endsWith("'"))) {
          words.head + word :: words.tail
        } else {
          word :: words
        }
      }.reverse
      words.zipWithIndex.foreach { case (word, wordIndex) =>
        if (word.nonEmpty) {
          writer.println(s"${sentenceIndex + 1} ${wordIndex + 1} $word")
        }
      }
    }
  }

  println("Finished tokenizing Reuters21578 into words.")
}
