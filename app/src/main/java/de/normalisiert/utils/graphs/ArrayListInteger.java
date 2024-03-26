package de.normalisiert.utils.graphs;

import java.util.ArrayList;

/*
 * We defined this class so we can do something like:
 * 
 * ArrayListInteger x[] = new ArrayListInteger[value];
 * 
 * Because doing:
 * 
 * ArrayList<Integer> x[] = new ArrayList<Integer>[value];
 *
 * Would yield a compile error: "cannot create a generic array of Arraylist<Integer>"
 * 
 */
public class ArrayListInteger extends ArrayList<Integer> {}
