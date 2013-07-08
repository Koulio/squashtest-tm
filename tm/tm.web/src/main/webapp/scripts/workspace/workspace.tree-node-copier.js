/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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



/*
 * create a singleton instance if needed, 
 * then returns it to the client. 
 * 
 * 
 */

define(['jquery', 'squash.translator'], function($, translator){


	squashtm = squashtm || {}
	squashtm.workspace = squashtm.workspace || {};
	
	if (squashtm.workspace.treenodecopier !== undefined){
		return squashtm.workspace.treenodecopier;
	}
	else{
		squashtm.workspace.treenodecopier = new TreeNodeCopier();
		return squashtm.workspace.treenodecopier;
	}
	
	this.message = function(messageName){	
		
		if (this._messages === undefined){
			
			this._messages = translator.get({
				warnCopyToDifferentLibrary : 'message.warnCopyToDifferentLibrary'					
			});		

		}
		
		return this._messages[messageName];
	}
			
	
	
	function TreeNodeCopier() {
		
		
		this.tree = $("#tree");	//default that should work 99% of the time.
		
		this.setTree = function(tree){
			this.tree = tree;
		}
	
		// ***************** private methods *********************

		var displayError = function() {
			if (!arguments.length) {
				squashtm.notification.showInfo(this.message['errMessage']);
			} else {
				squashtm.notification.showInfo(arguments[0]);
			}
		};

		var reset = function() {
			$.cookie('squash-copy-nodes', null);
		};

		var retrieve = function() {
			var data = $.cookie('squash-copy-nodes');
			return JSON.parse(data);
		};


		var store = function(nodesData, librariesIds) {

			var data = {
				libraries : librariesIds,
				nodes : nodesData
			};

			var jsonData = JSON.stringify(data);

			$.cookie('squash-copy-nodes', jsonData);
		};


		// ****************** public methods **********************
		
		
		// public version of 'retrieve'
		this.bufferedNodes = function(){
			var data = retrieve();
			if (data == null){
				return $();
			}
			else{
				return this.tree.jstree('findNodes', data.nodes);
			}
		}

		//assumes that all checks are green according to the rules of this workspace.
		this.copyNodesToCookie = function() {

			reset();

			var nodes = this.tree.jstree('get_selected');

			var nodesData = nodes.toData();
			var libIds = [];
			nodes.getLibrary().each(function() {
				libIds.push($(this).attr("id"));
			});
			
			store(nodesData, libIds);
		};



		this.preparePasteData = function(nodes, target) {

			var destinationType;
			var url;

			// todo : makes something better if we can refractor the whole service
			// in depth one day.
			switch (target.getDomType()) {
			case "drive":
				destinationType = "library";
				break;

			case "folder":
				destinationType = "folder";
				break;

			case "file":
				destinationType = "campaign";
				break;

			case "resource":
				destinationType = "iteration";
				break;
			default:
				destinationType = "azeporiapzeorj"; // should not happen if this.mayPaste() did its job.
			}

			// here we mimick the move_object used by tree.moveNode, defined in
			// jquery.squashtm.jstree.ext.js.
			var pasteData = {
				inst : this.tree,
				sendData : {
					"object-ids" : nodes.all('getResId'),
					"destination-id" : target.attr('resid'),
					"destination-type" : destinationType
				},
				newParent : target,
				url : nodes.getCopyUrl()
			};

			// another special delivery for iterations (also should be refractored)
			if (target.is(':campaign')) {
				pasteData.sendData["next-iteration-number"] = target.getChildren().length;
			}

			return pasteData;

		};

		
		//assumes that the operation is ok according to the rules of this workspace.
		this.pasteNodesFromCookie = function() {
			var self = this;
			var tree = this.tree;


			var data = retrieve();
			var target = tree.jstree('get_selected');
			// warn user if not same libraries
			var targetLib = target.getLibrary().getDomId();
			var destLibs = data.libraries;
			var sameLib = true;
			for ( var i = 0; i < destLibs.length; i++) {
				if (targetLib != destLibs[i]) {
					sameLib = false;
					break;
				}
			}
			if (!sameLib) {
				oneShotConfirm('Info', this.message('warnCopyToDifferentLibrary'),
						squashtm.message.confirm, squashtm.message.cancel)
						.done(function() {
							doPasteNodesFromCookies.call(self, tree, target, data);
						})
						.fail(function() {
					data.inst.refresh();
				});
			} else {
				doPasteNodesFromCookies.call(self, tree, target, data);
			}

		};

		var doPasteNodesFromCookies = function(tree, target, data) {
			var nodes = tree.jstree('find_nodes', data.nodes);

			target.open();

			var pasteData = this.preparePasteData(nodes, target);

			// now we can proceed
			squashtm.tree.copyNode(pasteData, pasteData.url).fail(function(json) {
				tree.refresh();
			});
		};

	}
	

});
