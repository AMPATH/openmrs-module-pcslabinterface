<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Audit" otherwise="/login.htm" redirect="/module/pcslabinterface/pcsLabInterfaceInfo.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<br />
<h2><spring:message code="pcslabinterface.info"/></h2>
<br />

<openmrs:portlet
    url="globalProperties"
    parameters="title=${title}|propertyPrefix=pcslabinterface.|excludePrefix=pcslabinterface.started|hidePrefix=true|readOnly=false"/>

<br/>
<br/>
<%@ include file="/WEB-INF/template/footer.jsp" %>