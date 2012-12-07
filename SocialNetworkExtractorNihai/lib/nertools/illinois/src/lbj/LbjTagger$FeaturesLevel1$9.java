// Modifying this comment will cause the next execution of LBJ2 to overwrite this file.
// discrete% LbjTagger$FeaturesLevel1$9(NEWord word) <- PreviousTag1Level1 && Forms

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


public class LbjTagger$FeaturesLevel1$9 extends Classifier
{
  private static final PreviousTag1Level1 left = new PreviousTag1Level1();
  private static final Forms right = new Forms();

  private static FeatureVector cache;
  private static Object exampleCache;

  public LbjTagger$FeaturesLevel1$9() { super("lbj.LbjTagger$FeaturesLevel1$9"); }

  public String getInputType() { return "LbjTagger.NEWord"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof NEWord))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'LbjTagger$FeaturesLevel1$9(NEWord)' defined on line 275 of LbjTagger.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    if (__example == LbjTagger$FeaturesLevel1$9.exampleCache) return LbjTagger$FeaturesLevel1$9.cache;

    FeatureVector leftVector = left.classify(__example);
    FeatureVector rightVector = right.classify(__example);
    LbjTagger$FeaturesLevel1$9.cache = new FeatureVector();
    for (java.util.Iterator I = leftVector.iterator(); I.hasNext(); )
    {
      Feature lf = (Feature) I.next();
      for (java.util.Iterator J = rightVector.iterator(); J.hasNext(); )
      {
        Feature rf = (Feature) J.next();
        if (lf.equals(rf)) continue;
        LbjTagger$FeaturesLevel1$9.cache.addFeature(lf.conjunction(rf, this));
      }
    }

    LbjTagger$FeaturesLevel1$9.cache.sort();

    LbjTagger$FeaturesLevel1$9.exampleCache = __example;
    return LbjTagger$FeaturesLevel1$9.cache;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    for (int i = 0; i < examples.length; ++i)
      if (!(examples[i] instanceof NEWord))
      {
        System.err.println("Classifier 'LbjTagger$FeaturesLevel1$9(NEWord)' defined on line 275 of LbjTagger.lbj received '" + examples[i].getClass().getName() + "' as input.");
        new Exception().printStackTrace();
        System.exit(1);
      }

    return super.classify(examples);
  }

  public int hashCode() { return "LbjTagger$FeaturesLevel1$9".hashCode(); }
  public boolean equals(Object o) { return o instanceof LbjTagger$FeaturesLevel1$9; }

}

