package com.example.resumebuilder.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.resumebuilder.domain.model.Resume
import java.io.OutputStream

object PdfGenerator {

    fun generateResumePdf(
        context: Context,
        resume: Resume
    ): Boolean {
        return try {
            Log.d("PdfGenerator", "Starting PDF generation...")

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

            Log.d("PdfGenerator", "PDF generated successfully")
            true

        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating PDF", e)
            e.printStackTrace()
            false
        }
    }

    private fun createPdfContent(
        outputStream: OutputStream,
        resume: Resume
    ) {
        createModernTemplate(outputStream, resume)
    }

    private data class TextSegment(
        val text: String,
        val isBold: Boolean = false
    )


    private fun parseMarkdownText(text: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        var currentText = text
        var startIndex = 0

        while (startIndex < currentText.length) {
            val boldStart = currentText.indexOf("**", startIndex)

            if (boldStart == -1) {

                if (startIndex < currentText.length) {
                    segments.add(TextSegment(currentText.substring(startIndex), isBold = false))
                }
                break
            }


            if (boldStart > startIndex) {
                segments.add(TextSegment(currentText.substring(startIndex, boldStart), isBold = false))
            }


            val boldEnd = currentText.indexOf("**", boldStart + 2)
            if (boldEnd == -1) {

                segments.add(TextSegment(currentText.substring(boldStart), isBold = false))
                break
            }

            val boldText = currentText.substring(boldStart + 2, boldEnd)
            segments.add(TextSegment(boldText, isBold = true))

            startIndex = boldEnd + 2
        }

        return segments
    }

    private fun createModernTemplate(
        outputStream: OutputStream,
        resume: Resume
    ) {
        try {
            val document = PdfDocument()
            val pageWidth = 595f
            val pageHeight = 842f
            val marginLeft = 40f
            val marginRight = 40f
            val marginTop = 40f
            val marginBottom = 40f
            val maxWidth = pageWidth - marginLeft - marginRight

            var currentPage = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), currentPage).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var yPosition = marginTop

            val titlePaint = Paint().apply {
                textSize = 28f
                isFakeBoldText = true
            }

            val sectionHeaderPaint = Paint().apply {
                textSize = 13f
                isFakeBoldText = true
            }

            val regularPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = false
            }

            val boldPaint = Paint().apply {
                textSize = 11f
                isFakeBoldText = true
            }

            val dividerPaint = Paint().apply {
                strokeWidth = 1f
            }

            fun ensurePage() {
                if (yPosition > pageHeight - marginBottom) {
                    document.finishPage(page)
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = marginTop
                }
            }

            canvas.drawText(resume.fullName, marginLeft, yPosition, titlePaint)
            yPosition += 30f
            ensurePage()

            val contactInfo = "${resume.email} | ${resume.phone}"
            canvas.drawText(contactInfo, marginLeft, yPosition, regularPaint)
            yPosition += 15f
            ensurePage()

            canvas.drawLine(marginLeft, yPosition, pageWidth - marginRight, yPosition, dividerPaint)
            yPosition += 20f
            ensurePage()

            if (resume.summary.isNotEmpty()) {
                canvas.drawText("PROFESSIONAL SUMMARY", marginLeft, yPosition, sectionHeaderPaint)
                yPosition += 16f
                ensurePage()

                yPosition = drawFormattedText(
                    resume.summary,
                    canvas,
                    regularPaint,
                    boldPaint,
                    marginLeft,
                    yPosition,
                    maxWidth,
                    pageHeight,
                    marginBottom
                )
                yPosition += 12f
                ensurePage()
            }

            if (resume.education.isNotEmpty()) {
                canvas.drawText("EDUCATION", marginLeft, yPosition, sectionHeaderPaint)
                yPosition += 16f
                ensurePage()

                resume.education.forEach { edu ->
                    yPosition = drawFormattedText(
                        edu,
                        canvas,
                        regularPaint,
                        boldPaint,
                        marginLeft + 10f,
                        yPosition,
                        maxWidth - 10f,
                        pageHeight,
                        marginBottom
                    )
                }
                yPosition += 12f
                ensurePage()
            }

            if (resume.skills.isNotEmpty()) {
                canvas.drawText("TECHNICAL SKILLS", marginLeft, yPosition, sectionHeaderPaint)
                yPosition += 16f
                ensurePage()

                val skillsText = resume.skills.joinToString(", ")
                yPosition = drawFormattedText(
                    skillsText,
                    canvas,
                    regularPaint,
                    boldPaint,
                    marginLeft + 10f,
                    yPosition,
                    maxWidth - 10f,
                    pageHeight,
                    marginBottom
                )
                yPosition += 12f
                ensurePage()
            }

            if (resume.experience.isNotEmpty()) {
                canvas.drawText("RELEVANT EXPERIENCE", marginLeft, yPosition, sectionHeaderPaint)
                yPosition += 16f
                ensurePage()

                resume.experience.forEach { exp ->
                    yPosition = drawFormattedText(
                        exp,
                        canvas,
                        regularPaint,
                        boldPaint,
                        marginLeft + 10f,
                        yPosition,
                        maxWidth - 10f,
                        pageHeight,
                        marginBottom
                    )
                }
                yPosition += 12f
                ensurePage()
            }

            if (resume.projects.isNotEmpty()) {
                canvas.drawText("PROJECTS", marginLeft, yPosition, sectionHeaderPaint)
                yPosition += 16f
                ensurePage()

                resume.projects.forEach { project ->
                    yPosition = drawFormattedText(
                        project,
                        canvas,
                        regularPaint,
                        boldPaint,
                        marginLeft + 10f,
                        yPosition,
                        maxWidth - 10f,
                        pageHeight,
                        marginBottom
                    )
                }
            }

            document.finishPage(page)
            document.writeTo(outputStream)
            document.close()

            Log.d("PdfGenerator", "PDF created successfully")

        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error creating PDF", e)
            throw e
        }
    }

    private fun drawFormattedText(
        text: String,
        canvas: android.graphics.Canvas,
        regularPaint: Paint,
        boldPaint: Paint,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        pageHeight: Float,
        marginBottom: Float
    ): Float {
        if (text.isEmpty()) return startY

        val segments = parseMarkdownText(text)
        var y = startY
        var xPos = startX
        var line = ""
        var lineSegments = mutableListOf<Pair<String, Boolean>>()

        for (segment in segments) {
            val words = segment.text.split(" ")

            for (word in words) {
                if (word.isEmpty()) continue

                val testPaint = if (segment.isBold) boldPaint else regularPaint
                val testLine = if (line.isEmpty()) word else "$line $word"
                val textWidth = testPaint.measureText(testLine)

                if (textWidth > maxWidth && line.isNotEmpty()) {
                    y = drawMixedFormattingLine(
                        lineSegments,
                        canvas,
                        regularPaint,
                        boldPaint,
                        startX,
                        y,
                        maxWidth
                    )
                    line = word
                    lineSegments.clear()
                    lineSegments.add(Pair(word, segment.isBold))
                } else {
                    line = testLine
                    if (lineSegments.isEmpty() || lineSegments.last().second != segment.isBold) {
                        lineSegments.add(Pair(word, segment.isBold))
                    } else {
                        val lastIndex = lineSegments.size - 1
                        val lastSegment = lineSegments[lastIndex]
                        lineSegments[lastIndex] = Pair(lastSegment.first + " " + word, lastSegment.second)
                    }
                }
            }
        }


        if (line.isNotEmpty()) {
            y = drawMixedFormattingLine(
                lineSegments,
                canvas,
                regularPaint,
                boldPaint,
                startX,
                y,
                maxWidth
            )
        }

        return y
    }


    private fun drawMixedFormattingLine(
        segments: List<Pair<String, Boolean>>,
        canvas: android.graphics.Canvas,
        regularPaint: Paint,
        boldPaint: Paint,
        startX: Float,
        y: Float,
        maxWidth: Float
    ): Float {
        var xPos = startX

        for ((text, isBold) in segments) {
            val paint = if (isBold) boldPaint else regularPaint
            canvas.drawText(text + " ", xPos, y, paint)
            xPos += paint.measureText(text + " ")
        }

        return y + regularPaint.textSize + 4f
    }
}
