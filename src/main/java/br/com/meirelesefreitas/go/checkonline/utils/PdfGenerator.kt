package br.com.meirelesefreitas.go.checkonline.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.meirelesefreitas.go.checkonline.data.model.ChecklistDoc
import br.com.meirelesefreitas.go.checkonline.data.model.Colaborador
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateWeeklySummaryPdf(
        context: Context,
        colaborador: Colaborador,
        checklists: List<ChecklistDoc>,
        periodDescription: String
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Header Background
        paint.color = Color.rgb(0, 109, 59) // Brand green
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CheckOnline - Relatório de Checklists", 30f, 40f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Período: $periodDescription | Gerado em: ${DateUtils.formatDateTime(Date())}", 30f, 65f, paint)

        // Colaborador Info Card
        var yPos = 120f
        paint.color = Color.rgb(240, 245, 241)
        canvas.drawRoundRect(25f, 100f, 570f, 180f, 12f, 12f, paint)

        paint.color = Color.rgb(25, 28, 25)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("COLABORADOR: ${colaborador.nome.uppercase()}", 40f, yPos, paint)
        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Matrícula: ${colaborador.matricula}   |   Localidade: ${colaborador.localidade}   |   Supervisor: ${colaborador.supervisor}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Telefone: ${colaborador.telefone}   |   Placa: ${colaborador.placa}   |   Ven. CNH: ${colaborador.venCnh}", 40f, yPos, paint)

        // Section: Resumo das Respostas
        yPos = 210f
        paint.textSize = 14f
        paint.color = Color.rgb(0, 109, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Resumo das Vistorias Realizadas", 25f, yPos, paint)

        yPos += 15f
        // Table Header
        paint.color = Color.rgb(220, 230, 222)
        canvas.drawRect(25f, yPos, 570f, yPos + 25f, paint)

        paint.color = Color.rgb(25, 28, 25)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ID", 35f, yPos + 17f, paint)
        canvas.drawText("Data / Horário", 70f, yPos + 17f, paint)
        canvas.drawText("Conformes (Sim)", 210f, yPos + 17f, paint)
        canvas.drawText("Não Conformes (Não)", 320f, yPos + 17f, paint)
        canvas.drawText("Observação", 440f, yPos + 17f, paint)

        yPos += 25f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        if (checklists.isEmpty()) {
            yPos += 25f
            paint.color = Color.GRAY
            canvas.drawText("Nenhum checklist registrado para o período selecionado.", 35f, yPos, paint)
        } else {
            var rowIndex = 0
            for (item in checklists.take(15)) {
                if (rowIndex % 2 == 1) {
                    paint.color = Color.rgb(247, 250, 247)
                    canvas.drawRect(25f, yPos, 570f, yPos + 22f, paint)
                }

                paint.color = Color.rgb(30, 30, 30)
                canvas.drawText("#${item.id}", 35f, yPos + 15f, paint)
                canvas.drawText(DateUtils.formatDateTime(item.data), 70f, yPos + 15f, paint)

                paint.color = Color.rgb(0, 120, 50)
                canvas.drawText("${item.totalSim} itens (OK)", 210f, yPos + 15f, paint)

                paint.color = if (item.totalNao > 0) Color.rgb(180, 20, 20) else Color.rgb(80, 80, 80)
                canvas.drawText("${item.totalNao} itens", 320f, yPos + 15f, paint)

                paint.color = Color.rgb(30, 30, 30)
                val obsTrimmed = if (item.observacao.length > 20) item.observacao.take(17) + "..." else item.observacao.ifEmpty { "-" }
                canvas.drawText(obsTrimmed, 440f, yPos + 15f, paint)

                yPos += 22f
                rowIndex++
            }
        }

        // Summary Note
        yPos += 25f
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 9f
        canvas.drawText("* Itens de conformidade por checklist inspecionados em campo.", 25f, yPos, paint)
        yPos += 14f
        canvas.drawText("* Documento gerado automaticamente pelo aplicativo CheckOnline via Firebase Cloud Firestore.", 25f, yPos, paint)

        // Signatures area at bottom
        val sigY = 770f
        paint.color = Color.rgb(180, 180, 180)
        canvas.drawLine(40f, sigY, 260f, sigY, paint)
        canvas.drawLine(330f, sigY, 550f, sigY, paint)

        paint.color = Color.rgb(50, 50, 50)
        paint.textSize = 10f
        canvas.drawText("Assinatura do Colaborador", 85f, sigY + 15f, paint)
        canvas.drawText("Assinatura do Supervisor", 380f, sigY + 15f, paint)

        pdfDocument.finishPage(page)

        // Save PDF file
        return try {
            val outputDir = File(context.cacheDir, "relatorios").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(outputDir, "CheckOnline_${colaborador.matricula}_$timeStamp.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun openPdfViewerOrShare(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Semanal de Checklist - CheckOnline")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Compartilhar Relatório PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
