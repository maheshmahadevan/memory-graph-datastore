package com.example.datastore.query;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.datastore.entity.Node;

public class QueryResultSet implements ResultSet{
	
	private Deque<String> projections;
	private List<Deque<Node>> rows;
	
	private Map<String,Integer> projectionIndex;
	
	private int index = -1;
	
	
	public QueryResultSet() {
		this.projections = new ArrayDeque<>();
		this.setProjectionIndex(new HashMap<>());
	}

	@Override
	public Deque<String> getProjections() {
		return projections;
	}

	public void addProjections(Deque<String> projs) {
		for(String p: projs) {
			projectionIndex.put(p, ++index);
			projections.add(p);
		}
		
	}
	
	public void addSingleProjection(String proj) {
		projectionIndex.put(proj, ++index);
		projections.add(proj);
		
		
	}
		
	@Override
	public List<Deque<Node>> getRows() {
		return rows;
	}

	public void setRows(List<Deque<Node>> rows) {
		this.rows = rows;
	}

	public Map<String,Integer> getProjectionIndex() {
		return projectionIndex;
	}

	public void setProjectionIndex(Map<String,Integer> projectionIndex) {
		this.projectionIndex = projectionIndex;
	}
	
	

	

}
