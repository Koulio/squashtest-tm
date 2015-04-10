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
/*
 *
 * This module defines the most important urls in the application. Each url is a template because it may accept placeholders.
 * A placeholder is enclosed by curly braces '{}' and a regular expression in between that describe what an actual replacement
 * should look to.
 *
 * ==========API============
 *
 *	'url name' : returns the URL template mapped to the 'url name' (see the list right below)
 *
 *	buildURL : function(urlName, placeholders ) : builds an URL based on a template and values for its placeholders.
 *				The first argument is an 'url name' and the rest are arbitrary additional arguments.
 *				The placeholders will be filled with the arguments in the order they are those additional arguments are supplied.
 *				NB : the placeholders values won't be tested against the regular expression so
 *
 *	matches : function(urlName, candidate) : tests the candidate url against the template named 'urlName'
 */
define([], function(){

	"use strict";

	var root = window.squashtm.app.contextRoot.replace(/\/$/, '');



	// returns the template as a RegExp object.
	// it is done by escaping the static part of the template,
	// and inlining the regex embedded in the placeholders.
	function templateToRegex(template){

		var exprNoCapture = /\{[^\}]+\}/g,
			exprCapture = /\{([^\}]+)\}/g;


		// let's break down the template, we separate the static parts from the placeholder parts. By construction
		// we'll normally have the relation placeholders.length <= staticparts.length <= placeholders.length + 1
		var staticparts,
			placeholders = [],
			buf;

		staticparts = template.split(exprNoCapture);

		while ((buf = exprCapture.exec(template))!== null){
			placeholders.push(buf[1]);
		}

		// now we can build our regexp
		var expression = "";
		while (staticparts.length>0){
			var statik = staticparts.shift() || "",
				placeholdexpr = placeholders.shift() || "";

			var escapedStatik =
				statik.replace(/[\-\[\\\]\{\}\(\)\*\+\?\.\,\\\^\$\|\#\s\/]/g, "\\$&");

			expression += escapedStatik + placeholdexpr;
		}

		return new RegExp("^"+expression+"$");

	}

	return {

		// url names mapping
		'attachments.manager':				root + '/attach-list/{\\d+}/attachments/manager',
		'search' :							root + '/advanced-search',
		'search.results' :					root + '/advanced-search/results',

		'bugtracker.execution' :			root + '/bugtracker/execution/{\\d+}',
		'bugtracker.execsteps.new' :		root + '/bugtracker/execution-step/{\\d+}/new-issue',

		'testcases.workspace' :				root + '/test-case-workspace/',
		'testcases.info' :					root + '/test-cases/{\\d+}/info',
		'testcases.requirements.manager' :	root + '/test-cases/{\\d+}/verified-requirement-versions/manager',

		'teststeps.info' :					root + '/test-steps/{\\d+}',
		'teststeps.requirements.manager' :	root + '/test-steps/{\\d+}/verified-requirement-versions/manager',

		'requirements.workspace':			root + '/requirement-workspace/',
		'requirements' :					root + '/requirements/{\\d+}',
		'requirements.info'	:				root + '/requirements/{\\d+}/info',
		'requirements.versions.new' :		root + '/requirements/{\\d+}/versions/new',
		'requirements.currentversion'	:	root + '/requirement-versions/{\\d+}"',
		'requirements.statuses'			:	root + '/requirements/{\\d+}/next-status',
		'requirements.versions.manager'	:	root + '/requirements/{\\d+}/versions/manager',
		'requirements.testcases' :			root + '/requirement-versions/{\\d+}/verifying-test-cases',
		'requirements.testcases.manager':	root + '/requirement-versions/{\\d+}/verifying-test-cases/manager',
		'requirements.audittrail.change' :	root + '/audit-trail/requirement-versions/fat-prop-change-events/{\\d+}',
		'requirements.audittrail.model' :	root + '/audit-trail/requirement-versions/{\\d+}/events-table',

		'requirementversions'	:			root + '/requirement-versions/{\\d+}',

		'campaigns.workspace' :				root + '/campaign-workspace/',
		'campaigns.testplan.manager' :		root + '/campaigns/{\\d+}/test-plan//manager',
		'iterations.testplan.manager' :		root + '/iterations/{\\d+}/test-plan-manager',
		'testsuites.testplan.manager' :		root + '/test-suites/{\\d+}/test-plan-manager',

		'executions' :						root + '/executions/{\\d+}',
		'executions.generalinfos' :			root + '/executions/{\\d+}/general',
		'executions.steps' :				root + '/executions/{\\d+}/steps',
		'executions.autosteps' :			root + '/executions/{\\d+}/auto-steps',
		'executions.runner' :				root + '/executions/{\\d+}/runner',
		'execute' :							root + '/execute',
		'execute.stepbyid' :				root + '/execute/{\\d+}/step/{\\d+}',
		'execute.stepbyindex' :				root + '/execute/{\\d+}/step/index/{\\d+}',

		'administration.bugtrackers'	:	root + '/administration/bugtrackers',
		'customfield.values' :				root + '/custom-fields/values',
		'customfield.values.get' :			root + '/custom-fields/values?boundEntityId={\\d+}&boundEntityType={[A-Z_]+}',
		'denormalizefield.values.get' :		root + '/denormalized-fields/values?denormalizedFieldHolderId={\\d+}&denormalizedFieldHolderType={[A-Z_]+}',
		'administration.milestones'   :     root + '/administration/milestones',
		'administration.milestones.clone' :  root + '/administration/milestones/{\\d+}/clone',
		'milestone.bindedproject'     :     root + '/milestones-binding/milestone/{\\d+}/project?binded',
		'milestone.bindableproject'   :     root + '/milestones-binding/milestone/{\\d+}/project?bindable',
		'milestone.bind-projects-to-milestone':  root + '/milestones-binding/milestone/{\\d+}/project',
		'milestone.bind-milestones-to-project':  root + '/milestones-binding/project/{\\d+}/milestone',
		'milestone.info'							: root + '/milestones/{\\d+}/info',
		'milestone.unbind-templates'			: root + '/milestones-binding/milestone/{\\d+}/template',
		'milestone.unbind-objects'				: root + '/milestones/{\\d+}/unbindallobjects',
		'milestones.selectable' : root + '/milestones?selectable',
		'info-list.info'							: root + '/info-lists/{\\d+}',
		'info-list-item.info'					: root + '/info-list-items/{\\d+}',
		'info-list.position'					: root + '/info-lists/{\\d+}/items/positions',
		'info-list.items'						: root + '/info-lists/{\\d+}/items',
		'info-list.isUsed'						: root +  '/info-lists/{\\d+}/isUsed',
		'info-list.defaultItem'					: root + '/info-lists/{\\d+}/defaultItem ',
		'info-list-item.isUsed'					: root + '/info-list-items/{\\d+}/isUsed',
		'info-list-item.delete'					: root + '/info-lists/{\\d+}/items/{\\d+}',
		'info-list.bind-to-project'				: root + '/info-list-binding/project/{\\d+}/{\\w+}',
		'docxtemplate'							: root + '/reports/{\\w+}/{\\d+}/views/{\\d+}/docxtemplate',
		'ie9sucks'								: root + '/reports/0/0/ie9',
		'milestone.synchronize'					: root + '/administration/milestones/{\\d+}/synchronize/{\\d+}',
		'search-tc.mass-change.associable-milestone'  : root + '/advanced-search/milestones/tc-mass-modif-associables/{\\d+}',
		'search-tc.mass-change.data'  : root + '/advanced-search/milestones/tc-mass-modif-data/{\\d+}',
		'search-tc.mass-change.bindmilestones' : root + '/advanced-search/tcs/{\\d+}/milestones',
		'search-reqV.mass-change.associable-milestone'  : root + '/advanced-search/milestones/reqV-mass-modif-associables/{\\d+}',
		'search-reqV.mass-change.data'  : root + '/advanced-search/milestones/reqV-mass-modif-data/{\\d+}',
		'search-reqV.mass-change.bindmilestones' : root + '/advanced-search/reqVersions/{\\d+}/milestones',

		// helper methods
		buildURL : function(){
			var args = Array.prototype.slice.call(arguments);
			var template = this[args.shift()];

			var res = template;
			while (args.length>0) {
				res = res.replace(/\{[^\}]+\}/, args.shift());
			}

			return res;
		},

		matches : function(urlName, candidate){
			var template = this[urlName];
			var tplExp = templateToRegex(template);
			window.ex = tplExp;
			return tplExp.test(candidate.replace(/\/\//,'/'));	// we remove the double '//' that may occur sometimes
		}

	};


});