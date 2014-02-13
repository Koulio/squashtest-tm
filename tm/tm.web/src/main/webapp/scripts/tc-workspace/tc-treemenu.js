/*
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

define(['jquery', './utils', './permissions-rules',
        'workspace/WorkspaceWizardMenu',
        'jquery.squash.buttonmenu', 
        'jquery.squash.squashbutton'], function($, utils, permissions, WizardMenu){
	

	function createWidgets(){
		
		var createconf = utils.btnconf("ui-icon ui-icon-plusthick");
		var copyconf = utils.btnconf("ui-icon-copy");
		var pasteconf = utils.btnconf("ui-icon-clipboard");
		var renameconf = utils.btnconf("ui-icon-pencil");
		var importconf = utils.btnconf("ui-icon-transferthick-e-w");
		var deleteconf = utils.btnconf("ui-icon-trash");
		var searchconf = utils.btnconf("ui-icon-search");
		
		$("#tree-create-button").buttonmenu({
			button : createconf
		});	
		
		$("#copy-node-tree-button").squashButton(copyconf);
		
		$("#paste-node-tree-button").squashButton(pasteconf);
		
		$("#rename-node-tree-button").squashButton(renameconf);
		
		$("#tree-import-button").buttonmenu({
			button : importconf
		});		
		
		$("#search-tree-button").squashButton(searchconf);
		
		$("#delete-node-tree-button").squashButton(deleteconf);
				
	}
	
	function decorateEnablingMethods(buttons){
		var i=0, len = buttons.length;
		
		function btnenable(){
			this.squashButton('enable');
		}
		
		function btndisable(){
			this.squashButton('disable');
		}
		
		function itemenable(){
			this.removeClass('ui-state-disabled');
		}
		
		function itemdisable(){
			this.addClass('ui-state-disabled');
		}
		
		for (i=0;i<len;i++){
			var jqbtn = buttons[i];
			if (jqbtn.attr('role')==='button'){
				jqbtn.enable = btnenable;
				jqbtn.disable = btndisable;
			}
			else{
				jqbtn.enable = itemenable;
				jqbtn.disable = itemdisable;
			}
		}
	}
	
	
	function bindTreeEvents(){
		
		var btnselector =   "#new-folder-tree-button, #new-test-case-tree-button, #copy-node-tree-button, #paste-node-tree-button, "+
							"#rename-node-tree-button, #import-excel-tree-button, #import-links-excel-tree-button, #export-tree-button, "+
							"#delete-node-tree-button, #search-tree-button";
		
		var buttons = [];
		
		$(btnselector).each(function(){
			var $this = $(this);
			buttons.push($this);
		});
		
		decorateEnablingMethods(buttons);

		var tree = $("#tree");	
		
		function loopupdate(event, data){
			
			var rules = permissions.buttonrules;
			var arbuttons = buttons;
			var nodes = tree.jstree('get_selected'); 
			var i=0,len = buttons.length;
			
			for (i=0;i<len;i++){
				var btn = arbuttons[i];
				var id = btn.attr('id');
				var rule = rules[id];
				if (rule(nodes)){
					btn.enable();
				}else{
					btn.disable();
				}				
			}
			
			return true;
			
		}
		
		tree.on('select_node.jstree deselect_node.jstree deselect_all.jstree', loopupdate);
		
		//init the button states immediately
		loopupdate("", {
			rslt : {
				obj : tree.jstree('get_selected')
			}
		});
		

	}
	
	
	// the wizard menu is a bit different from the rest, hence the init code
	// is put appart
	function createWizardMenu(){

		var wizards = squashtm.app.testCaseWorkspace.wizards;
		
		if (!!wizards && wizards.length>0){
			
			var wmenu = new WizardMenu({
				collection : wizards
			});
			
			var tree = $("#tree");
			
			//state init
			wmenu.refreshSelection(tree.jstree("get_selected"));
			
			//evt binding
			tree.on('select_node.jstree deselect_node.jstree deselect_all.jstree', function(evt, data){
				wmenu.refreshSelection(data.inst.get_selected());
			});
		}
		
	}
	
	function initExportPlugins(){
		var plugins = $("#tree_element_menu .export-plugin");
		var modules = plugins.map(function(idx, elt){
			var modulename = $(elt).data('module');
			return require.toUrl(modulename);
		}).get();
		var items = plugins.get();
		
		require(modules, function(){
			var i, len = modules.length;
			for (i=0;i<len;i++){
				var module = arguments[i],
					item = items[i];
				module.init(item);
			}
		});
	}
	
	function init(){
		createWidgets();
		bindTreeEvents();
		createWizardMenu();
		initExportPlugins();

		$("#tree_element_menu").removeClass("unstyled-pane");
	}
	
	
	
	return {
		
		init : init
		
	};
	
});