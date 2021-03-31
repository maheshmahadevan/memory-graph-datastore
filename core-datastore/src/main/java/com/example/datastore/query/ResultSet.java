package com.example.datastore.query;

import java.util.Deque;
import java.util.List;

import com.example.datastore.entity.Node;

public interface ResultSet {
	
	Deque<String> getProjections();
	List<Deque<Node>> getRows();

}
