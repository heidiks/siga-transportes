package br.gov.jfrj.siga.tp.vraptor.job;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.tasks.Task;
import br.gov.jfrj.siga.base.Correio;
import br.gov.jfrj.siga.base.SigaBaseProperties;
import br.gov.jfrj.siga.dp.DpPessoa;
import br.gov.jfrj.siga.tp.model.Andamento;
import br.gov.jfrj.siga.tp.model.Condutor;
import br.gov.jfrj.siga.tp.model.Parametro;
import br.gov.jfrj.siga.tp.util.FormataCaminhoDoContextoUrl;


@ApplicationScoped
public class WorkFlowNotificacao  implements Task  {
	
	private static final String espacosHtml = "&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String CRON_EXECUTA_WORKFLOW = "cron.executaw";
    private static final String CRON_LISTA_EMAIL = "cron.listaEmailw";
	
	@Override
	public void execute() 
	{
		System.out.println(">>>>>>>> Mensagem produzida pela task!");

		CustomScheduler.criaEntityManager();
		
		String executa = Parametro.buscarConfigSistemaEmVigor(CRON_EXECUTA_WORKFLOW);
		
		if (executa.toUpperCase().equals("FALSE")) {
			verificarAndamentoDaRequisicao();
		}
		else {
			//Logger.info("Servi�o de Nofitifica��o do WorkFlow desligado");
			Logger.getLogger("Servi�o de Nofitifica��o do WorkFlow desligado");
		}
		//Logger.info("Servi�o de Nofitifica��o do WorkFlow finalizado");
		Logger.getLogger("Servi�o de Nofitifica��o do WorkFlow finalizado");
	}
	
	
	private void verificarAndamentoDaRequisicao() {
		List<Andamento> andamentos = new ArrayList<Andamento>();
		String tituloEmail = "Notificacoes do andamento de requisi��es para o WorkFlow do SIGA-DOC";
		String tipoNotificacao = "notificadas ao SIGA-DOC";
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -7);
			andamentos = Andamento.AR.find("dataNotificacaoWorkFlow IS NULL and requisicaoTransporte in (select r from RequisicaoTransporte r where origemExterna = true)").fetch();
			notificarAndamentos(andamentos, tituloEmail, tipoNotificacao);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void notificarAndamentos(List<Andamento> andamentos, String titulo, String notificacao) throws Exception  {
		Condutor condutor = new Condutor();
		HashMap<Condutor, String> dadosCondutor = new HashMap<Condutor, String>();

		for(Andamento item : andamentos) {
			String sequencia = item.getRequisicaoTransporte().buscarSequence() + " " + item.getRequisicaoTransporte().getId() + ",";

			if (dadosCondutor.containsKey(condutor)) {
				dadosCondutor.put(condutor, dadosCondutor.get(condutor) + sequencia);
			}
			else {
				dadosCondutor.put(condutor, sequencia);
			}
		}

		if (dadosCondutor.size() > 0) {
			enviarEmail(titulo, notificacao, dadosCondutor);
		}
	}

	public static String substituirMarcacoesMensagem(String titulo, String notificacao, String lista, Object pessoa) {
		String sexo = "";
		String nome = "";
		String parteMensagem = "";
		Boolean plural = lista.split(",").length > 1 ? true : false;
		String mensagem;

		if (pessoa.getClass().equals(Condutor.class)) {
			sexo = "M";//((Condutor)pessoa).getDpPessoa().getSexo().toUpperCase();
			nome = "TESTE";//((Condutor)pessoa).getNome();

			if (titulo.contains("Missoes")) {
				parteMensagem = plural ? "as miss&otilde;es " : "a miss&atilde;o ";

				if (notificacao.contains("Nao finalizada")) {
					parteMensagem += "abaixo, caso j&aacute; tenha/m sido realizada/s, " +
								    "precisa/m ser finalizada/s.<br>";

				}
				else if (notificacao.contains("Nao iniciada")) {
					parteMensagem += "abaixo precisa/m ser iniciada/s ou cancelada/s.<br>";
				}
			}
		}
		else if(pessoa.getClass().equals(DpPessoa.class)) {
			sexo = "F";//((DpPessoa)pessoa).getSexo().toUpperCase();
			nome = "TESTE";//((DpPessoa)pessoa).getNomePessoa();

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
		String hostName = InetAddress.getLocalHost().getHostName();
		final String finalMensagem = "Att.<br>M&oacute;dulo de Transportes do Siga.<br><br>" +
		   		"Aten&ccedil;&atilde;o: esta &eacute; uma mensagem autom&aacute;tica. Por favor, n&atilde;o responda.";

		Set<Object> itensKey = (Set<Object>) dados.keySet();

		for(Object item : itensKey){
			String mensagemAlterada = substituirMarcacoesMensagem(titulo, notificacao, dados.get(item), item);
			String conteudoHTML = "<html>" + mensagemAlterada;
			String[] lista = dados.get(item).split(",");

			for (String itemLista : lista) {
	    		Boolean primeiraVez = true;
				String sequence =  itemLista.substring(0, itemLista.indexOf(" "));
				String id = itemLista.substring(itemLista.indexOf(" ") + 1);
				List<String> parametros = new ArrayList<String>();

				if (titulo.contains("Missoes")) {
					if (notificacao.contains("Nao finalizada")) {
						parametros.add("id," + id + ",Missoes.finalizar,Finalizar");
					}
					else if (notificacao.contains("Nao iniciada")) {
						parametros.add("id," + id + ",Missoes.iniciar,Iniciar");
						parametros.add("id," + id + ",Missoes.cancelar,Cancelar");
					}
				}

				if (titulo.contains("Requisicoes")) {
					if (notificacao.contains("Pendente aprovar")) {
						parametros.add("id," + id + ",Andamentos.autorizar,Autorizar");
						parametros.add("id," + id + ",Andamentos.rejeitar,Rejeitar");
					}
				}

				for (String parametro : parametros) {
					String[] itens = parametro.split(",");
					Map<String,Object> param = new HashMap<String, Object>();
					param.put(itens[0], itens[1]);

					FormataCaminhoDoContextoUrl formata = new FormataCaminhoDoContextoUrl();
					String caminhoUrl = "";//formata.retornarCaminhoContextoUrl(Router.getFullUrl(itens[2],param));

					conteudoHTML += (primeiraVez ? "<p>" + sequence : "") + espacosHtml +
								    "<a href='" + "http://" + hostName + caminhoUrl + "'>" + itens[3] + "</a>" +
								    espacosHtml;
					primeiraVez = false;
				}
			}

			conteudoHTML += "</p>";
			String remetente = SigaBaseProperties.getString("servidor.smtp.usuario.remetente");
			String assunto = titulo;
			String email = "";
			String destinatario[];
			email = "guilherme.rufino@db1.com.br";//Parametro.buscarConfigSistemaEmVigor(CRON_LISTA_EMAIL);
			destinatario = email.split(",");


			conteudoHTML += finalMensagem + "</html>";
			String conteudo = retirarTagsHtml(conteudoHTML);

			Correio.enviar(remetente, destinatario, assunto, conteudo, conteudoHTML);
			SimpleDateFormat fr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			
			Logger.getLogger("sigatp.workflow").info(fr.format(calendar.getTime()) + " - Email enviado para " + email + ", assunto: " + assunto);
			//Logger.info(fr.format(calendar.getTime()) + " - Email enviado para " + email + ", assunto: " + assunto);
		}
	}
	
}