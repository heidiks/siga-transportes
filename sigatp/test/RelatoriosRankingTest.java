import java.util.Calendar;
import java.util.List;

import models.Missao;
import models.RelatorioRanking;
import models.RequisicaoTransporte;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;
import controllers.RelatoriosRanking;

public class RelatoriosRankingTest extends UnitTest {
	
	private RelatorioRanking iniciarClasse() {
		RelatorioRanking relatorio = new RelatorioRanking();
		relatorio.quantidadeDadosRetorno = 2;
		
		relatorio.dataInicio = Calendar.getInstance();
		relatorio.dataInicio.set(2014, 9, 1); // 01/10/2014 
		
		relatorio.dataFim = Calendar.getInstance();
		relatorio.dataFim.set(2014,10,11); // 11/11/2014
		
		return relatorio;
	}
	
	@Test
	public void testRetornarCondutoresQueAtenderamMaisRequisicoes()  {
		List<models.RelatorioRanking.RankingCondutorRequisicao> rankingCondutor = null;
		
		try {
			RelatorioRanking relatorio = iniciarClasse();
			String linha = "";
			rankingCondutor = RelatoriosRanking.retornarCondutoresQueAtenderamMaisRequisicoes(relatorio);
			
			for (models.RelatorioRanking.RankingCondutorRequisicao item : rankingCondutor ) {
				linha = "Condutor : " + item.condutor.getNome() + " ";
				linha += "Missoes : ";
				
				for (Missao missao : item.missoes) {
					linha += missao.getSequence() + " ";
				}
				
				linha += "Requisicoes : ";
				
				for (RequisicaoTransporte requisicao : item.requisicoes) {
					linha += requisicao.getSequence() + " ";
				}

				System.out.println(linha);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}

		Assert.assertTrue(rankingCondutor != null);
	}

	@Test
	public void testRetornarVeiculosQueAtenderamMaisRequisicoes()  {
		List<models.RelatorioRanking.RankingVeiculoRequisicao> rankingVeiculo = null;
		
		try {
			RelatorioRanking relatorio = iniciarClasse();
			String linha = "";
			rankingVeiculo = RelatoriosRanking.retornarVeiculosQueAtenderamMaisRequisicoes(relatorio);
			
			for (models.RelatorioRanking.RankingVeiculoRequisicao item : rankingVeiculo ) {
				linha = "Veiculo : " + item.veiculo.placa + " ";
				linha += "Requisicoes : ";
				
				for (RequisicaoTransporte requisicao : item.requisicoes) {
					linha += requisicao.getSequence() + " ";
				}

				System.out.println(linha);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}

		Assert.assertTrue(rankingVeiculo != null);
	}

	@Test
	public void testRetornarFinalidadesQueAtenderamMaisRequisicoes()  {
		List<models.RelatorioRanking.RankingFinalidadeRequisicao> rankingFinalidade = null;
		
		try {
			RelatorioRanking relatorio = iniciarClasse();
			String linha = "";
			rankingFinalidade = RelatoriosRanking.retornarFinalidadesComMaisRequisicoes(relatorio);
			
			for (models.RelatorioRanking.RankingFinalidadeRequisicao item : rankingFinalidade ) {
				linha = "Finalidade : " + item.finalidade.descricao + " ";
				linha += "Total : " + item.totalFinalidade + " ";
				System.out.println(linha);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}

		Assert.assertTrue(rankingFinalidade != null);
	}

	@Test
	public void testRetornarTiposDePassageiroQueAtenderamMaisRequisicoes()  {
		List<models.RelatorioRanking.RankingTipoPassageiroRequisicao> rankingTipoDePassageiro = null;
		
		try {
			RelatorioRanking relatorio = iniciarClasse();
			String linha = "";
			rankingTipoDePassageiro = RelatoriosRanking.retornarTipoPassageiroComMaisRequisicoes(relatorio);
			
			for (models.RelatorioRanking.RankingTipoPassageiroRequisicao item : rankingTipoDePassageiro ) {
				linha = "Tipo de passageiro : " + item.tipoPassageiro + " ";
				linha += "Total : " + item.totalTipoPassageiros + " ";
				System.out.println(linha);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}

		Assert.assertTrue(rankingTipoDePassageiro != null);
	}
}