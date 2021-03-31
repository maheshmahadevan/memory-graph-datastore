package com.example.datastore.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.datastore.util.IdGenUtility;

public class Node implements DbEntity,Comparable<Node>{
	
	private final int _id;
	private final String label;
	
	private volatile int  _hashcode = 0;
	
	
	private Map<String,List<Relationship>> out;
	private Map<String,List<Relationship>> in;
	
	private Node(String label) {
		this._id = IdGenUtility.INSTANCE.getNextId();
		this.label = label;
		out = new HashMap<>();
		in = new HashMap<>();
	}
	
	
	public int getId() {
		return _id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Map<String,List<Relationship>> getOutgoingRelationships() {
		return out;
	}
	
	public Map<String,List<Relationship>> getIncomingRelatioships() {
		return in;
	}
	
	/**
	 * Only label is key/unique id since
	 * we dont expect another instance of Node
	 * having same label present in the store
	 */	
	public String getKey() {
		return label;
	}
	
	//TODO - check permissions
	public static Node createNode(String label) {
		return new Node(label);
	}


	


	@Override
	public int hashCode() {
		
		if(_hashcode == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + _id;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			_hashcode =  result;
		}
		
		return _hashcode;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (_id != other._id)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Node [_id=" + _id + ", label=" + label + "]";
	}


	@Override
	public int compareTo(Node node) {
		return this.label.compareTo(node.label);
	}
	
	
	
	
	

}
