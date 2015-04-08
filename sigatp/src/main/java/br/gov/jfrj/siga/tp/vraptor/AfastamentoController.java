package br.gov.jfrj.siga.tp.vraptor;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.Localization;
import br.com.caelum.vraptor.validator.ValidationMessage;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.tp.auth.annotation.RoleAdmin;
import br.gov.jfrj.siga.tp.auth.annotation.RoleAdminMissao;
import br.gov.jfrj.siga.tp.auth.annotation.RoleAdminMissaoComplexo;
import br.gov.jfrj.siga.tp.model.Afastamento;
import br.gov.jfrj.siga.tp.model.Condutor;
import br.gov.jfrj.siga.tp.model.ItemMenu;
import br.gov.jfrj.siga.tp.model.Missao;
import br.gov.jfrj.siga.tp.model.TpDao;
import br.gov.jfrj.siga.vraptor.SigaObjects;

//@With(AutorizacaoGI.class)
@Resource
public class AfastamentoController extends TpController {

	public AfastamentoController(HttpServletRequest request, Result result, CpDao dao, Localization localization, Validator validator, SigaObjects so, EntityManager em) throws Exception {
		super(request, result, TpDao.getInstance(), localization, validator, so, em);
	}

	@Path("/app/afastamento/listar/{idCondutor}")
	public void lista(Long idCondutor) throws Exception {
		Condutor condutor = Condutor.AR.findById(idCondutor);
		List<Afastamento> afastamentos = Afastamento.buscarTodosPorCondutor(condutor);
		MenuMontador.instance(result).recuperarMenuCondutores(idCondutor, ItemMenu.AFASTAMENTOS);
		result.include("afastamentos", afastamentos);
		result.include("condutor", condutor);
	}

	@RoleAdmin
	@RoleAdminMissao
	@RoleAdminMissaoComplexo
	@Path("/app/afastamento/editar/{idCondutor}/{id}")
	public void edita(Long idCondutor, Long id) throws Exception {
		Afastamento afastamento;
		if (id == null || id == 0){
			afastamento = new Afastamento();
			Condutor condutor = Condutor.AR.findById(idCondutor);
			afastamento.setCondutor(condutor);	
		}else{
			afastamento = Afastamento.AR.findById(id);
		}
		result.include("afastamento", afastamento);
	}

	@RoleAdmin
	@RoleAdminMissao
	@RoleAdminMissaoComplexo
	//TODO Wlad @Valid
	@Path("/app/afastamento/salvar")
	public void salvar(/*@Valid*/ Afastamento afastamento) throws Exception {
		if ((afastamento.getDataHoraInicio() != null ) && (afastamento.getDataHoraFim() != null) && (!afastamento.getDescricao().equals(""))) {
			if (!afastamento.ordemDeDatasCorreta()) {
				//TODO Wlad Validation.addError("dataHoraInicio", "afastamentos.dataHoraInicio.validation");
				validator.add(new ValidationMessage("afastamentos.dataHoraInicio.validation", "dataHoraInicio"));
			}
		}
		
		if (!validator.getErrors().isEmpty()) {
			List<Condutor> condutores = Condutor.listarTodos(getTitular().getOrgaoUsuario());
			
			result.include("afastamento", afastamento);
			result.include("condutores", condutores);			
			result.redirectTo(this).edita(afastamento.getCondutor().getId(), afastamento.getId());
		} else {
			afastamento.setCondutor(Condutor.AR.findById(afastamento.getCondutor().getId()));
			List<Missao> missoes = Missao.retornarMissoes("condutor.id",
					afastamento.getCondutor().getId(),
					afastamento.getCondutor().getCpOrgaoUsuario().getId(),
					afastamento.getDataHoraInicio(), afastamento.getDataHoraFim());
			String listaMissoes = "";
			String delimitador = "";

			for (Missao item : missoes) {
				listaMissoes += delimitador;
				listaMissoes += item.getSequence();
				delimitador = ",";
			}

			if (missoes.size() > 0) {
				//TODO Wlad Validation.addError("LinkErroCondutor", listaMissoes);
				validator.add(new ValidationMessage(listaMissoes, "LinkErroCondutor"));
				
				result.include("afastamento", afastamento);
				result.redirectTo(this).edita(afastamento.getCondutor().getId(), afastamento.getId());
			} else {
				afastamento.save();
				result.redirectTo(this).lista(afastamento.getCondutor().getId());
			}
		}
	}

	@RoleAdmin
	@RoleAdminMissao
	@RoleAdminMissaoComplexo
	@Path("/app/afastamento/excluir/{id}")
	public void exclui(Long id) throws Exception {
		Afastamento afastamento = Afastamento.AR.findById(id);
		afastamento.delete();
		result.redirectTo(this).lista(afastamento.getCondutor().getId());
	}

}
