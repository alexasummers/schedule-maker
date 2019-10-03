import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class App {
	
	public static void main(String[] args) throws FileNotFoundException
	{
		Solver test = new Solver();
		Schedule schedule=test.randomSchedule();
		System.out.println("Schedule and fitness scores printed to text file: Schedule.txt.\nSchedule found at bottom of output file.");
        PrintStream o = new PrintStream("Schedule.txt");  //print the schedule and fitness scores to text file
        PrintStream console = System.out; 
        System.setOut(o); 
        System.out.println(test.Solve(1, 5000)); //500 instances of 1 schedule
        
	}	
}
