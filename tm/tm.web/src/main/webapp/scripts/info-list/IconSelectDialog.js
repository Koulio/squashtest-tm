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
define([ "jquery", "backbone", "handlebars", "workspace.routing", "jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, routing) {
	var View = Backbone.View.extend({
		el : "#choose-item-icon-popup",

		initialize : function() {

		this.initIcon();
			this.$el.confirmDialog({
				autoOpen : true,  width : 800,
			});
		},

		events : {
			"click td"           : "selectIcon",
			"confirmdialogcancel" : "cancel",
			"confirmdialogconfirm" : "confirm",

		},
		
		changeIconOpacity: function (event){
			var icon = event.currentTarget;	
			var $icon = this.$(icon);
			this.$("td").addClass("low-opacity");
			$icon.removeClass("low-opacity");
			
		},
		restoreIconOpacity : function (event){
			this.$("td").removeClass("low-opacity");
		},
		initIcon : function(){
	        //clean the style
			this.$("td").removeClass("info-list-item-icon-selected");
			this.$("td").removeClass("low-opacity");
			
			//if icon is selected add correct style
			if (this.model.icon && this.model.icon !== "info-list-icon-noicon"){
			this.$("td").addClass("low-opacity");
			var selected = this.$("." + this.model.icon);
			selected.addClass("info-list-item-icon-selected");
			selected.removeClass("low-opacity");
			}
			
		},
		selectIcon : function(event){
			var icon = event.currentTarget;	
			var selected = this.$(".info-list-item-icon-selected");
			selected.removeClass("info-list-item-icon-selected");
			var $icon = this.$(icon);
		
			
			if (!selected[0] || selected[0].cellIndex !== $icon[0].cellIndex){
			$icon.addClass("info-list-item-icon-selected");
			this.$("td").addClass("low-opacity");
			$icon.removeClass("low-opacity");
			} else {
				this.$("td").removeClass("low-opacity");

			}
	
		},
		
		
		cancel : function(event) {
			this.cleanup();
			this.trigger("selectIcon.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			
			var self = this;

			var icon = "noicon";
			var selected = this.$(".info-list-item-icon-selected");
			if (selected.length > 0){
				var classList = selected.attr('class').split(/\s+/);
				classList.forEach(function(item, index){
					var indx = item.indexOf("info-list-icon-");
				if (indx  > -1){
				icon = item.substring(indx + "info-list-icon-".length);
				}
				});
			}	
			this.trigger("selectIcon.confirm", icon);
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this.$el.confirmDialog("destroy");
		},



	});

	return View;
});