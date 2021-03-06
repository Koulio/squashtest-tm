<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2016 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="sq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<layout:info-page-layout titleKey="workspace.campaign.title" highlightedWorkspace="campaign">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.purple.css" />
	</jsp:attribute>
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.campaign.title" /></h2>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">	
		<jsp:include page="/WEB-INF/jsp/fragment/executions/execution.jsp">
			<jsp:param name="hasBackButton" value="true" />
		</jsp:include>
	</jsp:attribute>
	
</layout:info-page-layout>