package com.ashi.src;

/**
 * Enumeration object used to assign node state. Node can be a regular node with
 * no special properties, a beginning node to which the automata starts testing
 * from and a terminal node. A query string is successful if, by the end of the
 * query, a terminal node is reached
 * 
 * @author Andy
 *
 */
public enum NodeState {
	REGULAR, BEGINNING, TERMINAL;
}
