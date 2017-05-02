package edu.wit.cs;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Course {
	
	public static final String[] COOP_NAME = {"COOP-3500", "COOP-4500"};
	public static final int[] COOP_PREREQ = {64, 96};
	
	//
	
	public static Map<String, Course> OFFERINGS = new TreeMap<>();
	
	public final String name;
	public final int credits;
	public final int slot;
	public final boolean[] semesters;
	public final String[] prereqs;
	public final String[] coreqs;
	
	private final String myString;
	
	private Course(String name, int credits, int slot, boolean[] semesters, String[] prereqs, String[] coreqs) {
		this.name = name;
		this.credits = credits;
		this.slot = slot;
		this.semesters = Arrays.copyOf(semesters, semesters.length);
		this.prereqs = Arrays.copyOf(prereqs, prereqs.length);
		this.coreqs = Arrays.copyOf(coreqs, coreqs.length);
		
		final StringBuilder sb = new StringBuilder(String.format("%s (%d credits; slot #%d; ", name, credits, slot));
		boolean firstSem = true;
		for (int i=0; i<3; i++) {
			if (semesters[i]) {
				sb.append(String.format("%s%s", firstSem?"":", ", semesterName(i)));
				firstSem = false;
			}
		}
		sb.append(String.format("; pre=%s, co=%s)", Arrays.toString(prereqs), Arrays.toString(coreqs)));
		myString = sb.toString();
		
		OFFERINGS.put(name, this);
	}
	
	public static void offer(String name, int credits, int slot, boolean[] semesters, String[] prereqs, String[] coreqs) {
		new Course(name, credits, slot, semesters, prereqs, coreqs);
	}
	
	public static void offer(String name, int credits, int slot, boolean[] semesters, String[] prereqs) {
		offer(name, credits, slot, semesters, prereqs, new String[] {});
	}
	
	public static void offer(String name, int credits, int slot, boolean[] semesters) {
		offer(name, credits, slot, semesters, new String[] {}, new String[] {});
	}
	
	public static void offerCoop(int slot1, int slot2) {
		offer(COOP_NAME[0], 0, slot1, new boolean[] {true, true, true});
		offer(COOP_NAME[1], 0, slot2, new boolean[] {true, true, true}, new String[] {COOP_NAME[0]});
	}
	
	public static String semesterName(int s) {
		if (s == 0) {
			return "Fall";
		} else if (s == 1) {
			return "Spring";
		} else if (s == 2) {
			return "Summer";
		} else {
			return "";
		}
	}
	
	public static int semester(int start, int offset) {
		return (start + offset) % 3;
	}
	
	@Override
	public String toString() {
		return myString;
	}
}
