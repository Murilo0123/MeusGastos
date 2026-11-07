## Estrago 1 — O construtor no lugar do `viewModel()`

```kotlin
vm: GastosViewModel = GastosViewModel()
```

**Previsão:** o ViewModel vai ser recriado e a lista vai "esquecer" o que eu cadastrei.

**O que aconteceu:** Adicionei letras na descrição e a tela foi restaurada, fiz outros teste e tive o mesmo resultado.

**Mensagem literal:** nenhuma.

**Por quê:** `GastosViewModel()` é uma chamada comum de construtor. Nada liga esse objeto ao ciclo de vida da tela. O valor padrão de um parâmetro `@Composable` é calculado de novo quando a função recompõe, então cada recomposição cria um ViewModel novo, com repositório novo e estado zerado. O `viewModel()` é diferente: ele pede a instância ao `ViewModelStoreOwner` (a Activity), que cria uma vez e devolve sempre a mesma, inclusive depois de girar a tela.

---

## Estrago 2 — A lista comum no ViewModel

```kotlin
private val _gastos = mutableListOf<Gasto>()
```

**Previsão:** vou adicionar um gasto e ele não vai aparecer na lista. O Compose não sabe que a lista mudou.

**O que aconteceu:** Adicionei os gastos, o total foi atualizado, mas só apareceu na listagem quando atualizei a tela.

**Mensagem literal:** nenhuma.

**Por quê:** o Compose só redesenha quando lê um valor de estado observável (`mutableStateOf`, `mutableStateListOf`). Uma `MutableList` comum muda sem avisar ninguém. Os dados estão certos, mas a tela não é notificada. É o oposto do que `mutableStateListOf` faz.

---

## Estrago 3 — O total guardado em variável

```kotlin
var total by mutableStateOf(0.0)   // atualizado só no adicionar()
```

**Previsão:** depois de remover um gasto, o total continua o mesmo.

**O que aconteceu:** Adicionando ou removendo gastos, o valor continua R$ 0,00.

**Mensagem literal:** nenhuma.

**Por quê:** `total` virou uma cópia do dado, e uma cópia precisa ser atualizada à mão em todo lugar que mexe na lista: adicionar, remover, filtrar, e qualquer função futura.

---

## Estrago 4 — O ViewModel dependendo da implementação

```kotlin
private val repositorio = GastoRepositoryMemoria()   // sem a interface
```

**Previsão:** o app continua funcionando.

**O que aconteceu:** Tudo funciona igual.

**Mensagem literal:** nenhuma.

**Por quê:** a interface `GastoRepository` era o contrato entre ViewModel e dados. Sem ela, o ViewModel fica preso a uma classe específica. Fica mais difícil: (1) trocar por API ou Room sem editar o ViewModel; (2) testar o ViewModel com um repositório falso; (3) usar dados de exemplo em preview. E com Room o problema piora: a implementação precisa de Context/banco, e o ViewModel teria que criar isso sozinho, quebrando a regra de que o ViewModel não conhece Context.

---

## Estrago 5 — A regra de volta para a tela

```kotlin
Button(onClick = { if (vm.descricao.isNotBlank()) { ... } })
```

**Previsão:** funciona mas não aparece mensagem de erro.

**O que aconteceu:** Funcionou, mas não apareceu a mensagem pedindo par preencher a descrição.

**Mensagem literal:** nenhuma.

**Por quê:** a regra passou a morar na View. Se existisse uma segunda tela que também cadastra gastos, eu teria que copiar o `if` para lá, e as duas telas podiam divergir (uma valida, a outra não; uma mostra erro, a outra não). Não dá para testar a regra sem abrir a tela, e a mensagem de erro teria de ser guardada em estado na própria UI, no lugar errado. No ViewModel a regra existe uma vez só, é testável com `vm.adicionar()` e vale para qualquer tela.
