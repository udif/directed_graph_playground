package de.normalisiert.utils.graphs;

import java.util.List;
import java.util.ArrayList;



/**
 * Searchs all elementary cycles in a given directed graph. The implementation
 * is independent from the concrete objects that represent the graphnodes, it
 * just needs an array of the objects representing the nodes the graph
 * and an adjacency-matrix of type boolean, representing the edges of the
 * graph. It then calculates based on the adjacency-matrix the elementary
 * cycles and returns a list, which contains lists itself with the objects of the
 * concrete graphnodes-implementation. Each of these lists represents an
 * elementary cycle.<br><br>
 *
 * The implementation uses the algorithm of Donald B. Johnson for the search of
 * the elementary cycles. For a description of the algorithm see:<br>
 * Donald B. Johnson: Finding All the Elementary Circuits of a Directed Graph.
 * SIAM Journal on Computing. Volumne 4, Nr. 1 (1975), pp. 77-84.<br><br>
 *
 * The algorithm of Johnson is based on the search for strong connected
 * components in a graph. For a description of this part see:<br>
 * Robert Tarjan: Depth-first search and linear graph algorithms. In: SIAM
 * Journal on Computing. Volume 1, Nr. 2 (1972), pp. 146-160.<br>
 *
 * @author Frank Meyer, web_at_normalisiert_dot_de
 * @version 1.2, 22.03.2009
 *
 */
// Define the Getter interface

public class ElementaryCyclesSearch {
	/** List of cycles */
	interface Getter {
		Object get(int index);
	}
	private List<List<Object>> cycles = null;

	/** Adjacency-list of graph */
	private int[][] adjList = null;

	/** Graphnodes */
	private Object[] nodes_array = null;
	private List<Object> nodes_list = null;
	private Getter get_node;
	/** Blocked nodes, used by the algorithm of Johnson */
	private boolean[] blocked = null;

	/** B-Lists, used by the algorithm of Johnson */
	private ArrayListInteger[] B = null;

	/** Stack for nodes, used by the algorithm of Johnson */
	private List<Integer> stack = null;

	/**
	 * Constructor.
	 *
	 * @param matrix adjacency-matrix of the graph
	 * @param graphNodes array or a List of the graphnodes of the graph; this is used to
	 * build sets of the elementary cycles containing the objects of the original
	 * graph-representation
	 * array is more efficient if the size is known before it is created,
	 * while a List interface is convenient if it needs to be created gradually
	 */

	public ElementaryCyclesSearch(boolean[][] matrix, List<Object> graphNodes) {
		this.nodes_list = graphNodes;
		this.get_node = index -> nodes_list.get(index);
		this.adjList = AdjacencyList.getAdjacencyList(matrix);
	}

	public ElementaryCyclesSearch(boolean[][] matrix, Object[] graphNodes) {
		this.nodes_array = graphNodes;
		this.get_node = index -> nodes_array[index];
		this.adjList = AdjacencyList.getAdjacencyList(matrix);
	}



	/**
	 * Returns List::List::Object with the Lists of nodes of all elementary
	 * cycles in the graph.
	 *
	 * @return List::List::Object with the Lists of the elementary cycles.
	 */
	public List<List<Object>> getElementaryCycles() {
		this.cycles = new ArrayList<List<Object>>();
		this.blocked = new boolean[this.adjList.length];
		this.B = new ArrayListInteger[this.adjList.length];
		this.stack = new ArrayList<Integer>();
		StrongConnectedComponents sccs = new StrongConnectedComponents(this.adjList);
		int s = 0;

		while (true) {
			SCCResult sccResult = sccs.getAdjacencyList(s);
			if (sccResult != null && sccResult.getAdjList() != null) {
				ArrayListInteger[] scc = sccResult.getAdjList();
				s = sccResult.getLowestNodeId();
				for (int j = 0; j < scc.length; j++) {
					if ((scc[j] != null) && (scc[j].size() > 0)) {
						this.blocked[j] = false;
						this.B[j] = new ArrayListInteger();
					}
				}

				this.findCycles(s, s, scc);
				s++;
			} else {
				break;
			}
		}

		return this.cycles;
	}

	/**
	 * Calculates the cycles containing a given node in a strongly connected
	 * component. The method calls itself recursivly.
	 *
	 * @param v
	 * @param s
	 * @param adjList adjacency-list with the subgraph of the strongly
	 * connected component s is part of.
	 * @return true, if cycle found; false otherwise
	 */
	private boolean findCycles(int v, int s, ArrayListInteger[] adjList) {
		boolean f = false;
		this.stack.add(Integer.valueOf(v));
		this.blocked[v] = true;

		for (int i = 0; i < adjList[v].size(); i++) {
			int w = ((Integer) adjList[v].get(i)).intValue();
			// found cycle
			if (w == s) {
				List<Object> cycle = new ArrayList<Object>();
				for (int j = 0; j < this.stack.size(); j++) {
					int index = ((Integer) this.stack.get(j)).intValue();
					cycle.add(this.get_node.get(index));
				}
				this.cycles.add(cycle);
				f = true;
			} else if (!this.blocked[w]) {
				if (this.findCycles(w, s, adjList)) {
					f = true;
				}
			}
		}

		if (f) {
			this.unblock(v);
		} else {
			for (int i = 0; i < adjList[v].size(); i++) {
				int w = ((Integer) adjList[v].get(i)).intValue();
				if (!this.B[w].contains(Integer.valueOf(v))) {
					this.B[w].add(Integer.valueOf(v));
				}
			}
		}

		// Notice the argument is an Integer object, not an int
		// This means that the value of v is removed from stack,
		// not the item at index v
		this.stack.remove(Integer.valueOf(v));
		return f;
	}

	/**
	 * Unblocks recursivly all blocked nodes, starting with a given node.
	 *
	 * @param node node to unblock
	 */
	private void unblock(int node) {
		this.blocked[node] = false;
		ArrayList<Integer> Bnode = this.B[node];
		while (Bnode.size() > 0) {
			Integer w = (Integer) Bnode.get(0);
			Bnode.remove(0);
			if (this.blocked[w.intValue()]) {
				this.unblock(w.intValue());
			}
		}
	}
}

