# This script rules Apriori algorithm on NER output for reuters21578 dataset.
# Don't forget to call `install.packages` for `arules` package if you have not done it yet.

library(Matrix)
library(arules)
reuters21578.ner = read.transactions("../data/processed/reuters21578.ner", format = "basket", sep=",", rm.duplicates = T)
reuters21578.rules <- apriori(reuters21578.ner,
                              parameter = list(supp = 0.003, conf = 0.5, target = "rules", minlen = 2, maxlen = 7))
inspect(reuters21578.rules)