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
 * This is an abstract popup. 
 * Must be supplied the following options :
 * 
 * a tree, 
 * a permissions-rules, 
 * an implementation of getSimulXhr (using option extender from formDialog),
 * an implementation of getConfirmXhr (using option extender from formDialog),
 * an implementation of deletionSuccess (using option extender from formDialog)
 * 
 */
define(['jquery', 'jquery.squash.formdialog'], function(){

	
	if (($.squash !== undefined) && ($.squash.deletenodeDialog !== undefined)) {
		// plugin already loaded
		return;
	}
	
	$.widget("squash.deletenodeDialog", $.squash.formDialog, {

		options : {
			tree : null,
			rules : null
		},
		
		getSimulXhr : function(nodes){
			throw "must be overriden"
		},
		
		fillDetails : function(xhrs){
			console.log(xhrs);
			var detailHtml = "";
			var i=0,len = xhrs[0].length;
			for (i=0;i<len;i++){
				var resp = JSON.parse(xhrs[i].responseText);
				detailHtml += resp.message;			
			}
			if (detailHtml.length > 0){
				this.element.find('delete-node-dialog-details').removeClass('not-displayed').html(data);
				this.showContent('confirm');
			}
			else{
				this.element.find('delete-node-dialog-details').addClass('not-displayed');
			}			
		},
		
		_findPrevNode : function(nodes){
			var tree = this.options.tree;
			if (nodes.length==0) return nodes;
			var ids = nodes.all('getResId');
			var loopnode = nodes.first().treeNode().getAncestors();
			
			var oknode= tree.find(':library').filter(':first');
			loopnode.each(function(){
				var $this = $(this), $thisid = $this.attr('resid');
				if ($this.is(':library') || $.inArray($thisid, ids)== -1){
					oknode = $this.treeNode();
					//break;
				}
			});
			
			return oknode;
			
		},
		
		simulateDeletion : function(){
			var self = this;
			var tree = this.options.tree;
			var rules = this.options.rules;
			
			//first, check that the operation is allowed.
			this.showContent("pleasewait");
			
			var nodes = tree.jstree('get_selected');
			this.uiDialog.data('selected-nodes', nodes);
			
			if (! rules.canDelete(nodes)){
				this.deletenodeDialog('showContent', 'rejected');
				return;
			}
			
			//else we can proceed.
			var xhrs = this.getSimulXhr(nodes) 
			$.when ( xhrs )			
			.done(function(responses){		
				self.fillDetails(responses);
			})
			.fail(function(){
				self.showContent('reject');
			});
		},

		getConfirmXhr : function(nodes){
			throw "must be overriden";
		},
		
		deletionSuccess : function(){
			throw "must be overriden";
		},
		
		performDeletion : function(){
			var tree = this.options.tree;
			var nodes = this.uiDialog.data('selected-nodes');		
			var newSelection = this._findPrevNode(nodes);
			
			nodes.all('deselect');
			newSelection.select();
			
			this.showContent('pleasewait');

			var xhrs = this.getConfirmXhr(nodes);
			$.when(xhrs)
			.done($.proxy(this.deletionSuccess, this));
			
		}
		
	});


});