/*
 * Basic structure where COL, E, ROOT are doubly linked list.
root-> COL1->COL2 -> COL3->....->COLN
       |     |       |            |
       v     V       V            v
      E11 -> E12 -> E13 -> ... ->E1N 
       |     |       |
       |     V       V
       |     E22 -> E23
       |     |
       V     v
       E31-> E32
 
       each COL also maintains the number of current elements

“Algorithm X”

We can find solution sets, if any exist, by a straightforward application of 
depth-first search with backtracking. In particular, we can incrementally build 
up a set of rows making up a solution as follows:

1.  Pick an unsatisfied constraint: that is, a column with no black cell 
    in any of the rows in the solution set. If there are no unsatisfied 
    constraints remaining, the solution set is complete: if all we wanted 
    was any solution, we’re done; if we want all solutions, then make note 
    of the solution we’ve got and then backtrack to the previous time we 
    chose a row and take the next choice instead.

2.  Pick a row that satisfies that constraint: that is, one with a black 
    cell in the chosen column. If there is no such row, then we’ve reached 
    a dead end and we must backtrack to the previous time we chose a row and 
    take the next choice instead.

3.  Add that row to the solution set.

4.  Delete all rows that satisfy any of the constraints satisfied by 
    the chosen row: that is, all rows that have a black cell in the same 
    column as a black cell in the chosen row.

5.  Return to step 1.
*/


import java.util.*;
public class AlgoX {
    private final int maxP; //max possibilities
    private final int priC; //primary constraints
    private final int maxC; //max constraints
    private boolean[] satisfiedC;
    private NODE root;
    private NODE[] cols;
    private boolean solvable;
    Stack<Integer> sols;// = new Stack<>();
    private static class DATA {
        private int r, c, v;
        public DATA(int r, int c, int v)
        {
            this.r = r;
            this.c = c;
            this.v = v;
        }
    }
    private static class NODE {
        private int row, col, value, count;
        private NODE left, right;
        private NODE top, bottom;
       
        public void init(int r, int c, int v)
        {
            row    = r;
            col    = c;
            value  = v;
            count  = 0;
            left   = this;
            right  = this;
            top    = this;
            bottom = this; 
        }

        public NODE(int r, int c)
        {
            init(r, c, 0);
        }
   
        public NODE(int r, int c, int v)
        {
            init(r, c, v);
        }
        public void print()
        {
            if (value == 0)
                StdOut.print("Col node:");
            else
                StdOut.print("Nor node:");

            StdOut.println("(" + row + "," + col + " = " + value + "," + count + ")");
        }

    }

    
    public void insertLeft(NODE h, NODE p)
    {
        NODE last = h.left;
       
        last.right = p;
        p.left     = last;

        h.left     = p;
        p.right    = h;
    }


    public void insertTop(NODE h, NODE p)
    {
        NODE last = h.top;
        
        last.bottom = p;
        p.top       = last;

        h.top       = p;
        p.bottom    = h;
    }

    //linked list of constrains, and the total number of constraints
    public AlgoX(LinkedList<DATA>[] list, int possibilities, int constraints, 
                        int priConstraints, int[] partialSols, int partialN)
    {
        solvable = true;
        
        maxP = possibilities;
        maxC = constraints;
        priC = priConstraints;

        // create a root
        root = new NODE(0, 0);
        cols = new NODE[maxC+1];
        satisfiedC = new boolean[maxC+1];
        

        NODE prev = root;
        //create a doubly linked list of the cols, with root at the head
        for (int i = 1; i <= maxC; i++) {
            cols[i] = new NODE(0, i);
            insertLeft(root, cols[i]);
            satisfiedC[i] = false;
        }

        NODE rt = cols[priC].right;
        NODE lt = root.left;

        //disconnect primary constrains from rest
        cols[priC].right = root;
        root.left = cols[priC];

        rt.left  = lt;
        lt.right = rt;

        //temporary storage to store the head of row, for preprocessing with partialSolutions
        NODE[] trows = new NODE[partialN];
        boolean psolution = partialN > 0;
        
        // Create a doubly linked list of each column elements
        // Create a doubly linked list of each row elements
        for (int i = 1, j = 0; i <= maxP; i++) {
            boolean first = true;
            NODE l = null;
            for (DATA d:list[i]) {
                NODE nd = new NODE(d.r, d.c, d.v);

                assert(i == d.r);

                insertTop(cols[d.c], nd);
                cols[d.c].count += 1;
                if (!first)                             //insert into row
                    insertLeft(l, nd);
                else 
                    first = false;

                l = nd;
            }
            // j can be zero and partialSols would be invalid, for full solvers
            if (psolution) {
                if (partialSols[j] == i) 
                    trows[j++]  = l;
                if (j == partialN)
                    psolution = false;
            }
            
        }
        sols = new Stack<>();
        NODE n;
        for (int i = 0; i < partialN; i++) {
            n = trows[i]; 
            sols.push(n.row);
            if (n.top.bottom != n) { // the corresponding column has been removed by previous
                solvable = false;  // row solution, which means voilation of constraints
                //StdOut.println("Unsolvable");
                break;
            }
            processRow(n, null);
        }
    }

    // if minIndex == 0 , solved
    //             == -1, cannot be solved
    //  otherwise carry on with work
    public int minColumn()
    {
        int min = maxP+1;
        int minIndex = 0;
        for (NODE c = root.right; c != root; c = c.right) {
            if (!satisfiedC[c.col]) {
                int cnt = c.count;
                if (cnt == 0)
                    return -1;
                if (cnt < min) {
                    min = cnt;
                    minIndex = c.col;
                }
            }
        }
        return minIndex;
    }
    public int getNextColumn(int curCol)
    {
        for (NODE c = root.right; c != root; c = c.right) {
            if (c.col != curCol && !satisfiedC[c.col] ) {
                if (cols[c.col].count == 0)
                    return -1;
                return c.col;
            }
        }
        return 0;
    }
    
    public boolean isSolved()
    {
        return (root == root.right);
    }

    public boolean isColNode(NODE h)
    {
        return (h.value == 0);
    }

    public boolean isRootNode(NODE h)
    {
        return (h.row == 0);
    }

    public void print()
    {
        NODE r, c;
        for (r = root.right; r != root; r = r.right) {
            StdOut.println("===============================");
            r.print();
            StdOut.println("---------------");
            for (c = r.bottom; c != r; c = c.bottom) 
                c.print();
        }
    }

    //disconnect node from a column
    public void detachNode(NODE h)
    {
        h.top.bottom = h.bottom;
        h.bottom.top = h.top;
        cols[h.col].count -= 1;
    }

    //attach node to a column
    public void attachNode(NODE h)
    {
        h.top.bottom = h;
        h.bottom.top = h;
        cols[h.col].count += 1;
    }

    public void detachRow(NODE h, Stack<NODE> st)
    {
        if (isColNode(h))
            return;
       
        if (st != null)
            st.push(h);

        NODE p;
        for (p = h.right; p != h; p = p.right) 
            detachNode(p);
        detachNode(p);
    }

    public void attachRow(NODE h)
    {
        assert(!isColNode(h));

        NODE p;
        for (p = h.right; p != h; p = p.right) 
            attachNode(p);
        attachNode(p);
    }

    public void attachCol(NODE c)
    {
        c.left.right = c;
        c.right.left = c;
    }

    public void detachCol(NODE c, Stack<NODE> st)
    {
        if (st != null)
            st.push(c);

        c.left.right = c.right;
        c.right.left = c.left;
    }

    public void printSolution(Stack<Integer> q)
    {
        for (int i:q) 
            StdOut.print(i + " ");
        StdOut.println();
    }


    /* Go through the elements of the column 
     * and remove the rows of the elements
     * c1 -> c2 -> c3 -> c4
     * r1-----------------
     * ------------r2-----
     * ------r3-----------     
     * r4-----------------
     * r5-----------------
     * while processing c1 and selecting r1, have to remove r4, r5
     *
     * do not detach the main row here, as it will be done in a separate function
     * but detach the corresponding column
     */
    public void processRowCols(NODE h, Stack<NODE> st)
    {
        NODE p;
        for (p = h.bottom; p != h; p = p.bottom) 
            detachRow(p, st);
        
        detachCol(cols[h.col], st);
    }

    public void processRow(NODE h, Stack<NODE> st)
    {
        NODE p;
        for (p = h; p != h.left; p = p.right) {
            satisfiedC[p.col] = true;
            processRowCols(p, st);
        }
        satisfiedC[p.col] = true;
        processRowCols(p, st);
        detachRow(h, st);
    }
   

    public void deprocessRow(Stack<NODE> st)
    {
        NODE h;
        while (!st.isEmpty())  {
            h = st.pop();
            if (isColNode(h))
                attachCol(h);
            else {
                attachRow(h); 
                satisfiedC[h.col] = false;
            }
        }
    }
    
    public void solve(Stack<Integer> q)
    {
        int mc = minColumn(); 
        if (mc == -1) return;
        if (mc == 0)  { 
            printSolution(q);
            return;
        }
        NODE c = cols[mc];
        assert(c.count > 0); 
        for (NODE h = c.bottom; h != c; h = h.bottom) {
            if (isColNode(h)) 
                continue;

            Stack<NODE> st = new Stack<>();
            q.push(h.row);
            processRow(h, st);

            solve(q);

            deprocessRow(st);
            q.pop();
            assert(st.isEmpty() == true);
        }
    }

    public Stack<Integer> solve()
    {
        if (solvable) {
            solve(sols);
            return sols;
        }
        else {
            return null;
        }
    }

    public static void main(String[] args)
    {
        In in = new In(args[0]);

        String[] q = in.readLine().split("\\s+"); //StdIn.readLine().split("\\s+");
        int possibilities       = Integer.parseInt(q[0]); // total possibilities
        int constraints         = Integer.parseInt(q[1]); // primary constraints
        int priConstraints      = Integer.parseInt(q[2]); // total constraints
        int partialN            = Integer.parseInt(q[3]); 

        if (priConstraints > constraints) {
            StdOut.println("primary constraints greater than total constraints");
            return;
        }

        LinkedList<DATA>[] list = new LinkedList[possibilities+1];

        for (int i = 1; i <= possibilities; i++) {
            list[i] = new LinkedList<DATA>();
            for (String s:in.readLine().split("\\s+")) { 
                list[i].addFirst(new DATA(i, Integer.parseInt(s), 1));
            }
        }

        int[] partialSols = new int[partialN];
        for (int i = 0; i < partialN; i++) {
            for (String s:in.readLine().split("\\s+")) 
                partialSols[i] = Integer.parseInt(s);
        }
        AlgoX al = new AlgoX(list, possibilities, constraints, priConstraints, partialSols, partialN);
        al.solve();
    }
}
