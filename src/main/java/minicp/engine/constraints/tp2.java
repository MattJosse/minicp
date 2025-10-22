/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (v)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.cp.Factory;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.state.State;
import minicp.state.StateInt;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

import java.util.Arrays;
import java.util.stream.IntStream;


/**
 * Reified equality constraint
 * @see minicp.cp.Factory#isEqual(IntVar, int)
 */
public class tp2 extends AbstractConstraint { //see doc
    
    private  IntVar x;
    private  IntVar[] y;
    private  IntVar z;
    
    private int[] fixed;
    private StateInt nFixed;
    private State<Long> sumFixed;
    private int[] min;
    private int n;
    private StateInt xCanZero;
    private int factor;

    /**
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public tp2(IntVar x, IntVar[] y, IntVar z) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.z = z;
        this.n = y.length;

        min = new int[y.length];                                                    //array of minimums of variables of y.Doesnt need state int cause doesnt need to backtrack.
        nFixed = getSolver().getStateManager().makeStateInt(0);                     // number of fixed variables. Can backtrack.
        sumFixed = getSolver().getStateManager().makeStateRef(Long.valueOf(0));   // sum of fixed variables. Can backtrack.
        fixed = IntStream.range(0, n).toArray();                     // array of indexes of not fixed variables. 
        xCanZero = getSolver().getStateManager().makeStateInt(0);                   // indicates if x can be zero. Can backtrack.
        

    }

    @Override
    public void post() {
        //#we should progate on min change.
        for (IntVar var : y)
            var.propagateOnBoundChange(this);

        //#We should propagate on max change.
        z.propagateOnBoundChange(this);
        //#We should propagate on zero change.
        x.propagateOnBoundChange(this);
        propagate();
    }

    public void propagate() {
        // Filter the unfixed vars and update the partial sum


        int nF = nFixed.value();
        long sumMin = sumFixed.value(); //sumMin is the sum of the minimums of all the variables in y

        if (x.contains(0)) {
            xCanZero.setValue(1);
        } else {
            xCanZero.setValue(0);
        }
        factor = 1 + xCanZero.value();
        
        // iterate over not-fixed variables and update partial sum
        // if  one variable is detected as fixed
        for (int i = nF; i < y.length; i++) {
            int idx = fixed[i];

            min[idx] = y[idx].min();
            sumMin += min[idx]; // Update partial sum
            if (y[idx].isFixed()) {
                sumFixed.setValue(sumFixed.value() + y[idx].min());
                fixed[i] = fixed[nF]; // Swap the variables
                fixed[nF] = idx;
                nF++; 
            }
        }
        nFixed.setValue(nF);
        if (sumMin > factor * z.max() * n) {
            throw new InconsistencyException();
        }
        // iterate over not-fixed variables
        for (int i = nF; i < y.length; i++) {
            int idx = fixed[i];
            y[idx].removeAbove((factor * n * z.max()) - ((int) (sumMin - min[idx])));
        }
    }
}