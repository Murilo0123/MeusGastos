# ARQUITETURA.md — Meus Gastos (MVVM)

> **RASCUNHO.** Reescreva com as suas palavras antes de entregar. O tutorial
> pede respostas curtas e próprias. Use este texto como base para checar se
> o raciocínio está certo.

---

## 1. Se o app precisar buscar os gastos de uma API em vez da memória, quais arquivos mudam? Quais NÃO mudam?

**Mudam:**

- `model/GastoRepository.kt` (ou um arquivo novo no `model/`): crio uma nova
  classe, por exemplo `GastoRepositoryApi : GastoRepository`, que faz a
  requisição e converte a resposta em `Gasto`.
- O lugar onde o repositório é escolhido: o valor padrão no construtor do
  `GastosViewModel`, que é uma troca de uma palavra, como na Etapa 6.
- `AndroidManifest.xml`: permissão de internet.

**Com certeza NÃO mudam:**

- Todos os arquivos de `ui/`: `TelaGastos`, `FormularioGasto`, `LinhaGasto`,
  `ResumoTotal`, `Etiqueta`.
- `Gasto.kt` e `Categoria.kt`.
- As regras do ViewModel: validação, filtro e total. Elas trabalham com
  `List<Gasto>` e não sabem de onde ela veio.

**Ressalva:** chamada de rede é lenta e não pode rodar na thread principal.
Se eu precisar tornar `listar()` uma função `suspend`, o contrato muda e o
`sincronizar()` do ViewModel também (passa a usar `viewModelScope.launch`).
Mesmo assim, a pasta `ui/` continua intocada, porque a tela só lê o estado
do ViewModel.

---

## 2. Por que o total é calculado e não guardado? Bug concreto se fosse variável atualizada manualmente.

Porque um valor calculado não tem como ficar dessincronizado dos dados dos
quais ele deriva. Uma variável guardada é uma cópia, e eu preciso lembrar de
atualizá-la em todo lugar que mexe na lista.

**Bug concreto (Estrago 3):** adiciono gastos de R$ 10, R$ 20 e R$ 30, e o
total mostra R$ 60. Removo o de R$ 30. A lista tem 2 itens, a quantidade
mostra 2, mas o total continua R$ 60, porque esqueci de atualizar no
`remover()`. O mesmo acontece com o filtro: se o total é uma variável, ele
não passa a refletir só a categoria selecionada. Calculado a partir de
`gastosVisiveis`, ele acompanha adicionar, remover e filtrar sem nenhuma linha
extra.

---

## 3. O FormularioGasto não recebe o ViewModel. O que isso me permitiu fazer que não seria possível se recebesse?

- **Desenhar a tela de erro sem provocar o erro.** O `@Preview`
  `FormularioComErroPreview` passa `erro = "O valor precisa ser um número"`
  direto, sem rodar o app e sem digitar nada.
- **Criar vários previews** do mesmo componente (vazio, com erro, com
  categoria selecionada) só variando os parâmetros. Se ele recebesse o
  ViewModel, o preview precisaria construir um ViewModel (e um repositório)
  só para desenhar o formulário.
- **Reaproveitar em outra tela:** ele só recebe valores e devolve eventos,
  então serve para qualquer tela com qualquer fonte de estado.
- **Testar o componente isolado**, passando os valores que eu quiser.
