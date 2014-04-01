/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;


public class LockedFileInferenceGraph extends
		LibraryGraph<NamedReference, LockedFileInferenceGraph.Node> {

	private List<Long> candidatesToDeletion;

	public void init(LibraryGraph<NamedReference, SimpleNode<NamedReference>> initialGraph){
		
		LinkedList<SimpleNode<NamedReference>> processing = 
				new LinkedList<SimpleNode<NamedReference>>(initialGraph.getOrphans());
		
		Set<SimpleNode<NamedReference>> processed = new HashSet<SimpleNode<NamedReference>>();
		
		while (! processing.isEmpty()){
			
			SimpleNode<NamedReference> current = processing.pop();
			Node newParent = new Node(current);
			
			for (SimpleNode<NamedReference> child : current.getChildren()){

				addEdge(newParent, new Node(child));
				
				if (! processed.contains(child)){
					processing.add(child);
					processed.add(child);
				}
			}
			
			// in case the node had no children it might be useful to add itself again
			addNode(newParent);
		}
		
	}
	
	
	
	public void setCandidatesToDeletion(List<Long> candidates) {
		this.candidatesToDeletion = candidates;
	}


	public void reset() {
		for (Node node : getNodes()) {
			node.setDeletable(false);
			node.setCounter(0);
		}
	}

	/**
	 * <p>That method will check that if a test case is candidate to deletion, all
	 * test cases calling it directly or indirectly will be deleted along. If so
	 * the test case is deletable, else it is locked.</p>
	 * 
	 *  
	 * <p>Requires the graph to be built. The graph must be acyclic, of fear my wrath.</p>
	 * 

	 * 
	 * algorithm :
	 * <ol>
	 * 		<li>init : all nodes are non deletable</li>	
	 * 		<li>remaining_nodes := orphan nodes that are to be deleted</li> 
	 * 		<li>for all nodes in remaining_nodes, node.deletable := true</li>	
	 * 		<li>while remaining_nodes is not empty</li>
	 * 		<ol>
	 * 			<li>node := first of remaining_nodes</li>	
	 * 			<li>for child in node.children</li>	
	 * 			<ol>
	 * 				<li>child.parentProcessed +=1</li>
	 * 				<li>if child.parentDeletableCount = child.parent.size and child should be deleted</li>
	 * 				<ol>
	 * 					<li>child.deletable := true</li>
	 * 					<li>remaining_nodes += child</li>
	 * 				</ol>
	 * 			</ol> 
	 *			 <li>remove node from remaining_nodes</li>
	 * 		</ol>
	 * 		<li>done</li>
	 * 
	 * </ol>
	 * 
	 * <p>note : the algorithm starts from the parent nodes of the graph and explore the nodes breadth-first. So we are sure
	 * that all parent nodes will be processed before their children are (and so the algorithm works).</p>
	 * 
	 */
	protected void resolveLockedFiles() {

		LinkedList<Node> remainingNodes = new LinkedList<Node>();

		reset();

		for (Node orphan : getOrphans()) {
			if (isCandidate(orphan)) {
				orphan.setDeletable(true);
				remainingNodes.add(orphan);
			}
		}

		while (!remainingNodes.isEmpty()) {

			Node currentNode = remainingNodes.getFirst();

			for (Node child : currentNode.getChildren()) {

				child.increaseCounter();

				boolean childShouldBeDeleted = isCandidate(child);

				if ((child.areAllParentsDeletable()) && (childShouldBeDeleted)) {
					child.setDeletable(true);
					remainingNodes.add(child);
				}
			}

			remainingNodes.removeFirst();
		}

	}

	/**
	 * Once the locks are resolved, returns all the nodes that are locked.
	 * 
	 * @return
	 */
	public List<Node> collectLockedNodes() {
		List<Node> lockedNodes = new ArrayList<Node>();

		for (Node node : getNodes()) {
			if (!node.isDeletable()) {
				lockedNodes.add(node);
			}
		}

		return lockedNodes;
	}

	/**
	 * Once the locks are resolved, will return the list of nodes that are both
	 * locked and candidates.
	 * 
	 * @return
	 */
	public List<Node> collectLockedCandidates() {

		List<Node> lockedCandidates = new ArrayList<Node>();

		for (Node node : getNodes()) {
			if (! node.isDeletable() && isCandidate(node)) {
				lockedCandidates.add(node);
			}
		}

		return lockedCandidates;
	}

	/**
	 * Once the locks are resolved, return the list of nodes that are locked and
	 * were not candidates.
	 * 
	 * They are most likely locking other nodes.
	 * 
	 * @return
	 */
	public List<Node> collectLockers() {

		List<Node> lockers = new ArrayList<Node>();

		for (Node node : getNodes()) {
			if ( ! node.isDeletable() && ! isCandidate(node)) {
				lockers.add(node);
			}
		}

		return lockers;
	}

	/**
	 * Once the locks are resolved, will return the nodes that are eventually
	 * deletable.
	 * 
	 * Note that by design, a node is deletable only if it was a candidate to
	 * deletion (see {@link #resolveLockedFiles()} for the reasons of that
	 * statement).
	 * 
	 * @return
	 */
	public List<Node> collectDeletableNodes() {
		List<Node> deletableNodes = new ArrayList<Node>();

		for (Node node : getNodes()) {
			if (node.isDeletable()) {
				deletableNodes.add(node);
			}
		}

		return deletableNodes;
	}

	public boolean hasLockedFiles() {
		for (Node node : getNodes()) {
			if (!node.isDeletable()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCandidate (Node node){
		return candidatesToDeletion.contains(node.getKey().getId());
	}

	static class Node extends GraphNode<NamedReference, Node> {

		private Boolean deletable = true;
		private Integer parentDeletableCount = 0;
		
		public Node(NamedReference key){
			super(key);
		}
		
		public Node(SimpleNode<NamedReference> node){
			super(node.getKey());
		}
		
		public Boolean isDeletable() {
			return deletable;
		}

		public void setDeletable(Boolean isDeletable) {
			this.deletable = isDeletable;
		}

		public void increaseCounter() {
			parentDeletableCount++;
		}

		public Integer getCounter() {
			return parentDeletableCount;
		}

		public void setCounter(Integer parentDeletableCount) {
			this.parentDeletableCount = parentDeletableCount;
		}

		public boolean areAllParentsDeletable() {
			return (parentDeletableCount == getParents().size());
		}

		public String getName() {
			return key.getName();
		}


	}
}
