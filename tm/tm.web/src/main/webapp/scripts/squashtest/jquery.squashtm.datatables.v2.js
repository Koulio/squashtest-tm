/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
 dependencies :

 jquery,
 tableDnD,
 dataTables
 KeyEventListener
 statusFactory
 jquery.squashtm.oneshotdialog.js
 jquery.squashtm.datatables

 */

/**
 * ======================Intoduction===================================
 * 
 * 
 * keys used for data lookup -------------------------
 * 
 * That table uses mPropData for its columns. More explictly, it uses json data
 * as a map. Specifically, the defaults keys used here are : 
 * - 'entity-id' : the entity id 
 * - 'entity-index' : the position of the entity when the list is sorted
 * 
 * Those keys may be redefined through configuration, using a field object 'dataKeys' :  
 * 
 * {
 * 	...
 * 	dataKeys : {
 * 		entityId : default is 'entity-id' ,
 * 		entityIndex : default is 'entity-index'
 * 	} 
 * 
 * }
 * 
 * In some cases more keys might be required for the modules decscribed below,
 * refer to the documentation if need be.
 * 
 * 
 * placeholders : --------------
 * 
 * When configuring a module sometimes you will see that a given string supports
 * placeholders. It means that anything between curly braces '{something}' are
 * placeholders that will be replaced by the corresponding value from
 * aoData["something"]. That's where the data keys above are useful.
 * 
 * =========== Regular Datatable settings=======================================
 * 
 * the inherited part of the datatable is configured using the first parameter :
 * 'datatableSettings'. Any regular datatable configuration is supported.
 * 
 * It uses defaults values yet the following parameters are still REQUIRED : 
 * - "oLanguage" (internationalization), 
 * - "sAjaxSource" (chargement ajax), 
 * - "aoColumnDefs" (les colonnes)
 * 
 * 
 * ============= object datasource and DOM data ================================
 * 
 * Structured object datasource is great except when you need to read those data
 * from the DOM. Normally the initial data should be provided by other means (eg 
 * ajax call or supplied to the configuration), because DOM-based simply doesn't 
 * fit. For instance, if you configure your column to use 
 * "mDataProp : 'cake.cherry'", datatable.js will crash because it cannot find 
 * it in the DOM (because it assumes that all you want is a scalar, not an object).
 * 
 * If you still decide to use an object datasource yet initialize it by reading 
 * the DOM, this datatable will help you to work around this by creating the 
 * missing parts of the data object on the fly. Note that it still can produce 
 * buggy datatables if later on the datatable uses data that couldn't be found 
 * that way.
 * 
 * To enable this feature, please add to your configuration 
 * 'fixObjectDOMInit : true'
 * 
 * ============= Squash additional settings=====================================
 * 
 * 
 * The squash specifics are configured using the second parameter :
 * 'squashSettings'. It is an object that accepts the following members : 
 * 
 * 
 * 
 * - functions : any function defined as public member of the table can be redefined as a member of .function (read the source to pimpoint them at the end of this file)
 * 
 *  examples : 
 *  dropHandler : 
 * 		what : a function that must handle the row drop.
 * 		param : an object { itemIds : array of row ids, newIndex : the drop position }
 * 		default : nothing 
 * 
 * 
 * 
 *  getODataId : 
 * 		what : a function fetching the id from the data 
 * 		param : what $().dataTable().fnGetData() would normally accept
 * 		default : return fnGetData()["entity-id"] 
 * 
 * 
 * 
 * - enableDnD : if coalesce to true, will enable table drag and drop. If coalesce 
 * 				to false, it will not. 
 * 
 * 
 * 
 * - enableHover : if coalesce to true, will enable lines color change when
 * hovered. If coalesce to false, it will not. 
 * 
 * 
 * - fixObjectDOMInit : boolean, refer to the documentation above ('object datasource and DOM data')
 * 
 * 
 * - confirmation popups : if 'confirmPopup' is set, will configure any confirmation 
 * 				dialog used in that table. it's an object such as :
 * 		oklabel : label for okay buttons 
 * 		cancellabel : label for cancel buttons 
 * 
 * 
 * 
 * - attachments : If the table finds tds having a given cssClass (see cssMatcher)
 * if will turn them into link to the attachment manager. 'attachments' is an
 * object. It must define at least url. It may also override the others of
 * course.
 * 	url : url where the attachment manager is. Accepts placeholders. 
 *  			Note : that one accepts no defaults ! 
 *  cssMatcher : the css class of cells that must be treated. defaults to 
 *  			'has-attachment-cells' 
 *  aoDataNbAttach : the name of the column in aoData where to look for how 
 *  			many attachment the row has. defaults to "nb-attachments" 
 *  aoDataListId : the name of the column in aoData where to look for the 
 *  			attachment list id, defaults to "attach-list-id"
 *  
 *  
 *  
 *  - rich editables configuration : if a property 'richEditables' is set, will
 * attempt to turn some cells to rich editables. If undefined, nothing will
 * happen. the property 'richEditables' is an compound object and must define at
 * least 1 member for 'target'. 
 * 		conf : a regular object configuring the plugin $.ui.richEditable (see
 * 					jquery.squashtm.jeditable.ext.js). 
 * 		targets : a map of key-values. A key represents a css class and the 
 * 				  value represents an url supporting placeholders. Any td having 
 * 				  the given css class will be turned to a rich jeditable configured 
 * 				  with 'conf' and posting to the supplied url. 
 * 
 * 
 * 
 * - execution status : If a property 'executionStatus' is set, will attempt to 
 * decorate some cells with execution statuses. If undefined, nothing will happen. 
 * The matched cells are identified by css class 'has-status'.
 * 
 * 	'executionStatus' is an object defining the localized status text : 
 * 		blocked : internationalized version of status 'blocked' 
 * 		failure : internationalized version of status 'failure' 
 * 		success : internationalized version of status 'success' 
 * 		running : internationalized version of status 'running' 
 * 		ready : internationalized version of status 'ready' 
 * 
 * 
 * 
 * - delete button : if the property 'deleteButtons' is set then will look for cells 
 * having the css class 'delete-button'. Configuration as follow : 
 * 		url : the url where to post the 'delete' instruction. Supports placeholders. 
 * 		popupmessage : the message that will be displayed 
 * 		tooltip : the tooltip displayed by the button 
 * 		success : a callback on the ajax call when successful 
 * 		fail : a callback on the ajax call when failed.
 * 		dataType : the dataType parameter for the post. (default = "text")
 * 
 * 
 * 
 * - add links to cell text : if the property 'bindLinks' is set then will look for cells
 *  according to the parameters given and make their text a link to the wanted url.
 *  Configuration as follow: 
 *  	 list : a list of object to represent each td of a row to make as url
 *  			Object params as follow : 
 *  				-url : the url to wrap the text with (place holder will be set to row object id)
 *  				-target : the td rank in the row (starts with 1)
 *  				-targetClass : alternate to the above, uses css class to find its target
 *  				-isOpenInTab : boolean to set the target of the url to "_blank" or not.
 */
var squashtm = squashtm || {};
squashtm.keyEventListener = squashtm.keyEventListener || new KeyEventListener();

(function($) {

	/***************************************************************************
	 * 
	 * The following functions assume that the instance of the datatable is
	 * 'this'.
	 * 
	 * Note the '_' prefixing each of them.
	 * 
	 * Typically when the squash datatable initialize it will also declare
	 * public methods that will access them. Those methods then have the same
	 * name, without the '_' prefix.
	 * 
	 * In some of the functions here such methods belonging to 'this' are
	 * invoked. It's not a typo : it's the expected behaviour.
	 * 
	 * 
	 **************************************************************************/

	/*
	 * what : a function that must handle the row drop. param : an obect {
	 * itemIds : array of row ids, newIndex : the drop position } default :
	 * nothing
	 */
	function _dropHandler(dropData) {

	}

	/*
	 * what : a function fetching the id from the data param : what
	 * $().dataTable().fnGetData() would normally accept default : return
	 * aoData[0]; : the datatable expects the id to be first.
	 */
	function _getODataId(arg) {
		var key = this.squashSettings.dataKeys.entityId;
		var id = this.fnGetData(arg)[key];
		if ((id != "") && (!isNaN(id))) {
			return id;
		} else {
			return null;
		}
	}

	/**
	 * Enables DnD on the given table.
	 * 
	 * Note : we calculate the 'offset' because the first displayed element is
	 * not necessarily the first item of the table. For instance, if we are
	 * displaying page 3 and drop our rows at the top of the table view, the
	 * drop index is not 0 but (3*pagesize);
	 * 
	 * @this : the datatable instance
	 * @param dropCallback
	 *            function called on drop. args are row and drop position.
	 * @returns
	 */
	function _enableTableDragAndDrop() {
		if (!this.squashSettings.enableDnD)
			return;
		var self = this;
		this.tableDnD({
			dragHandle : "drag-handle",
			onDragStart : function(table, rows) { // remember that we are
				// using our modified dnd :
				// rows is a jQuery object

				rows.find('.drag-handle').addClass('ui-state-active');

				var key = self.squashSettings.dataKeys.entityIndex;
				var offset = self.fnGetData(0)[key] - 1;

				var index = rows.get(0).rowIndex - 1;
				self.data("previousRank", index);
				self.data("offset", offset);

			},

			onDrop : function(table, rows) { // again, that is now a jQuery
				// object

				var newInd = rows.get(0).rowIndex - 1;
				var oldInd = self.data("previousRank");
				var offset = self.data("offset");
				if (newInd != oldInd) {

					var ids = [];
					rows.each(function(i, e) {
						var id = self.getODataId(e);
						ids.push(id);
					});

					self.dropHandler({
						itemIds : ids,
						newIndex : newInd + offset
					});
				}
			}

		});
	}

	/*
	 * For the current datatable, will bind hover coloring
	 */
	function _bindHover() {

		this.delegate('tr', 'mouseleave', function() {
			$(this).removeClass('ui-state-highlight');
		});

		this.delegate('tr', 'mouseenter', function() {
			var jqR = $(this);
			if (!jqR.hasClass('ui-state-row-selected')) {
				jqR.addClass('ui-state-highlight');
			}
		});

	}

	/*
	 * Just does what the name says.
	 */

	function _bindClickHandlerToSelectHandle() {
		var self = this;
		this.delegate('td.select-handle', 'click', function() {
			var row = this.parentNode;

			var ctrl = squashtm.keyEventListener.ctrl;
			var shift = squashtm.keyEventListener.shift;

			if (!ctrl && !shift) {
				_toggleRowAndDropSelectedRange.call(self, row);

			} else if (ctrl && !shift) {
				_toggleRowAndKeepSelectedRange.call(self, row);

			} else if (!ctrl && shift) {
				_growSelectedRangeToRow.call(self, row);

			} else {
				_growSelectedRangeToRow.call(self, row);

			}

			_memorizeLastSelectedRow.call(self, row);
			clearRangeSelection();

			return true;
		});
	}

	/*
	 * that method programatically remove the highlight due to native range
	 * selection.
	 */
	function clearRangeSelection() {
		if (window.getSelection) {
			window.getSelection().removeAllRanges();
		} else if (document.selection) { // should come last; Opera!
			document.selection.empty();
		}
	}

	/* private */function _toggleRowAndDropSelectedRange(row) {
		var jqRow = $(row);
		jqRow.toggleClass('ui-state-row-selected').removeClass(
				'ui-state-highlight');
		jqRow.parent().find('.ui-state-row-selected').not(row).removeClass(
				'ui-state-row-selected');

	}

	/* private */function _toggleRowAndKeepSelectedRange(row) {
		$(row).toggleClass('ui-state-row-selected').removeClass(
				'ui-state-highlight');
	}

	/* private */function _growSelectedRangeToRow(row) {
		var rows = this.$("tr");
		var range = _computeSelectionRange.call(this, row);

		for ( var i = range[0]; i <= range[1]; i++) {
			var r = rows[i];
			$(r).addClass('ui-state-row-selected');
		}

		$(row).removeClass('ui-state-highlight');
	}
	/**
	 * Computes the 0-based range of row that should be selected. Note :
	 * row.rowIndex is a 1-based index.
	 * 
	 * @param row
	 * @param table
	 * @returns
	 */
	/* private */function _computeSelectionRange(row) {
		var baseRow = this.data("lastSelectedRow");
		var baseIndex = baseRow ? baseRow.rowIndex : 1;
		var currentIndex = row.rowIndex;

		var rangeMin = Math.min(baseIndex, currentIndex);
		rangeMin = Math.max(rangeMin, 1);

		var rangeMax = Math.max(baseIndex, currentIndex);
		var rows = this.$("tr");
		rangeMax = Math.min(rangeMax, rows.length);

		return [ rangeMin - 1, rangeMax - 1 ];
	}

	/* private */function _memorizeLastSelectedRow(row) {
		if ($(row).hasClass('ui-state-row-selected')) {
			this.data("lastSelectedRow", row);
		}
	}

	/**
	 * saves the ids of selected rows
	 */
	function _saveTableSelection() {
		var selectedIds = _getSelectedIds.call(this);
		this.attr('selectedIds', selectedIds);
	}

	function _restoreTableSelection() {
		var selectedIds = this.attr('selectedIds');
		if (selectedIds != null) {
			_selectRows.call(this, selectedIds);
		}
		this.removeAttr('selectedIds');
	}

	/* private */function _selectRows(ids) {
		var rows = this.fnGetNodes();

		var self = this;
		$(rows).filter(function() {
			return self.getODataId(this);
		}).addClass('ui-state-row-selected');

	}

	// no arguments mean all rows
	/* private */function _deselectRows(ids) {
		var rows = this.fnGetNodes();

		var self = this;
		if (arguments.length == 1) {
			$(rows).filter(function() {
				return self.getODataId(this);
			}).removeClass('ui-state-row-selected');
		} else {
			$(rows).removeClass('ui-state-row-selected');
		}
	}

	/**
	 * @returns {Array} of ids of selected rows
	 */
	function _getSelectedIds() {
		var rows = this.fnGetNodes();
		var ids = new Array();

		var self = this;

		$(rows).each(function(index, row) {
			if ($(row).hasClass('ui-state-row-selected')) {
				var id = self.getODataId(row);
				if ((id != "") && (!isNaN(id))) {
					ids.push(id);
				}
			}
		});

		return ids;
	}

	function _addHLinkToCellText(td, url, isOpenInTab) {

		var link = $('<a></a>');
		link.attr('href', url);
		if (isOpenInTab) {
			link.attr('target', '_blank');
		}

		$(td).contents().filter(function() {
			// IE doesn't define the constant Node so we'll use constant value
			// instead of Node.TEXT_NODE
			return this.nodeType == 3;
		}).wrap(link);
	}

	function _resolvePlaceholders(input, data) {
		var pattern = /\{\S+\}/;
		var result = input;
		var match = pattern.exec(result);
		while (match) {
			var pHolder = match[0];
			var key = pHolder.substr(1, pHolder.length - 2);
			result = result.replace(pHolder, data[key]);
			match = pattern.exec(result);
		}

		return result;
	}

	/*
	 * 'this' is the table. That function will be called as a draw callback.
	 */
	function _attachButtonsCallback() {

		var attachConf = this.squashSettings.attachments;

		var self = this;
		var cells = $('td.' + attachConf.cssMatcher, this);

		$(cells).each(
				function(i, cell) {

					var data = self.fnGetData(cell.parentNode);

					// first : set the proper icon
					var nbAttach = data[attachConf.aoDataNbAttach];
					var linkClass = (nbAttach > 0) ? "manage-attachments"
							: "add-attachments";

					// second : what url we navigate to when clicked.
					var url = _resolvePlaceholders.call(self, attachConf.url,
							data);

					// design the link and voila !
					var link = '<a href="' + url + '" class="' + linkClass
							+ '"></a>';

					$(cell).html(link);
				});
	}

	function _buggedPicsCallback() {
		var buggedConf = this.squashSettings.bugged;

		var self = this;
		var cells = $('td.' + buggedConf.cssMatcher, this);

		$(cells).each(function(i, cell) {

			var data = self.fnGetData(cell);
			var buggedClass = (data.length > 0) ? "hasBugs" : "noBugs";

			var pics = '<span class="' + buggedClass + '"> </span>';

			$(cell).html(pics);
		});
	}

	/*
	 * again 'this' is the table instance.
	 */
	function _configureRichEditables() {

		var editableConf = this.squashSettings.richEditables;
		var self = this;

		if (!editableConf)
			return;

		var baseconf = editableConf.conf;
		var targets = editableConf.targets;

		if (!targets)
			return;

		for ( var css in targets) {

			var cells = $('td.' + css, this);

			$(cells).each(function(i, cell) {
				var data = self.fnGetData(cell.parentNode);
				var url = _resolvePlaceholders.call(self, targets[css], data);
				var finalConf = $.extend(true, {
					"url" : url
				}, baseconf);
				$(cell).richEditable(finalConf);
			});
		}
	}

	function _configureExecutionStatus() {

		var statusConf = this.squashSettings.executionStatus;

		if (!statusConf)
			return;

		var factory = new squashtm.StatusFactory(statusConf);

		var cells = $('td.has-status', this);

		$(cells).each(function(i, cell) {
			var data = (cell.textContent) ? cell.textContent : cell.innerText;
			var newhtml = factory.getHtmlFor(data);
			cell.innerHTML = newhtml;
		});
	}
	;

	function _configureDeleteButtons() {
		var deleteConf = this.squashSettings.deleteButtons;
		if (!deleteConf)
			return;

		var template = '<a href="javascript:void(0)">' + deleteConf.tooltip
				+ '</a>';

		var cells = $('td.delete-button', this);
		cells.html(template);
		cells.find('a').button({
			text : false,
			icons : {
				primary : "ui-icon-minus"
			}
		});

	}
	;
	
	function _bindDeleteButtons() {
		var conf = this.squashSettings.deleteButtons;
		var popconf = this.squashSettings.confirmPopup;

		if (!conf)
			return;
		var self = this;

		this.delegate('td.delete-button > a', 'click', function() {
			var row = this.parentNode.parentNode; // hopefully, that's the
			// 'tr' one
			var jqRow = $(row);
			jqRow.addClass('ui-state-row-selected');
		

			oneShotConfirm(conf.tooltip || "", conf.popupmessage || "",
					popconf.oklabel, popconf.cancellabel).done(
					function() {
						var finalUrl = _resolvePlaceholders.call(self,
								conf.url, self.fnGetData(row));
						var request;
						
						request = $.ajax({
							type : 'delete',
							url : finalUrl,
							dataType : self.squashSettings.deleteButtons.dataType || "text",
						});
						
						if (conf.success)
							request.done(conf.success);
						if (conf.fail)
							request.fail(conf.fail);

					}).fail(function() {
				jqRow.removeClass('ui-state-row-selected');
			});
		});
	}
	;
	
	/**
	 * Wrap cell text with link tags according to the given settings : squashSettings.bindLinks
	 * More info on top of the page on "Squash additional settings" doc.
	 * 
	 */
	function _configureLinks() {
		var linksConf = this.squashSettings.bindLinks;
		if (!linksConf){return;}
		
		var self = this;
		
		for(var i=0; i< linksConf.list.length; i++){
			var linkConf = linksConf.list[i];
			//1. build link
			var link = $('<a></a>');
			if (linkConf.isOpenInTab) {
				link.attr('target', '_blank');
			}
			//2. select required td and wrap their thext with the built link
			var cells;
			if (linkConf.targetClass!==undefined){
				cells = $("td."+linkConf.targetClass, self);
			}
			else{
				cells = $('td:nth-child('+linkConf.target+')', self);
			}
			
			cells.contents().filter(function () {
				// IE doesn't define the constant Node so we'll use constant value
				// instead of Node.TEXT_NODE
				return this.nodeType == 3;
			}).wrap(link);
			//3. add id to cells
			$.each(cells, function(index, cell){
				var row = cell.parentNode; // should be the tr
				var finalUrl = _resolvePlaceholders(linkConf.url, self.fnGetData(row));
				var cellLink = $(cell).find("a");
				cellLink.attr('href', finalUrl);
			});
		}
	}
	;
	
	
	/**
	 * Unlike the above, that function will not be a member of the squash datatable.
	 * This is a factory function that returns a method handling the corner case
	 * of initializing an object based datasource from the DOM (refer to the documentation above).
	 * 
	 * See also the function just below ( _fix_mDataProp )
	 * 
	 * some bits are taken from jquery.datatable.js, sorry for the copy pasta.
	 */
	function _createObjectDOMInitFixer(property){
		

		function exists(data, property){
			var localD = data;
			var a = property.split('.');
			for ( var i=0, iLen=a.length-1 ; i<iLen ; i++ )
			{
				localD = localD[ a[i] ];
				if ( localD === undefined )
				{
					return false;
				}
			}
			return true;
		}
		
		function setValue(data, val, property){
			var localD = data;
			var a = property.split('.');
			for ( var i=0, iLen=a.length-1 ; i<iLen; i++ )
			{
				var ppt = a[i];
				if (localD[ppt]===undefined){
					localD[ppt] = {};
				}
				localD = localD[ppt];
			}
			localD[a[a.length-1]] = val;
		}
		
		function getValue(data, property){
			var localD = data;
			var a = property.split('.');
			for ( var i=0, iLen=a.length ; i<iLen ; i++ )
			{
				localD = localD[ a[i] ];
			}
			return localD;		
		}
		
		return function(data,operation,val){
			if (operation=='set' && exists(data, property)===false){
				setValue(data, val, property);
			}
			else{
				return getValue(data, property);
			}
		};
		
	}
	;
	
	/**
	 * this function will process the column defs, looking for 
	 * mDataProp settings using a dotted object notation, 
	 * to fix them when reading the DOM (read documentation above).
	 * 
	 */
	function _fix_mDataProp(datatableSettings){
		
		var columnDefs = datatableSettings.aoColumnDefs;
		
		var needsWrapping = function(rowDef){
			var mDataProp = rowDef.mDataProp;
			return (
					(!!mDataProp) &&
					(typeof mDataProp === 'string') && 
					(mDataProp.indexOf('.') != -1)
				);
		};
		
		var i=0;
		var length = columnDefs.length;
		for (i=0; i<length; i++){
			var rowDef = columnDefs[i];
			if ( needsWrapping(rowDef)===true ){
				var attribute = rowDef.mDataProp;
				rowDef.mDataProp = _createObjectDOMInitFixer(attribute);
			}
			
		};
	}
	

	/***************************************************************************
	 * 
	 * now we can declare our plugin
	 * 
	 **************************************************************************/

	var datatableDefaults = squashtm.datatable.defaults;

	var squashDefaults = {
		dataKeys : {
			entityId : 'entity-id' ,
			entityIndex : 'entity-index'
		}, 
		attachments : {
			cssMatcher : "has-attachment-cell",
			aoDataNbAttach : "nb-attachments",
			aoDataListId : "attach-list-id"
		},
		bugged : {
			cssMatcher : "bugged-cell",
			aoDataNbBugs : "bugged",
		},
		confirmPopup : {
			oklabel : "ok",
			cancellabel : "cancel",
		},
	};

	/*
	 * temporary hack here : due to multiple inclusions of that file, the
	 * variable $.fn.squashTable.instances might be redefined. Thus previously
	 * existing instances of a datatable would be lost (and we don't want that).
	 * 
	 * So we're saving it in case it already exists, and rebind it below.
	 */
	if ($.fn.squashTable)
		var existingInstances = $.fn.squashTable.instances;

	$.fn.squashTable = function(datatableSettings, squashSettings) {

		/*
		 * are we in retrieve mode or init mode ? the answer is simple : no
		 * param means retrieve mode. Note that the key is the dome element.
		 */

		if (arguments.length == 0) {
			return $.fn.squashTable.instances[this.selector];
		}

		/*
		 * else we will initialize a new instance.
		 */

		var datatableEffective = $.extend(true, {}, datatableDefaults,
				datatableSettings);
		
		
		var squashEffective = $.extend(true, {}, squashDefaults, squashSettings);

		/* ************** squash init first *********************** */

		// save the settings in that instance
		this.squashSettings = squashEffective;

		this.dropHandler = _dropHandler;
		this.getODataId = _getODataId;
		this.saveTableSelection = _saveTableSelection;
		this.restoreTableSelection = _restoreTableSelection;
		this.getSelectedIds = _getSelectedIds;
		this.addHLinkToCellText = _addHLinkToCellText;
		this.selectRows = _selectRows;
		this.deselectRows = _deselectRows;
		this.configureLinks = _configureLinks;
		
		this.attachButtonsCallback = _attachButtonsCallback;
		this.buggedPicsCallback = _buggedPicsCallback;
		this.configureRichEditables = _configureRichEditables;
		this.configureExecutionStatus = _configureExecutionStatus;
		this.configureDeleteButtons = _configureDeleteButtons;
		this.enableTableDragAndDrop = _enableTableDragAndDrop;
		this.restoreTableSelection = _restoreTableSelection;
		
		if(squashSettings.bindDeleteButtons != null){
			this.bindDeleteButtons = squashSettings.bindDeleteButtons;
		}
		else{
			this.bindDeleteButtons = _bindDeleteButtons;
		}
		
		this.refresh = function() {
			this.fnDraw(false);
		};

		

		// ************** function overrides *********************************************

		if (squashEffective.functions) {
			$.extend(this, squashEffective.functions);
		}
		

		
		// ************** preprocess the column definitions if need be ********************
		
		if (squashEffective.fixObjectDOMInit){
			_fix_mDataProp(datatableEffective);
		}

		/*
		 * ************* prepare a custom rowcallback and drawcallback if needed * ********
		 */

		// pre draw callback
		var userPreDrawCallback = datatableEffective["fnPreDrawCallback"];

		var customPreDrawCallback = function(oSettings) {
			if (userPreDrawCallback)
				userPreDrawCallback.call(this, oSettings);
			_saveTableSelection.call(this);
		};

		datatableEffective["nfPreDrawCallback"] = customPreDrawCallback;

		// draw callback
		var userDrawCallback = datatableEffective["fnDrawCallback"];

		var customDrawCallback = function(oSettings) {
			if (userDrawCallback)	userDrawCallback.call(this, oSettings);
			
			this.attachButtonsCallback();
			this.buggedPicsCallback();
			this.configureRichEditables();
			this.configureExecutionStatus();
			this.configureDeleteButtons();
			this.configureLinks();
			this.enableTableDragAndDrop();
			this.restoreTableSelection();
			
			
			/*
			_attachButtonsCallback.call(this);
			_buggedPicsCallback.call(this);
			_configureRichEditables.call(this);
			_configureExecutionStatus.call(this);
			_configureDeleteButtons.call(this);
			_configureLinks.call(this);
			_enableTableDragAndDrop.call(this);
			_restoreTableSelection.call(this);
			*/
		};

		datatableEffective["fnDrawCallback"] = customDrawCallback;

		/* **************** store the new instance ***************** */

		$.fn.squashTable.instances[this.selector] = this;
		
		
		
		/* ************* now call the base plugin ***************** */
		

		this.dataTable(datatableEffective);

		/* ****** last : event binding ***** */

		_bindClickHandlerToSelectHandle.call(this);

		if (squashEffective.enableHover) {
			_bindHover.call(this);
		}
		;

		if (squashEffective.deleteButtons) {
			_bindDeleteButtons.call(this);
		}
		;
		
		this.addClass("is-contextual");
	};

	$.fn.squashTable.instances = existingInstances || {}; // end of the hack

})(jQuery);
