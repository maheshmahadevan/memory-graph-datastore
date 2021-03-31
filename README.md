# Memory-graph-datastore
## What ?
It is simple poc of in memory graph/relational databases which maintains network between entities. This is similar to  graph databases like Neo4J. 

This POC demonstrates how to store relational data in memory and then query them using simple query language.

### Query Syntax
This implementation is making use of simple query language in form of 

`select <projections 1..n> where <predicates 1..n>` 

Predicates are of following form

`{entity} {relation} {entity}`

Parameters begin with `$` - for example `$person`

### Example Data
Here is an example of data
```
{Ann},{isFriendOf},{Bob}
{Ann},{isFriendOf},{Mike}
{Bob},{isFriendOf},{Will}
{Bob},{isFriendOf},{Sam}
{Mike},{isFriendOf},{Sam}
{Will},{worksAt},{BurgerKing}
{Will},{staysIn},{NewYork}
{Will},{studiedAt},{Columbia}
{Sam},{staysIn},{Chicago}
{Sam},{studiedAt},{Harvard}
{Sam},{worksAt},{CVS}
{Mike},{plays},{Baseball}
{Bob},{plays},{Soccer}
{Sam},{plays},{Baseball}
{Will},{plays},{Soccer}
```

### Sample Queries
```
1. select   $person1,  $person2 where   $person1 {isFriendOf}   $person2
Result:
person1,person2  
Ann,  Bob  
Ann,  Mike  
Bob,  Sam  
Bob,  Will  
Mike,  Sam  
  
2. select $person  where  {Bob}  {isFriendOf} $person, $person  {studiedAt}  {Harvard}  
Result:  
person  
Sam  
  
3. select $person1, $person2  where  {Ann}  {isFriendOf} $person1, $person1  
{isFriendOf} $person2, $person2  {staysIn}  {Chicago} 
Result: 
person1,  person2  
Bob,  Sam  
Mike,  Sam  
  
4. select $person1, $person2  where $person1  {plays}  {Soccer}, $person2  {plays}  
{Baseball}, $person1  {isFriendOf} $person2  
Result:
person1,  person2  
Bob,  Sam  
  
5.  select $person1, $person2, $person3  where $person1  {isFriendOf} $person2,  
  $person2  {isFriendOf} $person3, $person3  {worksAt}  {CVS} 
Result: 
person1,  person2,  person3  
Ann,  Bob,  Sam  
Ann,  Mike,  Sam  
  
6.  select $city where  $person2 {plays} {Soccer},$person2 {isFriendOf} $person1,$person1 {staysIn} $city
Results:
city
NewYork
Chicago 
```

## Implementation

1. Contains two maven modules 
   a. core-datastore - this contains the data structure and required components for data store application
   b. datastore-shell - this is the spring shell module which allows you to execute queries on top of data store
2. Build - mvn clean install  - on both modules in the same order as above 
3. Run - java -jar target/datastore-shell-0.0.1-SNAPSHOT.jar  - on the datastore-shell folder to start the shell
4. Logs - datastore.log in the current directory datastore-shell

SHELL EXAMPLE
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.3.RELEASE)


shell:>load initialData.dat
Data Loaded successfully.
13 nodes and 15 relationships added to the store.

shell:>execute "select $person1,$person2 where $person1 {isFriendOf} $person2"
Results:
person1,person2
Ann,Bob
Ann,Mike
Bob,Will
Bob,Sam
Mike,Sam

shell:>clearstore
Datastore cleaned successfully
0 nodes and 0 relationships present in the store.
```

### Shell Commands
1. load - to load file data to store , you can use initialData.dat present in resources folder
2. execute - run queries on existing data
3. clearstore - clear current data

### Behaviour considerations
1. The query is parsed using regex,with following assumptions
  a. Relationship only has word and no space characters i.e `[a-zA-Z_0-9]`
  b. Predicate statements only separated by spaces

2. All the predicate clauses are joined together as AND only operation

3. Projections in Predicates must be specified in the order of the join required
 For example - $person1 {relation} $person2, $person2 {relation} $person3. Here person2 is the column on which the join is performed between two predicates and hence must be mentioned in the same order.This holds true only for RELATION_ONLY predicates where both LEFT and RIGHT side of predicate relationship is a projection i.e $p1 {relationship} $p2

```
Examples of out of order predicates  - $person2 {relation} $person3, person1 {relation} $person2
```

4. In case of RELATION_ONLY predicates having no join column between two predicates in query will cause exception
Example -  person1 {relation} $person2,$person3 {relation} $person4

5. Similarly , as no 4 , in case of predicates having either one of the Nodes mentioned in the query , LEFT or RIGHT , query will fail if there is no common join column anywhere in query for the same.

```
Example - select $person1, $person2 where $person1 {plays} {Soccer}, $person2 {plays} {Baseball}
```









