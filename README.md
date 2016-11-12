Nerwip v4.1 [French edition]
=======
*Named Entity Extraction in Wikipedia Pages*

* Copyright 2011 (v1) Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
* Copyright 2012 (v2) Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
* Copyright 2013 (v3) Samet Atdağ & Vincent Labatut
* Copyright 2014 (v4) Vincent Labatut
* Copyright 2015 (v4.1) Sabrine Ayachi & Vincent Labatut

Nerwip is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation. For source availability and license information see `licence.txt`

* **Lab site:** http://lia.univ-avignon.fr
* **GitHub repo:** https://github.com/CompNet/nerwip
* **Contact:** Vincent Labatut <vincent.labatut@univ-avignon.fr>

-----------------------------------------------------------------------

## Description
This platform was initially designed to apply and compare Named Entity Recognition (NER) tools on collections of texts. It was later specialized to process biographical articles extracted from the English version of Wikipedia. It allows using several standard standalone tools listed below, as well as some custom tools. It also implements several ways of combining the outputs of standalone NER tools to improve the global output.

The first results obtained on Wikipedia biographical texts were published in [AL'13]. 

## Organization
The source code takes the form of an Eclipse project. It is organized as follows: 
* Package `data` contains all the classes used to represent data: articles, mentions, entities, etc.
* Package `edition` contains a secondary tool, allowing the annotation of articles and the visualization of annotated articles.
* Pacakge  `evaluation` contains classes used to measure the performances of NER tools. Additional measures can be implemented (see the Extension section)
* Package `recognition` contains the NER-related source code. We distinguish different types of NER tools: 
  * Combiners: tools combining the output of standalone tools
  * External: tools executed out of Nerwip, in this case the classes are basically just wrappers.
  * Internal: tools executed from within Nerwip. This requires some internal object-to-object conversion.
    * Model-based: some model (or other data) must be loaded before the NER tool is applied.
    * Model-less: no model needed (e.g. our too Subee, or the Web-service OpenCalais).
* Package `retrieval` contains classes used to get Wikipedia articles and constitute a collection of biogprahical texts.
* Package `tools`: various classes used throughout the software.

The rest of the files are resources:
* Folder `lib` contains the external libraries, especially the NER-related ones (cf. the *Dependencies* section).
* Folder `log` contains the log generated during the processing.
* Folder `out` contains the articles and the files generated during the process. On this GitHub repo, it is empty for space matters, but a corpus can be retrieved from [FigShare](http://figshare.com/articles/Nerwip_Corpus/1289791).
* Folder `res` contains the XML schemas (XSD files) used by Nerwip, as well as the configuration files required by certain NER tools.

## Installation
First, be sure to get the source code of a stable release by checking the [release page](https://github.com/CompNet/Nerwip/releases) of the GitHub repo. Second, you need to download some additional files to get the required data.

Most of the data files are too large to be compatible with GitHub constraints. For this reason, they are hosted on [FigShare](http://figshare.com/articles/Nerwip_Corpus/1289791). Before using Nerwip, you need to retrieve these archives and unzip them in the Eclipse project.

1. Go to our FigShare page http://figshare.com/articles/Nerwip_Corpus/1289791
2. *Optional*: this GitHub project contains only a small part of our corpus. If you want the whole dataset, then: 
  * Download the v4 of the corpus as a Zip archive, 
  * Extract the `out` folder,
  * Put it in the Eclipse project in place of the existing `out` folder.
3. You also need the data related to the different NER tools (models, dictionaries, etc.).
  * Download all 4 Zip files containing the NER data,
  * Extract the `res` folder,  
  * Put it in the Eclipse project, in place of the existing `res` folder. **Do not** remove the existing folder, just overwrite it (we need the existing folders and files).

Certain NER tools require other programming languages than Java, and/or some libraries, and/or to be compiled:
* HeidelTime: it is based on **TreeTagger**, and this tool requires [Perl](https://www.perl.org/). Just install it normally, and do not forget to check the location of the Perl interpreter was added to the PATH system variable (see the official Perl documentation). If Perl is correctly installed and configured, then you should obtain its help page by opening a terminal and executing `perl -h`. Note that, for Linux systems, it is usually installed by default. For Windows systems, you could use the [ActiveState](http://www.activestate.com/activeperl/downloads) implementation of Perl.
* TagEN: this C++ software is already compiled for Linux (it was not tested for Windows). It also needs the Perl language (cf. HeidelTime).
* Nero: this C++ software must be compiled and needs the **OpenFST** and **Wapiti** libraries. Everything is explained in the `README` file located in the res/ner/nero folder. Do not forget to set the `LD_LIBRARY_PATH` and `IRISA_NE` system variables, as explained in the same file. We did not test this tool on Windows (only on Linux). 

Finally, some of the NER tools integrated in Nerwip require some key or password to work. This is the case of:
* Subee: our Wikipedia/Freebase-based NER tool requires a Freebase key to work correctly.
* OpenCalais: this NER tool takes the form of a Web service, and also requires a key.

All keys (and possibly their associated IDs) are set up in the dedicated XML file `keys.xml`, which is located in `res/misc`.

## Use
The main objective of this project is the automatic annotation of entities. However, it also includes a tool allowing to easily perform the manual annotation of texts, in order to constitute a ground-truth used when evaluating the automatic tools.

### Automatic Annotation
For now, there is not interface, not even a command-line one. All the processes need to be launched programmatically, as illustrated by class `tr.edu.gsu.nerwip.Launch`. I advise to import the project in Eclipse and directly edit the source code in this class. A more appropriate interface will be added once the software is more stable.

The output folder is `out` by default. This can be changed by editing the class `tr.edu.gsu.nerwip.tools.file.FileNames` (field `FO_OUTPUT`).

### Manual Annotation
The project contains a graphical interface allowing the  visualization of annotated texts, as well as the annotation of texts. This means this tool might be used to annotate new articles, to change existing annotations, or to visualy check the output of a NER tool. This annotation tool corresponds to the class `tr.edu.gsu.nerwip.edition.EntityEditor`, which can be directly launched.

## Extension
It is relatively straightforward to extend our platform in order to include other NER tools (standalone or combination). For this purpose, one has to extend one of the classes `tr.edu.gsu.nerwip.recognition.combiner.AbstractCombiner` (to combine some NER tools already integrated in Nerwip), `tr.edu.gsu.nerwip.recognition.internal.AbstractInternalRecognizer` (for a NER tool taking the form of a Java program, and programmatically invocable directly from Nerwip), or `tr.edu.gsu.nerwip.recognition.external.AbstractExternalRecognizer` (for a NER tool whose only invocable through the command line). For standalone NER tools, it is also necessary to extend either `tr.edu.gsu.nerwip.recognition.internal.AbstractInternalConverter` (conversion between the NER tool Java objects and Nerwip's)
or `tr.edu.gsu.nerwip.recognition.external.AbstractExternalConverter` (reads the file generated by the external NER tool and convert its content in Nerwip objects). Note new evaluation measures can also be defined by extending the class `tr.edu.gsu.nerwip.evaluation.measure.AbstractMeasure`.

## Dependencies
Here are the dependencies for Nerwip. All of them are included in the Eclipse project, unless specified otherwise.  
* NER tools:
  * [alias-i LingPipe](http://alias-i.com/lingpipe/)
  * [Apache OpenNLP](https://opennlp.apache.org/)
  * [HeidelTime](https://code.google.com/p/heideltime/)
  * [Illinois Named Entity Tagger](http://cogcomp.cs.illinois.edu/page/software_view/NETagger)
  * [LIA-NE](http://pageperso.lif.univ-mrs.fr/~frederic.bechet/download.html)
  * [Nero](https://nero.irisa.fr/)
  * [Stanford Named Entity Recognizer](http://nlp.stanford.edu/software/CRF-NER.shtml)
  * [TagEN](http://gurau-audibert.hd.free.fr/josdblog/wp-content/uploads/2008/03/TagEN.tar.gz)
  * [Thomson Reuters OpenCalais](http://www.opencalais.com/)
* Other libraries:
  * [Apache Commons](http://commons.apache.org/)
  * [Apache HttpComponents](https://hc.apache.org/downloads.cgi)
  * [HTML Parser](http://htmlparser.sourceforge.net/)
  * [JDOM](http://www.jdom.org/)
  * [Jericho HTML Parser](http://jericho.htmlparser.net/docs/index.html)
  * [JSON Simple](https://code.google.com/p/json-simple/)
  * [Jsoup](http://jsoup.org/)
  * [LIBSVM](http://www.csie.ntu.edu.tw/~cjlin/libsvm/)
  * [TreeTagger](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/), needed by HeidelTime.
  * [Unitex](http://www-igm.univ-mlv.fr/~unitex/), needed by TagEn.
* Non-included libraries: some libraries are not included in Nerwip and must be installed manually.  
  * [OpenFST](http://www.openfst.org/), needed by Nero (see its `README` file for instructions).
  * [Wapiti](http://wapiti.limsi.fr/), needed by Nero (again, see its `README` file for instructions).

The project also makes use of several Web services:
* [Reuters OpenCalais](http://new.opencalais.com/)
* [OpeNER](http://www.opener-project.eu/)

## References
* **[AL'13]** Atdağ, S. & Labatut, V. A Comparison of Named Entity Recognition Tools Applied to Biographical Texts, 2nd International Conference on Systems and Computer Science, 2013, 228-233. 
http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6632052&tag=1
