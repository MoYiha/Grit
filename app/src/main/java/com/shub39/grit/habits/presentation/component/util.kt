package com.shub39.grit.habits.presentation.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.shub39.grit.habits.domain.Habit
import com.shub39.grit.habits.domain.HabitStatus
import ir.ehsannarmani.compose_charts.models.Bars
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

fun prepareLineChartData(
    firstDay: DayOfWeek,
    habitstatuses: List<HabitStatus>
): List<Double> {
    val today = LocalDate.now()
    val weekFields = WeekFields.of(firstDay, 1)

    val startDateOfPeriod = today.minusWeeks(9).with(weekFields.dayOfWeek(), 1)

    val habitCompletionByWeek = habitstatuses
        .filter { !it.date.isBefore(startDateOfPeriod) && !it.date.isAfter(today) }
        .groupBy {
            val yearOfWeek = it.date.get(weekFields.weekBasedYear())
            val weekOfYear = it.date.get(weekFields.weekOfWeekBasedYear())
            yearOfWeek * 100 + weekOfYear
        }
        .mapValues { (_, habitStatuses) -> habitStatuses.size }
    val values = (0..9).map { i ->
        val currentWeekStart = today.minusWeeks(9 - i.toLong()).with(weekFields.dayOfWeek(), 1)
        val yearOfWeek = currentWeekStart.get(weekFields.weekBasedYear())
        val weekOfYear = currentWeekStart.get(weekFields.weekOfWeekBasedYear())
        val weekKey = yearOfWeek * 100 + weekOfYear
        (habitCompletionByWeek[weekKey]?.toDouble() ?: 0.0).coerceIn(0.0, 7.0)
    }
    return values
}

fun prepareWeekDayData(
    dates: List<LocalDate>,
    lineColor: Color
): List<Bars> {
    val dayFrequency = dates.groupingBy { it.dayOfWeek }
        .eachCount()

    val weekdayBars = DayOfWeek.entries.map { dayOfWeek ->
        val weekName = dayOfWeek.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        )

        Bars(
            label = weekName,
            values = listOf(
                Bars.Data(
                    label = weekName,
                    value = dayFrequency[dayOfWeek]?.toDouble() ?: 0.0,
                    color = SolidColor(lineColor)
                )
            )
        )
    }

    return weekdayBars
}

fun prepareHeatMapData(
    habitData: Map<Habit, List<HabitStatus>>
): Map<LocalDate, Int> {
    val allDates = habitData.values.flatten().map { it.date }
    val dateFrequency = allDates.groupingBy { it }
        .eachCount()

    return dateFrequency
}

fun getOrdinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "${day}th"
        else -> when (day % 10) {
            1 -> "${day}st"
            2 -> "${day}nd"
            3 -> "${day}rd"
            else -> "${day}th"
        }
    }
}

fun formatDateWithOrdinal(date: LocalDate): String {
    val day = date.dayOfMonth
    val month = date.format(DateTimeFormatter.ofPattern("MMMM"))
    val year = date.year

    return "${getOrdinalSuffix(day)} $month $year"
}