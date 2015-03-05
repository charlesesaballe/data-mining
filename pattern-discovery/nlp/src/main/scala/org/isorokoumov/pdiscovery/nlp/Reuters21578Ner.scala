package org.isorokoumov.pdiscovery.nlp

import epic.models.NerSelector

/**
 * Performs named entity recognitions on reuters21578 dataset.
 * Results are saved into a new file(./data/ner/reuters21578.ner).
 * @author ilya40umov
 */
object Reuters21578NER extends Reuters21578NLP {

  println("Started NER on Reuters21578...")

  val ner = NerSelector.loadNer("en").get

  val articleEntities = readAndParseArticles { articleBody =>
    val sentences = sentenceSplitter(articleBody).map(treeBankTokenizer)
    val namedEntities = sentences.flatMap { sentence =>
      val nerSequence = ner.bestSequence(sentence)
      nerSequence.label.map {
        case (label, span) => label + ": " + sentence.view(span.begin, span.end).mkString(" ")
      }.filter(!_.startsWith("MISC:"))
    }.toSet
    namedEntities
  }

  writeIntoFile("./data/processed", "reuters21578.ner") { writer =>
    articleEntities.foreach { entities =>
      writer.println(entities.mkString(","))
    }
  }

  println("Finished NER on Reuters21578.")
}
