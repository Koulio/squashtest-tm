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
/* ********************
This object somewhat implements a distributed MVC. Locally, within the contextual content, it represents the model. However, when modifications happen it must warn it's listeners, but also
the object squashtm.workspace.contextualContent. It will relay the information to other models (ie, the tree).

As such a model object is both master (of its listeners) and slave (of the contextual content).

 ********************* */

define([ "jquery", "workspace.event-bus", "jqueryui" ], function($, eventBus) {

	function TestSuiteModel(settings) {

		this.createUrl = settings.createUrl;
		this.baseUpdateUrl = settings.baseUpdateUrl;
		this.getUrl = settings.getUrl;
		this.removeUrl = settings.removeUrl;
		this.eventBus = eventBus;

		if (settings.initData !== undefined) {
			this.data = settings.initData;
		} else {
			this.data = [];
		}

		this.listeners = [];

		var self = this;

		/* ************** private ************* */

		// we have to re-implement indexOf because IE8 doesn't support it
		// returns -1 if not found
		var indexById = $.proxy(function(id) {
			for ( var i = 0; i < this.data.length; i++) {
				if (this.data[i].id == id) {
					return i;
				}
			}
			return -1;
		}, self);

		var renameSuite = $.proxy(function(json) {
			var index = indexById(json.id);
			if (index != -1) {
				this.data[index].name = json.name;
			}
		}, self);

		var removeSuites = $.proxy(function(commands) {
			var removed = commands.removed;			
			for ( var i=0,len=removed.length; i<len; i++ ) {
				var index = indexById(removed[i].resid);
				if (index != -1) {
					this.data.splice(index, 1);
				}
			}
		}, self);

		var _getModel = function() {
			return $.ajax({
				'url' : self.getUrl,
				type : 'GET',
				dataType : 'json'
			}).success(function(json) {
				this.data = json;
			});
		};

		var notifyListeners = $.proxy(function(evt) {
			for ( var i = 0; i < this.listeners.length; i++) {
				this.listeners[i].update(evt);
			}
		}, self);

		var notifyContextualContent = $.proxy(function(evt) {
			this.eventBus.fire(this, evt);			
		}, self);
		
		var notifyBind = $.proxy(function(){
			var evt = {
					evt_name : "node.bind"
			};
			notifyListeners(evt);
			notifyContextualContent(evt);
		});
		/* ************** public interface (slave) **************** */

		this.update = function(event) {
			// the event 'contextualcontent.clear' means that the page will be
			// flushed (and so will be this object)
			if (event.evt_name && event.evt_name == "contextualcontent.clear"){
				return;
			}
			
			// in any other case we refetch the data. Perhaps we will refine
			// this
			// later.
			this.getModel();
		};

		/* ************** public interface (master) *************** */

		this.addListener = function(listener) {
			this.listeners.push(listener);
		};

		this.getData = function() {
			return this.data;
		};

		this.postNew = function(name) {

			return $.ajax({
				'url' : self.createUrl,
				type : 'POST',
				data : {
					'name' : name
				},
				dataType : 'json'
			}).success(function(json) {
				self.data.push(json);
				var evt = {
					evt_name : "node.add",
					newSuite : json
				};
				notifyListeners(evt);
				notifyContextualContent(evt);
			});
		};

		this.postRename = function(toSend) {

			var url = this.baseUpdateUrl + "/" + toSend.id + "/rename";

			return $.ajax({
				'url' : url,
				type : 'POST',
				data : toSend,
				dataType : 'json'
			}).success(function(json) {
				renameSuite(json);
				var evt = {
					evt_name : "node.rename",
					evt_target : {
						obj_id : toSend.id,
						obj_restype : "test-suites"
					},
					evt_newname : toSend.name
				};
				notifyListeners(evt);
				notifyContextualContent(evt);
			});
		};

		this.postRemove = function(toSend) {

			var url = this.removeUrl;

			return $.ajax({
				'url' : url,
				type : 'POST',
				data : toSend,
				dataType : 'json'
			}).success(function(json) {
				removeSuites(json);
				var evt = {
					evt_name : "node.remove"
				};
				notifyListeners(evt);
				notifyContextualContent(evt);
			});
		};

		this.postBind = function(toSend) {
			var url = this.baseUpdateUrl + '/' + toSend['test-suites'].join(',') + "/test-plan/";

			return $.ajax({
				'url' : url,
				type : 'POST',
				data :  { 'itemIds[]' : toSend['test-plan-items']},
				dataType : 'json'
			}).success(function(json) {
				notifyBind();
			});
		};
		
		this.postBindChanged = function(toSend){
			return $.ajax({
				'url': this.baseUpdateUrl+"/test-plan/",
				type : 'POST',
				data : { 'itemIds[]' : toSend['test-plan-items'],
					'boundSuiteIds[]' : toSend['bound-test-suites'],
					'unboundSuiteIds[]' : toSend['unbound-test-suites']},
				dataType : 'json'
			}).success(function(json){
				notifyBind();
			});
		};
		
		this.getModel = function() {
			_getModel().success(function() {
				notifyListeners({
					evt_name : "node.refresh"
				});
			});
		};

		// register to the contextual content manager if exists

		this.eventBus.addContextualListener(this);

	}

	return TestSuiteModel;
});