# This script rules SPADE algorithm on words for reuters21578 dataset(treating all sentences as sequences of words).
# Don't forget to call `install.packages` for `arules`/`arulesSequences` packages if you have not done it yet.

library(Matrix)
library(arules)
library(arulesSequences)
options(width=120)

words.con <- file("../data/processed/reuters21578.words", "r")
reuters21578.words <- read_baskets(con = words.con, sep = " ", info = c("sequenceID","eventID"))
close(words.con)

# note that maxgap should be 1 to mine phrases
spade.res <- cspade(reuters21578.words, parameter = list(support = 0.001, maxgap = 1), control = list(verbose = TRUE))

df <- as(spade.res, "data.frame")
# only prining rules with 4+ words because the rest contains too much junk
df[grepl("<.*,.*,.*,.*>",df$sequence), ]