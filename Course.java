
public class Course {
	String name;
	int expectedEnrollment;
	public Course(String name, int expectedEnrollment) {
		super(); //Eclipse generated
		this.name = name;
		this.expectedEnrollment = expectedEnrollment;
	}
	@Override //Eclipse generated
	public String toString() {
		return name + "[" + expectedEnrollment + "]";
	}
	
}
