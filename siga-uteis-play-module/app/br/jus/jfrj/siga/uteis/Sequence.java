package br.jus.jfrj.siga.uteis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tem por finalidade gerar uma sequencia única para o atributo informado.
 * Utilizada em classes que derivam da classe GenericModel ou Model do pacote 
 * play.db.jpa e que implementam a interface SequenceMethods. 
 * @author jlo
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sequence
{
    String propertieOrgao();
    SiglaDocumentoType siglaDocumento(); 
}
