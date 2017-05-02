package edu.wit.cs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jacop.constraints.And;
import org.jacop.constraints.Distance;
import org.jacop.constraints.IfThen;
import org.jacop.constraints.LinearInt;
import org.jacop.constraints.Or;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.Reified;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.XltC;
import org.jacop.constraints.XltY;
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

import edu.wit.cs.Program.ProgramLoader;

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
	
	private static void printSchedule(Map<String, Integer> solution, int startSemester, int startYear, int numSemesters) {
		final Map<Integer, List<String>> schedule = new LinkedHashMap<>();
		for (int i=-1; i<numSemesters; i++) {
			schedule.put(i, new ArrayList<String>());
		}
		
		for (Entry<String, Integer> e : solution.entrySet()) {
			schedule.get(e.getValue()).add(e.getKey());
		}
		
		if (!schedule.get(-1).isEmpty()) {
			printSemester("Taken", schedule.get(-1));
		}
		
		int year = startYear;
		for (int t=0; t<numSemesters; t++) {
			final int semester = Course.semester(startSemester, t);
			if (semester == 1) {
				year++;
			} else if (semester==0 && t!=0) {
				System.out.printf("%n");
			}
			
			printSemester(String.format("%s %d", Course.semesterName(semester), year), schedule.get(t));
		}
	}
	
	private static List<Map<String, Integer>> buildSchedules(ProgramLoader pl, int startSemester, int startYear, int maxSemesters,
																int minCredits, int maxCredits, int maxCreditsWithCoOp,
																Integer deviationFromTracking, boolean noAllClassYear, 
																Set<String> classesTaken, int creditsTakenOther, Set<Integer> avoidSemesters,
																Map<Integer, List<String>> classReservations,
																Integer maxSolverTime, Integer maxSolutions) {
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1. Load program
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		final Iterable<String[]> equiv = pl.get(); // load courses, get equivalencies
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. Create CSP; some useful constants
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		final Store store = new Store();
		
		final IntVar bT = new BooleanVar(store, new BoundDomain(1, 1));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. Create search variables, each with domains; organize via useful indexes
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// list of search variables
		final IntVar[] searchVars = new IntVar[Course.OFFERINGS.size()];
		
		// by class name: 0=sem_1?, 1=sem_2?, ... (maxt-1)=sem_{maxt-1}?, maxt=value
		final Map<String, IntVar[]> varMap = new TreeMap<>();
		final int[] creditReservations = new int[maxSemesters];
		
		// create variables with domains (w.r.t offering, reservations) -> searchVars
		// create reified boolean equivalencies per semester -> varMap
		{
			int i = 0;
			for (Course c : Course.OFFERINGS.values()) {
				// semester assignment variable
				final IntVar v = new IntVar(store, c.name);
				searchVars[i++] = v;
				
				// class -?> semester
				final IntVar[] vars = new IntVar[maxSemesters+1];
				for (int t=0; t<maxSemesters; t++) {
					final BooleanVar b = new BooleanVar(store, String.format("%s=%d", c.name, t));
					store.impose(new Reified(new XeqC(v, t), b));
					
					vars[t] = b;
				}
				vars[maxSemesters] = v;
				varMap.put(c.name, vars);
				
				// domain
				if (classesTaken.contains(c.name)) {
					v.addDom(-1, -1); // taken courses get -1
				} else {
					boolean reserved = false;
					for (int t=0; t<maxSemesters; t++) {
						final int semester = Course.semester(startSemester, t);
						
						// if a course is reserved
						// assign it to the first
						// semester in which it
						// is offered
						if (classReservations.containsKey(t) && classReservations.get(t).contains(c.name) && c.semesters[semester]) {
							reserved = true;
							creditReservations[t] += c.credits;
							v.addDom(t, t);
							break;
						}
					}
					
					// if not reserved, domain = any offered semester
					if (!reserved) {
						for (int t=0; t<maxSemesters; t++) {
							final int semester = Course.semester(startSemester, t);
							
							if (c.semesters[semester]) {
								v.addDom(t, t);
							}
						}
					}
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Create constraints!
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Course equivalencies (via partial ordering)
		//  if
		//    class A_i is equivalent to class A_j, and i<j
		//  then
		//    A_i <= A_j
		for (String[] e : equiv) {
			for (int i=0; i<e.length-1; i++) {
				store.impose(new XlteqY(varMap.get(e[i])[maxSemesters], varMap.get(e[i+1])[maxSemesters]));
			}
		}
		
		// Prerequisites
		//  if 
		//    class A is a prerequisite for class B, and A hasn't been taken
		//  then
		//   A < B
		for (Course c : Course.OFFERINGS.values()) {
			for (String pre : c.prereqs) {
				if (!classesTaken.contains(pre)) {
					store.impose(new XltY(varMap.get(pre)[maxSemesters], varMap.get(c.name)[maxSemesters]));
				}
			}
		}
		
		// Corequisites
		//  if 
		//    class A is a corequisite for class B
		//  then
		//   A <= B
		for (Course c : Course.OFFERINGS.values()) {
			for (String co : c.coreqs) {
				store.impose(new XlteqY(varMap.get(co)[maxSemesters], varMap.get(c.name)[maxSemesters]));
			}
		}
		
		
		// Total hours per semester...
		//  if avoid: [reserved, reserved]
		//  else: (!COOP AND ([reserved, reserved] OR [minCredits, maxCredits])) OR (COOP AND [0, maxCoOpCredits])
		{
			// # credits per class
			final int[] credits = new int[Course.OFFERINGS.size()];
			int i = 0;
			for (Course c : Course.OFFERINGS.values()) {
				credits[i++] = c.credits;
			}
			
			// for each semester
			for (int t=0; t<maxSemesters; t++) {
				// t/f: whether a class is assigned to a semester
				final BooleanVar[] bvars = new BooleanVar[Course.OFFERINGS.size()];
				i = 0;
				for (Course c : Course.OFFERINGS.values()) {
					bvars[i++] = (BooleanVar) varMap.get(c.name)[t];
				}
				
				if (avoidSemesters.contains(t)) {
					store.impose(new LinearInt(store, bvars, credits, "==", creditReservations[t]));
				} else {
					store.impose(
						new Or(
							new And(
								new And(
									new XneqY(varMap.get(Course.COOP_NAME[0])[t], bT),
									new XneqY(varMap.get(Course.COOP_NAME[1])[t], bT)
								),
								new Or(
									new LinearInt(store, bvars, credits, "==", creditReservations[t]), 
									new And(new LinearInt(store, bvars, credits, ">=", minCredits), new LinearInt(store, bvars, credits, "<=", maxCredits))
								)
							),
							new And(
								new Or(
									new XeqY(varMap.get(Course.COOP_NAME[0])[t], bT),
									new XeqY(varMap.get(Course.COOP_NAME[1])[t], bT)
								),
								new And(new LinearInt(store, bvars, credits, ">=", 0), new LinearInt(store, bvars, credits, "<=", maxCreditsWithCoOp))
							)
						)
					);
				}
			}
		}
		
		// Hours for co-op
		//  for each semester t, sum_{credits in semesters < t) >= requirement
		for (int t=0; t<maxSemesters; t++) {
			// # credits per class
			final int[] credits = new int[Course.OFFERINGS.size()];
			
			// v<t
			final BooleanVar[] bvars = new BooleanVar[Course.OFFERINGS.size()];
			
			int i = 0;
			for (Course c : Course.OFFERINGS.values()) {
				credits[i] = c.credits;
				
				final BooleanVar b = new BooleanVar(store);
				store.impose(new Reified(
					new XltC(varMap.get(c.name)[maxSemesters], t),
				b));
				bvars[i] = b;
				
				i++;
			}
			
			for (i=0; i<Course.COOP_PREREQ.length; i++) {
				store.impose(
					new IfThen(
						new XeqY(varMap.get(Course.COOP_NAME[i])[t], bT),
						new LinearInt(store, bvars, credits, ">=", Course.COOP_PREREQ[i]-creditsTakenOther)
					)
				);
			}
		}
		
		// No all-class-semester years (other than those reserved)
		if (noAllClassYear) {
			for (int t=0; t<maxSemesters; t++) {
				// representing a non-course semester
				final int semester = Course.semester(startSemester, t);
				
				// if fall and the whole academic year fits
				if (semester==0 && (t+2)<maxSemesters) {
					final PrimitiveConstraint[] legalVar = new PrimitiveConstraint[3];
					
					// loop over the semesters of
					// this academic year
					for (int i=0; i<3; i++) {
						// convenience variable for semester
						// in this academic year
						final int s = t + i;
						
						// reserved classes in this semester (if any)
						final List<String> semesterReservations = classReservations.containsKey(s)?classReservations.get(s):null;
						
						// classes not reserved for this semester
						final int numNonReserved = Course.OFFERINGS.size() - (semesterReservations==null?0:semesterReservations.size());
						
						// indication of if any non-reserved classes are assigned to this semester
						final PrimitiveConstraint[] cSem = new PrimitiveConstraint[numNonReserved-2];
						
						// indication of if co-op is during this semester
						final PrimitiveConstraint coopSem = new Or(
							new XeqY(varMap.get(Course.COOP_NAME[0])[s], bT), 
							new XeqY(varMap.get(Course.COOP_NAME[1])[s], bT)
						);
						
						int j = 0;
						for (Course c : Course.OFFERINGS.values()) {
							if ((semesterReservations==null || !semesterReservations.contains(c.name)) && 
									!c.name.equals(Course.COOP_NAME[0]) && !c.name.equals(Course.COOP_NAME[1])) {
								cSem[j++] = new XneqY(varMap.get(c.name)[s], bT);
							}
						}
						
						//
						
						legalVar[i] = new Or(coopSem, new And(cSem));
					}
					
					// impose the constraint that at least 
					// one semester must be "legal" in an 
					// academic year
					store.impose(new Or(legalVar));
				}
			}
		}
		
		// Deviation from tracking sheet
		if (deviationFromTracking != null) {			
			// if not the beginning of the degree,
			// approximate semesters via number
			// of credits taken divide by minimum
			// per semester
			final int offsetEstimate = (int) Math.round(classesTaken.stream().mapToInt(c -> Course.OFFERINGS.get(c).credits).sum() * 1.0 / minCredits);
			
			// cost will be the sum of differences of
			// expected assignments (via course "slot"
			// and offset estimate) and actual
			final IntVar cost = new IntVar(store, "cost", 0, deviationFromTracking);
			final IntVar[] diffVars = new IntVar[Course.OFFERINGS.size()];
			
			int i=0;
			for (Course c : Course.OFFERINGS.values()) {
				final int classEstimate;
				if (classesTaken.contains(c.name)) {
					classEstimate = -1;
				} else {
					classEstimate = c.slot - offsetEstimate;
				}
				final IntVar exp = new IntVar(store, String.format("%s-expected", c.name), classEstimate, classEstimate);
				final IntVar diff = new IntVar(store, String.format("%s-diff", c.name), 0, maxSemesters);
				store.impose(new Distance(exp, varMap.get(c.name)[maxSemesters], diff));
				
				diffVars[i++] = diff;
			}
			
			store.impose(new SumInt(store, diffVars, "==", cost));
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. Search!
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		final Search<IntVar> search = new DepthFirstSearch<>();
		final SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store, searchVars, new IndomainMin<IntVar>());
		
		search.setPrintInfo(false);
		search.getSolutionListener().searchAll(true);
		search.getSolutionListener().recordSolutions(true);
		
		search.setStore(store);
		search.setSelectChoicePoint(select);
		
		if (maxSolverTime != null) {
			search.setTimeOut(maxSolverTime);
		}
		
		search.labeling();
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 6. Return results
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		final List<Map<String, Integer>> solutions = new ArrayList<>();
		{
			final IntVar[] vars = search.getVariables();
			
			for (int i=1; i<=search.getSolutionListener().solutionsNo(); i++) {
				if (maxSolutions != null && i > maxSolutions) break;
				
				final Map<String, Integer> solution = new LinkedHashMap<>();
				final Domain[] vals = search.getSolution(i);
				
				for (int j=0; j<vars.length; j++) {
					solution.put(vars[j].id, Integer.valueOf(vals[j].toString()));
				}
				
				solutions.add(solution);				
			}
		}	
		
		return solutions;
	}
	
	public static void main(String[] args) {
		
		final ProgramLoader PROGRAM = Program::BCOS;
		final int START_SEMESTER = 0; // starting semester is fall=0, spring=1, summer=2
		final int START_YEAR = 2017; // starting year (for printing purposes)
		final int MAX_SEMESTERS = 12; // semesters over which to search
		
		final int MIN_CREDITS = 12; // per semester
		final int MAX_CREDITS = 16; //  per semester
		final int MAX_CREDITS_COOP  = 0; // per semester with co-op
		
		final Integer DEVIATION_FROM_TRACKING = 0; // allowed changes from course slot
		
		final boolean NO_ALL_CLASS_YEAR = true; // three semesters of class in the same academic year (fall-summer)
		
		//
		
		final Set<String> taken = new HashSet<>(); // already taken
//		taken.add("COMP1000");
//		taken.add("COMP1050");
//		taken.add("MATH2300");
		
		final int CREDITS_TAKEN_OTHER = 0;
		
		final Set<Integer> avoid = new HashSet<>(); // semesters to avoid
//		avoid.add(2);
//		avoid.add(5);
		
		final Map<Integer, List<String>> peg = new LinkedHashMap<>(); // fixing courses to individual semesters
//		peg.put(2, Arrays.asList("MATH1850"));
		
		final Integer MAX_TIME = 1; // solver timeout (null for no limit)
		final Integer MAX_SOLUTIONS = 10; // maximum solutions to return (null for no limit)
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		final List<Map<String, Integer>> solutions = buildSchedules(
			PROGRAM, START_SEMESTER, START_YEAR, MAX_SEMESTERS, 
			MIN_CREDITS, MAX_CREDITS, MAX_CREDITS_COOP, 
			DEVIATION_FROM_TRACKING, NO_ALL_CLASS_YEAR, 
			taken, CREDITS_TAKEN_OTHER, avoid, peg, 
			MAX_TIME, MAX_SOLUTIONS
		);

		////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		if (!solutions.isEmpty()) {
			for (int i=1; i<=solutions.size(); i++) {
				System.out.printf("== Schedule %d ==%n", i);
				printSchedule(solutions.get(i-1), START_SEMESTER, START_YEAR, MAX_SEMESTERS);
				System.out.printf("%n");
				
				System.out.printf("Found %,d schedule(s), %s; searched for %ds%n", solutions.size(), MAX_SOLUTIONS==null?"no limit":String.format("limited to %,d", MAX_SOLUTIONS), MAX_TIME);
			}
		} else {
			System.out.println("No satisfying schedules :(");
		}
	}

}
