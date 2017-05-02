package edu.wit.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Program {
	
	public static interface ProgramLoader extends Supplier<Iterable<String[]>> {
	}
	
	public static Iterable<String[]> BCOS() {
		Course.OFFERINGS.clear();
		
		Course.offer("COMP1000", 4, 0, new boolean[] {true, true, false});
		Course.offer("MATH2300", 4, 0, new boolean[] {true, true, true});
		Course.offer("MATH1750", 4, 0, new boolean[] {true, true, false});
		Course.offer("COMP1050", 4, 1, new boolean[] {true, true, false}, new String[] {"COMP1000"});
		Course.offer("MATH1850", 4, 1, new boolean[] {true, true, true}, new String[] {"MATH1750"});
		Course.offer("COMP1200", 4, 1, new boolean[] {true, true, false}, new String[] {"COMP1000", "MATH2300"});
		Course.offer("COMP2000", 4, 3, new boolean[] {true, true, false}, new String[] {"COMP1050", "MATH2300"});
		Course.offer("COMP2100", 4, 3, new boolean[] {true, true, false}, new String[] {"COMP1050"});
		Course.offer("MATH2860", 4, 3, new boolean[] {true, true, false}, new String[] {"MATH1850"});
		Course.offer("COMP2350", 4, 4, new boolean[] {true, true, false}, new String[] {"COMP1050", "MATH2300"});
		Course.offer("COMP2650", 4, 4, new boolean[] {true, true, false}, new String[] {"COMP1050", "MATH2300"});
		Course.offer("MATH2100", 4, 4, new boolean[] {false, true, true}, new String[] {"MATH1850"});
		Course.offer("COMP3400", 4, 6, new boolean[] {true, false, false}, new String[] {"COMP2000", "COMP2350"});
		Course.offer("COMP3350", 4, 8, new boolean[] {false, false, true}, new String[] {"COMP2000", "COMP2350"});
		Course.offer("COMP3450", 4, 8, new boolean[] {false, false, true}, new String[] {"COMP2000", "COMP2350"});
		Course.offer("COMP4960", 4, 10, new boolean[] {false, true, false}, new String[] {"COMP2650", "COMP2000", "COMP2350"});
		Course.offer("COMP5500", 4, 11, new boolean[] {false, false, true}, new String[] {"COMP4960"});
		
		Course.offer("ENGLISH1", 4, 0, new boolean[] {true, true, true});
		Course.offer("ENGLISH2", 4, 1, new boolean[] {true, true, true}, new String[] {"ENGLISH1"});
		Course.offer("HUSS-E01", 4, 3, new boolean[] {true, true, true}, new String[] {"ENGLISH2"});
		Course.offer("HUSS-E02", 4, 4, new boolean[] {true, true, true}, new String[] {"ENGLISH2"});
		Course.offer("HUSS-E03", 4, 6, new boolean[] {true, true, true}, new String[] {"ENGLISH2"});
		Course.offer("HUSS-E04", 4, 10, new boolean[] {true, true, true}, new String[] {"ENGLISH2"});
		Course.offer("HUSS-E05", 4, 11, new boolean[] {true, true, true}, new String[] {"ENGLISH2"});
		
		Course.offer("MSCI-E01", 4, 6, new boolean[] {true, true, true}, new String[] {"MATH1850"});
		Course.offer("MSCI-E02", 4, 8, new boolean[] {true, true, true}, new String[] {"MATH1850"});
		Course.offer("MSCI-E03", 4, 11, new boolean[] {true, true, true}, new String[] {"MATH1850"});
		
		Course.offer("COMP-E01", 4, 6, new boolean[] {true, true, true}, new String[] {"COMP1050"});
		Course.offer("COMP-E02", 4, 8, new boolean[] {true, true, true}, new String[] {"COMP1050"});
		Course.offer("COMP-E03", 4, 10, new boolean[] {true, true, true}, new String[] {"COMP1050"});
		
		Course.offer("ADCS-E01", 4, 10, new boolean[] {true, true, true}, new String[] {"COMP2000", "COMP2350"});
		Course.offer("ADCS-E02", 4, 11, new boolean[] {true, true, true}, new String[] {"COMP2000", "COMP2350"});
		
		Course.offerCoop(7, 9);
		
		//
		
		final List<String[]> equiv = new ArrayList<>();
		equiv.add(new String[] {"HUSS-E01", "HUSS-E02", "HUSS-E03", "HUSS-E04", "HUSS-E05"});
		equiv.add(new String[] {"MSCI-E01", "MSCI-E02", "MSCI-E03"});
		equiv.add(new String[] {"COMP-E01", "COMP-E02", "COMP-E03"});
		equiv.add(new String[] {"ADCS-E01", "ADCS-E02"});
		
		return equiv;
	}

}
