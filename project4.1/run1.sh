./bin/hadoop distcp s3://jiexing/project41/ngram/output2/total /mnt/input/
sort -rnk 2 -k 1 ngrams200 | head -n 100 > ngrams

