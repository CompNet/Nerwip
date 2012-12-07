package tr.edu.gsu.burcu.dateextractor;	

public class Date {

	private int day = 0;
	private int month = 0;
	private int year = 0;
	private int posStart = -1;
	private int posEnd = -1;
	
	public void setDay(int day) {
		this.day = day;
	}
	public int getDay() {
		return day;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getMonth() {
		return month;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getYear() {
		return year;
	}
	public void setPosStart(int posStart) {
		this.posStart = posStart;
	}
	public int getPosStart() {
		return posStart;
	}
	public void setPosEnd(int posEnd) {
		this.posEnd = posEnd;
	}
	public int getPosEnd() {
		return posEnd;
	}
	
	@Override
	public String toString(){
		
		String date="";
		if(day!=0)
			date=date+String.valueOf(day)+" ";
		if(month!=0)
			date=date+String.valueOf(month)+" ";
		if(year!=0)
			date=date+String.valueOf(year)+" ";
		if(date!="")
			date=date+" start: "+posStart+" end:"+posEnd;
		return date;
	}
}