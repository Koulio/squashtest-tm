/**
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
package org.squashtest.tm.web.internal.report;

import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;
import org.squashtest.tm.api.report.BasicReport;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.ReportPlugin;
import org.squashtest.tm.api.report.ReportView;
import org.squashtest.tm.api.report.jasperreports.JasperReportsView;
import org.squashtest.tm.web.internal.report.ReportViewServletContextInitializer;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class ReportViewServletContextInitializerTest extends Specification {
	ReportViewServletContextInitializer registry = new ReportViewServletContextInitializer()
	ServletContext servletContext = Mock()
	
	def setup() {
		registry.setServletContext servletContext
	}

	def "should bind servlet context to view definition"() {
		given:
		// Spock Mock interaction checks not worky. Could it be because of downcast in tested code ? 
		JasperReportsMultiFormatView viewDef = new JasperReportsMultiFormatView()

		ReportView view = new JasperReportsView(springView: viewDef) 

		Report report = new BasicReport(views: [view])

		ReportPlugin plugin = new ReportPlugin(reports: [report])

		and:
		Map pluginProps = ['osgi.service.blueprint.compname' : 'awesome report']

		when:
		registry.registerViews plugin, pluginProps

		then:
		viewDef.servletContext == servletContext
	}

	def "should unbind servlet context from view definition"() {
		given:
		JasperReportsMultiFormatView viewDef = new JasperReportsMultiFormatView(servletContext: servletContext)

		ReportView view = Mock()
		view.springView >> viewDef

		Report report = Mock()
		report.views >> [view]

		ReportPlugin plugin = Mock()
		plugin.reports >> [report]

		and:
		Map pluginProps = ['osgi.service.blueprint.compname' : 'awesome report']
		
		when:
		registry.unregisterViews plugin, pluginProps
		// throws ex because no context
		viewDef.servletContext

		then:
		thrown IllegalStateException
	}
}
