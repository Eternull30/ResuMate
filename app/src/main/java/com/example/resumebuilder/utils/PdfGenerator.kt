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
        when (resume.templateType) {
            "professional" -> createProfessionalTemplate(outputStream, resume)
            "creative" -> createCreativeTemplate(outputStream, resume)
            else -> createModernTemplate(outputStream, resume)
        }
    }

    // Modern Template (Clean & Minimal - like the image)
    private fun createModernTemplate(
        outputStream: OutputStream,
        resume: Resume
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val maxWidth = pageWidth - (margin * 2)
        val bottomLimit = 800f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, pageNumber
        ).create()

        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50f

        fun checkPageOverflow() {
            if (y > bottomLimit) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, pageNumber
                ).create()
                page = document.startPage(pageInfo)
                y = 50f
            }
        }

        // ===== HEADER =====
        val titlePaint = Paint().apply {
            textSize = 26f
            isFakeBoldText = true
        }
        canvas.drawText(resume.fullName, margin, y, titlePaint)
        y += 30f
        checkPageOverflow()

        val contactPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = false
        }
        val contactText = "${resume.phone} | ${resume.email}"
        canvas.drawText(contactText, margin, y, contactPaint)
        y += 20f
        checkPageOverflow()

        // Draw horizontal line
        val linePaint = Paint().apply {
            strokeWidth = 1f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 20f
        checkPageOverflow()

        // ===== PROFESSIONAL SUMMARY =====
        if (resume.summary.isNotEmpty()) {
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("PROFESSIONAL SUMMARY", margin, y, sectionPaint)
            y += 18f
            checkPageOverflow()

            val contentPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            resume.summary.split("\n").forEach { line ->
                y = drawWrappedText(line, canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 12f
        }

        // ===== EDUCATION =====
        if (resume.education.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("EDUCATION", margin, y, sectionPaint)
            y += 18f
            checkPageOverflow()

            val contentPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            resume.education.forEach { edu ->
                y = drawWrappedText("• $edu", canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 12f
        }

        // ===== TECHNICAL SKILLS =====
        if (resume.skills.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("TECHNICAL SKILLS", margin, y, sectionPaint)
            y += 18f
            checkPageOverflow()

            val contentPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            val skillsText = resume.skills.joinToString(" • ")
            y = drawWrappedText(skillsText, canvas, contentPaint, margin, y, maxWidth)
            checkPageOverflow()
            y += 12f
        }

        // ===== RELEVANT EXPERIENCE =====
        if (resume.experience.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("RELEVANT EXPERIENCE", margin, y, sectionPaint)
            y += 18f
            checkPageOverflow()

            val contentPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            resume.experience.forEach { exp ->
                y = drawWrappedText("• $exp", canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 12f
        }

        // ===== PROJECTS =====
        if (resume.projects.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }
            canvas.drawText("PROJECTS", margin, y, sectionPaint)
            y += 18f
            checkPageOverflow()

            val contentPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            resume.projects.forEach { project ->
                y = drawWrappedText("• $project", canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    // Professional Template (Traditional)
    private fun createProfessionalTemplate(
        outputStream: OutputStream,
        resume: Resume
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 45f
        val maxWidth = pageWidth - (margin * 2)
        val bottomLimit = 800f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, pageNumber
        ).create()

        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50f

        fun checkPageOverflow() {
            if (y > bottomLimit) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, pageNumber
                ).create()
                page = document.startPage(pageInfo)
                y = 50f
            }
        }

        // Header
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText(resume.fullName, margin, y, titlePaint)
        y += 25f

        val contactPaint = Paint().apply {
            textSize = 11f
        }
        canvas.drawText("${resume.email} | ${resume.phone}", margin, y, contactPaint)
        y += 25f

        val dividerPaint = Paint().apply {
            strokeWidth = 2f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 20f

        // Professional Summary
        if (resume.summary.isNotEmpty()) {
            val sectionPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("PROFESSIONAL SUMMARY", margin, y, sectionPaint)
            y += 16f
            checkPageOverflow()

            val contentPaint = Paint().apply { textSize = 11f }
            resume.summary.split("\n").forEach { line ->
                y = drawWrappedText(line, canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 10f
        }

        // Education
        if (resume.education.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("EDUCATION", margin, y, sectionPaint)
            y += 16f
            checkPageOverflow()

            val contentPaint = Paint().apply { textSize = 11f }
            resume.education.forEach { edu ->
                y = drawWrappedText(edu, canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 10f
        }

        // Skills
        if (resume.skills.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("TECHNICAL SKILLS", margin, y, sectionPaint)
            y += 16f
            checkPageOverflow()

            val contentPaint = Paint().apply { textSize = 11f }
            val skillsText = resume.skills.joinToString(" • ")
            y = drawWrappedText(skillsText, canvas, contentPaint, margin, y, maxWidth)
            checkPageOverflow()
            y += 10f
        }

        // Experience
        if (resume.experience.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("RELEVANT EXPERIENCE", margin, y, sectionPaint)
            y += 16f
            checkPageOverflow()

            val contentPaint = Paint().apply { textSize = 11f }
            resume.experience.forEach { exp ->
                y = drawWrappedText("• $exp", canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
            y += 10f
        }

        // Projects
        if (resume.projects.isNotEmpty()) {
            checkPageOverflow()
            val sectionPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("PROJECTS", margin, y, sectionPaint)
            y += 16f
            checkPageOverflow()

            val contentPaint = Paint().apply { textSize = 11f }
            resume.projects.forEach { project ->
                y = drawWrappedText("• $project", canvas, contentPaint, margin, y, maxWidth)
                checkPageOverflow()
            }
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    // Creative Template (Bold & Modern)
    private fun createCreativeTemplate(
        outputStream: OutputStream,
        resume: Resume
    ) {
        // This is similar to Modern but with more spacing
        createModernTemplate(outputStream, resume)
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
                y += paint.textSize + 5f
            } else {
                line = testLine
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, startX, y, paint)
            y += paint.textSize + 5f
        }

        return y
    }
}
