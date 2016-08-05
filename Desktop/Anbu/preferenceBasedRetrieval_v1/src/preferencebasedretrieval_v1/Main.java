package preferencebasedretrieval_v1;

import org.jacop.core.*;
import org.jacop.constraints.*;
import static org.jacop.examples.scala.Cos.x;
import static org.jacop.examples.scala.Quadratic.x;
import static org.jacop.examples.scala.TSP.cost;
import org.jacop.floats.constraints.PminusQeqR;
import org.jacop.floats.constraints.PmulQeqR;
import org.jacop.floats.constraints.PplusQeqR;
import org.jacop.floats.core.FloatDomain;
import org.jacop.floats.core.FloatVar;
import org.jacop.floats.search.LargestDomainFloat;
import org.jacop.floats.search.Optimize;
import org.jacop.floats.search.SplitSelectFloat;
import org.jacop.search.*;

public class Main {

    static Main m = new Main();

    public static void main(String[] args) {
        Store store = new Store(); // define FD store
        int size = 4;
        // define finite domain variables
        IntVar[] v = new IntVar[size];
        for (int i = 0; i < size; i++) {
            v[i] = new IntVar(store, "v" + i, 1, size);
        }
        // define constraints
        store.impose(new XneqY(v[0], v[1]));
        store.impose(new XneqY(v[0], v[2]));
        store.impose(new XneqY(v[1], v[2]));
        store.impose(new XneqY(v[1], v[3]));
        store.impose(new XneqY(v[2], v[3]));
        // search for a solution and print results
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new InputOrderSelect<IntVar>(store, v, new IndomainMin<IntVar>());
        boolean result = search.labeling(store, select);

        if (result) {
            System.out.println("Solution: " + v[0] + ", " + v[1] + ", " + v[2] + ", " + v[3]);
        } else {
            System.out.println("*** No");
        }
        
        store = new Store();
        FloatDomain.setPrecision(1E-4);
        FloatVar P = new FloatVar(store, "P", 0.0, 1.0);
        FloatVar[] Q = new FloatVar[1];
        
        FloatVar Const = new FloatVar(store, "Const",1.0,1.0);
        FloatVar temp = new FloatVar(store,"temp", 0.0, 1e150);
        FloatVar cost = new FloatVar(store,"temp", 0.0, 1e150);
        store.impose(new PmulQeqR(P, P, temp));
        store.impose(new PminusQeqR(temp,Const, cost));
        Q[0] = P;

        DepthFirstSearch<FloatVar> search1 = new DepthFirstSearch<FloatVar>();
        SplitSelectFloat<FloatVar> select1 = new SplitSelectFloat<FloatVar>(store, Q,new LargestDomainFloat<FloatVar>());
        Optimize min = new Optimize(store, search1, select1, cost);
        boolean result1 = min.minimize();


        
        if (result1) {
            System.out.println("Solution: " + result1);
        } else {
            System.out.println("*** No");
        }


    }

}
