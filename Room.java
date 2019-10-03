
public class Room {
	String name;
	int capacity;
	String building;
	public Room(String name, int capacity, String building) {
		super(); //Eclipse generated
		this.name = name;
		this.capacity = capacity;
		this.building = building;
	}
	@Override //Eclipse generated
	public String toString() {
		return name + "[" + capacity + "]";
	}
	
}