package edu.wit.cs;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Course {
	public static Map<String, Course> OFFERINGS = new TreeMap<>();
	
	public final String name;
	public final int credits;
	public final boolean[] semesters;
	public final String[] prereqs;
	
	private final String myString;
	
	private Course(String name, int credits, boolean[] semesters, String[] prereqs) {
		this.name = name;
		this.credits = credits;
		this.semesters = Arrays.copyOf(semesters, semesters.length);
		this.prereqs = Arrays.copyOf(prereqs, prereqs.length);
		
		final StringBuilder sb = new StringBuilder(String.format("%s (%d credits; ", name, credits));
		boolean firstSem = true;
		for (int i=0; i<3; i++) {
			if (semesters[i]) {
				sb.append(String.format("%s%s", firstSem?"":", ", semesterName(i)));
				firstSem = false;
			}
		}
		sb.append(String.format("; %s)", Arrays.toString(prereqs)));
		myString = sb.toString();
		
		OFFERINGS.put(name, this);
	}
	
	public static void offer(String name, int credits, boolean[] semesters) {
		new Course(name, credits, semesters, new String[] {});
	}
	
	public static void offer(String name, int credits, boolean[] semesters, String[] prereqs) {
		new Course(name, credits, semesters, prereqs);
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
	
	@Override
	public String toString() {
		return myString;
	}
}