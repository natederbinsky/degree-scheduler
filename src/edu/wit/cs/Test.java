package edu.wit.cs;

import org.jacop.constraints.And;
import org.jacop.constraints.Or;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.Reified;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XeqY;
import org.jacop.constraints.XneqY;
import org.jacop.core.BooleanVar;
import org.jacop.core.BoundDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;

public class Test {

	public static void main(String[] args) {
		
		final Store store = new Store();
		
		final IntVar bT = new BooleanVar(store, new BoundDomain(1, 1));
		final IntVar bF = new BooleanVar(store, new BoundDomain(0, 0));
		
		
		
		final IntVar n1 = new IntVar(store, "n1", 0, 5);
		final IntVar n2 = new IntVar(store, "n2", 0, 5);
		final BooleanVar a = new BooleanVar(store, "a");
		final BooleanVar b = new BooleanVar(store, "b");
		final BooleanVar c = new BooleanVar(store, "c");
		final IntVar[] searchVars = {n1, n2};
		
		//
		
		final BooleanVar n1_0 = new BooleanVar(store);
		final BooleanVar n1_1 = new BooleanVar(store);
		final BooleanVar n1_2 = new BooleanVar(store);
		final BooleanVar n1_3 = new BooleanVar(store);
		final BooleanVar n1_4 = new BooleanVar(store);
		final BooleanVar n1_5 = new BooleanVar(store);
		final BooleanVar n2_0 = new BooleanVar(store);
		final BooleanVar n2_1 = new BooleanVar(store);
		final BooleanVar n2_2 = new BooleanVar(store);
		final BooleanVar n2_3 = new BooleanVar(store);
		final BooleanVar n2_4 = new BooleanVar(store);
		final BooleanVar n2_5 = new BooleanVar(store);
		
		store.impose(new Reified(new XeqC(n1, 0), n1_0));
		store.impose(new Reified(new XeqC(n1, 1), n1_1));
		store.impose(new Reified(new XeqC(n1, 2), n1_2));
		store.impose(new Reified(new XeqC(n1, 3), n1_3));
		store.impose(new Reified(new XeqC(n1, 4), n1_4));
		store.impose(new Reified(new XeqC(n1, 5), n1_5));
		
		store.impose(new Reified(new XeqC(n2, 0), n2_0));
		store.impose(new Reified(new XeqC(n2, 1), n2_1));
		store.impose(new Reified(new XeqC(n2, 2), n2_2));
		store.impose(new Reified(new XeqC(n2, 3), n2_3));
		store.impose(new Reified(new XeqC(n2, 4), n2_4));
		store.impose(new Reified(new XeqC(n2, 5), n2_5));
		
		store.impose(new And(
			new XneqY(n1_0, n2_0), 
			new Or(
				new PrimitiveConstraint[] {new XeqY(n1_0, bT), new XeqY(n2_0, bT)}
			)
		));
		
//		store.impose(new Or(new PrimitiveConstraint[] {
//			new And(new XeqY(n1_0, bT), new XneqY(n1_1, bF))
//			new Or(new And(new XeqY(n1_0, bT), new XneqY(n1_1, bF)), new And(new XeqY(n1_0, bT), new XneqY(n1_1, bF))),
//			new Xor(),
//			new Xor(),
//		}));
		
//		store.impose(new XneqC(n, 4));
		
//		store.impose(new Reified(new Or(new OrBool(new BooleanVar[] {n0}, bT), new OrBool(new BooleanVar[] {n1}, bF)), a));
//		store.impose(new Reified(new Or(new OrBool(new BooleanVar[] {n2}, bT), new OrBool(new BooleanVar[] {n3}, bF)), b));
//		store.impose(new Reified(new Or(new OrBool(new BooleanVar[] {n4}, bT), new OrBool(new BooleanVar[] {n5}, bF)), c));
//		
//		store.impose(new OrBool(new BooleanVar[] {a, b, c}, bT));
		
		//
		
		final Search<IntVar> search = new DepthFirstSearch<>();
		final SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store, searchVars, new IndomainMin<IntVar>());
		
		search.setPrintInfo(false);
		search.getSolutionListener().searchAll(true);
		search.getSolutionListener().recordSolutions(true);
		
		search.setStore(store);
		search.setSelectChoicePoint(select);
		
		search.labeling();
		
		search.printAllSolutions();
		
	}

}
