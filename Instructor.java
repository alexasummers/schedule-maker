import java.util.Map;
import java.util.Set;

public class Instructor {
	String name;
	Set<String>courses;
	boolean gradStudent;
	public Instructor(String name, Set<String> courses, boolean gradStudent) {
		super();//Eclipse generated
		this.name = name;
		this.courses = courses;
		this.gradStudent = gradStudent;
	}
	@Override //Eclipse generated
	public String toString() {
		return name;
	}
	
	}

