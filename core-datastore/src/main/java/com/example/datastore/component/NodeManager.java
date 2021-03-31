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

/**
 * 
 * @author mahesh
 * Class is responsible for storing all the 
 * nodes in the datastore along with services/
 * methods on the nodes
 */
public class NodeManager {
	
	private static Logger logger = LoggerFactory.getLogger(NodeManager.class);
	
	public static volatile NodeManager instance = null;
	
	private Map<String,Node> nodeStore = null;
	
	private NodeManager() {
		nodeStore = new HashMap<>();
		
	}
	
	public static NodeManager getInstance() {
		
		if(instance == null) {
			synchronized(NodeManager.class) {
				if(instance == null) {
					instance = new NodeManager();
				}
			}
		}
		
		return instance;
		
	}
	
	public Node getOrCreateNode(String label) {
		
		Node node = nodeStore.get(label);
		
		if(node == null) {
			logger.info("No node found with label {} , creating new node",label);
			node = Node.createNode(label);
			nodeStore.put(node.getKey(), node);
		}
				
		return node;
	}
	
	public void addRelationship(Node node,Relationship rel,Direction direction) {
		
		if(direction == Direction.RELATION_IN){
			List<Relationship> incomingRels = node.getIncomingRelatioships().get(rel.getLabel());
			if(incomingRels==null) {
				incomingRels = new ArrayList<>();
				node.getIncomingRelatioships().put(rel.getLabel(), incomingRels);
			}
			incomingRels.add(rel);
		}else if(direction == Direction.RELATION_OUT){
			List<Relationship> outgoingRels =node.getOutgoingRelationships().get(rel.getLabel());
			if(outgoingRels == null) {
				outgoingRels = new ArrayList<>();
				node.getOutgoingRelationships().put(rel.getLabel(), outgoingRels);
			}
			outgoingRels.add(rel);
		}else {
			throw new IllegalArgumentException("No matching direction to add relationship to node");
		}
		
	}
	
	public Node lookupNodeByLabel(String label) {
		return nodeStore.get(label);
	}

	public List<Node> lookupNodesByRegex(String regex){
		List<Node> nodes = new ArrayList<>();
		for(Entry<String,Node> entry : nodeStore.entrySet()) {
			if(entry.getKey().matches(regex)) {
				nodes.add(entry.getValue());
			}
		}
		
		return nodes;
	}
	
	public void clearStore() {
		nodeStore.clear();
	}
}
