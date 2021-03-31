package com.example.datastore.shell;

import java.io.IOException;
import java.util.List;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.example.datastore.component.DataStoreManager;
import com.example.datastore.entity.Node;
import com.example.datastore.entity.Relationship;
import com.example.datastore.exception.InvalidPredicateException;
import com.example.datastore.exception.PredicateMergeException;
import com.example.datastore.exception.QueryParsingException;
import com.example.datastore.query.Query;

@ShellComponent
public class DatastoreCommands {
	
	DataStoreManager datastoreManager;
	
	public DatastoreCommands() {
		datastoreManager = DataStoreManager.getInstance();
	}
	
	@ShellMethod("Load data from a given file path location.Usage - load <location_of_file>")
	public String load(String filePath) {
		try {
			datastoreManager.loadData(filePath);
			
			List<Node> nodes = datastoreManager.fetchNodesByRegex("\\w+");
			List<Relationship> relations = datastoreManager.fetchRelationshipByRegex("\\w+");
			
			return "Data Loaded successfully." + "\n"
					+ nodes.size() + " nodes and " + relations.size() + " relationships added to the store."  ;
		} catch (IOException e) {
			return "Failed to Load Data from file " + filePath + "\n"
					+ "Cause - " + e.getMessage();
		}
	}
	
	@ShellMethod("Executes datastore queries.Usage - execute \"select {$projection1,projection2,...} where (predicate1,predicate2,...) \"")
	public String execute(String queryStmt) {
		
			try {
				Query query = Query.createQuery(queryStmt);
				query.execute();
				
				return "Results:\n" + query.resultSetToString();
				
				
			} catch (QueryParsingException  e) {
				return "Failure while parsing Query, check the query syntax again " + e.getMessage();
			} catch (PredicateMergeException e) {
				return "Failure during merging the result sets, check the query syntax."  + e.getMessage();
			} catch (InvalidPredicateException e) {
				return "Failure to create Predicate, check the query syntax. " + e.getMessage();
			}		
		
	}
	
	@ShellMethod("Clears all the current store data")
	public String clearstore() {
		datastoreManager.clearDataStore();
		List<Node> nodes = datastoreManager.fetchNodesByRegex("\\w+");
		List<Relationship> relations = datastoreManager.fetchRelationshipByRegex("\\w+");
		
		return "Datastore cleaned successfully" + "\n"
		+ nodes.size() + " nodes and " + relations.size() + " relationships present in the store."  ;
	}
	
}
