package tr.edu.gsu.nerwip.tools.file;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
 * Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
 * Copyright 2013 Samet Atdağ & Vincent Labatut
 * Copyright 2014-15 Vincent Labatut
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * This class contains various methods
 * related to file management.
 *  
 * @author Vincent Labatut
 */
public class FileTools
{	
	/////////////////////////////////////////////////////////////////
	// FILTERS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Filter able to retain only directories */
	public final static FileFilter FILTER_DIRECTORY = new FileFilter()
	{	@Override
		public boolean accept(File file)
		{	boolean result = file.isDirectory();
			return result;
		}
	};
	
	/**
	 * Creates a filter able to retain only files
	 * with the same name than the specified parameter.
	 * 
	 * @param fileName
	 * 		Targetted filename.
	 * @return
	 * 		Filter dedicated to this name.
	 */
	public final static FilenameFilter createFilter(final String fileName)
	{	FilenameFilter result = new FilenameFilter()
		{	@Override
			public boolean accept(File folder, String name)
			{	boolean result = fileName.equals(name);
				return result;
			}
		};
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// READ				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Open the file at the specified path,
	 * for reading.
	 * 
	 * @param filePath 
	 * 		File to open.
	 * @return 
	 * 		Scanner object ready to read the file.
	 * 
	 * @throws FileNotFoundException 
	 * 		Problem while accessing the file.
	 */
	public static Scanner openTextFileRead(String filePath) throws FileNotFoundException
	{	File file = new File(filePath);
		Scanner result = openTextFileRead(file);
		return result;
	}
	
	/**
	 * Open the specified file for reading.
	 * 
	 * @param file
	 * 		File to open.
	 * @return 
	 * 		Scanner object ready to read the file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static Scanner openTextFileRead(File file) throws FileNotFoundException
	{	FileInputStream fis = new FileInputStream(file);
//		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		InputStreamReader isr = new InputStreamReader(fis);
		Scanner result = new Scanner(isr);
		return result;
	}

	/**
	 * Reads the specified text file and returns a String object.
	 * 
	 * @param file
	 * 		File to be read.
	 * @return
	 * 		String corresponding to the file content.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static String readTextFile(File file) throws FileNotFoundException
	{	StringBuffer temp = new StringBuffer();
		Scanner scanner = FileTools.openTextFileRead(file);
		
		while(scanner.hasNextLine())
		{	String line = scanner.nextLine();
			temp.append(line+"\n");
		}
		
		scanner.close();
		String result = temp.toString();
		return result;
	}
	
	/**
	 * Reads the specified text file and returns a String object.
	 * 
	 * @param filePath
	 * 		Path of the file to be read.
	 * @return
	 * 		String corresponding to the file content.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static String readTextFile(String filePath) throws FileNotFoundException
	{	File file = new File(filePath);
		
		String result = readTextFile(file);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// WRITE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Open the file at the specified path,
	 * for writing.
	 * 
	 * @param filePath
	 * 		File to open.
	 * @return 
	 * 		PrintWriter object ready to write in the file.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the file.
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static PrintWriter openTextFileWrite(String filePath) throws UnsupportedEncodingException, FileNotFoundException
	{	File file = new File(filePath);
		PrintWriter result = openTextFileWrite(file);
		return result;
	}
	/**
	 * Open the specified file for writing.
	 * 
	 * @param file
	 * 		File to open.
	 * @return 
	 * 		PrintWriter object ready to write in the file.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the file.
	 * @throws FileNotFoundException
	 * 		Problem while accessing the file.
	 */
	public static PrintWriter openTextFileWrite(File file) throws UnsupportedEncodingException, FileNotFoundException
	{	FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		PrintWriter result = new PrintWriter(osw);
		return result;
	}

	/**
	 * Records the specified String in a text file.
	 * 
	 * @param file
	 * 		File to be created.
	 * @param content
	 * 		String corresponding to the file content.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the file.
	 */
	public static void writeTextFile(File file, String content) throws IOException
	{	File folder = file.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		PrintWriter pw = FileTools.openTextFileWrite(file);
		
		pw.print(content);
		
		pw.close();
	}
	
	/**
	 * Records the specified String in a text file.
	 * 
	 * @param filePath
	 * 		Path of the file to be created.
	 * @param content
	 * 		String corresponding to the file content.
	 * 
	 * @throws IOException
	 * 		Problem while accessing the file.
	 */
	public static void writeTextFile(String filePath, String content) throws IOException
	{	File file = new File(filePath);
		writeTextFile(file, content);
	}
	
	/////////////////////////////////////////////////////////////////
	// DELETE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Deletes a file or directory, even
	 * if the directory is not empty.
	 * 
	 * @param file
	 * 		File or directory to delete.
	 */
	public static void delete(File file)
	{	// parameter is a folder
		if(file.isDirectory())
		{	File files[] = file.listFiles();
			for(File f: files)
				delete(f);
			file.delete();
		}
	
		// parameter is a file
		else
			file.delete();
	}
	
	/////////////////////////////////////////////////////////////////
	// MOVE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Moves a file or directory, even
	 * if the directory is not empty.
	 * 
	 * @param oldFile
	 * 		File or directory to delete.
	 * @param newFile
	 * 		Resulting file or directory.
	 * @return
	 * 		{@code true} iff every folder and file could be moved.
	 */
	public static boolean move(File oldFile, File newFile)
	{	boolean result = true;
		
		// parameter is a folder
		if(oldFile.isDirectory())
		{	File files[] = oldFile.listFiles();
			for(File of: files)
			{	String path = newFile + File.separator + of.getName();
				File nf = new File(path);
				if(nf.exists())
					nf.delete();
				result = result && move(of,nf);
			}
			if(result)
				oldFile.delete();
		}
		
		// parameter is a file
		else
		{	File parent = newFile.getParentFile();
			if(parent!=null && !parent.exists())
				parent.mkdirs();
			result = oldFile.renameTo(newFile);
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// COPY				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Copies a file or directory.
	 * 
	 * @param oldFile
	 * 		File or directory to copy.
	 * @param newFile
	 * 		Resulting file or directory.
	 * @return
	 * 		{@code true} iff every folder and file could be copied.
	 */
	public static boolean copy(File oldFile, File newFile)
	{	boolean result = true;
		
		// parameter is a folder
		if(oldFile.isDirectory())
		{	File files[] = oldFile.listFiles();
			for(File of: files)
			{	String path = newFile + File.separator + of.getName();
				File nf = new File(path);
				if(nf.exists())
					nf.delete();
				result = result && copy(of,nf);
			}
		}
		
		// parameter is a file
		else
		{	File parent = newFile.getParentFile();
			if(parent!=null && !parent.exists())
				parent.mkdirs();
			result = copyFile(oldFile,newFile);
		}
		
		return result;
	}
	
	/**
	 * Copy a single file. Source code adapted from
	 * http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
	 * <br/>
	 * Note folders are not created by this function.
	 * 
	 * @param sourceFile
	 * 		Original file.
	 * @param destFile
	 * 		New file.
	 * @return
	 * 		{@code true} iff the original file could be copied.
	 */
	private static boolean copyFile(File sourceFile, File destFile)
	{	boolean result = false;
		
		try
		{	if(!destFile.exists())
				destFile.createNewFile();
			result = true;
		}
		catch (IOException e)
		{	//e.printStackTrace();
		}
		
		if(result)
		{	FileChannel source = null;
			FileChannel destination = null;

		    try 
		    {	source = new FileInputStream(sourceFile).getChannel();
			    destination = new FileOutputStream(destFile).getChannel();
			    destination.transferFrom(source, 0, source.size());
			    result = true;
		    }
		    catch (Exception e)
		    {	//e.printStackTrace();
			}
			finally
			{	try
				{
					if(source != null)
						source.close();
					if(destination != null)
						destination.close();
				}
				catch (IOException e)
				{	//e.printStackTrace();
				}
		    }
		}
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// LIST				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns a list of files whose name starts with the specified
	 * prefix, and which are located in the specified folder.
	 *  
	 * @param folder
	 * 		Folder directly containing the files.
	 * @param prefix
	 * 		Begining of the file name.
	 * @return
	 * 		A list of files contained in the folder and whose name starts like the prefix.
	 */
	public static List<File> getFilesStartingWith(String folder, String prefix)
	{	final String pfx = prefix.toLowerCase(Locale.ENGLISH);
		File ff = new File(folder);
		FileFilter filter = new FileFilter()
		{	@Override
			public boolean accept(File file)
			{	String fileName = file.getName().toLowerCase(Locale.ENGLISH);
				boolean result = fileName.startsWith(pfx);
				result = result && !file.isDirectory();
				return result;
			}
		};
		File files[] = ff.listFiles(filter);
		List<File> result =  new ArrayList<File>(Arrays.asList(files));
		return result;
	}
}
