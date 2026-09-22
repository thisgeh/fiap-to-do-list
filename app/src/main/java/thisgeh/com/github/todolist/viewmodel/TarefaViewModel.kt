package thisgeh.com.github.todolist.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import thisgeh.com.github.todolist.data.Tarefa
import thisgeh.com.github.todolist.data.TarefaDatabase
import thisgeh.com.github.todolist.repository.TarefaRepository
import kotlin.collections.emptyList

class TarefaViewModel(private val repository: TarefaRepository) : ViewModel() {

    val tarefas: StateFlow<List<Tarefa>> = repository.tarefas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _tarefaParaExcluir = MutableStateFlow<Tarefa?>(null)
    val tarefaParaExcluir: StateFlow<Tarefa?> = _tarefaParaExcluir.asStateFlow()

    fun inserir(tarefa: Tarefa) = viewModelScope.launch { repository.inserir(tarefa) }

    fun atualizar(tarefa: Tarefa) = viewModelScope.launch { repository.atualizar(tarefa) }

    fun deletar(tarefa: Tarefa) = viewModelScope.launch { repository.deletar(tarefa) }

    fun solicitarExclusao(tarefa: Tarefa) {
        _tarefaParaExcluir.value = tarefa
    }

    fun cancelarExclusao() {
        _tarefaParaExcluir.value = null
    }

    fun confirmarExclusao() {
        val tarefa = _tarefaParaExcluir.value ?: return
        _tarefaParaExcluir.value = null
        deletar(tarefa)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val dao = TarefaDatabase.getDatabase(context).tarefaDao()
                    return TarefaViewModel(TarefaRepository(dao)) as T
                }
            }
    }
}