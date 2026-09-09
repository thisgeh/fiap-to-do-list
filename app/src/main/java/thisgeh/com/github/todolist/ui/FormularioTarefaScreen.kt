package thisgeh.com.github.todolist.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import thisgeh.com.github.todolist.data.Tarefa
import thisgeh.com.github.todolist.util.combinarDataHora
import thisgeh.com.github.todolist.util.extrairDataDoDatePicker
import thisgeh.com.github.todolist.util.formatarDataHora
import thisgeh.com.github.todolist.viewmodel.TarefaViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioTarefaScreen(
    viewModel: TarefaViewModel,
    tarefaId: Int,
    onVoltar: () -> Unit
) {
    val tarefas by viewModel.tarefas.collectAsStateWithLifecycle()
    val tarefaExistente = remember(tarefas, tarefaId) {
        if (tarefaId != 0) tarefas.find { it.id == tarefaId } else null
    }

    var titulo by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var dataHora by rememberSaveable { mutableStateOf<Long?>(null) }
    var carregado by rememberSaveable { mutableStateOf(false) }

    // Preenche os campos uma única vez, quando a tarefa a editar chega pela StateFlow.
    LaunchedEffect(tarefaExistente) {
        if (!carregado && tarefaExistente != null) {
            titulo = tarefaExistente.titulo
            descricao = tarefaExistente.descricao
            dataHora = tarefaExistente.dataHora
            carregado = true
        }
    }

    var mostrarData by rememberSaveable { mutableStateOf(false) }
    var mostrarHora by rememberSaveable { mutableStateOf(false) }
    // Guardam a data escolhida até a hora ser confirmada.
    var anoSel by rememberSaveable { mutableStateOf(0) }
    var mesSel by rememberSaveable { mutableStateOf(0) }
    var diaSel by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tarefaId == 0) "Nova tarefa" else "Editar tarefa") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = dataHora?.let { "Prazo: ${formatarDataHora(it)}" } ?: "Sem prazo definido"
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { mostrarData = true }) {
                Text("Definir prazo")
            }
            if (dataHora != null) {
                TextButton(onClick = { dataHora = null }) {
                    Text("Remover prazo")
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val tarefa = (tarefaExistente ?: Tarefa(titulo = "", descricao = "")).copy(
                        titulo = titulo.trim(),
                        descricao = descricao.trim(),
                        dataHora = dataHora
                    )
                    if (tarefaId == 0) viewModel.inserir(tarefa) else viewModel.atualizar(tarefa)
                    onVoltar()
                },
                enabled = titulo.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }
        }
    }

    // 1) Escolha da data — o DatePicker devolve millis em UTC.
    if (mostrarData) {
        val estadoData = rememberDatePickerState(initialSelectedDateMillis = dataHora)
        DatePickerDialog(
            onDismissRequest = { mostrarData = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoData.selectedDateMillis?.let { millisUtc ->
                        val (a, m, d) = extrairDataDoDatePicker(millisUtc)
                        anoSel = a; mesSel = m; diaSel = d
                    }
                    mostrarData = false
                    mostrarHora = true
                }) { Text("Avançar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarData = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoData)
        }
    }

    // 2) Escolha da hora — combina com a data via combinarDataHora (fuso local).
    if (mostrarHora) {
        val agora = remember { Calendar.getInstance() }
        val estadoHora = rememberTimePickerState(
            initialHour = agora.get(Calendar.HOUR_OF_DAY),
            initialMinute = agora.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { mostrarHora = false },
            confirmButton = {
                TextButton(onClick = {
                    dataHora = combinarDataHora(
                        ano = anoSel, mes = mesSel, dia = diaSel,
                        hora = estadoHora.hour, minuto = estadoHora.minute
                    )
                    mostrarHora = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarHora = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = estadoHora) }
        )
    }
}