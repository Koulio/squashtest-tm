/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.csp.tm.internal.service.deletion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.library.structures.GraphNode;
import org.squashtest.csp.tm.domain.library.structures.LibraryGraph;



public class LockedFileInferenceGraph extends LibraryGraph<LockedFileInferenceGraph.Node> {

	private List<Long> candidatesToDeletion;
	
	public void setCandidatesToDeletion(List<Long> candidates){
		this.candidatesToDeletion=candidates;
	}
	
	
	public void build(List<Object[]> rawData){
		// first phase : build the thing
		for (Object[] data : rawData) {
			Node parent = new Node((Long) data[0],(String) data[1], false);
			Node child = new Node((Long) data[2], (String) data[3], false);
			addNodes(parent, child);
		}		
	}

	public void reset(){
		for (Node node : getNodes()){
			node.setDeletable(false);
			node.setCounter(0);
		}
	}
	

	/*
	 * 
	 * requires the graph to be built.
	 * 
	 * That method will check that if a test case is candidate to deletion, 
	 * all test cases calling it directly or indirectly will be deleted along.
	 * If so the test case is deletable, else it is locked. 
	 * 
	 * algorithm
	 * 
	 * - init : all nodes are non deletable
	 * 
	 * - ready list = orphan nodes that are to be deleted 
	 * 
	 * - for all nodes in ready list, node.deletable := true
	 * 
	 * - while ready list is not empty
	 * 
	 * 		- node := first of ready list
	 * 
	 * 		- for child in node.children 
	 * 			child.parentProcessed +=1 
	 * 				if child.parentDeletableCount = child.parent.size and child should be deleted
	 * 					child.deletable := true 
	 * 					readyList += child
	 * 
	 * - remove node from ready list - done
	 * 
	 */
	protected void resolveLockedFiles() {

		LinkedList<Node> readyList = new LinkedList<Node>();

		reset();

		for (Node orphan : getOrphans()) {
			if (candidatesToDeletion.contains(orphan.getKey())) {
				orphan.setDeletable(true);
				readyList.add(orphan);
			}
		}

		while (!readyList.isEmpty()) {

			Node currentNode = readyList.getFirst();

			for (Node child : currentNode.getChildren()) {
				
				child.increaseCounter();

				boolean childShouldBeDeleted = candidatesToDeletion.contains(child.getKey());

				if ((child.areAllParentsDeletable()) && (childShouldBeDeleted)) {
					child.setDeletable(true);
					readyList.add(child);
				}
			}

			readyList.removeFirst();
		}

	}

	
	/**
	 * Once the locks are resolved, returns all the nodes that are locked.
	 * 
	 * @return
	 */
	public List<Node> collectLockedNodes(){
		List<Node> lockedNodes = new ArrayList<Node>();
		
		for (Node node : getNodes() ){
			if (! node.isDeletable()){
				lockedNodes.add(node);
			}
		}
		
		return lockedNodes;
	}

	
	
	/**
	 * Once the locks are resolved, will return the list of nodes 
	 * that are both locked and candidates.
	 * 
	 * @return
	 */
	public List<Node> collectLockedCandidates(){
		
		List<Node> lockedCandidates = new ArrayList<Node>();
		
		for (Node node : getNodes()){
			if ((! node.isDeletable()) && (candidatesToDeletion.contains(node.getKey())) ){
				lockedCandidates.add(node);
			}
		}
		
		return lockedCandidates;		
	}
	
	
	/**
	 * Once the locks are resolved, return the list of nodes
	 * that are locked and were not candidates.
	 * 
	 *  They are most likely locking other nodes. 
	 * 
	 * @return
	 */
	public List<Node> collectLockers(){
		
		List<Node> lockers = new ArrayList<Node>();
		
		for (Node node : getNodes()){
			if ((! node.isDeletable()) && (! candidatesToDeletion.contains(node.getKey())) ){
				lockers.add(node);
			}
		}
		
		return lockers;	
	}
	
	
	
	/**
	 * Once the locks are resolved, will return the nodes that are eventually deletable.
	 * 
	 * Note that by design, a node is deletable only if it was a candidate 
	 * to deletion (see {@link #resolveLockedFiles()} for the reasons of that statement).  
	 * 
	 * @return
	 */
	public List<Node> collectDeletableNodes(){
		List<Node> deletableNodes = new ArrayList<Node>();
		
		for (Node node : getNodes() ){
			if (node.isDeletable()){
				deletableNodes.add(node);
			}
		}
		
		return deletableNodes;		
	}
	

	
	public boolean hasLockedFiles(){
		for(Node node : getNodes()){
			if (! node.isDeletable()){
				return true;
			}
		}
		return false;
	}
		
	
	class Node extends GraphNode<Node>{
		
		private Boolean deletable=true;
		private Integer parentDeletableCount=0;
		private String name;
		
		public Boolean isDeletable() {
			return deletable;
		}
		
		public void setDeletable(Boolean isDeletable) {
			this.deletable = isDeletable;
		}
		
		public void increaseCounter(){
			parentDeletableCount++;
		}
		
		public Integer getCounter() {			
			return parentDeletableCount;
		}
		
		public void setCounter(Integer parentDeletableCount) {
			this.parentDeletableCount = parentDeletableCount;
		}
		
		public boolean areAllParentsDeletable(){
			return (parentDeletableCount == getParents().size());
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Node(){
			parentDeletableCount=0;
		}
		
		public Node(Long key){
			super(key);
		}

		public Node(Long key, String name){
			this(key);
			this.name=name;
		}
		
		public Node(Long key, String name, Boolean deletable){
			this(key, name);
			this.deletable=deletable;
		}
		
	}
}
