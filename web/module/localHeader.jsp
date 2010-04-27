<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	
	<li <c:if test='<%= request.getRequestURI().contains("pcslabinterface/pcsLabInterfaceInfo") %>'>class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/pcslabinterface/pcsLabInterfaceInfo.htm">
			<spring:message code="pcslabinterface.info"/>
		</a>
	</li>

	<openmrs:extensionPoint pointId="org.openmrs.admin.pcslabinterface.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>