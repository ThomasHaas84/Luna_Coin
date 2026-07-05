package de.meson_labs.luna_coin.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.components.dialogs.ConfirmationDialog
import de.meson_labs.luna_coin.components.tasks.DateSelector
import de.meson_labs.luna_coin.components.tasks.DogPlanSection
import de.meson_labs.luna_coin.components.tasks.EmptyCard
import de.meson_labs.luna_coin.components.tasks.TaskCard
import de.meson_labs.luna_coin.components.tasks.isTaskVisibleForChildAndDate
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.TaskItem
import java.time.LocalDate

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    data: LunaCoinData,
    selectedChild: Child?,
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onCompleteTask: (String) -> Unit,
    onCompleteDogPlanTask: (
        templateId: String,
        date: String,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ) -> Unit = { _, _, _, _, _, _ -> },
    onAssignDogPlanEarlyShift: (date: String, childId: String) -> Unit = { _, _ -> },
    onAssignDogPlanLateShift: (date: String, childId: String) -> Unit = { _, _ -> },
    onClearDogPlanEarlyShift: (date: String) -> Unit = {},
    onClearDogPlanLateShift: (date: String) -> Unit = {},
    onLogout: () -> Unit,
    canGoToPreviousDay: Boolean,
    canGoToNextDay: Boolean
) {
    val configuration = LocalConfiguration.current
    val isTabletLayout = configuration.smallestScreenWidthDp >= 600
    val isPhone = !isTabletLayout

    val screenPadding = if (isPhone) {
        14.dp
    } else {
        16.dp
    }

    val childId = selectedChild?.id

    val tasksForDate = data.tasks.filter { task ->
        if (childId == null) {
            false
        } else {
            isTaskVisibleForChildAndDate(
                task = task,
                childId = childId,
                date = selectedDate
            )
        }
    }

    var selectedArea by remember { mutableStateOf(TaskArea.HOUSEHOLD) }
    var taskToComplete by remember { mutableStateOf<TaskItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(screenPadding)
    ) {
        LunaScreenHeader(
            title = "Aufgaben",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 6.dp))

        DateSelector(
            selectedDate = selectedDate,
            onPreviousDay = onPreviousDay,
            onNextDay = onNextDay,
            onToday = onToday,
            canGoToPreviousDay = canGoToPreviousDay,
            canGoToNextDay = canGoToNextDay
        )

        Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 8.dp))

        TaskAreaSwitch(
            selectedArea = selectedArea,
            isPhone = isPhone,
            onSelectedAreaChanged = { selectedArea = it }
        )

        Spacer(modifier = Modifier.height(if (isPhone) 10.dp else 8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedArea) {
                TaskArea.HOUSEHOLD -> {
                    if (tasksForDate.isEmpty()) {
                        item {
                            EmptyCard(
                                text = "Für diesen Tag gibt es keine Aufgaben."
                            )
                        }
                    } else {
                        items(tasksForDate) { task ->
                            TaskCard(
                                task = task,
                                children = data.children,
                                selectedChild = selectedChild,
                                selectedDate = selectedDate,
                                onCompleteTask = { taskToComplete = task }
                            )
                        }
                    }
                }

                TaskArea.DOG_PLAN -> {
                    item {
                        DogPlanSection(
                            dogPlan = data.dogPlan,
                            children = data.children,
                            selectedChild = selectedChild,
                            selectedDate = selectedDate,
                            onCompleteDogPlanTask = onCompleteDogPlanTask,
                            onAssignEarlyShift = onAssignDogPlanEarlyShift,
                            onAssignLateShift = onAssignDogPlanLateShift,
                            onClearEarlyShift = onClearDogPlanEarlyShift,
                            onClearLateShift = onClearDogPlanLateShift
                        )
                    }
                }
            }
        }
    }

    taskToComplete?.let { task ->
        ConfirmationDialog(
            title = "Aufgabe abschließen?",
            message = "Möchtest du '${task.title}' wirklich als erledigt markieren?",
            confirmText = "Ja, erledigt",
            dismissText = "Abbrechen",
            onConfirm = {
                onCompleteTask(task.id)
                taskToComplete = null
            },
            onDismiss = {
                taskToComplete = null
            }
        )
    }
}

@Composable
private fun TaskAreaSwitch(
    selectedArea: TaskArea,
    isPhone: Boolean,
    onSelectedAreaChanged: (TaskArea) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskAreaButton(
            text = if (isPhone) "🧹 Haushalt" else "🧹 Haushaltsaufgaben",
            selected = selectedArea == TaskArea.HOUSEHOLD,
            modifier = Modifier.weight(1f),
            isPhone = isPhone,
            onClick = { onSelectedAreaChanged(TaskArea.HOUSEHOLD) }
        )

        TaskAreaButton(
            text = if (isPhone) "🐶 Hund" else "🐶 Hundeplan",
            selected = selectedArea == TaskArea.DOG_PLAN,
            modifier = Modifier.weight(1f),
            isPhone = isPhone,
            onClick = { onSelectedAreaChanged(TaskArea.DOG_PLAN) }
        )
    }
}

@Composable
private fun TaskAreaButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    isPhone: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(if (isPhone) 48.dp else 42.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(if (isPhone) 48.dp else 42.dp)
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private enum class TaskArea {
    HOUSEHOLD,
    DOG_PLAN
}
