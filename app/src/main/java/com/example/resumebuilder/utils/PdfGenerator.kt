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

        val pageInfo = PdfDocument.PageInfo.Builder(
            595, 842, 1
        ).create()

        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = 60f

        // Name
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(resume.fullName, 50f, y, paint)

        // Contact
        y += 30f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("${resume.email} | ${resume.phone}", 50f, y, paint)

        // Summary
        y += 50f
        paint.isFakeBoldText = true
        paint.textSize = 16f
        canvas.drawText("Professional Summary", 50f, y, paint)

        y += 25f
        paint.isFakeBoldText = false
        paint.textSize = 14f

        resume.summary.split("\n").forEach {
            canvas.drawText(it, 50f, y, paint)
            y += 20f
        }

        // Skills
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Skills", 50f, y, paint)

        y += 25f
        paint.isFakeBoldText = false

        resume.skills.forEach {
            canvas.drawText("• $it", 60f, y, paint)
            y += 20f
        }

        // Experience
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Experience", 50f, y, paint)

        y += 25f
        paint.isFakeBoldText = false

        resume.experience.forEach {
            canvas.drawText("• $it", 60f, y, paint)
            y += 20f
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
