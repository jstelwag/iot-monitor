package lighting;

import java.util.LinkedList;
import java.util.List;

public class Schedule {

    public enum Location {
        indoor,
        outdoor
    }

    public List<String> outdoorToMidnight = new LinkedList<>();
    public List<String> outdoorToDawn = new LinkedList<>();
    public List<String> indoorToMidnight = new LinkedList<>();
    public List<String> indoorToDawn = new LinkedList<>();
    public List<String> alwaysOn = new LinkedList<>();

    public Schedule() {
        // ================= Outdoor

        // 1/0/135	button	koetshuis	plein	Buitenlamp bij kelder
        outdoorToDawn.add("1/0/135");
        // 2/0/117	button	koetshuis	apartment_I	all	Terras buitenlamp
        outdoorToMidnight.add("2/0/117");
        // 2/0/118	button	koetshuis	plein	Buitenlamp voordeur rechts
        outdoorToDawn.add("2/0/118");
        // 3/0/105	button	koetshuis	plein	Buitenlamp wasruimte rechts
        outdoorToMidnight.add("3/0/105");
        // 3/0/104	button	koetshuis	plein	Buitenlamp plataan entree
        outdoorToMidnight.add("3/0/104");
        // 3/0/102	button	koetshuis	plein	Buitenlamp wasruimte links
        outdoorToMidnight.add("3/0/102");
        // 3/0/101	button	koetshuis	plein	Buitenlamp muur plein
        outdoorToDawn.add("3/0/101");
        // 3/0/106	button	garden	yet_unknown	kasteel, buitenlamp lindeboom
        outdoorToMidnight.add("3/0/106");
        // 4/0/107	button	kasteel	outside	toggle,all	Buitenlamp kelder
        outdoorToDawn.add("4/0/107");

        // ================= Indoor

        // 1/0/108	button	koetshuis	hall_coachhouse	Spots op verdieping
        alwaysOn.add("1/0/108");
        // 1/0/139	button	koetshuis	room_e	Hal spots plafond
        indoorToMidnight.add("1/0/139");
        // 1/0/144	button	koetshuis	hall_coachhouse	toggle,all	Spots onderaan trap centrale hal
        indoorToDawn.add("1/0/144");
        // 1/0/148	button	koetshuis	hall_coachhouse	Verdieping spot trap
        alwaysOn.add("1/0/148");
        // 2/0/109	button	koetshuis	hall_coachhouse	Spots plafond trap
        indoorToDawn.add("2/0/109");
        // 2/0/119	button	koetshuis	hall_coachhouse	Hal plafond
        indoorToDawn.add("2/0/119");
        // 5/0/101	button	kasteel	office	toggle,all	Staande lamp
        indoorToMidnight.add("5/0/101");
        // 5/0/103	button	kasteel	hall_toilet	Lamp WC
        indoorToMidnight.add("5/0/103");
    }
}

