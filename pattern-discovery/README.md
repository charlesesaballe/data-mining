# pattern-discovery

This SBT project contains my experiments related to materials of
[Pattern Discovery in Data Mining](https://www.coursera.org/course/patterndiscovery) class from Coursera.

### Datasets

The project uses the following datasets(which are downloaded and unpacked by fetchdata.sh):

* [Reuters-21578](http://www.daviddlewis.com/resources/testcollections/reuters21578/)

### NLP and NER

For various [NLP](http://en.wikipedia.org/wiki/Natural_language_processing) tasks, and in particular for parsing text
 into sentences/words as well as for [NER](http://en.wikipedia.org/wiki/Named-entity_recognition), I'm using
 [Epic](https://github.com/dlwh/epic/) library from [ScalaNLP](http://www.scalanlp.org/) family.

### Findings

1. [APRIORI-ON-NER.txt](https://github.com/ilya40umov/data-mining/tree/master/pattern-discovery/findings/APRIORI-ON-NER.txt)
2. [SPADEorGSP-ON-WORDS.txt](https://github.com/ilya40umov/data-mining/tree/master/pattern-discovery/findings/SPADEorGSP-ON-WORDS.txt)

### HOW-TO(repeat my experiments)

1. First of all, clone my repo to your computer using `git clone`. Or fork my repo first and then clone the fork.
2. Run `fetchdata.sh` script, which will download necessary data.

#### Apriori on NER output for Reuters-21578

1. Run `sbt "run-main org.isorokoumov.pdiscovery.nlp.Reuters21578NER"`
which will generate named entities for articles from reuters21578.
2. Start R in directory `rscripts` and call `source("apriori.R")` which uses `arules` package
OR alternatively use `sbt "run-main org.isorokoumov.pdiscovery.itemset.AprioriReuters21578"`
(Scala implementation also counts Kulczycki and IR for each association rule it produces).

#### SPADE or GSP on sentences as sequences of words from Reuters-21578

1. Run `sbt "run-main org.isorokoumov.pdiscovery.nlp.Reuters21578Words"`
which will generate named entities for articles from reuters21578.
2. Start R in directory `rscripts` and call `source("spade_on_words.R")` which uses `arules`/`arulesSequences` packages.
I have not implemented Scala version for sequential pattern mining.