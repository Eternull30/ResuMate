package com.example.resumebuilder.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.resumebuilder.domain.model.UserProfile
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.OutputStream

object PdfGenerator {

    fun generate(context: Context, profile: UserProfile): Uri? {

        return try {

            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${profile.name}_Resume.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            val outputStream = resolver.openOutputStream(uri) ?: return null

            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument)

            // Name
            document.add(
                Paragraph(profile.name)
                    .setBold()
                    .setFontSize(22f)
            )

            document.add(Paragraph(profile.email))
            document.add(Paragraph("\n"))

            // Summary
            document.add(
                Paragraph("Professional Summary")
                    .setBold()
                    .setFontSize(16f)
            )
            document.add(Paragraph(profile.bio))
            document.add(Paragraph("\n"))

            // Skills
            document.add(
                Paragraph("Skills")
                    .setBold()
                    .setFontSize(16f)
            )
            document.add(Paragraph(profile.skills))
            document.add(Paragraph("\n"))

            // Experience
            document.add(
                Paragraph("Experience")
                    .setBold()
                    .setFontSize(16f)
            )
            document.add(Paragraph(profile.experience))

            document.close()
            outputStream.close()

            uri

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

