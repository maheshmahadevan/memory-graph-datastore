package com.example.datastore.integration;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.example.datastore.component.DataStoreManager;
import com.example.datastore.entity.Node;
import com.example.datastore.entity.Relationship;
import com.example.datastore.exception.InvalidPredicateException;
import com.example.datastore.exception.PredicateMergeException;
import com.example.datastore.exception.QueryParsingException;
import com.example.datastore.query.Query;

public class BehaviorTests {
	
	DataStoreManager datastoreManager = null;
	
	@Before
	public void setup() {
		datastoreManager = DataStoreManager.getInstance();
	}
	
	@Test
	public void testLoadData() throws IOException {
		datastoreManager.clearDataStore();
		String filePath = getClass().getClassLoader().getResource("initialData.dat").getPath();
		datastoreManager.loadData(filePath);
		
		List<Node> nodes = datastoreManager.fetchNodesByRegex("\\w+");
		
				
		assertEquals(13, nodes.size());
		
		List<Relationship> relations = datastoreManager.fetchRelationshipByRegex("\\w+");
		
				
		assertEquals(15, relations.size());
		
	}
	
	@Test
	public void testQueriesPositive() throws Exception {
		datastoreManager.clearDataStore();
		String filePath = getClass().getClassLoader().getResource("initialData.dat").getPath();
		datastoreManager.loadData(filePath);
		
		Query query = Query.createQuery("select $person1,$person2 where $person1 {isFriendOf} $person2");
		query.execute();		
		assertEquals(5, query.getResultSet().getRows().size());
		
		
		Query query1 = Query.createQuery("select $person where {Anish} {isFriendOf} $person, $person {studiedAt} {IITB}");
		query1.execute();
		assertEquals(1, query1.getResultSet().getRows().size());
		assertEquals("Sachin",query1.getResultSet().getRows().get(0).getFirst().getLabel());
		
		Query query2 = Query.createQuery("select $person1, $person2 where {Neha} {isFriendOf} $person1, $person1 {isFriendOf} $person2, $person2 {staysIn} {Mumbai}");
		query2.execute();
		assertEquals(2, query2.getResultSet().getRows().size());
		assertEquals("Sachin",query2.getResultSet().getRows().get(0).getLast().getLabel());
		
		Query query3 = Query.createQuery("select $person1,$person2 where $person1 {plays} {Chess}, $person2 {plays} {Cricket}, $person1 {isFriendOf} $person2");
		query3.execute();
		assertEquals(1, query3.getResultSet().getRows().size());
		assertEquals("Anish",query3.getResultSet().getRows().get(0).getFirst().getLabel());
		
		
		Query query4 = Query.createQuery("select $person1,$person2,$person3 where $person1 {isFriendOf} $person2,$person2 {isFriendOf} $person3,$person3 {worksAt} {Cisco}");
		query4.execute();
		assertEquals(2, query4.getResultSet().getRows().size());
		assertEquals("Sachin",query4.getResultSet().getRows().get(0).getLast().getLabel());
		
		Query query5 = Query.createQuery("select $city where  $person2 {plays} {Chess},$person2 {isFriendOf} $person1,$person1 {staysIn} $city");
		query5.execute();
		assertEquals(2, query5.getResultSet().getRows().size());		
		
		
	}
	
	@Test(expected = PredicateMergeException.class)
	public void testExpectException() throws QueryParsingException, InvalidPredicateException, PredicateMergeException {
		 Query query = Query.createQuery("select $person1,$person2 where $person1 {isFriendOf} $person2,$person1 {isFriendOf} $person3");
		 query.execute();
		 
	}
	
	@Test
	public void testOnlyPrintingRequiredProjections() throws QueryParsingException, InvalidPredicateException, PredicateMergeException {
		Query query = Query.createQuery("select $city where  $person2 {plays} {Chess},$person2 {isFriendOf} $person1,$person1 {staysIn} $city");
		query.execute();
		
		assertFalse(query.resultSetToString().contains("person2"));
		assertFalse(query.resultSetToString().contains("person1"));
		assertTrue(query.resultSetToString().contains("city"));
		
	}
	
	
	@Test
	public void testEmptyResults() throws Exception {
		datastoreManager.clearDataStore();
				
		Query query = Query.createQuery("select $person1,$person2 where $person1 {isFriendOf} $person2");
		query.execute();		
		assertEquals(0, query.getResultSet().getRows().size());
	}

}
