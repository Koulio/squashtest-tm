<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<c:url var="editLoginMsgUrl" value="/configuration/modify-login-message"/>


<layout:info-page-layout titleKey="admin.login-message.management.url.label" highlightedWorkspace="requirement" isSubPaged="true">
	<jsp:attribute  name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.home.title" /></h2>	
	</jsp:attribute>
		
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="admin.login-message.management.url.label" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>
	
	<jsp:attribute name="footer">	
		
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">
		<div id="login-page-content">
			<span id="login-message">${loginMessage}</span>
			<comp:rich-jeditable targetUrl="${ editLoginMsgUrl }" componentId="login-message" welcome="true" />
		</div>
	</jsp:attribute>
	
</layout:info-page-layout>