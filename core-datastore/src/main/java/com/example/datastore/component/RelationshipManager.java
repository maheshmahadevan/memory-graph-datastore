package com.example.datastore.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.datastore.entity.Node;
import com.example.datastore.entity.Relationship;
import com.example.datastore.util.Direction;

public class RelationshipManager {
	
	private static Logger logger = LoggerFactory.getLogger(RelationshipManager.class);
	
	public static volatile RelationshipManager instance = null;
	
	
	private Map<String,List<Relationship>> relationshipStore = null;
	//TODO - check if needed
	private Map<String,Relationship> relationshipByKey = null;
	
	private RelationshipManager() {
		relationshipStore = new HashMap<>();
		relationshipByKey = new HashMap<>();
		
	}
	
	public static RelationshipManager getInstance() {
		
		if(instance == null) {
			synchronized(RelationshipManager.class) {
				if(instance == null) {
					instance = new RelationshipManager();
				}
			}
		}
		
		return instance;
		
	}
	
	public Relationship createRelationship(String label,Node left, Node right) {
		
		Relationship rel = Relationship.createRelationship(label, left, right);
		NodeManager.getInstance().addRelationship(left, rel, Direction.RELATION_OUT);
		NodeManager.getInstance().addRelationship(right, rel, Direction.RELATION_IN);
		
		List<Relationship> relList = relationshipStore.get(label);
		if(relList == null) {
			relList = new ArrayList<>();
			relationshipStore.put(label,relList);
			relationshipByKey.put(rel.getKey(), rel);
		}
		relList.add(rel);
		
		logger.info("Created new relationship {} -> {} -> {}",left.getLabel(),label,right.getLabel());
		
		return rel;
		
	}
	
	public List<Relationship> lookupRelationshipByLabel(String label){
		return relationshipStore.get(label);
	}
	
	public List<Relationship> lookupRelationshipByRegex(String regex){
		List<Relationship> relationships = new ArrayList<>();
		for(Entry<String,List<Relationship>> entry : relationshipStore.entrySet()) {
			if(entry.getKey().matches(regex)) {
				relationships.addAll(entry.getValue());
			}
		}
		
		return relationships;
	}
	
	public Relationship lookupRelationshipByKey(String leftNode,String relationship,String rightNode) {
		String key = leftNode + "->" + relationship + "->" + rightNode; 
		return relationshipByKey.get(key);
	}

	public void cleanStore() {
		relationshipStore.clear();
		relationshipByKey.clear();
	}
	
}
