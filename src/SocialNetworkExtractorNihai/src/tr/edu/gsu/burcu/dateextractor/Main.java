package tr.edu.gsu.burcu.dateextractor;


public class Main {

        public static void main(String[] args) {
                
        DateExtract de = new DateExtract();
        de.parseDate("january 2010"); // end-start==4 or listedeki bi önce bulduðunun start-end 0000-s
        for(Date d:de.getDates())
        {
                System.err.println(d.toString());
        }
    }

}