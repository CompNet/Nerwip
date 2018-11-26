package fr.univavignon.nerwip.evaluation;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-18 Vincent Labatut et al.
 * 
 * This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import fr.univavignon.common.data.article.ArticleCategory;
import fr.univavignon.common.data.entity.EntityType;

/**
 * This class contains a set of methods useful for
 * implementing performance measures.
 * <br/>
 * Those measures are used to assess how well
 * a recognizer performs on a given (annotated)
 * dataset.
 * <br/>
 * In these classes, we consider two kinds of
 * numerical values: counts and scores. Counts
 * are integers, such as the number of true positives,
 * false positives, etc. Scores are floats,
 * corresponding to calculations performed on
 * the counts, such as F-measure, percent correct,
 * etc.
 * 
 * @author Vincent Labatut
 */
public abstract class AbstractMeasure
{	
	/////////////////////////////////////////////////////////////////
	// NAME				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the measure.
	 * 
	 * @return
	 * 		Name used to represent the measure (particularly in log files).
	 */
	public abstract String getName(); 
	
	/////////////////////////////////////////////////////////////////
	// SCORES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the list of the scores
	 * supported by this class.
	 * 
	 * @return
	 * 		List of strings representing score names.
	 */
	public abstract List<String> getScoreNames();

	/**
	 * Get the total value for
	 * the specified score.
	 * 
	 * @param score
	 * 		Score required by the user.
	 * @return
	 * 		Associated total value.
	 */
	public abstract float getScoreAll(String score);
	
	/**
	 * Get the value for
	 * the specified score,
	 * when considering only
	 * the specified entity type.
	 * 
	 * @param score
	 * 		Score required by the user.
	 * @param type
	 * 		Type of interest.
	 * @return
	 * 		Associated value.
	 */
	public abstract float getScoreByType(String score, EntityType type);

	/**
	 * Get the value for
	 * the specified score,
	 * when considering only
	 * the specified article category.
	 * 
	 * @param score
	 * 		Score required by the user.
	 * @param category
	 * 		Category of interest.
	 * @return
	 * 		Associated value.
	 */
	public abstract float getScoreByCategory(String score, ArticleCategory category);

	/////////////////////////////////////////////////////////////////
	// RECORD			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the file name associated
	 * with the results stored in this measure object.
	 * 
	 * @return
	 * 		The file name of the corresponding results file.
	 */
	public abstract String getFileName();
	
	/**
	 * Writes the scores and counts stored
	 * in this object.
	 * 
	 * @param folder
	 * 		Where to write the file.
	 * @param dataName
	 * 		Name of the evaluated data.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while writing the file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while writing the file.
	 */
	public abstract void writeNumbers(File folder, String dataName) throws FileNotFoundException, UnsupportedEncodingException;
	
	/////////////////////////////////////////////////////////////////
	// STRING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public String toString()
	{	String result = getName();
		return result;
	}
}
