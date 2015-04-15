<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>
<%@ taglib prefix="sigatp" tagdir="/WEB-INF/tags/" %>

<siga:pagina titulo="Transportes">
	<div class="gt-bd clearfix">
		<div class="gt-content clearfix">
			<h2>
				<c:if test="${fixarVeiculo}">
					${avaria.veiculo.dadosParaExibicao} - 
				</c:if>
				${modo} <fmt:message key="avaria" />
			</h2>
			<jsp:include page="form.jsp" />
		</div>
	</div>
</siga:pagina>