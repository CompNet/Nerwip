java -mx700m -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier classifiers\ner-eng-ie.crf-3-all2008-distsim.ser.gz -textFile %1
