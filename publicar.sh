#!/bin/bash

. ~/.bash_profile

cd ~/workspace/siga-transportes/sigatp

export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=512m"

mvn clean 

mvn install -Dsiga.versao=develop -DskipTests

if [ $? -ne 0 ]
then
  echo ""
  echo ""
  echo ""
  echo "Erro ao gerar pacote! Saindo..."
  exit
fi

cp -f ../target/sigatp.war /usr/local/jboss/standalone/deployments/

if [ $? -ne 0 ]
then
  echo ""
  echo ""
  echo ""
  echo "Erro ao copiar pacote para o diretorio deployments do JBoss! Saindo..."
  exit
fi

echo ""
echo ""
echo ""
echo "Projeto publicado com sucesso. Saindo..."
exit
