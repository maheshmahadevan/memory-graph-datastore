package com.example.datastore.query;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.datastore.entity.Node;
import com.example.datastore.exception.InvalidPredicateException;
import com.example.datastore.exception.PredicateMergeException;
import com.example.datastore.exception.QueryParsingException;
import com.example.datastore.query.Predicate.PredicateType;

public class Query {
	
	private static Logger logger = LoggerFactory.getLogger(Query.class);

	private List<String> projections;
	private List<Predicate> predicates;
	
	private final String statement;
	private QueryResultSet resultSet;
	
	private Query(String stmt) {
		this.statement = stmt;
		
	}
	
	public List<String> getProjections() {
		return projections;
	}

	public void setProjections(List<String> projections) {
		this.projections = projections;
	}

	public List<Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<Predicate> predicates) {
		this.predicates = predicates;
	}

	public QueryResultSet getResultSet() {
		return resultSet;
	}

	
	public String getStatement() {
		return statement;
	}
	
	
	public static class LastNodeComparator implements Comparator<Deque<Node>>{
		
		@Override
		public int compare(Deque<Node> l1, Deque<Node> l2) {
			return l1.getLast().compareTo(l2.getLast());
		}
		
	}
	
	public static class FirstNodeComparator implements Comparator<Deque<Node>>{
		
		@Override
		public int compare(Deque<Node> l1, Deque<Node> l2) {
			return l1.getFirst().compareTo(l2.getFirst());
		}
		
	}

	public static Query createQuery(String stmt) throws QueryParsingException, InvalidPredicateException {
		
		Query query  = new Query(stmt);
		QueryParser parser = new QueryParser(query);
		
		
		parser.parse();
		return query;
		
	}
	
	public boolean execute() throws PredicateMergeException {
		
		for(Predicate p : predicates) {
			if(!p.execute()) {
				return false;
			}
		}
		
		mergePredicateResultSets(predicates);
		
		return true;
	}

	/**
	 * Method will perform sort of inner join 
	 * on the results when multiple predicate results
	 * are present in the query
	 * 
	 * Assumption is that the query text  predicates
	 * are in order of join operation 
	 * For example - $person1 {relation} $person2, $person2 {relation} $person3
	 * i.e if there are multiple RELATION_ONLY predicates in the query,
	 * the join column is always present ( no disjoint predicates) and 
	 * predicates are not specified out of order 
	 * This is an example of out of order predicates
	 * $person2 {relation} $person3, person1 {relation} $person2
	 * 
	 * As of now the predicate results are executed individually and will be
	 * sorted in following manner before join operation is performed
	 * RELATION_ONLY --> LEFT_NODE , RIGHT_NODE
	 * 
	 * If there are multiple RELATION_ONLY predicates without correct join
	 * projection ( like $person2) above , the merge will fail
	 * 
	 * Also when LEFT_NODE or RIGHT_NODE predicates are merged , it will
	 * try to merge on an empty resultSet or an existing resultSet already
	 * having this projection , for example following predicate would fail
	 * $person1 {plays} {Chess}, $person2 {plays} {Cricket} , since there is
	 * no join operation defined for $person1 and $person2 . in short 
	 * cartesian product is unsupported 
	 * 
	 * @param predicates
	 * @return
	 * @throws PredicateMergeException 
	 * @throws Exception 
	 */
	private void mergePredicateResultSets(List<Predicate> predicates) throws PredicateMergeException {
		resultSet = new QueryResultSet();
		
		//First merge RELATION_ONLY predicates
		for(Predicate p : predicates) {
			if(p.getPredicateType()==PredicateType.RELATION_ONLY) {
				mergeRelationOnlyPredicate(p.getResultSet());
			}
		}
		//MERGE LEFT_NODE and RIGHT_NODE predicates
		for(Predicate p: predicates) {
			if(p.getPredicateType() != PredicateType.RELATION_ONLY) {
				mergeNonRelationOnlyPredicate(p.getResultSet());
			}
		}
		
		
		
	}

	/**
	 * 
	 * @param predicateResultSet
	 * @throws PredicateMergeException 
	 * @throws Exception 
	 */
	private void mergeNonRelationOnlyPredicate(PredicateResultSet predicateResultSet) throws PredicateMergeException  {
		Deque<String> queryProjections =  resultSet.getProjections();
		//If this is the first of the predicate results , just add to queryResultSet
		if(queryProjections.isEmpty()) {
			//queryProjections.addAll(predicateResultSet.getProjections());
			resultSet.addProjections(predicateResultSet.getProjections());
			resultSet.setRows(predicateResultSet.getRows());
						
		}else {
			//check if the projections match to left side of column
			if(queryProjections.getFirst().equals(predicateResultSet.getProjections().getFirst())){
				
				Collections.sort(resultSet.getRows(), new FirstNodeComparator());
				Collections.sort(predicateResultSet.getRows(),new FirstNodeComparator());
				innerJoin(predicateResultSet,false,false);
			//otherwise if the projection match to right side of column	
			}else if(queryProjections.getLast().equals(predicateResultSet.getProjections().getFirst())) {

				Collections.sort(resultSet.getRows(), new LastNodeComparator());
				Collections.sort(predicateResultSet.getRows(),new FirstNodeComparator());
				innerJoin(predicateResultSet,true,false);
			}else {
				logger.error("Incorrect predicates mapping , missing join on certain projections");
				throw new PredicateMergeException("Missing join on projection -" + predicateResultSet.getProjections().getFirst()) ;
			}
		}
		
	}

	private void mergeRelationOnlyPredicate(PredicateResultSet predicateResultSet) throws PredicateMergeException {
		Deque<String> queryProjections =  resultSet.getProjections();
		//If this is the first of the predicate results , just add to queryResultSet
		if(queryProjections.isEmpty()) {
			//queryProjections.addAll(predicateResultSet.getProjections());
			resultSet.addProjections(predicateResultSet.getProjections());
			resultSet.setRows(predicateResultSet.getRows());			
			
		}else {
			
			//check if last query projection matches first predicate projection 
			if(queryProjections.getLast().equals(predicateResultSet.getProjections().getFirst())){
				
				//queryProjections.add(predicateResultSet.getProjections().getLast());
				resultSet.addSingleProjection(predicateResultSet.getProjections().getLast());
				//Sort the last column of query result set and first column of predicate result set
				// for making the join using common elements
				Collections.sort(resultSet.getRows(), new LastNodeComparator());
				Collections.sort(predicateResultSet.getRows(), new FirstNodeComparator());
				
				innerJoin(predicateResultSet,true,true);
				
			}else {
				logger.error("Relation Only predicates are mismatched , no common join projection found");
				throw new PredicateMergeException("Predicate merging exception on projection -" + predicateResultSet.getProjections().getFirst() );
			}
			
		}
		
	}

	/**
	 * performs inner join operation on query result set and predicate result set
	 * 
	 * @param predicateResultSet
	 * @param compareWithLastRow - if true will join on last row of queryResultSet, 
	 * otherwise with the first row
	 * @param mergeColumn - if true will merge the last column of the predicate row
	 * onto query row , true when merge is done with RELATION_ONLY predicate
	 */
	private void innerJoin(PredicateResultSet predicateResultSet,boolean compareWithLastRow,boolean mergeColumn) {
		List<Deque<Node>> queryRows = resultSet.getRows();
		List<Deque<Node>> predResultRows = predicateResultSet.getRows();				
				
		//create new List for the merge
		List<Deque<Node>> mergedRows = new ArrayList<>();
		
		int i=0,j=0;
		//perform match on join node and then stitch every matching
		//row together
		while(i < queryRows.size() && j < predResultRows.size()) {
			Deque<Node> queryRow = queryRows.get(i);
			Deque<Node> predRow = predResultRows.get(j);
			
			Node queryNode = compareWithLastRow ? queryRow.getLast() : queryRow.getFirst(); 
			Node predNode = predRow.getFirst();
			
			if(queryNode.compareTo(predNode) < 0){
				i++;
			}else if(queryNode.compareTo(predNode) > 0) {
				j++;
			}else {
				for(int k=j;k<predResultRows.size();k++) {
					Deque<Node> predRowForMerge = predResultRows.get(k);
					Node predNodeForMerge =  predRowForMerge.getFirst();
					
					if(queryNode.compareTo(predNodeForMerge) == 0) {
						Deque<Node> row = new ArrayDeque<>(queryRow);
						if(mergeColumn) {
							row.add(predRowForMerge.getLast());
						}
						mergedRows.add(row);
					}else {
						break;
					}
				}
				i++;
			}
			
		}
		
		resultSet.setRows(mergedRows);
		
	}
	
	public String resultSetToString() {
		
		StringBuilder sb = new StringBuilder();
		
		int[] printIndex = new int[projections.size()];
		
		for(int i=0;i<projections.size();i++) {
			printIndex[i] = resultSet.getProjectionIndex().get(projections.get(i));
		}
		
		sb.append(projections.stream().collect(Collectors.joining(",")));
		sb.append("\n");
		
		
		for(Deque<Node> row: resultSet.getRows()) {
			
			Node[] rowStr = row.toArray(new Node[0]);
			
			for(int i=0;i<printIndex.length;i++) {
				sb.append(rowStr[printIndex[i]].getLabel());
				if(i != printIndex.length - 1)
					sb.append(",");
			}
			sb.append("\n");
		}
		
		return sb.toString();
		
	}
	
	
}
