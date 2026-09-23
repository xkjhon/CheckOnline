package br.com.meirelesefreitas.go.checkonline.utils

import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {

    private val localePtBr = Locale("pt", "BR")

    fun formatCurrentTime(date: Date = Date()): String {
        val sdf = SimpleDateFormat("HH:mm:ss", localePtBr)
        return sdf.format(date)
    }

    fun formatCurrentDateFull(date: Date = Date()): String {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", localePtBr)
        val formatted = sdf.format(date)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localePtBr) else it.toString() }
    }

    fun formatDateShort(date: Date?): String {
        if (date == null) return "Data não disponível"
        val sdf = SimpleDateFormat("dd/MM/yyyy", localePtBr)
        return sdf.format(date)
    }

    fun formatDateTime(date: Date?): String {
        if (date == null) return "Data não disponível"
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", localePtBr)
        return sdf.format(date)
    }

    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate1 == localDate2
    }

    fun isToday(date: Date?): Boolean {
        if (date == null) return false
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate == LocalDate.now()
    }

    /**
     * Calculates pending business days (Monday to Friday) in the current month prior to today
     * that do not have a corresponding answered checklist date.
     */
    fun findPendingBusinessDays(answeredDates: List<Date>): List<LocalDate> {
        val today = LocalDate.now()
        val currentYear = today.year
        val currentMonth = today.monthValue

        val answeredLocalDates = answeredDates.map {
            it.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()

        val pendingDays = mutableListOf<LocalDate>()
        var dayPointer = LocalDate.of(currentYear, currentMonth, 1)

        while (dayPointer.isBefore(today)) {
            val dayOfWeek = dayPointer.dayOfWeek
            val isBusinessDay = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
            if (isBusinessDay && !answeredLocalDates.contains(dayPointer)) {
                pendingDays.add(dayPointer)
            }
            dayPointer = dayPointer.plusDays(1)
        }

        return pendingDays
    }

    fun formatLocalDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localePtBr)
        return localDate.format(formatter)
    }
}
