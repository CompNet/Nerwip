package tr.edu.gsu.burcu.dateextractor;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DateExtract{
	

    private ArrayList<String> patterns1;
    private ArrayList<String> patterns2;
    private ArrayList<String> patterns3;
    private ArrayList<String> patterns4;


    
    private ArrayList<Date> dates;
	
    public DateExtract(){
    	
        this.patterns1 = new ArrayList<String>();
        this.patterns2 = new ArrayList<String>();
        this.patterns3 = new ArrayList<String>();
        this.patterns4 = new ArrayList<String>();

        
        this.patterns1.add("dd MMMM yyyy");
        this.patterns1.add("dd MMMM, yyyy");
        this.patterns1.add("MMMM dd, yyyy");
        this.patterns1.add("dd/MMMM/yyyy");
        this.patterns1.add("dd.MMMM.yyyy");
        this.patterns1.add("dd-MMMM-yyyy");
        this.patterns1.add("dd MM yyyy"); 
        this.patterns1.add("dd/MM/yyyy");
        this.patterns1.add("dd.MM.yyyy");
        this.patterns1.add("yyyyy.MMMMM.dd");
        this.patterns2.add("MMMM yyyy");
        this.patterns2.add("MMM yyyy");
        this.patterns3.add("MMMM"); 
        this.patterns4.add("yyyy");

        
        dates = new ArrayList<Date>();
    }
    
    public void parseDate(String text){
    	Locale l = new Locale("en", "US");
        SimpleDateFormat sdf = new SimpleDateFormat("",l);
        
        extractDatePattern1(text,sdf);
        extractDatePattern2(text,sdf);
        extractDatePattern3(text,sdf);
        extractDatePattern4(text,sdf);
        setYearInfo();

    }

    @SuppressWarnings("deprecation")
	public void extractDatePattern1(String text, SimpleDateFormat sdf){
        
        ParsePosition pos = new ParsePosition(0);
            	
        java.util.Date d=null;
        while(pos.getIndex() < text.length()){
        	
        	if(!isSpace(text,pos.getIndex())){
        		
        		d = null;
            	Integer start = new Integer(0);
            	Integer end = new Integer(0);
          
                for(String p: this.patterns1)
                {
                	start = pos.getIndex();
                    sdf.applyPattern(p);
                    d=sdf.parse(text, pos);
                    end = pos.getIndex();
                    if(d!=null)
                    	break;
                }
                if(d!=null && !isInList(start)){
                	Date temp = new Date();
                	temp.setDay(d.getDate());
                	temp.setMonth(d.getMonth()+1);
                	temp.setYear(d.getYear()+1900);
                	temp.setPosStart(start);
                	temp.setPosEnd(end);
                	dates.add(temp);
                }
                
        	}
        	pos.setIndex(pos.getIndex()+1);
        }

    }

    public void extractDatePattern2(String text, SimpleDateFormat sdf){
        
        ParsePosition pos = new ParsePosition(0);

        java.util.Date d=null;
        while(pos.getIndex() < text.length()){
        	
        	if(!isSpace(text,pos.getIndex())){
            	d = null;
            	Integer start = new Integer(0);
            	Integer end = new Integer(0);
                for(String p: this.patterns2)
                {
                	start = pos.getIndex();
                    sdf.applyPattern(p);
                    d=sdf.parse(text, pos);
                    end = pos.getIndex();
                    if(d!=null)
                    	break;
                }
                if(d!=null && !isInList(start)){
                	Date temp = new Date();
                	temp.setMonth(d.getMonth()+1);
                	temp.setYear(d.getYear()+1900);
                	temp.setPosStart(start);
                	temp.setPosEnd(end);
                	dates.add(temp);
                }
        	}
            pos.setIndex(pos.getIndex()+1);
        }

    }

    public void extractDatePattern3(String text, SimpleDateFormat sdf){
        
        ParsePosition pos = new ParsePosition(0);

        java.util.Date d=null;
        while(pos.getIndex() < text.length()){
        	
        	if(!isSpace(text,pos.getIndex())){
        		Integer start = new Integer(0);
            	Integer end = new Integer(-1);
                for(String p: this.patterns3)
                {
                	start = pos.getIndex();
                    sdf.applyPattern(p);
                    d=sdf.parse(text, pos);
                    end = pos.getIndex();
                    if(d!=null)
                    	break;
                }
                if(d!=null&& !isInList(start) && start>0 && isSpace(text,(start-1)) && (isSpace(text,(end)) || isPunctuation(text,(end+1)))){
                	Date temp = new Date();
                	temp.setMonth(d.getMonth()+1);
                	temp.setPosStart(start);
                	temp.setPosEnd(end);
                	dates.add(temp);
                }
        	}
            pos.setIndex(pos.getIndex()+1);
        }

    }

    public void extractDatePattern4(String text, SimpleDateFormat sdf){
        
        ParsePosition pos = new ParsePosition(0);

        java.util.Date d=null;
        while(pos.getIndex() < text.length()){
        	
        	if(!isSpace(text,pos.getIndex())){
        		Integer start = new Integer(0);
            	Integer end = new Integer(0);
                for(String p: this.patterns4)
                {
                	start = pos.getIndex();
                    sdf.applyPattern(p);
                    d=sdf.parse(text, pos);
                    end = pos.getIndex();
                    if(d!=null)
                    	break;
                }
                
                if(d!=null&& !isInList(start) && 
                		(end-start==4 ||
                		( (dates.size()>0) && dates.get(dates.size()-1).getPosStart() == start-5 
                		&& dates.get(dates.size()-1).getPosEnd() == start-1 ))){
                	Date temp = new Date();
                	temp.setYear(d.getYear()+1900);
                	temp.setPosStart(start);
                	temp.setPosEnd(end);
                	dates.add(temp);
                }
        	}
            pos.setIndex(pos.getIndex()+1);
        }

    }
    
    private boolean isSpace(String text, int position){
    	
    	boolean ret = true;
    	char c = text.charAt(position);
    	if(Character.isDigit(c) || Character.isLetter(c) )
    		ret = false;
    	return ret;
    }
    
    private boolean isPunctuation(String text, int position){
    	
    	boolean ret = false;
    	char c = text.charAt(position);
    	if('c'> ' ' && 'c'<'A')
    		ret =true;
    	return ret;
    }
    
    private boolean isInList(int startIndex){
    	
    	for(Date di:dates){
    		if(startIndex>=di.getPosStart() && startIndex<=di.getPosEnd())
    			return true;
    	}
    	return false;
    }
        
    private void setYearInfo(){
    	
    	for(Date d:dates){
    		if(d.getYear() == 0) 
    			if( dates.get(dates.indexOf(d)+1).getYear() != 0 && 
    			(dates.get(dates.indexOf(d)+1).getPosEnd() - dates.get(dates.indexOf(d)).getPosStart()) < 30)
    				d.setYear(dates.get(dates.indexOf(d)+1).getYear());
    			else if(dates.indexOf(d) != 0 && dates.get(dates.indexOf(d)-1).getYear() != 0)
    				d.setYear(dates.get(dates.indexOf(d)-1).getYear());
    	}
    		
    }

    public ArrayList<Date> getDates(){
    	return dates;
    }
}