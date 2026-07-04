package de.meson_labs.luna_coin.components.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanShift
import de.meson_labs.luna_coin.models.DogPlanTaskCompletion
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogPlanTaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DogPlanSection(
    dogPlan: DogPlanData,
    children: List<Child>,
    selectedChild: Child?,
    selectedDate: LocalDate,
    onCompleteDogPlanTask: (
        templateId: String,
        date: String,
        peed: Boolean,
        pooped: Boolean,
        diarrhea: Boolean,
        comment: String
    ) -> Unit,
    onAssignEarlyShift: (date: String, childId: String) -> Unit = { _, _ -> },
    onAssignLateShift: (date: String, childId: String) -> Unit = { _, _ -> },
    onClearEarlyShift: (date: String) -> Unit = {},
    onClearLateShift: (date: String) -> Unit = {}
) {
    val selectedDateText = selectedDate.toString()
    val isToday = selectedDate == LocalDate.now()
    val canPlanShift = selectedChild != null && !selectedDate.isBefore(LocalDate.now())

    val activeTemplates = dogPlan.templates
        .filter { it.isActive }
        .sortedWith(
            compareBy<DogPlanTaskTemplate> { it.sortOrder }
                .thenBy { it.time }
                .thenBy { it.title }
        )

    val feedingTemplates = activeTemplates.filter {
        it.type == DogPlanTaskType.FEEDING_EARLY ||
                it.type == DogPlanTaskType.FEEDING_LATE
    }

    val walkTemplates = activeTemplates.filter {
        it.type == DogPlanTaskType.WALK
    }

    val otherTemplates = activeTemplates.filter {
        it.type == DogPlanTaskType.OTHER
    }

    val shift = dogPlan.shifts.firstOrNull { it.date == selectedDateText }

    var templateForCompletion by remember { mutableStateOf<DogPlanTaskTemplate?>(null) }
    var shiftDialogMode by remember { mutableStateOf<DogPlanShiftMode?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        DogPlanGroupTitle(text = "👤 Schichten")

        DogPlanShiftRow(
            title = "Frühschicht",
            assignedName = shift?.earlyShiftChildId.toChildName(children),
            canEdit = canPlanShift,
            onClick = { shiftDialogMode = DogPlanShiftMode.EARLY }
        )

        DogPlanShiftRow(
            title = "Spätschicht",
            assignedName = shift?.lateShiftChildId.toChildName(children),
            canEdit = canPlanShift,
            onClick = { shiftDialogMode = DogPlanShiftMode.LATE }
        )

        if (!canPlanShift) {
            Text(
                text = "Schichten können nur ab heute für die Zukunft geändert werden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTemplates.isEmpty()) {
            EmptyCard(text = "Es sind noch keine Hundeplan-Aufgaben eingerichtet.")
        } else {
            if (feedingTemplates.isNotEmpty()) {
                DogPlanGroupTitle(text = "🍖 Füttern")
                feedingTemplates.forEach { template ->
                    DogPlanTaskRow(
                        template = template,
                        completion = dogPlan.completions.findCompletion(template.id, selectedDateText),
                        children = children,
                        canComplete = selectedChild != null && isToday,
                        onClick = { templateForCompletion = template }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (walkTemplates.isNotEmpty()) {
                DogPlanGroupTitle(text = "🚶 Gassi")
                walkTemplates.forEach { template ->
                    DogPlanTaskRow(
                        template = template,
                        completion = dogPlan.completions.findCompletion(template.id, selectedDateText),
                        children = children,
                        canComplete = selectedChild != null && isToday,
                        onClick = { templateForCompletion = template }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (otherTemplates.isNotEmpty()) {
                DogPlanGroupTitle(text = "🐾 Sonstiges")
                otherTemplates.forEach { template ->
                    DogPlanTaskRow(
                        template = template,
                        completion = dogPlan.completions.findCompletion(template.id, selectedDateText),
                        children = children,
                        canComplete = selectedChild != null && isToday,
                        onClick = { templateForCompletion = template }
                    )
                }
            }
        }
    }

    templateForCompletion?.let { template ->
        DogPlanCompletionDialog(
            template = template,
            onDismiss = { templateForCompletion = null },
            onConfirm = { peed, pooped, diarrhea, comment ->
                onCompleteDogPlanTask(
                    template.id,
                    selectedDateText,
                    peed,
                    pooped,
                    diarrhea,
                    comment
                )
                templateForCompletion = null
            }
        )
    }

    shiftDialogMode?.let { mode ->
        DogPlanShiftSelectionDialog(
            mode = mode,
            children = children,
            currentShift = shift,
            onDismiss = { shiftDialogMode = null },
            onSelectChild = { childId ->
                when (mode) {
                    DogPlanShiftMode.EARLY -> onAssignEarlyShift(selectedDateText, childId)
                    DogPlanShiftMode.LATE -> onAssignLateShift(selectedDateText, childId)
                }
                shiftDialogMode = null
            },
            onClear = {
                when (mode) {
                    DogPlanShiftMode.EARLY -> onClearEarlyShift(selectedDateText)
                    DogPlanShiftMode.LATE -> onClearLateShift(selectedDateText)
                }
                shiftDialogMode = null
            }
        )
    }
}

@Composable
private fun DogPlanShiftRow(
    title: String,
    assignedName: String?,
    canEdit: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = assignedName != null,
                onCheckedChange = {
                    if (canEdit) {
                        onClick()
                    }
                },
                enabled = canEdit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = assignedName ?: "Noch nicht eingetragen",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (assignedName == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            OutlinedButton(
                onClick = onClick,
                enabled = canEdit
            ) {
                Text(if (assignedName == null) "Eintragen" else "Ändern")
            }
        }
    }
}

@Composable
private fun DogPlanGroupTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DogPlanTaskRow(
    template: DogPlanTaskTemplate,
    completion: DogPlanTaskCompletion?,
    children: List<Child>,
    canComplete: Boolean,
    onClick: () -> Unit
) {
    val completedByName = completion?.completedByChildId?.let { id ->
        children.firstOrNull { it.id == id }?.name
    }

    val enabled = completion == null && canComplete

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = completion != null,
                onCheckedChange = {
                    if (enabled) {
                        onClick()
                    }
                },
                enabled = enabled || completion != null
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (template.time.isNotBlank()) {
                    Text(
                        text = "Uhrzeit: ${template.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    CoinDisplay(
                        amount = template.rewardCoins,
                        showPlus = true,
                        coinSize = 28.dp
                    )

                    if (completion == null && !canComplete) {
                        Text(
                            text = " · nur heute abhakbar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (completion != null) {
                    DogPlanCompletionInfo(
                        completion = completion,
                        completedByName = completedByName
                    )
                }
            }
        }
    }
}

@Composable
private fun DogPlanCompletionInfo(
    completion: DogPlanTaskCompletion,
    completedByName: String?
) {
    val timeText = completion.completedAt.toDisplayTime()

    Text(
        text = buildString {
            append("Erledigt")
            completedByName?.let { append(" von $it") }
            if (timeText.isNotBlank()) {
                append(" · $timeText")
            }
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 2.dp)
    )

    val details = mutableListOf<String>()
    if (completion.peed) details += "gepinkelt"
    if (completion.pooped) details += "gekackt"
    if (completion.diarrhea) details += "Durchfall"

    if (details.isNotEmpty()) {
        Text(
            text = details.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (completion.comment.isNotBlank()) {
        Text(
            text = completion.comment,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DogPlanShiftSelectionDialog(
    mode: DogPlanShiftMode,
    children: List<Child>,
    currentShift: DogPlanShift?,
    onDismiss: () -> Unit,
    onSelectChild: (String) -> Unit,
    onClear: () -> Unit
) {
    val title = when (mode) {
        DogPlanShiftMode.EARLY -> "Frühschicht eintragen"
        DogPlanShiftMode.LATE -> "Spätschicht eintragen"
    }

    val currentChildId = when (mode) {
        DogPlanShiftMode.EARLY -> currentShift?.earlyShiftChildId
        DogPlanShiftMode.LATE -> currentShift?.lateShiftChildId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                children.forEach { child ->
                    OutlinedButton(
                        onClick = { onSelectChild(child.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (child.id == currentChildId) {
                                "✓ ${child.name}"
                            } else {
                                child.name
                            }
                        )
                    }
                }

                if (currentChildId != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eintragung entfernen")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

private enum class DogPlanShiftMode {
    EARLY,
    LATE
}

private fun String?.toChildName(children: List<Child>): String? {
    val childId = this ?: return null
    return children.firstOrNull { it.id == childId }?.name
}

private fun List<DogPlanTaskCompletion>.findCompletion(
    templateId: String,
    date: String
): DogPlanTaskCompletion? {
    return firstOrNull { completion ->
        completion.templateId == templateId && completion.date == date
    }
}

private fun String.toDisplayTime(): String {
    if (isBlank()) return ""

    return try {
        LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        ""
    }
}
