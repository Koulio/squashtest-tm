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
<%@ tag body-content="scriptless" %>
<%@ attribute name="hasRole" required="true" %>
<%@ attribute name="hasPermission" required="true" %>
<%@ attribute name="domainObject" required="true" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ss" uri="http://www.springframework.org/security/tags" %>

<ss:authorize ifNotGranted="${ hasRole }">
	<ss:accesscontrollist hasPermission="${ hasPermission }" domainObject="${ domainObject }">
		<c:set var="accessGranted" value="true" />
	</ss:accesscontrollist>
	<c:if test="${ not accessGranted }">
		<jsp:doBody />
	</c:if>
	<c:remove var="accessGranted"/>
</ss:authorize>
