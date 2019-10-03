import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
	
public class Solver {
	
		Map<String, Building>buildings = new HashMap<String, Building>();
		Map<String, Course>courses = new HashMap<String, Course>();
		Map<String, Instructor>instructors= new HashMap<String, Instructor>();
		Map<String, Room>rooms = new HashMap<String, Room>();
		Set<Time>times = new HashSet<Time>();
		
		public Solver() {
			super();
			this.initialize();
		}
		
		public Schedule Solve(int initialSchedules, int generations) { //output the schedule
			List<Schedule>schedules = new ArrayList<Schedule>(initialSchedules);
			for (int i=0; i<initialSchedules;i++) {
				schedules.add(this.randomSchedule());
				
			}
			
			for(int i =0;i<generations;i++) {
				this.printStats(schedules);;
				schedules=processGeneration(schedules);
			}
			
			this.printStats(schedules);
			return randomSchedule();
		}
		
		private void printStats(List<Schedule>schedules) //print the fitness information
		{
			int sum=0;
			
				for(Schedule sched:schedules) {
					sum+=sched.getScore();
				}
				System.out.println("Generation Average fitness= "+(double)sum/schedules.size());
				schedules.sort((s1,s2)->{
					return Double.compare(s1.getScore(),s2.getScore()); //sorts scores in the list
				});
				System.out.println("Generation max fitness= "+schedules.get(schedules.size()-1).getScore());
		}
		
		private List<Schedule>processGeneration(List<Schedule>schedules){ //calculate the fitness function
			schedules.sort((s1,s2)->{
				return Double.compare(s1.getScore(),s2.getScore()); //sorts scores in the list
			});
			
			List<Double>squares=new ArrayList<Double>();
			List<Double>cumulativeDistros=new ArrayList<Double>();
			int squareSum=0;
			//calculate squares
			for (int i = 0; i <schedules.size();i++) {
				double square=schedules.get(i).getScore()*schedules.get(i).getScore();
				squares.add(square); //square the totals together
				squareSum+=square; //sum the squares together
			}
			
			//Calculated cumulativeDistro
			double cumulativeDistro=0;
			
			for(int i =0; i<squares.size();i++) {
				double distro=(double)squares.get(i)/squareSum; //make it to double division instead of int division
				cumulativeDistro+=distro;
				cumulativeDistros.add(i,cumulativeDistro); //the cumulative distribution is here
			}
			
			List<Schedule>newSchedules=new ArrayList<Schedule>();
			//remove items that don't meet criteria
			while(schedules.size()>0) { //while there are things in schedule
				Schedule sched=schedules.get(0);
				double distro=cumulativeDistros.get(0);
				if(distro<=0) {
					schedules.remove(0); //remove things below the threshold to remove the elements that don't fit the criteria
					cumulativeDistros.remove(0);
				}
				
				else
				{
					break;
				}
			}
			
			//randomly select next generation based on cumulative distributions
			for (int i=0;i<schedules.size();i++) {
				double rand = Math.random();
				int index=0;
				for (int j = 0; j<squares.size();j++) {
					if (rand<cumulativeDistros.get(j)) {
						index = j;
						break;
					}
				}
				
				Schedule newSchedule =this.mutate(schedules.get(index)); //includes the mutation
				newSchedules.add(newSchedule);
			}			
			
			return newSchedules;
			
		}
		
		public Schedule mutate(Schedule s) { //the schedule mutation like in genetic crossovers
			Schedule newSchedule=new Schedule(this);
			Random rand=new Random();
			double min = 1.00;
			double max = 100.00;
			double mutate = min + (max - min) * rand.nextDouble();
			//int mutate=rand.nextInt(s.assignments.size()-1);
			//double mutate=rand.nextDouble()+.01;

			for(Column col:s.assignments) {
				
				if(mutate<0) {
					
					newSchedule.assignments.add(col);
				}
				
				Instructor instructor=col.instructor;
				Room room=col.room;
				Time times=col.time;
				
//				if (Math.random()<.05) {
//					instructor=this.randomSetItem(new HashSet<Instructor>(this.instructors.values()));
//				}
//				
//				if(Math.random()<.05)
//				{
//					room=this.randomSetItem(new HashSet<Room>(this.rooms.values()));
//				}
//				
//				if (Math.random()>.05)
//				{
//					times=this.randomSetItem(this.times);
//				}
				
				if (Math.random() >.05) {
						Column column=new Column(col.course,instructor,room,times);
						newSchedule.assignments.add(column);
				}
			
		}
			return newSchedule;
		}	
		
		
		public double scoreSchedule(Schedule schedule) { //giving the schedule a fitness score
			double score=0;
			double percentageModifier=1.00;
			for(Column column:schedule.assignments) {
				score+=qualifiedInstructor(column)?3:0; //if true, add 3. if not, add none
				score+=onlyCourseByRoomAndTime(column, schedule)?5:0;	
				score+=checkRoomSize(column)?5:0;
				score+=checkRoomSizeUnderCapacity(column)?2:0;
				score+=checkInstuctorDoubleBooking(column,schedule)?5:0;
//				System.out.println(score);
				percentageModifier+=check101And191DoubleBooking(schedule,column)?-.10:0;
				percentageModifier+=check201And291DoubleBooking(schedule,column)?-.10:0;
				percentageModifier+=check101And191AdjacentBooking(schedule,column)?.5:0;
				percentageModifier+=check201And291AdjacentBooking(schedule,column)?.5:0;
				score+=check201And291SameBuildingAndRoom(schedule,column)?5:0;
				score+=check101And191SameBuildingAndRoom(schedule,column)?5:0;
				score+=check201And291OnQuad(schedule,column)?0:0;
				score+=check101And191OnQuad(schedule,column)?0:0;
				percentageModifier+=checkInKatz101And191(schedule,column)?.3:0;
				percentageModifier+=checkInKatz201And291(schedule,column)?.3:0;
				percentageModifier+=checkInBloch101And191(schedule,column)?.3:0;
				percentageModifier+=checkInBloch201And291(schedule,column)?.3:0;
				score+=additionalFeature1KuhailBuildingPreference(schedule,column)?5:0;
				score+=additionalFeature2HareMorningTimePreference(schedule, column)?5:0;
			}
			
			score+=checkInstructorCourseLimit(schedule); //outside the loop because it's per instructor

			
			score = score*percentageModifier;
			return score;
		}
		
		public boolean additionalFeature2HareMorningTimePreference(Schedule schedule, Column col) { //extra credit -- gives Hare a preference of morning-only slots
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				if (currentCol.time.equals("1") || (currentCol.time.equals("2") || (currentCol.time.equals("3")))){
					return false;
				}			
			}
			return true;
		}
		
		public boolean additionalFeature1KuhailBuildingPreference(Schedule schedule, Column col) { //extra credit-- gives Kuhail a Haag building preference
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
			if (currentCol.instructor.equals("KUHAIL") && currentCol.room.equals("HAAG")) {
				return false;
			}
				
			}
			return true;
		}
		
		
		public boolean checkInBloch201And291(Schedule schedule, Column col) { //make sure corequisite courses are both in bloch
			if (check101And191AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
			if (currentCol.course.equals("CS201A") || (currentCol.course.equals("CS201B")) && col.course.equals("CS291A") || col.course.equals("CS291B") && currentCol.room.equals("BLOCH") && col.room.equals("HAAG") || col.room.equals("ROYALL") || col.room.equals("FLARSHEIM") || col.room.equals("KATZ"))
			{
				return false;
			}
			}
				}
				return true;
			
			}
		
		
		public boolean checkInBloch101And191(Schedule schedule, Column col) { //make sure corequisite courses are both in bloch
			if (check101And191AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
			if (currentCol.course.equals("CS101A") || (currentCol.course.equals("CS101B")) && col.course.equals("CS191A") || col.course.equals("CS191B") && currentCol.room.equals("BLOCH") && col.room.equals("HAAG") || col.room.equals("ROYALL") || col.room.equals("FLARSHEIM") || col.room.equals("KATZ"))
			{
					return false;
				}
				
				
				}
				
			}
			return true;
			}

		
		public boolean checkInKatz201And291(Schedule schedule, Column col) { //make sure corequisite courses are both in katz
			if (check101And191AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
			if (currentCol.course.equals("CS201A") || (currentCol.course.equals("CS201B")) && col.course.equals("CS291A") || col.course.equals("CS291B") && currentCol.room.equals("KATZ") && col.room.equals("HAAG") || col.room.equals("ROYALL") || col.room.equals("FLARSHEIM") || col.room.equals("BLOCH"))
			{
				return false;
			}
			}
				}
				return true;
			
			}
		
		public boolean checkInKatz101And191(Schedule schedule, Column col) { //make sure corequisite courses are both in katz
			if (check101And191AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
			if (currentCol.course.equals("CS101A") || (currentCol.course.equals("CS101B")) && col.course.equals("CS191A") || col.course.equals("CS191B") && currentCol.room.equals("KATZ") && col.room.equals("HAAG") || col.room.equals("ROYALL") || col.room.equals("FLARSHEIM") || col.room.equals("BLOCH"))
			{
				return false;
			}
			}
				}
				return true;
			
			}
		
		
		public boolean check101And191OnQuad(Schedule schedule, Column col) { //make sure corequisite courses are on the quad
			if (check101And191AdjacentBooking(schedule, col) == true) { 
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS101A") || (currentCol.course.equals("CS101B")) && col.course.equals("CS191A") || col.course.equals("CS191B") && col.course.name.equals("HAAG") || col.course.name.equals("ROYALL") || col.course.name.equals("FLARSHEIM"))
				{
					return false;
				}				
			}
			}
			return true;
			
		}
		
		
		public boolean check201And291OnQuad(Schedule schedule, Column col) { //make sure corequisite courses are on the quad
			if (check101And191AdjacentBooking(schedule, col) == true) { 
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS201A") || (currentCol.course.equals("CS201B")) && col.course.equals("CS291A") || col.course.equals("CS291B") && col.course.name.equals("HAAG") || col.course.name.equals("ROYALL") || col.course.name.equals("FLARSHEIM"))
				{
					return false;
				}				
				
			}
			}
			return true;
			
		}
		
		public boolean check201And291AdjacentBooking(Schedule schedule, Column col) { //make sure corequisite courses are at adjacent times
			if (check101And191AdjacentBooking(schedule, col) == true) { 
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS201A") || (currentCol.course.equals("CS201B")) && col.course.equals("CS291A") || col.course.equals("CS291B") && col.course.name.equals("HAAG"))
				{
					return false;
				}
				{
					return false;
				}
			}
			}
			return true;
			
		}
		
		public boolean check101And191SameBuildingAndRoom(Schedule schedule, Column col) { //make sure corequisite courses are in the same building/room
			if (check101And191AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
					
					if (currentCol.room==col.room)
					{
						return false;
					}
				}
				
			}
			return true;
		}
		
		public boolean check201And291SameBuildingAndRoom(Schedule schedule, Column col) { //make sure corequisite courses are in the same building and room
			if (check201And291AdjacentBooking(schedule, col) == true) { 
				for (Column currentCol:schedule.assignments) {
					if (currentCol == col) { //if it is iterating over itself, skip
						continue;
					}
					
					if (currentCol.room==col.room)
					{
						return false;
					}
				}
				
			}
			return true;
		}
		
		public boolean check101And191AdjacentBooking(Schedule schedule, Column col) { //make sure corequisite courses are at adjacent times
			Course course=col.course;
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS101A") || (currentCol.course.equals("CS101B")) && col.course.equals("CS191A") || col.course.equals("CS191B") && col.course.name.equals("HAAG"))
				{
					return false;
				}
			}
			return true;
		}
		
		public boolean check101And191DoubleBooking(Schedule schedule, Column col) { //make  sure corequisite courses are not at the same time
			Course course=col.course;
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS101A") || currentCol.course.equals("CS101B")==col.course.equals("CS191A") || col.course.equals("CS191B") && currentCol.time==col.time)
				{
					return false;
				}
			}
			return true;
		}
		
		public boolean check201And291DoubleBooking(Schedule schedule, Column col) { //make  sure corequisite courses are not at the same time
			Course course=col.course;
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.course.equals("CS201A") || (currentCol.course.equals("CS201B")==col.course.equals("CS291A") || col.course.equals("CS291B") && currentCol.time==col.time))
				{
					return false;
				}
			}
			return true;
		}
		
		public boolean checkGradFacultyOverbooking(Schedule sched) { //make sure grad faculty are not overloaded with undergrad classes
			int rao=0;
			int mitchell=0;
			int hare = 0;
			int bingham = 0;
			
			for(Column col:sched.assignments) {
				if("RAO".equals(col.instructor.name)) {
					rao++;
				}
				
				else if ("MITCHELL".equals(col.instructor.name)) {
					mitchell++;
				}
				
				else if("HARE".equals(col.instructor.name)) {
					hare++;
				}
				
				else if("BINGHAM".equals(col.instructor.name)) {
					bingham++;
				}
			}

			int raoMitchellMax=Math.max(rao,mitchell);
			int hareBinghamMax=Math.max(hare, bingham);
			return raoMitchellMax>hareBinghamMax;
		}
		
		public int checkInstructorCourseLimit( Schedule schedule) { //make sure profs are not teaching more than four courses
			int overedge=0;
			for(Instructor instructor:this.instructors.values()) {
				int courses=0;
			for (Column currentCol:schedule.assignments) {
				if (currentCol.instructor==instructor) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.instructor==instructor)
				{
					courses++;
				}
				if (courses<4) {
					overedge+=courses-4;
				}
			}
			
			}
			return overedge;
		}
		
		
		public boolean checkRoomSize(Column col) { //makes sure room can accomodate the amount of students
			Room room = this.rooms.get(col.room.name);
			Course course = this.courses.get(col.course.name);
					return room.capacity>=course.expectedEnrollment;
		}
		
		public boolean checkRoomSizeUnderCapacity(Column col) { //makes sure room capacity is no more than twice the amount allowed to enroll
			Room room = this.rooms.get(col.room.name);
			Course course = this.courses.get(col.course.name);
					return room.capacity<=(course.expectedEnrollment*2);
		}
		
		public boolean checkInstuctorDoubleBooking(Column col, Schedule schedule) { //make sure no instructors are booked twice at the same time
			Instructor instructor=col.instructor;
			for (Column currentCol:schedule.assignments) {
				if (currentCol == col) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.instructor==col.instructor && currentCol.time==col.time)
				{
					return false;
				}
			}
			return true;
			
		}
		
		
		public boolean onlyCourseByRoomAndTime(Column column, Schedule schedule) { //make sure the classrooms are not double booked
			for (Column currentCol:schedule.assignments) {
				if (currentCol == column) { //if it is iterating over itself, skip
					continue;
				}
				
				if (currentCol.room==column.room && currentCol.time==column.time)
				{
					return false;
				}
			}
			return true;
		}

		public boolean qualifiedInstructor(Column column) { //makes sure the instructor is qualified to instruct the course
			String courseName=column.course.name;
			String instructorName=column.instructor.name;
			Set<String>courses=this.instructors.get(instructorName).courses;
			return courses.contains(courseName);
		}

		public Schedule randomSchedule() { //generates the random schedules
			Schedule schedule=new Schedule(this);
			for (Course course:courses.values()) { //go through each iteration and assign a course-- a for each loop
				Column column=new Column(course,
						this.randomSetItem(new HashSet<Instructor>(this.instructors.values())),
						this.randomSetItem(new HashSet<Room>(this.rooms.values())),
						this.randomSetItem(this.times));
				schedule.assignments.add(column);
			}
			
			return schedule;
		}
		
		public static <T> T randomSetItem(Set<T>set) { //this is a generic method-- take in a set of whatever the object is, and this method returns that same object.
			int size = set.size();
			int item = new Random().nextInt(size);
			int i = 0;
			for(T obj : set) {
				if (i==item)
					return obj;
					i++;
			}
				return null;
		}

		public void initialize() {
			this.InitializeBuildings();
			this.InitializeCourses();
			this.InitializeInstructors();
			this.InitializeRooms();
			this.InitializeTimes();
			
		}
		
		public void InitializeBuildings() { //all the buildings
			buildings.put("HAAG", new Building("HAAG",new HashSet<String>(Arrays.asList(new String[] {"HAAG301", "HAAG206"}))));
			buildings.put("ROYALL", new Building("ROYALL",new HashSet<String>(Arrays.asList(new String[] {"ROYALL204"}))));
			buildings.put("KATZ", new Building("KATZ",new HashSet<String>(Arrays.asList(new String[] {"KATZ209"}))));
			buildings.put("FLARSHEIM", new Building("FLARSHEIM",new HashSet<String>(Arrays.asList(new String[] {"FLARSHEIM310", "FLARSHEIM260"}))));
			buildings.put("BLOCH", new Building("BLOCH",new HashSet<String>(Arrays.asList(new String[] {"BLOCH0009"}))));
			
		}
		
		public void InitializeRooms() { //all the rooms
			rooms.put("HAAG301", new Room("HAAG301",70,"HAAG"));
			rooms.put("HAAG206", new Room("HAAG206",30,"HAAG"));
			rooms.put("ROYALL204", new Room("ROYALL204",70,"ROYALL"));
			rooms.put("KATZ209", new Room("KATZ209",50,"KATZ"));
			rooms.put("FLARSHEIM310", new Room("FLARSHEIM310",80,"FLARSHEIM"));
			rooms.put("FLARSHEIM260", new Room("FLARSHEIM260",25,"FLARSHEIM"));
			rooms.put("BLOCH0009", new Room("BLOCH0009",30,"BLOCH"));


		}
		
		public void InitializeCourses() { //all the courses and max enrollments
			courses.put("CS101A",new Course("CS101A",40));
			courses.put("CS101B", new Course("CS101B",25));
			courses.put("CS201A", new Course("CS201A",30));
			courses.put("CS201B", new Course("CS201B",30));
			courses.put("CS191A", new Course("CS191A",60));
			courses.put("CS191B", new Course ("CS191B",20));
			courses.put("CS291A", new Course ("CS291A",20));
			courses.put("CS291B", new Course ("CS291B",40));
			courses.put("CS303", new Course ("CS303",50));
			courses.put("CS341", new Course ("CS341",40));
			courses.put("CS449", new Course ("CS449",55));
			courses.put("CS461", new Course ("CS461",40));
			
		}
		
		
		public void InitializeInstructors() { //all the instructors and what they're qualified to teach
			instructors.put("HARE",new Instructor("HARE", new HashSet<String>(Arrays.asList(new String[] {"CS101A", "CS101B","CS201A", "CS201B","CS291A", "CS291B","CS303","CS449","CS461"})), false));
			instructors.put("BINGHAM", new Instructor("BINGHAM", new HashSet<String>(Arrays.asList(new String[] {"CS101A", "CS101B","CS201A", "CS201B","CS191A", "CS191B", "CS291A", "CS291B","CS449"})), false));
			instructors.put("KUHAIL",new Instructor("KUHAIL", new HashSet<String>(Arrays.asList(new String[] {"CS303","CS341"})), false));
			instructors.put("MITCHELL",new Instructor("MITCHELL", new HashSet<String>(Arrays.asList(new String[] {"CS191A", "CS191B","CS291A", "CS291B","CS303","CS341"})), true));
			instructors.put("RAO",new Instructor("RAO", new HashSet<String>(Arrays.asList(new String[] {"CS291A", "CS291B","CS303","CS341","CS461"})), true));

		}


		public void InitializeTimes() { //the time slots (NOT by actual time-- by time slot (for example, 10A would be slot 1)
			for (int i = 1; i < 8; i++)
			{
				Time newTime=new Time(i);
				this.times.add(newTime); //for one through 8 we'll go ahead and generate a new time for a slot
			}
		
	}

	}

