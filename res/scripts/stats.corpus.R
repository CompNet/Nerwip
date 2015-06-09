# 
# Generate some plots related to the corpus stats.
# More convenient to do that in R than in Java...
# 


#
#	Nerwip - Named Entity Extraction in Wikipedia Pages
#	Copyright 2011 Yasa Akbulut, Burcu Küpelioğlu & Vincent Labatut
#	Copyright 2012 Burcu Küpelioğlu, Samet Atdağ & Vincent Labatut
#	Copyright 2013 Samet Atdağ & Vincent Labatut
#	Copyright 2014-15 Vincent Labatut
#	
#	This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
#	
#	Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
#	redistribute it and/or modify it under the terms of the GNU General Public License 
#	as published by the Free Software Foundation, either version 2 of the License, or
#	(at your option) any later version.
#	
#	Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
#	that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
#	of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
#	License for more details.
#	
#	You should have received a copy of the GNU General Public License
#	along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
#	If not, see <http://www.gnu.org/licenses/>.
#

# setwd("C:/Eclipse/workspaces/Extraction/Nerwip2")
# source("scripts/stats.corpus.R")

# read the previously processed stat file
# (see the Java program: classes in tools.corus)
path <- "out/"
path <- "D:/Users/Vincent/Documents/Dropbox/NetExtraction/Data/"
data.file <- paste(path,"stats.txt",sep="")
table <- read.table(data.file,header=TRUE)

# plot word count frequency
##plot.file <- paste(path,"/stats.words.emf",sep="")
##win.metafile(file=plot.file)
#plot.file <- paste(path,"/stats.words.pdf",sep="")
#pdf(file=plot.file,bg="white")
#hist(sort(table[,2]),breaks=50, freq=TRUE, xlab="Number of words", main="Article Size Distribution", col="red")
#dev.off()

# plot character count frequency
##plot.file <- paste(path,"/stats.chars.emf",sep="")
##win.metafile(file=plot.file)
#plot.file <- paste(path,"/stats.chars.pdf",sep="")
#pdf(file=plot.file,bg="white")
#hist(sort(table[,3]),breaks=50, freq=TRUE, xlab="Number of characters", main="Article Size Distribution", col="red")
#dev.off()

# plot entity count frequency
##plot.file <- paste(path,"/stats.entities.emf",sep="")
##win.metafile(file=plot.file)
#plot.file <- paste(path,"/stats.entities.pdf",sep="")
#pdf(file=plot.file,bg="white")
#hist(sort(table[,4]+table[,5]+table[,6]+table[,7]),breaks=50, freq=TRUE, xlab="Number of entities", main="Article Size Distribution", col="red")
#dev.off()
