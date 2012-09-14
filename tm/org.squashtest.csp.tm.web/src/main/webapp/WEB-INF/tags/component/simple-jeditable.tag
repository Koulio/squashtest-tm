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
<%-- call rich-editable-init first --%>
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="targetUrl" required="true" %>
<%@ attribute name="componentId" required="true" %>
<%@ attribute name="submitCallback" required="false" %>
<%@ attribute name="maxLength" required="false" description="max number of characters" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<script type="text/javascript">

	$(function() {
		$( '#${componentId}' ).editable( '${ targetUrl }', {
			type: 'text',												//this input type isn't standard, refers to jquery.jeditable.ckeditor.js
			cols: 80,
			max_size: 20,
			placeholder: '<f:message key="rich-edit.placeholder" />',
			submit: '<f:message key="rich-edit.button.ok.label" />',
			cancel: '<f:message key="label.Cancel" />',
			<c:if test="${ not empty maxLength }" >
			maxlength: ${ maxLength },
			</c:if>
			onblur : function(){},											//this disable the onBlur handler, which would close the jeditable 
																			//when clicking in the rich editor (since it considers the click as			
																			//out of the editing zone)
			
			<c:if test="${ not empty submitCallback }">
			callback : function(value, settings){
				${submitCallback}(value, settings);
			},
			</c:if>
			indicator : '<img src="${ pageContext.servletContext.contextPath }/scripts/jquery/indicator.gif" alt="processing..." />' 
			
		}).addClass("editable");
	})
	
</script>