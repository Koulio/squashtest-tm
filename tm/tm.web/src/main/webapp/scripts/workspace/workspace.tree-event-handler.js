/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 *
 * That class will not handle the usual dom event (in particular the .jstree namespaced events). It will rather handle
 * the messages between the contextual content and the workspace tree. Squash uses messages - or events - defined in Events.js
 * in this same package directory.
 */

define([ 'jquery', 'tree', 'workspace.event-bus' ], function($, tree, eventBus) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.treeeventhandler !== undefined) {
		return squashtm.workspace.treeeventhandler;
	} else {
		squashtm.workspace.treeeventhandler = new TreeEventHandler();
		return squashtm.workspace.treeeventhandler;
	}

	function TreeEventHandler() {

		// Lazily initialized, see below
		this.tree = null;

		this.setTree = function(tree) {
			this.tree = tree;
		};

		this.getTree = function() {
			if (!this.tree) {
				this.tree = tree.get().jstree('get_instance');
			}
			return this.tree;
		};
		
		var self = this;
		
		// ********* generic attribute changed handler ************
		 
		/* Note that this approach might not be sufficient for 
		 * all cases, for instance a new name and a new reference 
		 * because of their special display.
		 */
		eventBus.on('node.attribute-changed', function(evt, data){
			var node = self.getTree().findNodes(data.identity);
			if (node.length !== 0){
				node.setAttr(data.attribute, data.value);			
			}
		});
		
		// ********** other handlers ****************
		
		eventBus.on('node.rename', function(evt, data){
			var node = self.getTree().findNodes(data.identity);
			if (node.length !== 0){
				node.setName(data.newName);			
			}
		});
		
		eventBus.on('node.update-reference', function(evt, data){
			var node = self.getTree().findNodes(data.identity);
			if (node.length !== 0){
				node.setReference(data.newRef);			
			}
		});
		
		eventBus.on('node.update-reqCoverage', function(evt, data){
			updateEventUpdateReqCoverage(data, self.getTree());
		});
		
		eventBus.on('node.add', function(evt, data){
			updateEventAdd(data, self.getTree());	
		});

		eventBus.on('node.remove', function(evt, data){
			self.getTree().refresh_selected();
		});
		
		
		eventBus.on('tc-req-links-updated', function(evt, data){
			var tree = self.getTree();
			var openedNodes  = tree.findNodes({restype : "test-cases"});
			var openedNodesIds = _.map(openedNodes, function(item){
				return item.getAttribute('resid');
			});
			var mapIdOldReq = findMapIdOldReq(openedNodesIds, tree);
			updateCallingTestCasesNodes( tree, mapIdOldReq);
		});
		

	}

	/* *************************** update Events ********************* */
	
	// the more informations in data, the more accurate it is treated
	function updateEventAdd(data, tree) {
		
		if (data === undefined || data.parent === undefined){
			tree.refresh_selected();
			return;
		}
		
		
		var parent = tree.findNodes(data.parent);
		parent.getChildren().each(function(){
			tree.delete_node(this);
		});
		
		parent.load().done(function(){
			if (!parent.isOpen()){
				parent.open();
			}

			if (data.child){
				var child = tree.findNodes(data.child);
				if (child){
					child.select();
				}
			}
		});
		
	}


	
	function updateEventUpdateReqCoverage(data, tree) {
		var openedNodes  = tree.findNodes({restype : "test-cases"});
		var targetIds = data.targetIds;
		var openedTargetIds = $(targetIds).filter(function(index){
			var itemId = targetIds[index];
			for(var i = 0; i < openedNodes.length; i++){
				if( itemId == openedNodes[i].getAttribute('resid')){
					return true;
				}
			}
			return false;
		});
		var mapIdOldReq = findMapIdOldReq(targetIds, tree);
		

		updateCallingTestCasesNodes( tree, mapIdOldReq);
	}
	
	function findMapIdOldReq(targetIds, tree){
		var mapIdOldReq = {};
		$.each(targetIds, function(index, item){
			var treeNode = tree.findNodes({
				restype : "test-cases",
				resid : item
			});
			if (treeNode.length !== 0) {
				var oldReq = treeNode.attr('isreqcovered');
				mapIdOldReq[item] = oldReq;
			}
		});
		return mapIdOldReq;
	}
	
	//tree : the tree instance
	//mapIdOldReq : a map with key=tcId, value= actual 'isreqcovered' attribute value
	function updateCallingTestCasesNodes( tree, mapIdOldReq){
		//if a test case change it's requirements then it's calling test cases might be newly bound/unbound to requirements or might have their importance changed.
		
		var target = tree.findNodes({restype : "test-cases"});
		var nodeIds = target.map(function(index, item){ return item.getAttribute("resid");});
		$.ajax({
			url:squashtm.app.contextRoot+"/test-cases/tree-infos",
			type:"post",
			contentType: "application/json",
			data: JSON.stringify({
				openedNodesIds :  nodeIds.toArray(),
				updatedIdsAndOldReq : mapIdOldReq
			}),
			dataType: "json"
		}).then(function(testCaseTreeIconsUpdate){
			
			$.each(testCaseTreeIconsUpdate, function(key, value){
				var target2 = tree.findNodes({
					restype : "test-cases",
					resid : value.id
				});
				if (!target2 || target2.length === 0) {
					return;
				}
				if(value.isreqcovered != 'same'){
					target2.setAttr('isreqcovered', value.isreqcovered);
				}
				if(value.importance != 'same'){
					target2.setAttr('importance', value.importance);
				}
			});
		});
	}
});
