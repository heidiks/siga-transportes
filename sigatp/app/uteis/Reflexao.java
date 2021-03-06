package uteis;

import java.lang.reflect.Field;

import br.jus.jfrj.siga.uteis.Sequence;

public class Reflexao {

	public static String invocaMetodo(String nomeDaClasseCompleto, Object objetoQueContemOMetodo, String nomeDoMetodo,
			String parametro) throws Exception {
		
		String dataHoraSaidaStr=null;
		Class<?> classe = Class.forName( nomeDaClasseCompleto );  
		dataHoraSaidaStr = (String) classe.getDeclaredMethod(nomeDoMetodo).invoke(objetoQueContemOMetodo,parametro);
	
		return dataHoraSaidaStr;
	}
	
	public static String recuperaAnotacaoField(Object objeto)  {
		String annotation = "";
		Field[] fields = objeto.getClass().getDeclaredFields();
		for (Field f : fields)
		{
		        if (f.isAnnotationPresent(Sequence.class))
		    {
		        	Sequence ta = f.getAnnotation(Sequence.class);
		        	annotation = ta.siglaDocumento().name();
		    }
		}
		return annotation;
	}

}
