package edu.wit.cs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jacop.constraints.And;
import org.jacop.constraints.Distance;
import org.jacop.constraints.LinearInt;
import org.jacop.constraints.Not;
import org.jacop.constraints.Or;
import org.jacop.constraints.OrBool;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.Reified;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XltY;
import org.jacop.constraints.XlteqC;
import org.jacop.constraints.XlteqY;
import org.jacop.constraints.XneqY;
import org.jacop.core.BooleanVar;
import org.jacop.core.BoundDomain;
import org.jacop.core.Domain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;

public class PreReq {
	
	private static void printSemester(String header, List<String> courses) {
		System.out.printf(" %s%n", header);
		if (courses.isEmpty()) {
			System.out.printf(" ");
			for (int i=0; i<header.length(); i++) {
				System.out.printf("-");
			}
			System.out.printf("%n");
		} else {
			for (String c : courses) {
				System.out.printf("   * %s%n", Course.OFFERINGS.get(c));
			}
		}
	}
	
	private static void printSchedule(Search<IntVar> search, int solNum, int startSemester, int startYear, int numSemesters) {
		final IntVar[] vars = search.getVariables();
		final Domain[] vals = search.getSolution(solNum);
		
		final Map<Integer, List<String>> schedule = new LinkedHashMap<>();
		for (int i=-1; i<numSemesters; i++) {
			schedule.put(i, new ArrayList<String>());
		}
		
		for (int i=0; i<vars.length; i++) {
			final IntVar v = vars[i];
			final int t = Integer.valueOf(vals[i].toString());
			
			schedule.get(t).add(v.id);
		}
		
		if (!schedule.get(-1).isEmpty()) {
			printSemester("Taken", schedule.get(-1));
		}
		
		int year = startYear;
		for (int t=0; t<numSemesters; t++) {
			final int semester = (startSemester + t) % 3;
			if (semester == 1) {
				year++;
			} else if (semester==0 && t!=0) {
				System.out.printf("%n");
			}
			
			printSemester(String.format("%s %d", Course.semesterName(semester), year), schedule.get(t));
		}
	}
	
	private static void loadBCOS(String coop1, String coop2, int coopCredits, List<String[]> equiv) {
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
		
		Course.offer(coop1, coopCredits, 7, new boolean[] {true, true, true});
		Course.offer(coop2, coopCredits, 9, new boolean[] {true, true, true}, new String[] {coop1});
		
		//
		
		equiv.add(new String[] {"HUSS-E01", "HUSS-E02", "HUSS-E03", "HUSS-E04", "HUSS-E05"});
		equiv.add(new String[] {"MSCI-E01", "MSCI-E02", "MSCI-E03"});
		equiv.add(new String[] {"COMP-E01", "COMP-E02", "COMP-E03"});
		equiv.add(new String[] {"ADCS-E01", "ADCS-E02"});
	}
	
	public static void main(String[] args) {
		
		final Integer MAX_TIME = 1;
//		final Integer MAX_TIME = null;
		
		//
		
		final int MAX_SEMESTERS = 12;
		final int MIN_CREDITS = 12;
		final int MAX_CREDITS = 16;
		
		final int START_SEMESTER = 0;
		final int START_YEAR = 2017;
		
		final Integer DEVIATION_FROM_TRACKING = 0;
		
		final boolean NO_ALL_CLASS_YEAR = true;
		
		final boolean NO_CLASS_WITH_COOP = true;
		final String COOP_1 = "COOP-3500";
		final String COOP_2 = "COOP-4500";
		final int COOP_CREDITS = 12;
		
		final Set<String> taken = new HashSet<>();
//		taken.add("COMP1000");
//		taken.add("COMP1050");
//		taken.add("MATH2300");
		
		final Set<Integer> avoid = new HashSet<>();
//		avoid.add(2);
//		avoid.add(5);
		
		final List<String[]> equiv = new ArrayList<>();
		
		final Map<Integer, List<String>> peg = new LinkedHashMap<>();
//		peg.put(2, Arrays.asList("MATH1850"));
		
		//
		
		loadBCOS(COOP_1, COOP_2, COOP_CREDITS, equiv);

		//
		
		final Store store = new Store();
		
		//
		
		final IntVar[] vars = new IntVar[Course.OFFERINGS.size()];
		{
			int i = 0;
			for (Course c : Course.OFFERINGS.values()) {
				final IntVar v = new IntVar(store, c.name);
				
				if (taken.contains(c.name)) {
					v.addDom(-1, -1);
				} else {
					boolean pegged = false;
					for (int t=0; t<MAX_SEMESTERS; t++) {
						final int semester = (START_SEMESTER + t) % 3;
						if (peg.containsKey(t) && peg.get(t).contains(c.name) && c.semesters[semester]) {
							pegged = true;
							v.addDom(t, t);
							break;
						}
					}
					
					if (!pegged) {
						for (int t=0; t<MAX_SEMESTERS; t++) {
							final int semester = (START_SEMESTER + t) % 3;
							if (c.semesters[semester]) {
								v.addDom(t, t);
							}
						}
					}
				}
				
				vars[i++] = v;
			}
		}
			
		final Map<String, IntVar> varMap = new TreeMap<>();
		for (IntVar v : vars) {
			varMap.put(v.id(), v);
		}
		
		// prereq
		for (Course c : Course.OFFERINGS.values()) {
			for (String pre : c.prereqs) {
				if (!taken.contains(pre)) {
					store.impose(new XltY(varMap.get(pre), varMap.get(c.name)));
				}
			}
		}
		
		// hours
		{
			final int[] credits = new int[Course.OFFERINGS.size()];
			int i = 0;
			for (Course c : Course.OFFERINGS.values()) {
				credits[i++] = c.credits;
			}
			
			for (int t=0; t<MAX_SEMESTERS; t++) {
				final BooleanVar[] bvars = new BooleanVar[Course.OFFERINGS.size()];
				i = 0;
				for (Course c : Course.OFFERINGS.values()) {
					final PrimitiveConstraint pc = new XeqC(varMap.get(c.name), t);
					final BooleanVar b = new BooleanVar(store, String.format("%s=%d", c.name, t));
					final Reified r = new Reified(pc, b);
					store.impose(r);
					bvars[i++] = b;
				}
				
				if (avoid.contains(t)) {
					store.impose(new LinearInt(store, bvars, credits, "==", peg.containsKey(t)?peg.get(t).stream().mapToInt(c->Course.OFFERINGS.get(c).credits).sum():0));
				} else {
					store.impose(
						new Or(
							new LinearInt(store, bvars, credits, "==", 0), 
							new And(new LinearInt(store, bvars, credits, ">=", MIN_CREDITS), new LinearInt(store, bvars, credits, "<=", MAX_CREDITS))
						)
					);
				}
			}
		}
		
		// equivalents
		for (String[] e : equiv) {
			for (int i=0; i<e.length-1; i++) {
				store.impose(new XlteqY(varMap.get(e[i]), varMap.get(e[i+1])));
			}
		}
		
		// no class with coop
		if (NO_CLASS_WITH_COOP) {
			final IntVar v1 = varMap.get(COOP_1);
			final IntVar v2 = varMap.get(COOP_2);
			
			for (IntVar v : vars) {
				if (v != v1 && v != v2) {
					store.impose(new XneqY(v, v1));
					store.impose(new XneqY(v, v2));
				}
			}
		}
		
		// no class all year
		if (NO_ALL_CLASS_YEAR) {
			final BooleanVar bT = new BooleanVar(store, new BoundDomain(1, 1));
			
			for (int t=0; t<MAX_SEMESTERS; t++) {
				final int semester = (START_SEMESTER + t) % 3;
				
				if (semester==0 && (t+2)<MAX_SEMESTERS) {
					final BooleanVar[] legalVar = new BooleanVar[3];
					
					for (int i=0; i<3; i++) {
						final int s = t + i;
						
						//
						
						final List<String> pegs = peg.containsKey(s)?peg.get(s):null;
						
						final int numNonPegged = Course.OFFERINGS.size() - (pegs==null?0:pegs.size());
						final PrimitiveConstraint[] cSem = new PrimitiveConstraint[numNonPegged];
						int j = 0;
						for (Course c : Course.OFFERINGS.values()) {
							if (pegs==null || !pegs.contains(c.name)) {
								cSem[j++] = new XeqC(varMap.get(c.name), s);
							}
						}
						
						//
						
						final BooleanVar b = new BooleanVar(store, String.format("ACY_%d_legal", s));
						store.impose(new Reified(
							new Or( // either...
								new Or(new XeqC(varMap.get(COOP_1), s), new XeqC(varMap.get(COOP_2), s)), // co-op 
								new Not(new Or(cSem))),  // or no courses
						b));
						
						legalVar[i] = b;
					}
					
					store.impose(new OrBool(legalVar, bT));
				}
			}
		}
		
		// deviation from tracking sheet
		final IntVar cost = new IntVar(store, "cost", 0, MAX_SEMESTERS * Course.OFFERINGS.size());
		{
			final int offsetEstimate = (int) Math.round(taken.stream().mapToInt(c -> Course.OFFERINGS.get(c).credits).sum() / 12.0);
			final IntVar[] diffVars = new IntVar[Course.OFFERINGS.size()];
			int i=0;
			for (Course c : Course.OFFERINGS.values()) {
				final int courseEstimate = c.slot - offsetEstimate;
				final IntVar exp = new IntVar(store, String.format("%s-expected", c.name), courseEstimate, courseEstimate);
				final IntVar diff = new IntVar(store, String.format("%s-diff", c.name), 0, MAX_SEMESTERS);
				store.impose(new Distance(exp, varMap.get(c.name), diff));
				
				diffVars[i++] = diff;
			}
			
			store.impose(new SumInt(store, diffVars, "==", cost));
			if (DEVIATION_FROM_TRACKING != null) {
				store.impose(new XlteqC(cost, DEVIATION_FROM_TRACKING));
			}
		}
		
		final Search<IntVar> search = new DepthFirstSearch<>();
		final SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store, vars, new IndomainMin<IntVar>());
		
		search.setPrintInfo(false);
		search.getSolutionListener().searchAll(true);
		search.getSolutionListener().recordSolutions(true);
		
		search.setStore(store);
		search.setSelectChoicePoint(select);
		
		if (MAX_TIME != null) {
			search.setTimeOut(MAX_TIME);
		}
		
		final boolean found = search.labeling();
		
		if (found) {
			for (int i=1; i<=search.getSolutionListener().solutionsNo(); i++) {
				System.out.printf("== Schedule %d ==%n", i);
				printSchedule(search, i, START_SEMESTER, START_YEAR, MAX_SEMESTERS);
				System.out.printf("%n");
				
				if (i>=10) {
					System.out.printf("Stopped after %,d/%,d", i, search.getSolutionListener().solutionsNo());
					if (MAX_TIME != null) {
						System.out.printf(" (searched for %ds)", MAX_TIME);
					}
					System.out.printf("%n");
					break;
				}
			}
		} else {
			System.out.println("No satisfying schedules :(");
		}
	}

}
