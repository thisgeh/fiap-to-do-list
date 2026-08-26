# To-Do List — Android (Jetpack Compose + Room)

Aplicativo Android de lista de tarefas desenvolvido como atividade individual da FIAP. O objetivo do projeto é evoluir uma base já existente (camada de dados com Room) implementando a camada de apresentação (UI em Jetpack Compose), a integração com a arquitetura (Repository e ViewModel) e a navegação entre telas (Navigation Compose), permitindo ao usuário **listar, criar, editar, concluir e excluir tarefas**.

## Tecnologias utilizadas

- **Kotlin** — linguagem principal do projeto.
- **Jetpack Compose** — construção declarativa da interface (telas, componentes e previews).
- **Room** — persistência local das tarefas em banco SQLite (`Tarefa`, `TarefaDao`, `TarefaDatabase`).
- **Coroutines / Flow** — operações assíncronas de banco de dados e observação reativa da lista de tarefas (`Flow` → `StateFlow`).
- **ViewModel** — retenção de estado da UI sobrevivendo a mudanças de configuração.
- **Navigation Compose** — navegação entre a tela de lista e a tela de formulário.

## Arquitetura

O projeto segue o padrão **UI (Compose) → ViewModel → Repository → DAO (Room)**:

```
ListaTarefasScreen ─┐
                     ├─► TarefaViewModel ─► TarefaRepository ─► TarefaDao ─► Room (tarefas.db)
FormularioTarefaScreen ─┘
```

### TarefaRepository

Fica em `repository/TarefaRepository.kt` e é a camada intermediária entre a ViewModel e o banco de dados. Sua responsabilidade é abstrair o acesso ao `TarefaDao`, expondo:

- `tarefas: Flow<List<Tarefa>>` — fluxo reativo com todas as tarefas cadastradas, ordenadas por data de criação.
- `suspend fun inserir(tarefa: Tarefa)` — insere uma nova tarefa.
- `suspend fun atualizar(tarefa: Tarefa)` — atualiza uma tarefa existente (usado tanto ao editar título/descrição quanto ao marcar/desmarcar como concluída).
- `suspend fun deletar(tarefa: Tarefa)` — remove uma tarefa.

Ela não conhece a UI nem o Android Framework diretamente (exceto pelo `TarefaDao` injetado), o que mantém a lógica de acesso a dados isolada e testável.

### TarefaViewModel

Fica em `viewmodel/TarefaViewModel.kt` e é responsável por conectar o Repository à camada de apresentação:

- Expõe `tarefas: StateFlow<List<Tarefa>>`, convertendo o `Flow` do Repository em um `StateFlow` (via `stateIn`) com `SharingStarted.WhileSubscribed(5_000)`, evitando que a coleta continue rodando sem tela ativa.
- Expõe as funções `inserir`, `atualizar` e `deletar`, cada uma disparando uma coroutine em `viewModelScope.launch` para não bloquear a thread principal.
- Possui uma `factory` (companion object) que cria a instância da ViewModel resolvendo a dependência do `TarefaDatabase`/`TarefaDao` e passando o `TarefaRepository` já construído — assim a `MainActivity` não precisa conhecer os detalhes de criação do banco.

### ListaTarefasScreen

Fica em `ui/ListaTarefasScreen.kt`. Observa o estado da ViewModel com `collectAsStateWithLifecycle()` e repassa a lista para o composable `ListaTarefasContent`, que:

- Renderiza as tarefas em uma `LazyColumn`, cada uma em um `Card` com `Checkbox` (concluir/desmarcar), título com `TextDecoration.LineThrough` quando concluída, descrição e um `IconButton` de exclusão.
- Clicar no card aciona a edição (`onEditarTarefa`), passando o `id` da tarefa.
- Um `FloatingActionButton` aciona `onNovaTarefa` para abrir o formulário de cadastro.
- Toda ação do usuário (concluir, editar, excluir, nova tarefa) é repassada como callback para a `ListaTarefasScreen`, que traduz em chamadas à `TarefaViewModel` (`atualizar`, `deletar`) — o composable de conteúdo (`ListaTarefasContent`) não conhece a ViewModel diretamente, o que facilita os `@Preview`s.

### FormularioTarefaScreen

Fica em `ui/FormularioTarefaScreen.kt` e atende tanto o cadastro quanto a edição de uma tarefa através de um único parâmetro, `tarefaId`:

- Quando `tarefaId == 0`, o formulário está em **modo de cadastro**: os campos começam vazios e, ao salvar, chama `viewModel.inserir(...)`.
- Quando `tarefaId != 0`, o formulário está em **modo de edição**: busca a tarefa correspondente na lista observada da ViewModel (`tarefas.find { it.id == tarefaId }`), pré-preenche os campos de título e descrição e, ao salvar, chama `viewModel.atualizar(...)` mantendo o `id` original.
- Em ambos os casos, ao salvar ou ao clicar no ícone de voltar da `TopAppBar`, a navegação retorna para a tela anterior (`onVoltar`).

### AppNavigation

Fica em `navegation/AppNavigation.kt` e define o grafo de navegação com `NavHost`:

| Rota | Tela | Observação |
|---|---|---|
| `"lista"` (rota inicial) | `ListaTarefasScreen` | Navega para `"formulario/0"` (nova tarefa) ou `"formulario/{id}"` (editar) |
| `"formulario/{tarefaId}"` | `FormularioTarefaScreen` | Lê o argumento `tarefaId` da rota e repassa para o formulário; `0` indica cadastro, qualquer outro valor indica edição do registro com aquele id |

Essa passagem do `id` pela própria rota é o que permite que a `FormularioTarefaScreen` decida, sozinha, se está em modo de criação ou edição.

### MainActivity

Fica em `MainActivity.kt` e é o ponto de entrada do app. Em `onCreate`, dentro de `setContent`:

1. Cria a `TarefaViewModel` usando `viewModel(factory = TarefaViewModel.factory(applicationContext))`, garantindo que a ViewModel receba o `TarefaRepository` já conectado ao `TarefaDao` do Room.
2. Chama `AppNavigation(viewModel = viewModel)` como conteúdo raiz da tela, substituindo por completo o conteúdo de exemplo gerado pelo template padrão do Android Studio.

## Como executar o projeto

1. Abra a pasta do projeto no **Android Studio** (versão compatível com AGP 9.2.1 / Kotlin 2.2.10).
2. Deixe o Gradle sincronizar as dependências (Room, Navigation Compose, Compose BOM, etc.).
3. Selecione um emulador ou dispositivo físico com **API 24+**.
4. Rode o app (▶️ Run 'app'). Na primeira execução a lista aparecerá vazia — use o botão **+** para cadastrar a primeira tarefa.

## Funcionalidades

- ✅ Listar tarefas cadastradas (lista reativa via `Flow`/`StateFlow`).
- ✅ Cadastrar nova tarefa.
- ✅ Editar tarefa existente.
- ✅ Marcar/desmarcar tarefa como concluída.
- ✅ Excluir tarefa.
- ✅ Navegar entre a lista e o formulário sem encerrar o app.
- ✅ Persistência local dos dados via Room.

## Evidências

As imagens abaixo estão na pasta [`prints/`](./prints) na raiz do repositório.

### Tela inicial com a lista de tarefas

![Tela inicial](/prints/telainicial.png)

### Cadastro de uma nova tarefa

![Tela de cadastro](/prints/teladecadastramento.png)

### Tarefa cadastrada aparecendo na lista

![Lista com tarefa](/prints/telacomtarefa.png)

### Tarefa marcada como concluída

![Tarefa concluída](/prints/tarefaconcluida.png)