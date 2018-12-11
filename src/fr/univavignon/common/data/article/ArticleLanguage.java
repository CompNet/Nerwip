package fr.univavignon.common.data.article;

import java.util.Locale;

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

/**
 * Language of an article.
 * 
 * @author Vincent Labatut
 */
public enum ArticleLanguage
{	/** English */
	EN,
	/** French */
	FR;
	
	/**
	 * Returns the locale associated to this language.
	 * 
	 * @return
	 * 		The Locale representing this language.
	 */
	public Locale getLocale()
	{	Locale result = null;
		
		switch(this)
		{	case EN:
				result = Locale.ENGLISH;
				break;
			case FR:
				result = Locale.FRENCH;
				break;
		}
		
		return result;
	}
}
