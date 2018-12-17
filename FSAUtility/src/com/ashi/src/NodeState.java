package com.ashi.src;

/**
 * Enumeration object used to assign node state. Node can be a regular node with
 * no special properties, a beginning node to which the automata starts testing
 * from and a terminal node which, upon reaching, means that the query string is
 * successful in this automata
 * 
 * @author Andy
 *
 */
public enum NodeState {
	REGULAR, BEGINNING, TERMINAL;
}
