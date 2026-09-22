# Evidências — Confirmação de exclusão de tarefas

Este documento registra, em sequência, o novo fluxo de exclusão com confirmação.
Ao tocar no ícone de lixeira, a tarefa **não** é mais excluída imediatamente: um
`AlertDialog` (Material 3) é exibido sobre a própria tela da lista, mostrando o título
da tarefa selecionada e as opções **Cancelar** e **Excluir**.

As imagens foram capturadas no emulador (Pixel 7) e estão em [`docs/prints/exclusao/`](docs/prints/exclusao/).

## 1. Lista antes da exclusão

Lista com cinco tarefas cadastradas (atv1 a atv5), ordenadas por prazo, antes de qualquer tentativa de exclusão.

![Lista antes da exclusão](docs/prints/exclusao/01-lista-antes.png)

## 2. Diálogo aberto com a tarefa selecionada

Após tocar na lixeira da tarefa **atv1**, o diálogo de confirmação é exibido sobre a lista,
informando que a tarefa será excluída permanentemente e apresentando o seu título.

![Diálogo de confirmação aberto](docs/prints/exclusao/02-dialogo-aberto.png)

## 3. Resultado ao cancelar

Ao tocar em **Cancelar**, o diálogo é fechado e a lista permanece inalterada: as cinco tarefas continuam cadastradas.

![Resultado ao cancelar](docs/prints/exclusao/03-apos-cancelar.png)

## 4. Nova abertura do diálogo

O diálogo é aberto novamente, desta vez tocando na lixeira da tarefa **atv2**. O título exibido
corresponde à tarefa selecionada.

![Nova abertura do diálogo](docs/prints/exclusao/04-dialogo-reaberto.png)

## 5. Resultado após confirmar a exclusão

Ao tocar em **Excluir**, somente a tarefa **atv2** é removida e o diálogo é fechado.
As demais tarefas (atv1, atv3, atv4 e atv5) continuam na lista, com seus prazos e a ordenação preservados.

![Resultado após confirmar a exclusão](docs/prints/exclusao/05-apos-excluir.png)