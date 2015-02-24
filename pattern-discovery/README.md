# pattern-discovery

This SBT project contains my experiments related to materials of
[Pattern Discovery in Data Mining](https://www.coursera.org/course/patterndiscovery) class from Coursera.

### Datasets

The project uses the following datasets(which are downloaded and unpacked by fetchdata.sh):

* [Reuters-21578](http://www.daviddlewis.com/resources/testcollections/reuters21578/)

### Named Entity Recognition

For [NER](http://en.wikipedia.org/wiki/Named-entity_recognition) I'm using [Epic](https://github.com/dlwh/epic/) library
 from [ScalaNLP](http://www.scalanlp.org/) family.

### HOW-TO

For repeating my experiment(s), you should perform the following steps:

1. Clone my repo to your computer using `git clone`. Or fork my repo first and then clone the fork.
2. Run `fetchdata.sh` script, which will download necessary data.
3. Run `sbt "run-main org.isorokoumov.pdiscovery.ner.NamedEntityRecognition"` which will generate named entities for articles from reuters21578.
4. Run `sbt "run-main org.isorokoumov.pdiscovery.apriori.Apriori"` which will find some association rules(params of the algorithm are hardcoded).

NOTE: Alternatively, instead of step #4 you can use `arules` package in R. Check `apriori.R` for an example.