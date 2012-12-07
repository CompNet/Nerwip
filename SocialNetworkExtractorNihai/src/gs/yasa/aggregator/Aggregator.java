package gs.yasa.aggregator;

import gs.yasa.aggregator.voting.Voting;
import gs.yasa.aggregator.voting.VotingSession;
import gs.yasa.sne.common.Annotation;
import gs.yasa.sne.common.AnnotationCandidate;
import gs.yasa.sne.common.AnnotationTool;
import gs.yasa.sne.common.AnnotationType;
import gs.yasa.sne.common.ImproperCandidateException;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * This class take an input annotation file, refine the results and writes the new
 * annotated file to an output file.
 * @author yasa akbulut
 * @version 1
 *
 */
public class Aggregator {

	/**
	 * Takes two parameters: an input file and an output file. Reads the input file,
	 * aggregates the annotations and writes them to the output file.
	 * @throws EOFException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @param args
	 * @author yasa akbulut
	 */
	public static void main(String[] args) {
		if(args.length<2)
		{
			System.out.println("Usage: java -jar aggregator.jar input_file output_file");
			System.exit(-1);
		}
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		FileInputStream instream;
		try {
			instream = new FileInputStream(args[0]);
			ObjectInputStream stream = new ObjectInputStream(instream);
			Object readObject;
			while((readObject=stream.readObject())!=null)
			{
				annotations.add((Annotation)readObject);
			}

		}catch(EOFException e)
		{
			System.out.println("EOF reached.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Aggregator aggregator = new Aggregator();
		annotations = aggregator.aggregate(annotations);
		
		File outfile = new File(args[1]);
		
		if(outfile!=null)
		{
			try {
				FileOutputStream fileOut = new FileOutputStream(outfile);
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
				for (Annotation annotation : annotations) {
					objectOut.writeObject(annotation);
				}
				objectOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method takes an annotation list as parameter. It compares them with
	 * PositionBasedAnnotationComparator. Detect groups, compares groups with
	 * LengthBasedAnnotationComparator. Then starts a VotingSession for annotations and
	 * decide a final result for each entity/annotation.
	 * @param annotations
	 * @return result
	 * @author yasa akbulut
	 */
	public ArrayList<Annotation> aggregate(ArrayList<Annotation> annotations)
	{
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		Collections.sort(annotations, new PositionBasedAnnotationComparator());
		
		//System.out.println("We have "+annotations.size()+"annotations.");
		
		ArrayList<AnnotationGroup> groups = detectGroups(annotations);
		
		//System.out.println("we have "+groups.size()+"groups and "+annotations.size()+"stray annotations.");
		

		for (AnnotationGroup annotationGroup : groups) {
			System.out.println("New group detected:"+annotationGroup.getName());
			Collections.sort(annotationGroup.annotations, new LengthBasedAnnotationComparator());
			Collections.reverse(annotationGroup.annotations);
			for (Annotation annotation : annotationGroup.annotations) {
				System.out.println("\t"+annotation);
			}
		}
		
		Voting.initialize();
		for (AnnotationGroup annotationGroup : groups) {
			VotingSession<Integer> startPosVotingSession = new VotingSession<Integer>();
			VotingSession<Integer> endPosVotingSession = new VotingSession<Integer>();
			VotingSession<AnnotationType> typeVotingSession = new VotingSession<AnnotationType>();
			
			for (Annotation annotation : annotationGroup.annotations) {
				startPosVotingSession.vote(annotation.getStartPos(),
						Voting.positionVotePowers.get(annotation.getSource()));
				endPosVotingSession.vote(annotation.getEndPos(),
						Voting.positionVotePowers.get(annotation.getSource()));
				typeVotingSession.vote(annotation.getEntityType(),
						Voting.positionVotePowers.get(annotation.getSource()));
			}
			
			if (annotationGroup.annotations.size() > 0) {
				result.add(new Annotation(annotationGroup.annotations.get(0).getEntityName()+"(...)",
						typeVotingSession.getWinningCandidate(), 
						startPosVotingSession.getWinningCandidate(),
						endPosVotingSession.getWinningCandidate(),
						annotationGroup.annotations.get(0).getRawString(),
						AnnotationTool.AGGREGATOR));
			}
			
		}
		
		Iterator<Annotation> iter = result.iterator();
		while(iter.hasNext())
		{
			Annotation a = iter.next();
			if(!a.getSource().equals(AnnotationTool.AGGREGATOR))
				iter.remove();
		}
		
		
		return result;
		
	}
	

	

	/**
	 * This method detects different annotations from each tool that can belong
	 * to the same entity to regroup them
	 * @param annotations
	 * @return result
	 * @author yasa akbulut
	 */
	private ArrayList<AnnotationGroup> detectGroups(ArrayList<Annotation> annotations)
	{
		PositionBasedAnnotationComparator comparator = new PositionBasedAnnotationComparator();
		ArrayList<AnnotationGroup> result = new ArrayList<AnnotationGroup>();
		
		ArrayList<Annotation> toRemove = new ArrayList<Annotation>();
		
		Iterator<Annotation> iter = annotations.iterator();
		
		while(iter.hasNext())
		{
			Annotation annotation = iter.next();
			
			if(!toRemove.contains(annotation))
			{
				AnnotationGroup group = new AnnotationGroup();
				group.setName(annotation.getEntityName()+" by "+annotation.getSource());
				for (Annotation annotation2 : annotations) {
					if(comparator.compare(annotation, annotation2)==0
							|| annotation.contains(annotation2)
							|| annotation.overlapsWith(annotation2))
					{
						group.annotations.add(annotation2);
					}
				}
				toRemove.addAll(group.annotations);
				result.add(group);
			}else
			{
				iter.remove();
				toRemove.remove(annotation);
			}
		}
		//we now have groups, but some of them are related.
		
		//we'll first sort the annotations within groups
		//so that the longest annotation is the first one within
		//the group.
		for (AnnotationGroup annotationGroup : result) {
			Collections.sort(annotationGroup.annotations, new LengthBasedAnnotationComparator());
			Collections.reverse(annotationGroup.annotations);
		}
	
		//then, we'll compare the first annotation of each group
		//with all of those belonging to the next. If the first 
		//overlaps with any of them, the groups are merged.
		//this will be done recursively until no more changes are made.
		mergeGroups(result);
		
		//last, but not least, we'll combine the annotations which
		//belong to the same tool within the same group. This will
		//combine broken annotations, and help build voting confidence
		//in longer annotations.
		for (AnnotationGroup annotationGroup : result) {
			
			for (AnnotationTool annotationTool : AnnotationTool.values()) {
				
				ArrayList<Annotation> sameToolAnnotations = new ArrayList<Annotation>();
				for (Annotation annotation : annotationGroup.annotations) {
					if(annotation.getSource().equals(annotationTool))
						sameToolAnnotations.add(annotation);
				}
				
				
				if(sameToolAnnotations.size()>1)
				{
					
					//determine boundaires
					int minStartPos = sameToolAnnotations.get(0).getStartPos();
					for (Annotation annotation : sameToolAnnotations) {
						if(annotation.getStartPos()<minStartPos)
							minStartPos = annotation.getStartPos();
					}
					int maxEndPos = sameToolAnnotations.get(0).getEndPos();
					for (Annotation annotation : sameToolAnnotations) {
						if(annotation.getEndPos()>maxEndPos)
							maxEndPos=annotation.getEndPos();
					}
					
					//merge entity names and raw strings
					StringBuilder entityName = new StringBuilder();
					StringBuilder rawString = new StringBuilder();
					for (Annotation annotation : sameToolAnnotations) 
					{
						entityName.append(annotation.getEntityName());
						rawString.append(annotation.getRawString());
						//for each element except the last one, add ellipsis
						if(sameToolAnnotations.indexOf(annotation)!=
							sameToolAnnotations.size()-1)
						{
							entityName.append(" (...) ");
							rawString.append(" (...) ");
						}
					}
					
					//create new annotation
					Annotation combinedAnnotation = new Annotation(
							entityName.toString(),
							sameToolAnnotations.get(0).getEntityType(),
							minStartPos,
							maxEndPos,
							rawString.toString(),
							sameToolAnnotations.get(0).getSource());
					annotationGroup.annotations.removeAll(sameToolAnnotations);
					annotationGroup.annotations.add(combinedAnnotation);
				}
			}
		}
		
		
		return result;
	}

	/**
	 * This method takes as parameter an annotation group list, compares each elements
	 * and merge annotations that belong to a same entity 
	 * @param result
	 * @author yasa akbulut
	 */
	private void mergeGroups(ArrayList<AnnotationGroup> result) {
		int changes = 0;
		Iterator<AnnotationGroup> groupIter = result.iterator();
		while(groupIter.hasNext())
		{
			AnnotationGroup currentGroup = groupIter.next();
			if (groupIter.hasNext()) {
				if (currentGroup.annotations.size() > 0) {
					Annotation annotation = currentGroup.annotations.get(0);
					int currentIndex = result.indexOf(currentGroup);
					AnnotationGroup nextGroup = result.get(currentIndex + 1);
					boolean found = false;
					for (Annotation annotation2 : nextGroup.annotations) {
						if (annotation.overlapsWith(annotation2)
								|| annotation.contains(annotation2)
								|| annotation2.contains(annotation)) {
							found = true;
							changes++;
						}
					}
					if (found) {
						currentGroup.annotations.addAll(nextGroup.annotations);
						nextGroup.annotations.clear();
					}

				} else {
					groupIter.remove();
				}
			}
		}
		if(changes>0)
		{
			mergeGroups(result);
		}
		
	}

	/**This method is the older version of 
	 * aggregate(ArrayList<Annotation> annotations)
	 * @param annotations
	 * @return result
	 * @author yasa akbulut
	 */
	public ArrayList<Annotation> aggregateOld(ArrayList<Annotation> annotations)
	{
		
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		HashMap<AnnotationTool, ArrayList<Annotation>> toolAnnotations = new HashMap<AnnotationTool, ArrayList<Annotation>>();
		HashMap<Annotation, Boolean> annotationVisited = new HashMap<Annotation, Boolean>();
		for (Annotation annotation : annotations) {
			if(!toolAnnotations.containsKey(annotation.getSource()))
				toolAnnotations.put(annotation.getSource(), new ArrayList<Annotation>());
			toolAnnotations.get(annotation.getSource()).add(annotation);
			annotationVisited.put(annotation, false);
		}
		
		if(toolAnnotations.containsKey(AnnotationTool.TAGLINKS))
		{
			ArrayList<Annotation> lerAnnotations = toolAnnotations.get(AnnotationTool.TAGLINKS);
			for (Annotation lerAnnotation : lerAnnotations) {
				if (!annotationVisited.get(lerAnnotation)) {
					for (Annotation annotation : annotations) {
						if (lerAnnotation.overlapsWith(annotation)
								|| lerAnnotation.contains(annotation)
								|| annotation.contains(lerAnnotation)) {
							annotationVisited.put(annotation, true);
							annotationVisited.put(lerAnnotation, true);
							result.add(lerAnnotation);
							//in case of a type mismatch, we can do
							//something here to make a note of it.
						} else {
							annotationVisited.put(lerAnnotation, true);
							result.add(lerAnnotation);
						}
					}
				}
			}
		}
		for (Annotation annotation : annotations) {
			if(!annotationVisited.get(annotation))
			{
				AnnotationCandidate aggregateAnnotationCandidate = new AnnotationCandidate();
				//we'll search for "related" annotations
				ArrayList<Annotation> relatedAnnotations = new ArrayList<Annotation>();
				for (Annotation annotationRel : annotations) {
					if(!annotationVisited.get(annotation))
					{
						if(annotation.overlapsWith(annotationRel)
								||annotation.contains(annotationRel)
								||annotationRel.contains(annotation))
						{
							//related.
							relatedAnnotations.add(annotationRel);
						}
					}
				}
				//let's deal with them now.
				//do the positions agree?
				boolean differentPosition = false;
				for (Annotation relatedAnnotation : relatedAnnotations) {
					if(!relatedAnnotation.hasSamePositions(annotation))
						differentPosition = true;
				}
				if(differentPosition)
				{
					boolean positionConflictSolved = false;
					//we'll first ask opencalais if applicable
					for (Annotation relatedAnnotation : relatedAnnotations) {
						if(relatedAnnotation.getSource().equals(AnnotationTool.OPENCALAIS))
						{
							//let's trust openCalais.
							aggregateAnnotationCandidate.setStartPos(relatedAnnotation.getStartPos());
							aggregateAnnotationCandidate.setEndPos(relatedAnnotation.getEndPos());
							aggregateAnnotationCandidate.setEntityName(relatedAnnotation.getEntityName());
							positionConflictSolved = true;
						}
					}
					if(!positionConflictSolved)
					{
						//TODO: solve this better
						//we'll stick with the longer one for now.
						int maximumLength = 0;
						for (Annotation relatedAnnotation : relatedAnnotations) {
							if(relatedAnnotation.getLength()>maximumLength)
							{
								maximumLength = relatedAnnotation.getLength();
							}
						}
						for (Annotation relatedAnnotation : relatedAnnotations) {
							if(relatedAnnotation.getLength()==maximumLength)
							{
								aggregateAnnotationCandidate.setStartPos(relatedAnnotation.getStartPos());
								aggregateAnnotationCandidate.setEndPos(relatedAnnotation.getEndPos());
								aggregateAnnotationCandidate.setEntityName(relatedAnnotation.getEntityName());
							}
						}
					}
				}else
				{
					aggregateAnnotationCandidate.setStartPos(annotation.getStartPos());
					aggregateAnnotationCandidate.setEndPos(annotation.getEndPos());
					aggregateAnnotationCandidate.setEntityName(annotation.getEntityName());
				}
				
				//how about types?
				boolean differentType = false;
				for (Annotation relatedAnnotation : relatedAnnotations) {
					if(!relatedAnnotation.getEntityType().equals(annotation.getEntityType()))
						differentType = true;
				}
				
				if(differentType)
				{
					boolean typeConflictSolved = false;
					//we'll first ask opencalais if applicable
					for (Annotation relatedAnnotation : relatedAnnotations) {
						if(relatedAnnotation.getSource().equals(AnnotationTool.OPENCALAIS))
						{
							//let's trust openCalais.
							aggregateAnnotationCandidate.setEntityType(relatedAnnotation.getEntityType());
							typeConflictSolved = true;
						}
					}
					if(!typeConflictSolved)
					{
						//TODO: solve this better
						HashMap<AnnotationType, Integer> typeCount = new HashMap<AnnotationType, Integer>();
						for (Annotation relatedAnnotation : relatedAnnotations) {
							if(typeCount.containsKey(relatedAnnotation.getEntityType()))
							{
								Integer value = typeCount.get(relatedAnnotation.getEntityType());
								value++;
								typeCount.put(relatedAnnotation.getEntityType(), value);
							}else
							{
								typeCount.put(relatedAnnotation.getEntityType(), 1);
							}
						}
						int maximum = 0;
						for (Entry<AnnotationType, Integer> type : typeCount.entrySet()) {
							if(type.getValue()>maximum)
								maximum = type.getValue();
						}
						for (Entry<AnnotationType, Integer> type : typeCount.entrySet()) {
							if(!typeConflictSolved)
							{
								if(type.getValue()==maximum)
								{
									aggregateAnnotationCandidate.setEntityType(type.getKey());
									typeConflictSolved = true;
								}
							}
							
						}
						
					}
				}else
				{
					aggregateAnnotationCandidate.setEntityType(annotation.getEntityType());
				}
				
				annotationVisited.put(annotation, true);
				for (Annotation relatedAnnotation : relatedAnnotations) {
					annotationVisited.put(relatedAnnotation, true);
				}
				
				
				try {
					Annotation resultAnnotation = aggregateAnnotationCandidate.toAnnotation();
					resultAnnotation.setSource(AnnotationTool.AGGREGATOR);
					result.add(resultAnnotation);
				} catch (ImproperCandidateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
		}
		
		return result;
		
	}
	
}
