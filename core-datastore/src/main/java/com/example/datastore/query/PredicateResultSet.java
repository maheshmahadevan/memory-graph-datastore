package com.example.datastore.query;

import java.util.Deque;
import java.util.List;

import com.example.datastore.entity.Node;

/**
 * A immutable version of ResultSet interface
 * Only to be used to store results of Predicate 
 * query executions. For Query execution results
 * @see QueryResultSet.class
 * @author mahesh
 *
 */
public class PredicateResultSet implements ResultSet{
	
	private final Deque<String> projections;
	private final List<Deque<Node>> rows;	
	
	
	private PredicateResultSet(Deque<String> projections,List<Deque<Node>> rows/*,Map<String,Set<Node>> uniqueColumnEntries*/) {
		this.projections = projections;
		this.rows = rows;
		
	}
	
	/**
	 * Method can be used when both projections and row results
	 * are final and not expected to be changed ( like predicate results)
	 * Not to be used for Query Result Set
	 * @param projections
	 * @param rows
	 * @return
	 */
	public static PredicateResultSet generateResultSet(Deque<String> projections,List<Deque<Node>> rows) {
		
		return new PredicateResultSet(projections, rows);
	}
	
	
	

	@Override
	public Deque<String> getProjections() {
		return projections;
	}
	
	@Override
	public List<Deque<Node>> getRows() {
		return rows;
	}
	

	

}
