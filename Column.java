
public class Column {
	Course course;
	Instructor instructor;
	Time time;
	Room room;
	public Column(Course course, Instructor instructor, Room room, Time time) {
		super();//Eclipse generated 
		this.course = course;
		this.instructor = instructor;
		this.room = room;
		this.time = time;
	}
	@Override
	public String toString() { //Eclipse generated
		return "Column [course=" + course + ", instructor=" + instructor + ", time=" + time + ", room=" + room + "]";
	}
	
}