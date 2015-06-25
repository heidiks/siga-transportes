package br.gov.jfrj.siga.tp.vraptor.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.tasks.Task;
import br.gov.jfrj.siga.base.Correio;
import br.gov.jfrj.siga.base.SigaBaseProperties;
import br.gov.jfrj.siga.cp.CpConfiguracao;
import br.gov.jfrj.siga.cp.CpServico;
import br.gov.jfrj.siga.cp.CpSituacaoConfiguracao;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.tp.model.Condutor;
import br.gov.jfrj.siga.tp.model.Parametro;
import br.gov.jfrj.siga.tp.model.TpDao;
import br.gov.jfrj.siga.tp.model.EstadoMissao;
import br.gov.jfrj.siga.tp.model.EstadoRequisicao;
import br.gov.jfrj.siga.tp.model.Missao;
import br.gov.jfrj.siga.tp.model.RequisicaoTransporte;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@ApplicationScoped
public class EmailNotificacao implements Task {
	private static final String espacosHtml = "&nbsp;&nbsp;&nbsp;&nbsp;";

	private static final String CRON_EXECUTA = "cron.executa";
	private static final String CRON_LISTAEMAIL = "cron.listaEmail";
	private static final String CRON_FLAGEMAIL = "cron.flagEmail";
	private static final String SIGATP_EMAIL = "sigatp.email";
	private static final String CAMINHO_HOSTNAME_STANDALONE = "caminhoHostnameStandalone";

	@Override
	public void execute() {
		try {
			CustomScheduler.criaEntityManager();

			String executa = Parametro.buscarConfigSistemaEmVigor(CRON_EXECUTA);

			if (executa.toUpperCase().equals("TRUE")) {
				verificarMissoesProgramadas();
				verificarMissoesIniciadasMaisDe7Dias();
				verificarRequisicoesPendentesDeAprovacao();
			} else {
				Logger.getLogger(SIGATP_EMAIL).info("Servi�o desligado");
			}
			Logger.getLogger(SIGATP_EMAIL).info("Servi�o finalizado");
		} catch (Exception e) {
			Logger.getLogger(SIGATP_EMAIL).info("Erro ao criar Entity: " + e.getMessage());
		} finally {
			CustomScheduler.fecharEntityManager();
		}
	}

	private void verificarMissoesProgramadas() {
		List<Missao> missoes = new ArrayList<Missao>();
		String tituloEmail = "Missoes programadas nao iniciadas";
		String tipoNotificacao = "Nao iniciada";

		try {
			Calendar calendar = Calendar.getInstance();
			missoes = Missao.AR.find("estadoMissao = ? and dataHoraSaida < ? " + "order by condutor", EstadoMissao.PROGRAMADA, calendar).fetch();
			notificarMissoes(missoes, tituloEmail, tipoNotificacao);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void verificarMissoesIniciadasMaisDe7Dias() {
		List<Missao> missoes = new ArrayList<Missao>();
		String tituloEmail = "Missoes iniciadas a mais de 7 dias nao finalizadas";
		String tipoNotificacao = "Nao finalizada";

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -7);
			missoes = Missao.AR.find("estadoMissao = ? and dataHoraSaida < ? " + "order by condutor", EstadoMissao.INICIADA, calendar).fetch();
			notificarMissoes(missoes, tituloEmail, tipoNotificacao);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void verificarRequisicoesPendentesDeAprovacao() {
		List<RequisicaoTransporte> requisicoes = new ArrayList<RequisicaoTransporte>();
		String tituloEmail = "Requisicoes pendentes de aprovacao";
		String tipoNotificacao = "Pendente aprovar";

		try {
			final Calendar calendar = Calendar.getInstance();
			requisicoes = RequisicaoTransporte.listar(EstadoRequisicao.ABERTA);

			if (requisicoes.size() > 0) {
				List<RequisicaoTransporte> requisicoesFiltradas = Lists.newArrayList(Iterables.filter(requisicoes, new Predicate<RequisicaoTransporte>() {
					public boolean apply(RequisicaoTransporte requisicao) {
						return requisicao.getDataHoraSaidaPrevista().after(calendar);
					}
				}));

				if (requisicoesFiltradas.size() > 0) {
					notificarRequisicoes(requisicoesFiltradas, tituloEmail, tipoNotificacao);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static List<DpPessoa> retornarAprovadores(CpServico servico, Long stConfiguracao) {
		List<CpConfiguracao> configuracoes = new ArrayList<CpConfiguracao>();
		Set<DpPessoa> setAprovador = new HashSet<DpPessoa>();
		List<DpPessoa> aprovadores = new ArrayList<DpPessoa>();

		configuracoes = TpDao.find(CpConfiguracao.class, "cpServico.idServico = ? and " + "cpSituacaoConfiguracao.idSitConfiguracao = ? and " + "hisDtFim is null", servico.getIdServico(),
				stConfiguracao).fetch();

		for (CpConfiguracao cpConfiguracao : configuracoes) {
			if (cpConfiguracao.getDpPessoa() != null) {
				setAprovador.add(cpConfiguracao.getDpPessoa());
			} else if (cpConfiguracao.getLotacao() != null) {
				aprovadores = DpPessoa.AR.find("lotacao.idLotacao = ? and dataFimPessoa is null ", cpConfiguracao.getLotacao().getIdLotacao()).fetch();
				setAprovador.addAll(aprovadores);
			}
		}

		return new ArrayList<DpPessoa>(setAprovador);
	}

	private static void notificarMissoes(List<Missao> missoes, String titulo, String notificacao) throws Exception {
		Condutor condutor = new Condutor();
		HashMap<Condutor, String> dadosCondutor = new HashMap<Condutor, String>();

		for (Missao item : missoes) {
			condutor = item.getCondutor();
			String sequencia = item.getSequence() + " " + item.getId() + ",";

			if (dadosCondutor.containsKey(condutor)) {
				dadosCondutor.put(condutor, dadosCondutor.get(condutor) + sequencia);
			} else {
				dadosCondutor.put(condutor, sequencia);
			}
		}

		if (dadosCondutor.size() > 0) {
			enviarEmail(titulo, notificacao, dadosCondutor);
		}
	}

	private static void notificarRequisicoes(List<RequisicaoTransporte> requisicoes, String titulo, String notificacao) throws Exception {
		List<DpPessoa> lstAprovadores = new ArrayList<DpPessoa>();
		DpPessoa aprovador = new DpPessoa();
		HashMap<DpPessoa, String> dadosAprovador = new HashMap<DpPessoa, String>();

		CpServico servico = TpDao.find(CpServico.class, "siglaServico = ?", "SIGA-TP-APR").first();
		Long stConfiguracao = CpSituacaoConfiguracao.SITUACAO_PODE;
		lstAprovadores = retornarAprovadores(servico, stConfiguracao);
		DpPessoa[] arrayAprovador = lstAprovadores.toArray(new DpPessoa[lstAprovadores.size()]);
		Comparator<DpPessoa> comp = null;

		for (RequisicaoTransporte item : requisicoes) {
			aprovador = item.getUltimoAndamento().getResponsavel();
			int index = Arrays.binarySearch(arrayAprovador, aprovador, comp);
			DpPessoa chave = lstAprovadores.get(index);
			String sequencia = item.buscarSequence() + " " + item.getId() + ",";

			if (dadosAprovador.containsKey(lstAprovadores.get(index))) {
				dadosAprovador.put(chave, dadosAprovador.get(chave) + sequencia);
			} else {
				dadosAprovador.put(chave, sequencia);
			}
		}

		if (dadosAprovador.size() > 0) {
			enviarEmail(titulo, notificacao, dadosAprovador);
		}
	}

	public static String substituirMarcacoesMensagem(String titulo, String notificacao, String lista, Object pessoa) {
		String sexo = "";
		String nome = "";
		String parteMensagem = "";
		Boolean plural = lista.split(",").length > 1 ? true : false;
		String mensagem;

		if (pessoa.getClass().equals(Condutor.class)) {
			sexo = ((Condutor) pessoa).getDpPessoa().getSexo().toUpperCase();
			nome = ((Condutor) pessoa).getNome();

			if (titulo.contains("Missoes")) {
				parteMensagem = plural ? "as miss&otilde;es " : "a miss&atilde;o ";

				if (notificacao.contains("Nao finalizada")) {
					parteMensagem += "abaixo, caso j&aacute; tenha/m sido realizada/s, " + "precisa/m ser finalizada/s.<br>";

				} else if (notificacao.contains("Nao iniciada")) {
					parteMensagem += "abaixo precisa/m ser iniciada/s ou cancelada/s.<br>";
				}
			}
		} else if (pessoa.getClass().equals(DpPessoa.class)) {
			sexo = ((DpPessoa) pessoa).getSexo().toUpperCase();
			nome = ((DpPessoa) pessoa).getNomePessoa();

			if (titulo.contains("Requisicoes")) {
				parteMensagem = plural ? "as requisi&ccedil;&otilde;es " : "a requisi&ccedil;&atilde;o ";

				if (notificacao.contains("Pendente aprovar")) {
					parteMensagem += "abaixo precisa/m ser autorizada/s ou rejeitada/s.<br>";
				}
			}
		}

		mensagem = sexo.equals("F") ? "Prezada Sra. " : "Prezado Sr. " + nome + ", ";
		mensagem += parteMensagem.replaceAll("/s", plural ? "s" : "").replaceAll("/m", plural ? "m" : "");
		return mensagem;
	}

	private static String retirarTagsHtml(String conteudo) {
		String retorno = conteudo.replace("<br>", "\n");
		retorno = retorno.replace("&aacute", "�");
		retorno = retorno.replace("&eacute", "�");
		retorno = retorno.replace("&oacute", "�");
		retorno = retorno.replace("&iacute", "�");
		retorno = retorno.replace("&uacute", "�");
		retorno = retorno.replace("&atilde", "�");
		retorno = retorno.replace("&otilde", "�");
		retorno = retorno.replace("&ccedil", "�");
		retorno = retorno.replace("<html>", "");
		retorno = retorno.replace("</html>", "");
		retorno = retorno.replace("<p>", "");
		retorno = retorno.replace("</p>", "\n");
		retorno = retorno.replace(espacosHtml, "");
		retorno = retorno.replace("</a href=", "");
		retorno = retorno.replace(">", "");
		retorno = retorno.replace("'", "");
		retorno = retorno.replace("</a>", "");
		return retorno;
	}

	@SuppressWarnings("unchecked")
	private static void enviarEmail(String titulo, String notificacao, HashMap<?, String> dados) throws Exception {
		String hostName = System.getProperty(Parametro.buscarConfigSistemaEmVigor(CAMINHO_HOSTNAME_STANDALONE));
		final String finalMensagem = "Att.<br>M&oacute;dulo de Transportes do Siga.<br><br>" + "Aten&ccedil;&atilde;o: esta &eacute; uma mensagem autom&aacute;tica. Por favor, n&atilde;o responda.";

		Set<Object> itensKey = (Set<Object>) dados.keySet();

		for (Object item : itensKey) {
			String mensagemAlterada = substituirMarcacoesMensagem(titulo, notificacao, dados.get(item), item);
			String conteudoHTML = "<html>" + mensagemAlterada;
			String[] lista = dados.get(item).split(",");

			for (String itemLista : lista) {
				Boolean primeiraVez = true;
				String sequence = itemLista.substring(0, itemLista.indexOf(" "));
				String id = itemLista.substring(itemLista.indexOf(" ") + 1);
				List<String> parametros = new ArrayList<String>();

				if (titulo.contains("Missoes")) {
					if (notificacao.contains("Nao finalizada")) {
						parametros.add("id," + id + ",sigatp/app/missao/finalizar,Finalizar");
					} else if (notificacao.contains("Nao iniciada")) {
						parametros.add("id," + id + ",sigatp/app/missao/iniciar,Iniciar");
						parametros.add("id," + id + ",sigatp/app/missao/cancelar,Cancelar");
					}
				}

				if (titulo.contains("Requisicoes")) {
					if (notificacao.contains("Pendente aprovar")) {
						parametros.add("id," + id + ",sigatp/app/andamento/autorizar,Autorizar");
						parametros.add("id," + id + ",sigatp/app/andamento/rejeitar,Rejeitar");
					}
				}

				for (String parametro : parametros) {
					String[] itens = parametro.split(",");

					String caminhoUrl = "/" + itens[2] + "/" + itens[1];

					conteudoHTML += (primeiraVez ? "<p>" + sequence : "") + espacosHtml + "<a href='" + hostName + caminhoUrl + "'>" + itens[3] + "</a>" + espacosHtml;
					primeiraVez = false;
				}
			}

			conteudoHTML += "</p>";
			String remetente = SigaBaseProperties.getString("servidor.smtp.usuario.remetente");
			String assunto = titulo;
			String email = "";
			String destinatario[];
			String flagEmail = Parametro.buscarConfigSistemaEmVigor(CRON_FLAGEMAIL);

			if (!flagEmail.toUpperCase().equals("TRUE")) {
				if (item.getClass().equals(Condutor.class)) {
					email = ((Condutor) item).getDpPessoa().getEmailPessoa();
				} else if (item.getClass().equals(DpPessoa.class)) {
					email = ((DpPessoa) item).getEmailPessoa();
				}
				destinatario = new String[1];
				destinatario[0] = email;
			} else {
				email = Parametro.buscarConfigSistemaEmVigor(CRON_LISTAEMAIL);
				destinatario = email.split(",");
			}

			conteudoHTML += finalMensagem + "</html>";
			String conteudo = retirarTagsHtml(conteudoHTML);

			Correio.enviar(remetente, destinatario, assunto, conteudo, conteudoHTML);
			SimpleDateFormat fr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			Logger.getLogger(SIGATP_EMAIL).info(fr.format(calendar.getTime()) + " - Email enviado para " + email + ", assunto: " + assunto);

		}
	}
}