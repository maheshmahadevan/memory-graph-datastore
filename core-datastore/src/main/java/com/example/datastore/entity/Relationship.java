package com.example.datastore.entity;

import com.example.datastore.util.IdGenUtility;

public class Relationship implements DbEntity{
	
	private final int _id;
	private final String label;
	
	private final Node left;
	private final Node right;
	
	private volatile int _hashcode = 0;
	
	private Relationship(String label,Node left, Node right) {
		this._id = IdGenUtility.INSTANCE.getNextId();
		this.label = label;
		this.left = left;
		this.right = right;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Node getLeftNode() {
		return left;
	}
	
	public Node getRightNode() {
		return right;
	}
	
	/**
	 * leftNode --> label --> rightNode  is key since
	 * we can have multiple relationship ith same label
	 */
	public String getKey() {
		return left + "->" + label + "->" + right;
	}
	
	//TODO - check permissions
	/**
	 * 
	 * Function to add Relationship. Avoid using this ,
	 * instead @see RelationshipManager.createRelationship
	 * If using this directly , need to take care of adding
	 * Relationships to Node directly
	 * 
	 * @param label
	 * @param left
	 * @param right
	 * @return Relationship
	 */
	public static Relationship createRelationship(String label, Node left , Node right) {
		return new Relationship(label,left,right);
	}

	@Override
	public int hashCode() {
		
		if(_hashcode == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + _id;
			result = prime * result + ((left == null) ? 0 : left.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + ((right == null) ? 0 : right.hashCode());
			_hashcode = result;
		}
		
		return _hashcode;
		
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relationship other = (Relationship) obj;
		if (_id != other._id)
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Relationship [_id=" + _id + ", left=" + left + "--> label=" + label + "--> right=" + right
				+ "]";
	}
	
	

}
