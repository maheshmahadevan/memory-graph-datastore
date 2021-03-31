package com.example.datastore.query;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.example.datastore.exception.InvalidPredicateException;
import com.example.datastore.exception.QueryParsingException;

public class QueryParserTest {

	@Test
	public void testValidQueries() {
		
			
		try {
			Query query = Query.createQuery("select $person1,$person2 where $person1 {isFriendOf} $person2");
			QueryParser parser = new QueryParser(query);		
			parser.parse();
			
			Query query2 = Query.createQuery("select $person Where  {Anish} {isFriendOf} $person,$person {studiedAt} {IITB} ");
			QueryParser parser2 = new QueryParser(query2);
			parser2.parse();
			
			Query query3 = Query.createQuery(" select $city WHERE $person {belongsTo} $city");
			QueryParser parser3 = new QueryParser(query3);
			parser3.parse();
				
			
			Query query4 = Query.createQuery("SELECT $person1,$person2 where {Neha} {isFriendOf} $person1,$person1 " + 
					"{isFriendOf} $person2,$person2 {staysIn} {Mumbai}");
			QueryParser parser4 = new QueryParser(query4);
			parser4.parse();
	
		} catch (QueryParsingException | InvalidPredicateException e) {
			fail("Not expecting to throw exception");
		}
	}
	
	@Test(expected = QueryParsingException.class)
	public void testInvalidQuery_1() throws QueryParsingException, InvalidPredicateException {				
		Query query = Query.createQuery("select $person1 $person1 {isFriendOf} $person2");		
		
	}
	@Test(expected = QueryParsingException.class)
	public void testInvalidQuery_2() throws QueryParsingException, InvalidPredicateException {
		Query query = Query.createQuery("$person1 where $person1 {isFriendOf} $person2");
					
	}
	
	@Test(expected = QueryParsingException.class)
	public void testInvalidQuery_3() throws QueryParsingException, InvalidPredicateException {
				
		Query query = Query.createQuery("select $person1 $person2 where $person1 {isFriendOf} $person2");		
		
	}
	
	
	
	

}
