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
- Jackson 3.x: Spring Boot 4 usa tools.jackson.core:jackson-databind. O pacote mudou de com.fasterxml.jackson.* para tools.jackson.* (ex.: tools.jackson.databind.ObjectMapper). Nunca importar com.fasterxml.*.
- Mockito: NUNCA usar mock-maker-subclass nem arquivos em mockito-extensions. O agente do Mockito é carregado via -javaagent no argLine do maven-surefire-plugin (o caminho do usuário tem espaço e acento, o que quebra a anexação dinâmica). Mantenha esse argLine igual nos dois projetos.

IDENTIDADE VISUAL
Do TCC copiamos arquitetura, organização de código e padrões de implementação; a aparência do GestãoLab é própria e NÃO deve reproduzir a do TCC (nem o sidebar escuro, nem o verde, nem o layout de login do AgroTrack).

Paleta de cores (variáveis em :root no gestaolab.css):
- Fundo geral:         #F5F8FA  (--gl-bg)
- Superfícies/cartões: #FFFFFF  (--gl-surface)
- Bordas:              #E2E8F0  (--gl-border)
- Texto principal:     #0F172A  (--gl-text)
- Texto secundário:    #64748B  (--gl-text-secondary)
- Primária azul-petróleo: #0F766E (--gl-primary), hover #115E59, fundo suave #E6F4F1
- Alerta âmbar:        #B45309 / fundo #FEF3C7
- Perigo:              #B91C1C / fundo #FEE2E2
- Sucesso:             #15803D / fundo #DCFCE7

Tipografia:
- Fonte de texto: "Inter" (Google Fonts) com fallback system-ui.
- Dados numéricos (quantidades, horários, durações, códigos): fonte monoespaçada
  com font-variant-numeric: tabular-nums (classe .gl-mono ou variável --gl-mono).

Menu lateral (sidebar):
- Fundo branco, borda direita 1px em --gl-border.
- Logo no topo: ícone frasco SVG inline + texto "GestãoLab" em bold.
- Sempre expandido no desktop; recolhível (toggle) em telas < 992px.
- Item ativo: fundo #E6F4F1, texto #0F766E, barra vertical de 3px à esquerda.
- Hover: fundo --gl-bg, texto --gl-text. Sem hover que expanda o sidebar.
- Rodapé: nome e perfil do usuário.

Topbar: branco, borda inferior 1px em --gl-border. Título à esquerda; usuário,
perfil e botão sair à direita. Sem barra colorida no topo.

Cartões: branco, border 1px --gl-border, border-radius 10px, sombra sutil
(box-shadow 0 1px 4px rgba(15,23,42,.05)). Hover eleva levemente.

Status: sempre como "chips" arredondados (.gl-badge) com as cores semânticas
da paleta acima. Nunca texto colorido solto.

Ícones: usar Bootstrap Icons relacionados a laboratório onde disponível:
bi-eyedropper (materiais), bi-clipboard2-pulse (uso do laboratório),
bi-thermometer (equipamentos), bi-activity (relatórios).
Evitar ícones de máquinas agrícolas ou contextos fora de laboratório.

Login / Trocar Senha: duas colunas. Painel esquerdo: fundo azul-petróleo
(#0F766E) com padrão SVG hexagonal sutil (hexágonos de contorno em branco
a ~9% de opacidade), logo + frase; padrão definido como inline SVG com
viewBox 400×700 e preserveAspectRatio="xMidYMid slice". Painel direito: fundo
branco, formulário centralizado. Em telas ≤ 768px mostra só o formulário.

Acessibilidade: contraste mínimo WCAG AA. :focus-visible com outline 2px
em --gl-primary. aria-hidden="true" em todos os ícones decorativos.