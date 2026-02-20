package com.example.resumebuilder.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import com.example.resumebuilder.domain.model.Resume
import java.io.OutputStream

object PdfGenerator {

    fun generateResumePdf(
        context: Context,
        resume: Resume
    ): Boolean {

        return try {

            val fileName =
                "${resume.fullName.replace(" ", "_")}_Resume_${System.currentTimeMillis()}.pdf"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/ResumeBuilder"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false

            resolver.openOutputStream(uri)?.use { outputStream ->
                createPdfContent(outputStream, resume)
            } ?: return false

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createPdfContent(
        outputStream: OutputStream,
        resume: Resume
    ) {

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f
        val maxWidth = pageWidth - (margin * 2)
        val bottomLimit = 800f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, pageNumber
        ).create()

        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        var y = 60f

        fun checkPageOverflow() {
            if (y > bottomLimit) {
                document.finishPage(page)
                pageNumber++

                pageInfo = PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, pageNumber
                ).create()

                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        // Name
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(resume.fullName, margin, y, paint)

        y += 30f
        checkPageOverflow()

        // Contact
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("${resume.email} | ${resume.phone}", margin, y, paint)

        y += 50f
        checkPageOverflow()

        // Summary
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Professional Summary", margin, y, paint)

        y += 25f
        checkPageOverflow()

        paint.textSize = 14f
        paint.isFakeBoldText = false

        resume.summary.split("\n").forEach {
            y = drawWrappedText(
                text = it,
                canvas = canvas,
                paint = paint,
                startX = margin,
                startY = y,
                maxWidth = maxWidth
            )
            checkPageOverflow()
        }

        // Skills
        y += 30f
        checkPageOverflow()

        paint.isFakeBoldText = true
        canvas.drawText("Skills", margin, y, paint)

        y += 25f
        checkPageOverflow()

        paint.isFakeBoldText = false

        resume.skills.forEach {
            y = drawWrappedText(
                text = "• $it",
                canvas = canvas,
                paint = paint,
                startX = margin + 10f,
                startY = y,
                maxWidth = maxWidth
            )
            checkPageOverflow()
        }

        // Experience
        y += 30f
        checkPageOverflow()

        paint.isFakeBoldText = true
        canvas.drawText("Experience", margin, y, paint)

        y += 25f
        checkPageOverflow()

        paint.isFakeBoldText = false

        resume.experience.forEach {
            y = drawWrappedText(
                text = "• $it",
                canvas = canvas,
                paint = paint,
                startX = margin + 10f,
                startY = y,
                maxWidth = maxWidth
            )
            checkPageOverflow()
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    private fun drawWrappedText(
        text: String,
        canvas: android.graphics.Canvas,
        paint: Paint,
        startX: Float,
        startY: Float,
        maxWidth: Float
    ): Float {

        var y = startY
        val words = text.split(" ")
        var line = ""

        for (word in words) {

            val testLine = if (line.isEmpty()) word else "$line $word"
            val textWidth = paint.measureText(testLine)

            if (textWidth > maxWidth) {
                canvas.drawText(line, startX, y, paint)
                line = word
                y += paint.textSize + 6f
            } else {
                line = testLine
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, startX, y, paint)
            y += paint.textSize + 6f
        }

        return y
    }

}
