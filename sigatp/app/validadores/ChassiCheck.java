package validadores;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.data.validation.Check;

public class ChassiCheck extends Check {
	


	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		// TODO Auto-generated method stub
		this.setMessage("Chassi Inválido");
    	return validarChassi(((String) value).toString());
 	}
	
	public Boolean validarChassi(String valor) {
		/* 
		Criticar o chassi sempre que: 
		1 - Possuir o número "0" (ZERO) como 1º dígito. 
		2 - Possuir espaço no chassi 
		3 - Se, a partir do 4º dígito, houver uma repetição consecutiva, por mais de seis vezes, do mesmo dígito (alfabético ou numérico). Exemplos: 9BW11111119452687 e 9BWZZZ5268AAAAAAA. 
		4 - Apresente os caracteres "i", "I", "o", "O", "q", "Q". 
		5 - Os quatro últimos caracteres devem ser obrigatoriamente numéricos 
		6 - Se possuir número de dígitos diferente de 17 (alfanuméricos).  
		*/  
		Pattern zeroNoPrimeiroDigito = Pattern.compile ("^0");  
		Pattern espacoNoChassi = Pattern.compile (" ");  
		Pattern repeticaoMaisDe6Vezes = Pattern.compile ("^.{4,}([0-9A-Z])\\1{5,}");  
		Pattern caracteresiIoOqQ = Pattern.compile ("[iIoOqQ]");  
		Pattern ultimos4Numericos = Pattern.compile ("[0-9]{4}$");
		
		Matcher MzeroNoPrimeiroDigito = zeroNoPrimeiroDigito.matcher(valor);
		Matcher MespacoNoChassi = espacoNoChassi.matcher(valor);
		Matcher MrepeticaoMaisDe6Vezes = repeticaoMaisDe6Vezes.matcher(valor);
		Matcher McaracteresiIoOqQ = caracteresiIoOqQ.matcher(valor);
		Matcher Multimos4Numericos = ultimos4Numericos.matcher(valor);
		
	    return true;
	//	return (MzeroNoPrimeiroDigito.find()) && (MespacoNoChassi.find())
	//			&& (MrepeticaoMaisDe6Vezes.find()) && (McaracteresiIoOqQ.find())
	//			&& (Multimos4Numericos.find());
    }	
	
;
    	

}
