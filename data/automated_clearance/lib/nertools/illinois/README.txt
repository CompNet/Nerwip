  * This project was started by Nicholas Rizzolo (rizzolo@uiuc.edu) . 
  * Most of design, development, modeling and
  * coding was done by Lev Ratinov (ratinov2@uiuc.edu).
  * For modeling details and citations, please refer
  * to the paper: 
  * External Knowledge and Non-local Features in Named Entity Recognition
  * by Lev Ratinov and Dan Roth 
  * submitted/to appear/published at NAACL 09.
  * 
  
INTRODUCTION:

Note: this code uses java templates, so you'll need java version of at least 1.5.

This this release allows you to annotate data with four flavors of 
pre-compiled models and to train an NER tagger with 4 different configurations. 
The configurations are:
Config/baselineFeatures.config.
Config/allLevel1.config
Config/allFeatures.config
Config/allFeaturesBigTrain
Note that the last two configurations use the same set of features, it's just
the the model is trained on more data for the last case. The first three models
are trained only on CoNLL03 training set, while the last model is also trained
on the CoNLL03 development set. The config files already specify where to store 
the models, so when training, you shouldn't be concerned about that.

A note one performance and speed tradeoff: the baseline model achieves modest
83.6 F1 score on CoNLL03 test set. It's very fast. The "allLevel1" model
is a one-layer model which achieves 90.25F score on CoNLL03 shared task,
it's almost as fast and is pretty good.
The 'allFeatures' model is a two-layer architecture that is considerably slower, 
and marginally better, achieving 90.5 F1 score on the CoNLL03 shared task.
The last model is also a two-layer model, it uses the same features as the
previous one, but it was trained both on training and the development set
of the CoNLL03 dataset. It achieves 90.8F1 score on the CoNLL03 test set.
For faster results, use one-layer model. For the better performance - use
the two layer model trained on the bigger dataset.

ANNOTATING PLAIN TEXT:

First of all - how to annotate data with Lbj Ner tagger?
Short answer, here is a sample command:
./annotate sampleText.txt sampleText.tagged.txt true Config/allFeaturesBigTrainingSet.config 

The first parameter is the path to the input plain text file, the second
parameter is the output file, the third parameter indicates whether we
force sentence splitting on newlines, and the last parameter specifies
the configuration used.

Also, it is important to note that many documents, such as webpages contain
formatting beyond punctuation marks. It is often the case that newlines indicate
new sentences even when there is no full stop at the end of the line. Therefore,
the tagger takes an additional parameter that specifies whether the tagger should
force sentence splitting on new lines. On the other hand, when the input is raw 
text where newlines are arbitrary inserted and don't indicate new sentences,
please use 'false' for the parameter value.


Other sample usage examples:

To annotate a file with 1-layer architecture and with forcing 
sentence splitting on new lines, run the 'annotate' script:
> ./annotate <inputfile> <outputfile> true  Config/allLayer1.config

To annotate a file with 1-layer architecture and without forcing 
sentence splitting on new lines, run the 'annotate' script:
> ./annotate <inputfile> <outputfile> false  Config/allLayer1.config



TRAINING THE MODELS:

To train a model on the new data, you need to run the following command:
java -classpath LBJ2.jar:LBJ2Library.jar:bin -Xmx2000m  LbjTagger.NerTagger -train <training-file> -test  <testing-file> <files-format> <force-sentence-splitting-on-newlines> <config-file> 

Where the parameters:
<files-format> can be either -c (for column format) or -r (for brackets format).
<force-sentence-splitting-on-newlines> can be either true or false.

The model will be trained with the number of iterations specified in the config file, 
after each iteration, the accuracy will be printed to the screen and the best model
will be saved into the model file specified in the config file.

Sample training command:
java -classpath LBJ2.jar:LBJ2Library.jar:bin -Xmx2000m  LbjTagger.NerTagger -train Data/GoldData/Reuters/train.brackets.gold -test  Data/GoldData/Reuters/BIO.test.brackets.gold -c true Config/allLayer1.config 

  
