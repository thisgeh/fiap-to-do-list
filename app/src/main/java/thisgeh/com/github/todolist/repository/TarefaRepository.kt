package thisgeh.com.github.todolist.repository

import kotlinx.coroutines.flow.Flow
import thisgeh.com.github.todolist.data.Tarefa
import thisgeh.com.github.todolist.data.TarefaDao

class TarefaRepository {
    fun inserir(tarefa: Tarefa) {}
    fun atualizar(tarefa: Tarefa) {}
    fun deletar(tarefa: Tarefa) {}

    class TarefaRepository(private val dao: TarefaDao) {

        val tarefas: Flow<List<Tarefa>> = dao.listarTodas()

        suspend fun inserir(tarefa: Tarefa) = dao.inserir(tarefa)

        suspend fun atualizar(tarefa: Tarefa) = dao.atualizar(tarefa)

        suspend fun deletar(tarefa: Tarefa) = dao.deletar(tarefa)
    }
}