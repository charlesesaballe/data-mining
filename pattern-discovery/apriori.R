library("arules")
reuters21578.ner = read.transactions("./data/ner/reuters21578.ner", format = "basket", sep=",", rm.duplicates = T)
reuters21578.rules <- apriori(reuters21578.ner,
                              parameter = list(supp = 0.003, conf = 0.5, target = "rules", minlen = 2, maxlen = 7))
inspect(reuters21578.rules)