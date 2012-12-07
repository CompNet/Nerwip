// Modifying this comment will cause the next execution of LBJ2 to overwrite this file.
// F1B8800000000000000054BC1BE02C0241581E759B32425D8C2E4E2E057224C44317EB69314221ADC58697D713E0E2769FFC7E369940514E6CB378001B8D09C8273E3661F4DAF86A3DE8218527F395E7221DA526E047107D5505E748A5CAF2AC8647B585A0C1A67DF6BF58015FF07ED02C10EFA0990B45993BADB918E030993A6D48CEF375BA9E7959000000

package lbj;

import LBJ2.classify.*;
import LBJ2.infer.*;
import LBJ2.learn.*;
import LBJ2.parse.*;
import LbjTagger.BrownClusters;
import LbjTagger.Gazzetteers;
import LbjTagger.NEWord;
import LbjTagger.Parameters;
import StringStatisticsUtils.*;
import java.util.*;




public class NETaggerLevel1 extends SparseNetworkLearner
{
  public static boolean isTraining = false;
  private static java.net.URL lcFilePath;
  private static NETaggerLevel1 instance;
  public static NETaggerLevel1 getInstance()
  {
    if (instance == null)
      instance = (NETaggerLevel1) Classifier.binaryRead(lcFilePath, "NETaggerLevel1");
    return instance;
  }

  static
  {
    lcFilePath = NETaggerLevel1.class.getResource("NETaggerLevel1.lc");

    if (lcFilePath == null)
    {
      System.err.println("ERROR: Can't locate NETaggerLevel1.lc in the class path.");
      System.exit(1);
    }
  }

  public void save()
  {
    if (instance == null) return;

    if (lcFilePath.toString().indexOf(".jar!" + java.io.File.separator) != -1)
    {
      System.err.println("WARNING: NETaggerLevel1.lc is part of a jar file.  It will be written to the current directory.  Use 'jar -u' to update the jar file.  To avoid seeing this message in the future, unpack the jar file and put the unpacked files on your class path instead.");
      instance.binaryWrite(System.getProperty("user.dir") + java.io.File.separator + "NETaggerLevel1.lc", "NETaggerLevel1");
    }
    else instance.binaryWrite(lcFilePath.getPath(), "NETaggerLevel1");
  }

  public static Parser getParser() { return null; }

  public static TestingMetric getTestingMetric() { return null; }

  private static FeatureVector cache;
  private static Object exampleCache;

  private boolean isClone;

  public NETaggerLevel1()
  {
    super("lbj.NETaggerLevel1");
    isClone = true;
    if (instance == null)
      instance = (NETaggerLevel1) Classifier.binaryRead(lcFilePath, "NETaggerLevel1");
  }

  private NETaggerLevel1(boolean b)
  {
    super(new SparseAveragedPerceptron(.1, 0, 16));
    containingPackage = "lbj";
    name = "NETaggerLevel1";
    setLabeler(new NELabel());
    setExtractor(new FeaturesLevel1());
    isClone = false;
  }

  public String getInputType() { return "LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete"; }

  public void learn(Object example)
  {
    if (isClone)
    {
      instance.learn(example);
      return;
    }

    Classifier saveExtractor = extractor;
    Classifier saveLabeler = labeler;

    if (!(example instanceof NEWord))
    {
      if (example instanceof FeatureVector)
      {
        if (!(extractor instanceof FeatureVectorReturner))
          setExtractor(new FeatureVectorReturner());
        if (!(labeler instanceof LabelVectorReturner))
          setLabeler(new LabelVectorReturner());
      }
      else
      {
        String type = example == null ? "null" : example.getClass().getName();
        System.err.println("Classifier 'NETaggerLevel1(NEWord)' defined on line 279 of LbjTagger.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }
    }

    super.learn(example);

    if (saveExtractor != extractor) setExtractor(saveExtractor);
    if (saveLabeler != labeler) setLabeler(saveLabeler);
  }

  public void learn(Object[] examples)
  {
    if (isClone)
    {
      instance.learn(examples);
      return;
    }

    Classifier saveExtractor = extractor;
    Classifier saveLabeler = labeler;

    if (!(examples instanceof NEWord[]))
    {
      if (examples instanceof FeatureVector[])
      {
        if (!(extractor instanceof FeatureVectorReturner))
          setExtractor(new FeatureVectorReturner());
        if (!(labeler instanceof LabelVectorReturner))
          setLabeler(new LabelVectorReturner());
      }
      else
      {
        String type = examples == null ? "null" : examples.getClass().getName();
        System.err.println("Classifier 'NETaggerLevel1(NEWord)' defined on line 279 of LbjTagger.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }
    }

    super.learn(examples);

    if (saveExtractor != extractor) setExtractor(saveExtractor);
    if (saveLabeler != labeler) setLabeler(saveLabeler);
  }

  public FeatureVector classify(Object __example)
  {
    if (isClone) return instance.classify(__example);

    Classifier __saveExtractor = extractor;

    if (!(__example instanceof NEWord))
    {
      if (__example instanceof FeatureVector)
      {
        if (!(extractor instanceof FeatureVectorReturner))
          setExtractor(new FeatureVectorReturner());
      }
      else
      {
        String type = __example == null ? "null" : __example.getClass().getName();
        System.err.println("Classifier 'NETaggerLevel1(NEWord)' defined on line 279 of LbjTagger.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }
    }

__classify:
    {
      if (__example == NETaggerLevel1.exampleCache) break __classify;
      NETaggerLevel1.exampleCache = __example;

      NETaggerLevel1.cache = super.classify(__example);
    }

    if (__saveExtractor != this.extractor) setExtractor(__saveExtractor);
    return NETaggerLevel1.cache;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (isClone)
      return instance.classify(examples);

    Classifier saveExtractor = extractor;

    if (!(examples instanceof NEWord[]))
    {
      if (examples instanceof FeatureVector[])
      {
        if (!(extractor instanceof FeatureVectorReturner))
          setExtractor(new FeatureVectorReturner());
      }
      else
      {
        String type = examples == null ? "null" : examples.getClass().getName();
        System.err.println("Classifier 'NETaggerLevel1(NEWord)' defined on line 279 of LbjTagger.lbj received '" + type + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }
    }

    FeatureVector[] result = super.classify(examples);
    if (saveExtractor != extractor) setExtractor(saveExtractor);
    return result;
  }

  public String discreteValue(Object __example)
  {
    DiscreteFeature f = (DiscreteFeature) classify(__example).firstFeature();
    return f == null ? "" : f.getValue();
  }

  public int hashCode() { return "NETaggerLevel1".hashCode(); }
  public boolean equals(Object o) { return o instanceof NETaggerLevel1; }

  public java.lang.String valueOf(java.lang.Object a0, java.util.Collection a1)
  {
    if (isClone)
      return instance.valueOf(a0, a1);
    return super.valueOf(a0, a1);
  }

  public void write(java.io.PrintStream a0)
  {
    if (isClone)
    {
      instance.write(a0);
      return;
    }

    super.write(a0);
  }

  public void setLTU(LBJ2.learn.LinearThresholdUnit a0)
  {
    if (isClone)
    {
      instance.setLTU(a0);
      return;
    }

    super.setLTU(a0);
  }

  public void setLabeler(LBJ2.classify.Classifier a0)
  {
    if (isClone)
    {
      instance.setLabeler(a0);
      return;
    }

    super.setLabeler(a0);
  }

  public void setExtractor(LBJ2.classify.Classifier a0)
  {
    if (isClone)
    {
      instance.setExtractor(a0);
      return;
    }

    super.setExtractor(a0);
  }

  public void doneLearning()
  {
    if (isClone)
    {
      instance.doneLearning();
      return;
    }

    super.doneLearning();
  }

  public void forget()
  {
    if (isClone)
    {
      instance.forget();
      return;
    }

    super.forget();
  }

  public LBJ2.classify.ScoreSet scores(java.lang.Object a0)
  {
    if (isClone)
      return instance.scores(a0);
    return super.scores(a0);
  }

  public LBJ2.classify.ScoreSet scores(java.lang.Object a0, java.util.Collection a1)
  {
    if (isClone)
      return instance.scores(a0, a1);
    return super.scores(a0, a1);
  }

  public LBJ2.classify.Classifier getLabeler()
  {
    if (isClone)
      return instance.getLabeler();
    return super.getLabeler();
  }

  public LBJ2.classify.Classifier getExtractor()
  {
    if (isClone)
      return instance.getExtractor();
    return super.getExtractor();
  }
}

