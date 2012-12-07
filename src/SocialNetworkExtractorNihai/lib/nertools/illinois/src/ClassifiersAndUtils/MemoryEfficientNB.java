package ClassifiersAndUtils;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import IO.InFile;
import IO.OutFile;
import StringStatisticsUtils.CharacteristicWords;
import StringStatisticsUtils.OccurrenceCounter;



/**
  * This project was started by Nicholas Rizzolo (rizzolo@uiuc.edu) . 
  * Most of design, development, modeling and
  * coding was done by Lev Ratinov (ratinov2@uiuc.edu).
  * For modeling details and citations, please refer
  * to the paper: 
  * External Knowledge and Non-local Features in Named Entity Recognition
  * by Lev Ratinov and Dan Roth 
  * submitted/to appear/published at NAACL 09.
  * 
 **/

public class MemoryEfficientNB extends Classifier {
	public static final double smooth=0.0001; 
	public double sampleSize=0;
	public double[] weights;//weights[i]=how many words we observed in class i
	public double[] wordCounts;//wordCounts[fid]=how many times we've seen fid in all classes collectively
	public double fidCount=0;//how many words we've seen totally
	public double[] classCounts;//how many instances of each class we've observed
	public Vector<Hashtable<Integer,Double>> fidCounts;//the counts can be fractional if the instances are wieghted
	public FeatureMap map;

	public void save(String file){
		map.save(file+".nb.featuremap");
		OutFile out=new OutFile(file);
		out.println(String.valueOf(sampleSize));
		out.println(String.valueOf(weights.length));
		for(int i=0;i<weights.length;i++)
			out.println(String.valueOf(weights[i]));
		for(int i=0;i<classCounts.length;i++)
			out.println(String.valueOf(classCounts[i]));
		for(int i=0;i<map.dim;i++)
			out.println(String.valueOf(wordCounts[i]));
		out.println(String.valueOf(fidCount));
		out.println(String.valueOf(fidCounts.size()));
		for(int i=0;i<fidCounts.size();i++){
			Hashtable<Integer,Double> h=fidCounts.elementAt(i);
			out.println(String.valueOf(h.size()));
			for(Iterator<Integer> iter=h.keySet().iterator();iter.hasNext();){
				int fid=iter.next();
				double val=h.get(fid);
				out.println(String.valueOf(fid));
				out.println(String.valueOf(val));
			}
		}
		out.close();
	}
	
	public MemoryEfficientNB(String file){
		methodName=file.substring(file.lastIndexOf('/')+1,file.length());
		map=new FeatureMap(file+".nb.featuremap");
		InFile in=new InFile(file);
		sampleSize=Double.parseDouble(in.readLine());
		classesN=Integer.parseInt(in.readLine());
		weights=new double[classesN];
		for(int i=0;i<weights.length;i++)
			weights[i]=Double.parseDouble(in.readLine());
		classCounts=new double[classesN];
		for(int i=0;i<classCounts.length;i++)
			classCounts[i]=Double.parseDouble(in.readLine());
		wordCounts=new double[map.dim];
		for(int i=0;i<map.dim;i++)
			wordCounts[i]=Double.parseDouble(in.readLine());
		fidCount=Double.parseDouble(in.readLine());
		int hashesN=Integer.parseInt(in.readLine());
		fidCounts=new Vector<Hashtable<Integer,Double>>();
		for(int i=0;i<hashesN;i++){
			Hashtable<Integer,Double> h=new Hashtable<Integer, Double>();
			int hSize=Integer.parseInt(in.readLine());
			for(int k=0;k<hSize;k++){
				int fid=Integer.parseInt(in.readLine());
				double val=Double.parseDouble(in.readLine());
				h.put(fid, val);
			}
			fidCounts.addElement(h);
		}
		in.close();
	}
	
	public MemoryEfficientNB(FeatureMap _map,int _classesN)
	{
		allocateSpace(_map,_classesN);
	}
	
	public MemoryEfficientNB(DocumentCollection docs,FeatureMap _map,int _classesN)
	{
		allocateSpace(_map,_classesN);
		for(int i=0;i<docs.docs.size();i++)
		{
			//System.out.println("Learning - document "+i+" of "+docs.docs.size());
			onlineLearning(docs.docs.elementAt(i));
		}
	}
	

	public void onlineLearning(Document doc){
		weightedOnlineLearning(doc.getActiveFid(map),1.0,doc.classID);
	}

	public void weightedOnlineLearning(int[] activeFeatures,double weight,int classID){
		sampleSize+=weight;
		classCounts[classID]+=weight;
		fidCount+=weight*activeFeatures.length;
		for(int j=0;j<activeFeatures.length;j++)
		{
			weights[classID]+=weight;
			wordCounts[activeFeatures[j]]+=weight;			
			updateFidCounts(activeFeatures[j],classID,weight);			
		}
	}
		
	/*
	 * if the conditional probability of a class give the observation
	 * is below the threshold, we return -1 refusing to make a decision
	 * 
	 * set thres to be 0 to make a decision all the time
	 */
	public int classify(Document doc,double thres)
	{
		double[] conf=getPredictionConfidence(doc);
		double sum=0;		
		int maxClass=0;
		for(int i=0;i<classesN;i++)
		{
			if(conf[i]>conf[maxClass])
				maxClass=i;
			sum+=conf[i];
		}
		if(thres!=-1)
		{
			if(conf[maxClass]>=thres)
				return maxClass;
			return -1;	
		}
		return maxClass;
	}
	
	public double[] getPredictionConfidence(Document doc)
	{
		double[] classLLProbs=new double[classesN];
		int[] activeFeats=doc.getActiveFid(map);
		for(int i=0;i<classesN;i++)
		{
			classLLProbs[i]=Math.log(getPrior(i));
			for(int j=0;j<activeFeats.length;j++)
				classLLProbs[i]+=Math.log(getFidProb(activeFeats[j],i));
		}
		int maxClass=0;
		for(int i=0;i<classesN;i++)
			if(classLLProbs[i]>classLLProbs[maxClass])
				maxClass=i;
		//all the log-likelyhoods are negative, so we're selecting the maximum LL
		//e.g. if the LLs were: -2001,-2002,-2003, we choose -2001.
		//then me multiply all the numbers by e^(-2001) which allows
		//us to do precise math
		double denom=0;
		double[] res=new double[classesN];
		for(int i=0;i<classesN;i++)
		{
			res[i]+=Math.exp(classLLProbs[i]-classLLProbs[maxClass]);
			denom+=res[i];
		}
		for(int i=0;i<classesN;i++)
			res[i]=res[i]/denom;
		return res;
	}
	public void allocateSpace(FeatureMap _map,int _classesN)
	{
		map=_map;
		classesN=_classesN;
		classCounts=new double[classesN];
		this.fidCounts=new Vector<Hashtable<Integer,Double>>(classesN);
		for(int i=0;i<classesN;i++)
			fidCounts.addElement(new Hashtable<Integer, Double>());
		weights=new double[classesN];//weights[i]=how many words we observed in class i
		wordCounts=new double[map.dim];
	}
	public double getFidProb(int fid,int classId){
		if(fidCounts.elementAt(classId).containsKey(fid))
			return (1-smooth)*((double)fidCounts.elementAt(classId).get(fid))/weights[classId];
		return smooth/map.dim; 
	}
	public double getPrior(int classId)
	{
		if(sampleSize==0)
			return 0;
		return((double) classCounts[classId])/sampleSize;
	}
	private void updateFidCounts(int fid,int classID,double weight)
	{
		if(fidCounts.elementAt(classID).containsKey(fid))
		{
			double d=fidCounts.elementAt(classID).get(fid);
			fidCounts.elementAt(classID).remove(fid);
			fidCounts.elementAt(classID).put(fid,d+weight);
		}
		else
			fidCounts.elementAt(classID).put(fid,weight);		
	}	
	public double getAcc(DocumentCollection test){
		double correct=0;
		for(int i=0;i<test.docs.size();i++)
		{
			Document doc=test.docs.elementAt(i);
			if(this.classify(doc, -1)==doc.classID)
				correct++;
		}
		return correct/test.docs.size();
	}
	
	/*
	 * score(w)=max{P(w,c)/(P(w)P(c))=max{P(w|c)/P(w)}
	 */
	public Hashtable<String,Integer> getTopPmiWords(int maxWordsPerClass,double confThres,int minAppThres){
		Hashtable<String,Integer> coolWords=new Hashtable<String, Integer>();
		for(int i=0;i<classesN;i++)
		{
			CharacteristicWords words= this.getTopPmiWords(i,maxWordsPerClass,confThres,minAppThres);
			System.out.println(words.toString());
			for(int j=0;j<words.topWords.size();j++)
				if(!coolWords.containsKey(words.topWords.elementAt(j)))
					coolWords.put(words.topWords.elementAt(j), 1);
		}		
		return coolWords;
	}
	public CharacteristicWords getTopPmiWords(int classId,int maxWordsPerClass,double confThres,int minAppThres){
		CharacteristicWords words=new CharacteristicWords(maxWordsPerClass);
		for(int i=0;i<map.dim;i++)
		{
			double pWgivenC=0;
			if(fidCounts.elementAt(classId).containsKey(i))
				pWgivenC=((double)fidCounts.elementAt(classId).get(i))/weights[classId];
			double pW=wordCounts[i]/fidCount;
			double importance=0;
			if((pW>0)&&(wordCounts[i]>minAppThres))
				importance=pWgivenC/pW;
			/*
			if(pW>0)
				importance=pWgivenC*Math.log(pWgivenC/pW);
			if(wordCounts[i]>minAppThres)
				if(fidCounts.elementAt(classId).containsKey(i))
					importance=((double)fidCounts.elementAt(classId).get(i))/wordCounts[i];*/
			if(importance>=confThres)
				words.addElement(map.fidToWord.get(i),importance);
		}
		return words;
	}
	
	public static boolean toBeKept(Vector<String> tokens,Hashtable<String,Integer> coolWords,double minRatio,int minLen){
		OccurrenceCounter counter=new OccurrenceCounter();
		Hashtable<String, Boolean> passed=new Hashtable<String, Boolean>(tokens.size()*2);
		for(int i=0;i<tokens.size();i++){
			String s=tokens.elementAt(i);
			counter.addToken(s);
			if((coolWords.containsKey(s))&&(!passed.containsKey(s)))
				passed.put(s, true);			
		}
		return ((tokens.size()>=minLen)&&(((double)passed.size())/((double)counter.uniqueTokens)>=minRatio));
	}
	public  String getExtendedFeatures(Document d){
		double[] conf=getPredictionConfidence(d);
		String res="";
		for(int i=0;i<classesN;i++)
			if(conf[i]>0)
				res+=methodName+i+"("+conf[i]+") ";
		return res;
	}
}
