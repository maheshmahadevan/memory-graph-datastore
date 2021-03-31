package com.example.datastore.query;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.datastore.component.DataStoreManager;
import com.example.datastore.entity.Node;
import com.example.datastore.exception.InvalidPredicateException;

public class Predicate {
	
	private static Logger logger = LoggerFactory.getLogger(Predicate.class);
	
	enum PredicateType { RELATION_ONLY,LEFT_NODE,RIGHT_NODE }
	
	private final PredicateType predicateType;
	private final String leftNode;
	private final String relationshipLabel;
	private final String rightNode;
	
	private final Deque<String> resultsProjections;
	
	private PredicateResultSet resultSet;
	
	private Predicate(String left,String relationship,String right,PredicateType predType,Deque<String> resultsProjections) {
		this.leftNode = left;
		this.relationshipLabel = relationship;
		this.rightNode = right;
		this.predicateType = predType;
		this.resultsProjections = resultsProjections;
	}
	
	
	public PredicateType getPredicateType() {
		return predicateType;
	}


	public String getLeftNode() {
		return leftNode;
	}


	public String getRelationshipLabel() {
		return relationshipLabel;
	}


	public String getRightNode() {
		return rightNode;
	}


	public Deque<String> getResultsProjections() {
		return resultsProjections;
	}


	public PredicateResultSet getResultSet() {
		return resultSet;
	}


	
	/**
	 * Will validate the predicate string in query ,
	 * parse them into either Relation_only, left_node or
	 * right_node predicate
	 * 
	 * @param predicateString
	 * @return
	 * @throws InvalidPredicateException
	 */
	public static Predicate validateandBuildPredicate(String predicateString) throws InvalidPredicateException {
		
		PredicateType predType = null;
		String left = null,relationship = null,right = null;
		Deque<String> resultsProjections = new LinkedList<>();
		
		String[] splitString = predicateString.trim().split("\\s+");
		
		if(splitString.length != 3) {
			logger.error("Predicate size incorrect. Check the  query");
			throw new InvalidPredicateException("Predicate size incorrect");
		}
		
		if(splitString[0].startsWith("$") && splitString[2].startsWith("$")) {
			predType = PredicateType.RELATION_ONLY;
			left = splitString[0].substring(1);
			right = splitString[2].substring(1);
			resultsProjections.add(left);
			resultsProjections.add(right);
		}else if(splitString[0].startsWith("$") && splitString[2].matches("^\\{\\w+\\}$")) {
			predType = PredicateType.RIGHT_NODE;
			left = splitString[0].substring(1);
			resultsProjections.add(left);
			right = splitString[2].substring(1, splitString[2].length()-1);
		}else if(splitString[0].matches("^\\{\\w+\\}$") && splitString[2].startsWith("$")) {
			predType = PredicateType.LEFT_NODE;
			left = splitString[0].substring(1, splitString[0].length()-1);
			right = splitString[2].substring(1);
			resultsProjections.add(right);
		}else {
			throw new InvalidPredicateException("Predicate does not  match any of the predicate types");
		}
		
		relationship = splitString[1].substring(1, splitString[1].length()-1);
		Predicate predicate = new Predicate(left,relationship,right,predType,resultsProjections);
		return predicate;
		
	}
	
	/**
	 * predicate execute function to fetch the results
	 * based on the type of predicate
	 * @return
	 */
	public boolean execute() {
		
		
		List<Deque<Node>> resultList = null;
		
				
		switch(predicateType) {
			case RELATION_ONLY:
				resultList = DataStoreManager.getInstance().fetchAllMatchingNodePairsForRelation(relationshipLabel);
				break;
			
			case LEFT_NODE:
				resultList = DataStoreManager.getInstance().fetchAllMatchingRightNodes(leftNode, relationshipLabel);
				break;
				
			case RIGHT_NODE:
				resultList = DataStoreManager.getInstance().fetchAllMatchingLeftNodes(relationshipLabel, rightNode);
				break;
				
			default:
				logger.error("No matching predicateType");
				return false;		
			
		}
		
		resultSet = PredicateResultSet.generateResultSet(resultsProjections, resultList);
		return true;
		
	}

}
