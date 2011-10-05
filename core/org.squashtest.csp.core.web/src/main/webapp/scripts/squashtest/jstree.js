/*
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
/**
 * Common functions for JsTree manipulation
 * 
 * @author Gregory Fouquet
 */

 
/**
 *
 * That function clean the contextual content from its temporary widgets.
 * The goal is to prevent undesired interactions between page controls.
 *
 * @param targetSelector 
 *			a jQuery selector being the handle to the contextual content div.
 */




(function($){
	/*
	
		Override of jstree.dnd.dnd_show. We want it to always target "inside" when the target is a container.
	
		For that purpose we need to modify the "marker". The marker represents the position where the node will be inserted, both graphically and internally. The marker can be set to 
		3 positions : before, inside and after. 
		
		The marker can be set to one of these positions if the node being browsed supports it. Let's call it the "drop profile" of the node. That profile is held by the this.data.dnd object.
		
			- For libraries (aka drives) and folders, the drop profile is true, true, true.
			- For leaves, the profile is true, false, true.
		
		The drop profile is unfortunately not configurable. Also, comparing the drop profile is the only way to differenciate a leaf from the reast.
		
		The present function will identify the hovered node and return the following marker position :
			- for leaves -> no modification
			- for folders, libraries -> force 'inside'.
	
	*/

	var overridenM;


	$.jstree._fn.dnd_show = function(){
				
			//that variable in the vanilla tree is part of the closure context we cannot access here, so we must fetch it by other means.
			if (overridenM===undefined){
				overridenM = $("div#jstree-marker");
			}	
			
			var o = ["before","inside","after"],
			r = false,
			rtl = this._get_settings().core.rtl,
			pos;		
			
			if(this.data.dnd.w < this.data.core.li_height/3) { o = ["before","inside","after"]; }
			else if(this.data.dnd.w <= this.data.core.li_height*2/3) {
				o = this.data.dnd.w < this.data.core.li_height/2 ? ["inside","before","after"] : ["inside","after","before"];
			}
			else { o = ["after","inside","before"]; }
			$.each(o, $.proxy(function (i, val) { 
				if(this.data.dnd[val]) {
					$.vakata.dnd.helper.children("ins").attr("class","jstree-ok");
					r = val;
					return false;
				}
			}, this));
			
			
			if(r === false) { 
				$.vakata.dnd.helper.children("ins").attr("class","jstree-invalid"); 
			}
			
			//here we override the function. if the profile matches the one of a container, we force r to "inside"
			if (this.data.dnd.before && this.data.dnd.inside && this.data.dnd.after){
				r = "inside";
			}
			
			pos = rtl ? (this.data.dnd.off.right - 18) : (this.data.dnd.off.left + 10);

			switch(r) {
				case "before":
					overridenM.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top - 6) + "px" }).show();
					break;
				case "after":
					overridenM.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height - 7) + "px" }).show();
					break;
				case "inside":
					overridenM.css({ "left" : pos + ( rtl ? -4 : 4) + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height/2 - 5) + "px" }).show();
					break;
				default:
					overridenM.hide();
					break;
			}
			return r; 	
			
	}
	

})(jQuery);





function clearContextualContent(targetSelector){
	$('.is-contextual').each(function(){
		//todo : kill the damn ckeditor instances				
		$(this).dialog("destroy").remove(); 
	});
	$(targetSelector).empty();		
}

/* ***************************  post new nodes operations ********************************************** */
/**
 * Post new contents to the url determined by the selected node of a tree and
 * creates a new node with returned JSON data.
 * 
 * @param treeId
 *            html id of the tree
 * @param contentDiscriminator
 *            discriminator to append to post url (determines content to be
 *            created)
 * @param postParameters
 *            map of post params
 */
function postNewTreeContent(treeId, contentDiscriminator, postParameters) {

	/* **************** variables init ****************** */

	var tree = $('#' + treeId);
	var newNode = null;
	var url = tree.data('selectedNodeContentUrl') + '/' + contentDiscriminator;
	var currentNode = tree.jstree("get_selected");
	
	var isOpen = tree.jstree('is_open', currentNode);
	
	/* ***************** function init ******************** */

	var openNode = function(){
		var defer = $.Deferred();
		tree.jstree('open_node', currentNode, defer.resolve);
		return defer.promise();
	}
	
	
	var postNode = function(){
		var defer = $.Deferred();
		return $.ajax({
			url : url,
			data : postParameters,
			type : 'POST',
			dataType : 'json',
			success : defer.resolve,
			contentType: "application/x-www-form-urlencoded;charset=UTF-8"
		});
		return defer.promise();
	}
	
	var addNode = function(data){
		var defer = $.Deferred();
		newNode = tree.jstree('create_node',
			currentNode,
			'last',
			data,
			defer.resolve,
			true);
			
		return defer.promise;	
	}
	
	var selectNode = function(){
		tree.jstree('select_node', newNode);
		openNode(); 	//yes, we need to repoen it. This is required if the newly added node is the first node that the parent contains.
	}

	
	var createNode = function(){
		postNode()
		.then(addNode)
		.then(selectNode)
		.then(openNode);
	}

	/* ********** actual code. ****************** */
	
	if (isOpen != true){
		openNode()			//first call will make the node load if necessary. 
		.then(createNode);
	}
	else{
		createNode();
	}

}

/* **************************** check move section **************************************** */

function treeCheckDnd(m){
	
	var object = m.o;
	var dest = m.np;
	var src = m.op;
	
	var jqSrc = $(src);
	var jqDest = $(dest);
	var jqObject = $(object);
	
	//check if the node is draggable first
	if (! jqObject.is(':editable')){
		return false;
	}
	

	//check if the src and dest are within the same project. If they aren't themselve a drive we
	//need to look for them.
	var srcDrive =  jqSrc.is(':library') ? jqSrc : jqSrc.parents(':library');
	var destDrive = jqDest.is(':library') ? jqDest : jqDest.parents(':library');
	
	if ((srcDrive==null) || (destDrive==null)){
		return false;
	}
	
	if (srcDrive.attr('resid')!=destDrive.attr('resid')){
		return false;
	}
	
	//in case we are moving an iteration, check the destination is 
	//of type campaign
	
	if ( ($(object).is(':iteration')) && (! $(dest).is(':file')) ){
		return false;
	}
	
	//if the object is an iteration, the destination must be the same campaign
	if(jqObject.is(':iteration') && jqSrc.attr('resid') != jqDest.attr('resid')){
		return false;
	}
	//prevent iteration copy
	if(jqObject.is(':iteration') && isCtrlClicked == true){
		return false;
	}
	
	return true;			
	
}




/* ***************************** node copy section **************************************** */


/*
	jstree inserts dumb copies when we ask for copies. We need to destroy them before inserting the correct
	ones incoming from the server.
	
	@param object : the move_object returned as part of the data of the event mode_node.jstree.
	
*/
function destroyJTreeCopies(object, tree){
	object.oc.each(function(index, elt){
		tree.delete_node( elt);
	});
}


/*
	will batch-insert nodes incoming from the server.
	
	@param jsonResponse : the node formatted in json coming from the server.
	
	@param currentNode : the node where we want them to be inserted.
	
	@param tree : the tree instance.
*/
function insertCopiedNodes(jsonResponse, currentNode, tree){
	for (var i=0;i< jsonResponse.length; i++){
		tree.create_node(
			currentNode,
			'last',
			jsonResponse[i],
			false,
			true
		);
	}
}

function moveObjectToCopyData(moveObject){
	
	var nodeData = moveObject.args[0];
	
	return  {
		inst : moveObject.inst,
		sendData : {
			"object-ids" : $(nodeData.o).collect(function(e){return $(e).attr('resid');}),
			"destination-id" : nodeData.np.attr('resid'),
			"destination-type" : isRoot(nodeData.np) ? "library" : "folder"
		},
		newParent : nodeData.np
	}
}

/*
	will erase fake copies in the tree, send the copied node data to the server, and insert the returned nodes.
	
	@param data : the data associated to the event move_node.jstree
	
	@param url : the url where to send the data.
	
	@returns : a promise

*/
function copyNode(data, url){
	
	var deferred = $.Deferred();

	var tree=data.inst;
	var newParent = data.newParent;	
	var dataSent = data.sendData;
	
	
	$.when(tree.open_node(newParent))
	.then(function(){
	
		$.ajax({
			type : 'POST',
			url : url,
			data : dataSent,
			dataType : 'json'
		})
		.success(function(jsonData){
			insertCopiedNodes(jsonData, newParent, tree);
			tree.open_node( newParent, deferred.resolve);
		})
		.error(deferred.fail);
	});
	
	return deferred.promise();
}


/* ******************************* node move section ******************************** */

/*
	we reject iterations. Beside that it's okay.
	
	@param data : the move_node object
	@param url : the url to post to.


*/
function moveNode(data, url){

	var tree=data.inst;
	var nodeData = data.args[0];
	var newParent = nodeData.np;
	
	//first check if we don't need to perform an operation
	if (nodeData.o.length==0){
		return;
	}
	
	//we also reject iterations.
	var firstNode=nodeData.o[0];
	if ($(firstNode).is(":iteration")){
		return;
	}
	
	var dataSent = {
		"object-ids" : $(nodeData.o).collect(function(e){return $(e).attr('resid');}),
		"destination-id" : nodeData.np.attr('resid'),
		"destination-type" : isRoot(nodeData.np) ? "library" : "folder"
	};
	
	tree.open_node(newParent);

	return $.ajax({
		type : 'POST',
		url : url,
		data : dataSent,
		dataType : 'json'
	})


}


/* ******************************* leaf URL management code ************************************* */ 

/**
 * Returns the url where to GET the content (ie children) of a node. This url
 * should return JSON tree nodes.
 * 
 * @param urlRoot
 * @param node
 * @returns {String}
 */
function nodeContentUrl(urlRoot, node) {
	return urlRoot + '/' + node.attr('rel') + 's/' + node.attr('resId')
			+ '/content';
}
/**
 * Returns the url where to GET the resource represented by a node. Thus url
 * usually returns HTML.
 * 
 * @param urlRoot
 * @param node
 */
function nodeResourceUrl(urlRoot, node) {
	return urlRoot + '/' + node.attr('resType') + '/' + node.attr('resId');
}
/**
 * Stores URLs relative to the current selected node in the tree
 * 
 * @param selResourceUrl
 * @param selResourceContentUrl
 * @param selNodeContentUrl
 * @param selResourceId
 */
function storeSelectedNodeUrls(treeId, selResourceUrl, selNodeContentUrl,
		selResourceId) {
	var tree = $('#' + treeId);
	tree.data('selectedResourceUrl', selResourceUrl);
	tree.data('selectedNodeContentUrl', selNodeContentUrl);
	tree.data('selectedResourceId', selResourceId);
}
/**
 * Gets the event target and if it is a node of the given tree, toggles it.
 * 
 * @param event
 * @param tree
 *            jquery-selected tree
 */
function toggleEventTargetIfNode(event, tree) {
	var target = $(event.target);

	if (target.is('a')) {
		$(tree).jstree('toggle_node', target.parent());
	}
}

/**
 * Unselects the nodes of the given tree which are not siblings of the given li
 * node.
 * 
 * @param liNode
 * @param tree
 */
function unselectNonSiblings(liNode, tree) {
	if (liNode.attr('rel') == 'drive') {
		return;
	}

	var previouslySelected = findSelectedNodes(tree);

	if (previouslySelected.length > 0) {
		var parent = liNode.parents('li:first');
		var siblings = $(parent).children('ul').children('li');

		var notSelectables = $(previouslySelected).not(siblings);

		notSelectables.each(function(index, element) {
			tree.jstree('deselect_node', element);
		});
	}
}

function findSelectedNodes(tree) {
	return tree.find('.jstree-clicked').parent('li');
}
/**
 * Cancels a tree click event if it is the second or more in a row. For example,
 * a double click triggers a dblclick event and 2 click events. Usually we
 * dont want to process the click event twice.
 * 
 * @param clickEvent
 */
function cancelMultipleClickEvent(clickEvent) {
	if (clickEvent.detail > 1) {
		clickEvent.stopImmediatePropagation();
	}
}
	
/* ****************************** allowed operations ********************************************** */

	function getTreeAllowedOperations(treeSelector){

		var selectedNodes = $(treeSelector).jstree('get_selected');		
		
		var operations = "";
		
		//tha variable will be set to true if at least one selected node is not editable.
		var noEdit = (selectedNodes.not(":editable").length > 0);
		
		//case 1 : not editable : no operations allowed. 
		if (noEdit){
			operations = "";
		}
		//case 2 : more than one item selected : no operations allowed except deletion and copy if the nodes aren't libraries
		else if (selectedNodes.length != 1){
			operations = (! selectedNodes.is(":library")) ? "delete copy" : "";
		}
		//case 3 : one item is selected, button activation depend on their nature.
		else{
			switch(selectedNodes.attr('rel')){			
				case "drive" :
					operations="create-folder create-file paste";
					break;
				
				case "folder" :
					operations="create-folder create-file rename delete copy paste";
					break;
					
				case "file" :
					operations="create-resource rename delete copy";
					break;
					
				case "resource" : 
					operations="rename delete";
					break;
			
			}
		}
		
		return operations;
				
	}


	
/* ****************************** other tree-related objects ************************************** */


function ButtonBasedTreeNodeCopier(initObj){
	
	//properties
	this.copySelector=initObj.copySelector;
	this.pasteSelector=initObj.pasteSelector;
	this.tree = $.jstree._reference(initObj.treeSelector);
	this.errMessage= initObj.errMessage;
	this.url= initObj.url;
	
	
	//private methods		
	var displayError = function(){
		displayInformationNotification(this.errMessage);
	}
	
	var checkSameProject = function(target){			
		var targetLib = findParentLibrary(target);
		var previousLibId = $.cookie('squash-copy-library-id');
		return targetLib.attr('resid') === previousLibId;
	}
	
	var findParentLibrary = function (nodes){
		return nodes.is(':library') ? nodes : nodes.parents(":library");
	}
	
	//public methods
	this.copyNodesToCookie = function(){
		var nodes = this.tree.get_selected();
		var ids = nodes.collect(function(elt){return $(elt).attr('resid');});
		var library = findParentLibrary(nodes);
		var libraryId = library.attr('resid');
		$.cookie('squash-copy-nodes-ids', ids.toString());			
		$.cookie('squash-copy-library-id', libraryId);
		
	}
	
	this.pasteNodesFromCookie = function(){
		var ids = $.cookie('squash-copy-nodes-ids').split(',');
		
		if (! ids) return;
		
		var target = this.tree.get_selected();
		
		if (target.length!=1 || (! target.is(':editable')) || (! checkSameProject(target))){
			displayError.call(this);
		}
		else{
							
			//here we mimick the move_object used by moveNode, describe earlier in the file
			var copyData = {
				inst : this.tree,
				sendData : {
					"object-ids" : ids,
					"destination-id" : target.attr('resid'),
					"destination-type" : isRoot(target) ? "library" : "folder"						
				},
				newParent : target
			}
			
			//then we send it to the copy routine
			copyNode(copyData, this.url)
			.fail(function(){this.tree.refresh();});
		}
	
	}		
	
	$(this.copySelector).click($.proxy(this.copyNodesToCookie, this));
	$(this.pasteSelector).click($.proxy(this.pasteNodesFromCookie, this));
	
}




