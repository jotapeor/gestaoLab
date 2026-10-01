CONTEXTO FIXO DO PROJETO
Sistema comercial de gestão de laboratório de faculdade, dividido em dois projetos Spring Boot 4.1.1 / Java 25, que seguem a MESMA arquitetura, stack, padrões de pacotes e estilo de código do meu TCC (AgroTrack), usado apenas como referência (NUNCA altere nada nele):
- TCC back-end: <C:\Users\João Paulo\Documents\tcc\codigo\agroTrackBackEnd>\agroTrackBackEnd  (pacote com.main.frotaBackEnd)
- TCC front-end: <C:\Users\João Paulo\Documents\tcc\codigo\agroTrackFrontEnd>\agroTrackFrontEnd (pacote com.main.frotaFrontEnd)

PROJETOS EM QUE VOCÊ TRABALHA (somente estes):
- Repositório Git (raiz): C:\Users\João Paulo\Documents\gestaolab
- Back-end (API REST, porta 8080, pacote com.main.gestaolabback):   C:\Users\João Paulo\Documents\gestaolab\gestaoLabBack
- Front-end (Thymeleaf, porta 8081, pacote com.main.gestaolabfront): C:\Users\João Paulo\Documents\gestaolab\gestaoLabFront
O front NUNCA acessa o banco; ele consome a API via RestClient com o token JWT guardado na HttpSession, como no TCC.

REGRAS DE GIT
- Antes de começar: git checkout main, git pull, e crie a branch indicada na tarefa a partir da main.
- Faça commits pequenos e coerentes, com mensagens em português no padrão Conventional Commits (feat:, fix:, chore:, test:, refactor:).
- NÃO faça push, merge nem rebase. Eu reviso e subo.
- Trabalhe apenas no escopo da tarefa desta branch. Não altere arquivos fora do escopo, nem mesmo finais de linha ou formatação (em especial o schema.sql).
- Ao renomear ou mover pacotes/pastas, apague as pastas antigas que ficarem vazias.
- Nomes de branch sem acentos nem "ç".

REGRAS DO BANCO (MySQL 8.0, banco gestaolab_db)
- gestaoLabBack\schema.sql é um dump do MySQL Workbench e é a fonte da verdade da estrutura. NUNCA edite esse arquivo e NUNCA execute comandos no banco.
- Se alguma alteração no banco for necessária, crie um arquivo NOVO em gestaoLabBack\sql\ com o próximo número disponível (ex.: 001_descricao.sql), contendo apenas os comandos da alteração, com USE gestaolab_db; no início, sem DROP DATABASE e sem DROP TABLE de tabelas existentes. Eu importo esse arquivo no MySQL Workbench.
- Não use ddl-auto para criar ou alterar tabelas (exceto H2 nos testes).

AMBIENTE
- Windows: use .\mvnw.cmd. Confirme com .\mvnw.cmd -v que o Maven está usando o JDK 25; se não estiver, me avise em vez de alterar o pom.

ENTREGA DE TODA TAREFA
- Rode .\mvnw.cmd clean verify no(s) projeto(s) alterado(s) e corrija até passar.
- No fim, informe: nome da branch, lista de commits, arquivos criados/alterados, arquivos .sql criados em gestaoLabBack\sql\ (se houver), como eu testo manualmente o que foi feito e qualquer decisão tomada sem estar especificada.

NOTAS TÉCNICAS (Spring Boot 4.1.1)
- @DataJpaTest: org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
- TestEntityManager: org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
- @AutoConfigureTestDatabase: org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
- Antes de concluir que uma classe/anotação "não existe" no Boot 4, procure o pacote novo no jar dentro do .m2.