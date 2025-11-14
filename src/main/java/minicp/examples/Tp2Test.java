package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.Arrays;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import minicp.engine.constraints.tp2;

/**
 * Programme test du tp2 de INF6101
 */
public class Tp2Test {

    public static void main(String[] args) {

        int domaineMin = Integer.parseInt(args[0]);
        int domaineMax = Integer.parseInt(args[1]);

        Solver cp = Factory.makeSolver();

        IntVar x = makeIntVar(cp, domaineMin, domaineMax);
        IntVar[] y = new IntVar[2];
        IntVar z = makeIntVar(cp, domaineMin, domaineMax);
        for(int i = 0; i < 2; i++)
            y[i] = makeIntVar(cp, domaineMin, domaineMax);

        cp.post(new tp2(x, y, z));

        //DFSearch dfs = makeDfs(cp, firstFail(new IntVar[]{x, y[0], y[1], z}));
        DFSearch dfs = makeDfs(cp, splitDomRange(new IntVar[]{x, y[0], y[1], z}));

        dfs.onSolution(() -> {
            System.out.println("Solution found");
            System.out.println("x = " + x.min() + " y = " + y[0].min() + ", " + y[1].min() + " z = " + z.min());
        });

        SearchStatistics stats = dfs.solve();

        System.out.println(stats);

    }

}
