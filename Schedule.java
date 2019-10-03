import java.util.HashSet;
import java.util.Set;

public class Schedule {
Set<Column>assignments;
Solver solver;

public Schedule(Solver solver) {
	super();
	assignments=new HashSet<Column>();
	this.solver=solver;
}

public double getScore() {
	return solver.scoreSchedule(this);
}

@Override
public String toString() {
	StringBuilder sb=new StringBuilder();
	
	
	
	for (Column column:this.assignments) { //formats the schedule
		System.out.println("");
		System.out.println(String.format("%-20s %s" , "Course Name: ", column.course.name));
		System.out.println(String.format("%-20s %s" , "Instructor Name: ", column.instructor.name));
		System.out.println(String.format("%-20s %s" , "Room Name: ", column.room.name));
		System.out.println(String.format("%-20s %s" , "Time slot: ", column.time.order));
		System.out.println("");
	}
	
	return sb.toString();
	
}


}
