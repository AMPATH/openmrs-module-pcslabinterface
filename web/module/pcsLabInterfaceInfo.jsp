<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Audit" otherwise="/login.htm" redirect="/module/pcslabinterface/pcsLabInterfaceInfo.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<!-- TODO - OpenmrsClassLoader not being used to load in classes here.  Must do it ourselves :-/ -->


<%@ page import="org.openmrs.api.context.Context" %>
<%
	Class pcsLabInterfaceService = org.openmrs.util.OpenmrsClassLoader.getInstance().loadClass("org.openmrs.module.pcslabinterface.PcsLabInterfaceService");
	pageContext.setAttribute("pcsLabInterfaceService", Context.getService(pcsLabInterfaceService));
%>
	
<br />
<h2><spring:message code="pcslabinterface.info"/></h2>
<br />

<table cellpadding="4" cellspacing="0">
	<tr>
		<th><spring:message code="SystemInfo.name"/></th>
		<th><spring:message code="SystemInfo.value"/></th>
	</tr>
	<c:forEach items="${pcsLabInterfaceService.systemVariables}" var="var" varStatus="status">
		<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td>${var.key}</td>
			<td>${var.value}</td>
		</tr>
	</c:forEach>
</table>

<br/><br/>
<%@ include file="/WEB-INF/template/footer.jsp" %>