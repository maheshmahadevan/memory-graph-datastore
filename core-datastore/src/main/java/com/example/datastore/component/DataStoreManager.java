package com.example.datastore.component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.datastore.entity.Node;
import com.example.datastore.entity.Relationship;

public class DataStoreManager {
	
	private static Logger logger = LoggerFactory.getLogger(DataStoreManager.class);
	
	private static final String regex = "^\\s*\\{(\\w+)\\}\\s*,\\s*\\{(\\w+)\\}\\s*,\\s*\\{(\\w+)\\}\\s*$";
	
	public static volatile DataStoreManager instance = null;
	
	private NodeManager nodeManager;
	private RelationshipManager relationshipManager;
	
	private Pattern pattern;
	
	
	private DataStoreManager() {
		nodeManager = NodeManager.getInstance();
		relationshipManager = RelationshipManager.getInstance();
		
		pattern = Pattern.compile(regex);
		
	}
	
	public static DataStoreManager getInstance() {
		
		if(instance == null) {
			synchronized(DataStoreManager.class) {
				if(instance == null) {
					instance = new DataStoreManager();
				}
			}
		}
		
		return instance;
		
	}
	
	public boolean loadData(String filePath) throws IOException {
		
		try(BufferedReader in = new BufferedReader(new FileReader(filePath))){
			while(in.ready()) {
				String line = in.readLine();
				Matcher matcher = pattern.matcher(line);
				if(matcher.matches()) {
					matcher.reset();
					while(matcher.find()) {
						String leftNode = matcher.group(1);
						String relationship = matcher.group(2);
						String rightNode = matcher.group(3);
						
						createNodesAndRelationship(leftNode,relationship,rightNode);
					}
				}else {
					logger.error("Incorrect input data for line - {} , moving to next line.",line);
				}
			}
		}		
		
		return true;
	}
	
	private void createNodesAndRelationship(String leftLabel, String relationLabel, String rightLabel) {
		Node left = nodeManager.getOrCreateNode(leftLabel);
		Node right = nodeManager.getOrCreateNode(rightLabel);
		
		Relationship relationship = relationshipManager.lookupRelationshipByKey(leftLabel, relationLabel, rightLabel);
		
		if(relationship==null) {
			relationshipManager.createRelationship(relationLabel, left, right);
		}else {
			logger.info("Relationship {} already exists , skipping creating new relationship for same node",leftLabel + "->" + relationLabel + "->" + rightLabel);
		}
		
	}

	/**
	 * Method provides the tuple <IN_NODE, OUT_NODE> for
	 * all the possible nodes which are present with 
	 * this relationship
	 * @param relationship - pass label for relationship
	 * @return
	 */
	public List<Deque<Node>> fetchAllMatchingNodePairsForRelation(String relationship){
		
		List<Deque<Node>> queryResult = new ArrayList<>();
		List<Relationship> relList = relationshipManager.lookupRelationshipByLabel(relationship);
		
		if(relList==null) {
			return Collections.emptyList();
		}
		
		for(Relationship rel: relList) {
			Deque<Node> nodeTuple = new ArrayDeque<>();
			nodeTuple.add(rel.getLeftNode());
			nodeTuple.add(rel.getRightNode());
			queryResult.add(nodeTuple);
		}
		
		return queryResult;
		
	}
	
	/**
	 * Provided the relationship and right node , 
	 * the  method will provide all the nodes from
	 * left side of relationship
	 * @param relationship - label for relationship
	 * @param rightNode - label for right node
	 * @return
	 */
	public List<Deque<Node>> fetchAllMatchingLeftNodes(String relationship,String rightNode){
		List<Deque<Node>> queryResult = new ArrayList<>();
		Node node = nodeManager.lookupNodeByLabel(rightNode);
		
		if(node == null) {
			return Collections.emptyList();
		}
		
		List<Relationship> relList = node.getIncomingRelatioships().get(relationship);
		
		if(relList==null) {
			return Collections.emptyList();
		}
		
		for(Relationship rel : relList) {
			Deque<Node> leftNode = new ArrayDeque<>();
			leftNode.add(rel.getLeftNode());			
			queryResult.add(leftNode);			
		}
		
		return queryResult;
	}
	
	/**
	 * Provided the relationship and left node , 
	 * the  method will provide all the nodes from
	 * right side of relationship
	 * @param relationship - label for relationship
	 * @param leftNode - label for left node
	 * @return
	 */
	public List<Deque<Node>> fetchAllMatchingRightNodes(String leftNode,String relationship)
	{
		List<Deque<Node>> queryResult = new ArrayList<>();
		Node node = nodeManager.lookupNodeByLabel(leftNode);
		
		if(node == null) {
			return Collections.emptyList();
		}
		
		List<Relationship> relList = node.getOutgoingRelationships().get(relationship);
		
		if(relList==null) {
			return Collections.emptyList();
		}
		
		for(Relationship rel : relList) {
			Deque<Node> rightNode = new ArrayDeque<>();
			rightNode.add(rel.getRightNode());			
			queryResult.add(rightNode);			
		}
		
		return queryResult;
	}
	
	public List<Node> fetchNodesByRegex(String regex){
		return nodeManager.lookupNodesByRegex(regex);
	}
	
	public List<Relationship> fetchRelationshipByRegex(String regex){
		return relationshipManager.lookupRelationshipByRegex(regex);
	}
	
	public void clearDataStore() {
		nodeManager.clearStore();
		relationshipManager.cleanStore();
	}

}
