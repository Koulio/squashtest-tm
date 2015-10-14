/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

	define([ "jquery", "backbone", "workspace.routing", "./wizardRouter", "./wizardView", "./chartWizardModel" ], function($, Backbone, router, WizardRouter, WizardView, WizardModel) {

		
	$.ajax({
		url: router.buildURL('chart.wizard.data')	
	}).done(function(data){
		
		data.parentId = squashtm.chart.parentId;
		
		var model = new WizardModel(data);
		
		var wizardView = new WizardView ({
			model: model
		});
		
		new WizardRouter({
			wizardView : wizardView
		});
		
		Backbone.history.start();
	});


});
 