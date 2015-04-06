<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://localhost/jeetags" prefix="siga" %>

<script type="text/javascript" language="Javascript1.1">
function verificaCampos(){
	var dataHoraInicio = document.getElementById("dataHoraInicio").value;
	var dataHoraFim = document.getElementById("dataHoraFim").value;
		
	if (dataHoraInicio == "" || dataHoraFim == ""){
		alert("Uma ou mais datas estão sem preenchimento.");
		return false;
	}
	else{
		dataHoraInicio = new Date(dataHoraInicio);
		dataHoraFim = new Date(dataHoraFim);
		if (dataHoraInicio > dataHoraFim) {
			alert("A data e hora do fim do plantão são anteriores ao início.");
			return false;
		}
		return true;
	}	
}
</script>
<siga:pagina titulo="Transportes">
	<div class="gt-content clearfix">
		<h2>${plantao.condutor.dadosParaExibicao} - ${plantao.id != null ? "Editar": "Inserir" } Plant&atilde;o</h2>
<%-- 		#{form @Plantoes.salvar(), enctype:'multipart/form-data', id:'formPlantoes' } --%>
		<form name="formulario" id="formulario" action="${linkTo[PlantaoController].salvar}" method="post" cssClass="form">
			<input type="hidden" name="plantao.id" value="${plantao.id}" />
			<input type="hidden" name="plantao.condutor.id" value="${plantao.condutor.id}">
			<div class="gt-content-box gt-form clearfix">
				<label for="plantao.dataHoraInicio" class= "obrigatorio">In&iacute;cio</label>
				<input type="text" id="dataHoraInicio" name="plantao.dataHoraInicio" value="<%-- ${plantao.dataHoraInicio?.format('dd/MM/yyyy HH:mm')} --%>" size="14" class="dataHora" />
		   		<input type="hidden" id="dataHoraInicioNova" name="plantao.dataHoraInicioNova" value="<%-- ${plantao.dataHoraInicio?.format('dd/MM/yyyy HH:mm')} --%>" size="14" class="dataHora" />
				<label for="plantao.dataHoraFim" class= "obrigatorio">Fim</label>
				<input type="text" id="dataHoraFim" name="plantao.dataHoraFim" value="<%-- ${plantao.dataHoraFim?.format('dd/MM/yyyy HH:mm')} --%>" size="14" class="dataHora" />
				<input type="hidden" id="dataHoraFimNova" name="plantao.dataHoraFimNova" value="<%-- ${plantao.dataHoraFim?.format('dd/MM/yyyy HH:mm')} --%>" size="14" class="dataHora" />
			</div>
			<span class="alerta menor">* Preenchimento obrigat&oacute;rio</span>
			<div class="gt-table-buttons">
<!-- 			<input type="submit" value="Salvar" class="gt-btn-medium gt-btn-left" onClick="return verificaCampos()" /> -->
		 		<input type="submit" value="<fmt:message key="views.botoes.salvar"/>" class="gt-btn-medium gt-btn-left" />
				<input type="button" value="<fmt:message key="views.botoes.cancelar"/>" onClick="javascript:window.location = 'listar';" class="gt-btn-medium gt-btn-left" />
			</div>
		${plantao}
		aaaaa
		</form>
	</div>
</siga:pagina>



