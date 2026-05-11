# ADR-013: Automação de Correção de Warnings e Code Smells

## Status
Aceito

## Contexto
O projeto enfrenta o desafio de manter a qualidade do código e a "higiene" técnica ao longo do tempo. Atualmente:
1.  Warnings de compilação, linter e análise estática (ex: SonarQube) acumulam-se silenciosamente.
2.  A correção desses itens é manual, tediosa e sujeita a erro humano.
3.  Revisões de código (PRs) muitas vezes desviam o foco da lógica de negócio para apontar problemas triviais de sintaxe ou estilo.
4.  Existe um desejo explícito de "não ficar olhando manualmente arquivo por arquivo" para corrigir esses problemas.

Necessitamos de uma solução que automatize o ciclo **Detectar -> Corrigir** para a grande maioria dos problemas de conformidade de código Java e Spring Boot.

## Opções Analisadas

### 1. Checkstyle / PMD / SpotBugs (Apenas Detecção)
*   **Pros:** Padrão de mercado, fácil integração.
*   **Contras:** Focam primariamente em *quebrar o build* quando encontram erro, não em *corrigir*. Exigem ação manual do desenvolvedor.
*   **Veredito:** Insuficiente para o objetivo de automação de correção.

### 2. IDE Inspection Profiles (IntelliJ Command Line)
*   **Pros:** Usa as mesmas inspeções poderosas da IDE.
*   **Contras:** Requer instalação da IDE no ambiente de CI/CD ou scripts complexos. Difícil de manter consistente entre desenvolvedores que usam editores diferentes (VS Code, Eclipse).
*   **Veredito:** Complexo operacionalmente.

### 3. Google Error Prone
*   **Pros:** Integra-se ao compilador, muito rápido.
*   **Contras:** Foca estritamente em bugs prováveis, não cobre estilo ou refatorações de "code smell" mais amplas. O suporte a auto-fix (Refaster) tem curva de aprendizado alta.
*   **Veredito:** Bom complemento, mas não solução completa.

### 4. OpenRewrite (Escolhido)
*   **Pros:**
    *   Ecossistema maduro de "Recipes" (Receitas) para Java e Spring.
    *   Realiza alterações na AST (Abstract Syntax Tree) preservando formatação original onde não houve mudança.
    *   Cobre desde limpeza simples (remover imports não usados) até migrações complexas de framework (JUnit 4 -> 5, Java 8 -> 17, Spring Boot 2 -> 3).
    *   Integração nativa via Maven Plugin (`mvn rewrite:run`).
*   **Contras:** Pode ser lento em projetos gigantescos (não é o nosso caso).

### 5. Spotless (Escolhido para Formatação)
*   **Pros:** Garante formatação determinística (Google Java Format, Palantir, etc.).
*   **Contras:** Apenas formata, não corrige lógica.

## Decisão
Adotaremos uma abordagem híbrida focada em **Correção Automática**:

1.  **OpenRewrite (`rewrite-maven-plugin`)**: Será a principal engine de correção semântica e de boas práticas.
    *   Será configurado para rodar receitas de "Cleanup" (limpeza de código), "Best Practices" e "Security".
    *   Executado sob demanda (ex: `mvn rewrite:run`) ou em pipelines de CI.

2.  **Spotless (`spotless-maven-plugin`)**: Será responsável pela formatação estética imutável.
    *   Garantirá indentação, quebras de linha e ordem de imports.

## Detalhes Técnicos da Implementação

### Configuração do Maven (Implementada)

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.x.x</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.java.spring.boot3.SpringBoot3BestPractices</recipe>
            <recipe>org.openrewrite.staticanalysis.CommonStaticAnalysis</recipe>
            <recipe>org.openrewrite.java.format.AutoFormat</recipe>
        </activeRecipes>
    </configuration>
</plugin>
```

### Workflow de Desenvolvimento

1.  **Desenvolvedor**: Programa a feature.
2.  **Antes do Commit (Automático)**: O Git Hook (`pre-commit`) executa automaticamente o script de qualidade.
    *   O script roda `mvn rewrite:run spotless:apply`.
    *   Se houver correções, o commit é **abortado** e o desenvolvedor é instruído a revisar e adicionar as mudanças.
3.  **Manual (Opcional)**: O desenvolvedor pode rodar `make fix` a qualquer momento para limpar o código.

## Consequências

### Positivas
*   **Redução de Technical Debt**: Warnings são resolvidos proativamente em lote.
*   **Code Review Focado**: Revisores focam em arquitetura e negócio, não em "faltou final aqui".
*   **Segurança**: Correções automáticas de vulnerabilidades conhecidas.
*   **Padronização**: Todo o código segue o mesmo estilo e práticas.
*   **Automação Total**: O desenvolvedor não precisa lembrar de rodar comandos; o hook garante a execução.

### Negativas
*   **Risco de Over-correction**: Em raros casos, uma receita pode alterar a intenção do código (falso positivo). Requer revisão do diff gerado pelo OpenRewrite.
*   **Tempo de Commit**: O commit leva alguns segundos a mais devido à execução do Maven.

## Status da Implementação
*   [x] Adicionar `rewrite-maven-plugin` ao `pom.xml`.
*   [x] Adicionar `spotless-maven-plugin` ao `pom.xml`.
*   [x] Criar script utilitário (`make fix`) para facilitar execução.
*   [x] Implementar Git Hook automatizado via `scripts/quality.sh`.
