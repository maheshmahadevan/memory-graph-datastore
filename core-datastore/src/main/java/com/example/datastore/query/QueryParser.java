package com.example.datastore.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.datastore.exception.InvalidPredicateException;
import com.example.datastore.exception.QueryParsingException;

public class QueryParser {

	//Assumptions
	//1. Relationship only had word and no space characters i.e [a-zA-Z_0-9]
	//2. predicate statements only separated by space \\s
	public static final String regex = "^(?i)\\s*\\bselect\\b\\s+((\\$\\w+\\s*,)*(\\s*\\$\\w+)?)"
										+ "\\s+\\bwhere\\b\\s+(((\\s*\\$\\w+|\\{\\w+\\})\\s+\\{\\w+\\}\\s+(\\$\\w+|\\{\\w+\\})\\s*,)*"
										+ "(\\s*(\\$\\w+|\\{\\w+\\})\\s+\\{\\w+\\}\\s+(\\$\\w+|\\{\\w+\\}))?)\\s*$";
	
	private Pattern pattern;
	private Matcher matcher;
	private final Query query;
	
	
	public QueryParser(Query query) {
		this.query = query;
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(query.getStatement());
	}
	
	private boolean validateQuery()  {
		return matcher.matches();
	}
	
	public void parse() throws QueryParsingException, InvalidPredicateException {
		if(!validateQuery()) {
			throw new QueryParsingException("Query statement failed to parse.\n " 
					+ "Syntax  - select {projection1,projection2,...} where (predicate1,predicate2,...)\n "
					+ "Example - select $person where {Anish} {isFriendOf} $person, $person {studiedAt} {IITB}");
		}
		matcher.reset();
		
		String projections=null,predicates=null;
		//Get projections and predicate
		while(matcher.find()) {
			projections = matcher.group(1);
			predicates = matcher.group(4);
		}
		
		addProjections(projections);
		addPredicates(predicates);
		
	}

	private void addProjections(String projections) {
		String[] split = projections.split("(\\$|\\s*,\\s*\\$)");
		List<String> projectionList = new ArrayList<>();
		for(String s : split) {
			if(!s.isEmpty()) 
			{
				projectionList.add(s);
			}				
		}
		query.setProjections(projectionList);
		
	}

	private void addPredicates(String predicates) throws InvalidPredicateException {
		String[] split = predicates.split("\\s*,\\s*");
		List<Predicate> predicateList = new ArrayList<>();
		
		for(String s: split) {
			Predicate predicate = Predicate.validateandBuildPredicate(s);
			predicateList.add(predicate);
		}
		
		query.setPredicates(predicateList);
		
	}
	
	
}
