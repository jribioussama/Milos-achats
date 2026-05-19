package com.example.milos_achats.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.milos_achats.data.BarProduct
import com.example.milos_achats.data.SupplierSection
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object OrderImageGenerator {

    fun generate(
        context: Context,
        supplier: SupplierSection,
        products: List<BarProduct>,
        formattedDate: String,
    ): Result<Pair<String, ByteArray>> = runCatching {
        val bitmap   = buildBitmap(supplier, products, formattedDate)
        val datePart = formattedDate.replace(Regex("[^\\w]"), "_").trim('_')
        val fileName = "bon_commande_${supplier.name.replace(Regex("[^\\w]"), "_")}_$datePart"
        saveBitmap(context, bitmap, fileName)
        val bytes = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
        bitmap.recycle()
        fileName to bytes
    }

    // ── Image builder ────────────────────────────────────────────────────────────

    private fun buildBitmap(
        supplier: SupplierSection,
        products: List<BarProduct>,
        formattedDate: String,
    ): Bitmap {
        val W            = 800
        val PAD          = 48f
        val headerH      = 170f
        val supplierH    = 90f
        val tableHeaderH = 52f
        val rowH         = 70f
        val footerH      = 76f
        val totalH       = (headerH + supplierH + tableHeaderH + products.size * rowH + footerH).toInt()

        val bmp    = Bitmap.createBitmap(W, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val cPrimary      = Color.parseColor("#4E7FC4")   // Milos blue
        val cPrimaryLight = Color.parseColor("#D4E5F7")   // Milos blue container
        val cPrimaryDeep  = Color.parseColor("#2B5597")   // header darker
        val cText         = Color.parseColor("#1A1F2E")
        val cTextSub      = Color.parseColor("#3D5068")
        val cRowAlt       = Color.parseColor("#F7F9FD")
        val cTableHeader  = Color.parseColor("#EBF3FC")
        val cDivider      = Color.parseColor("#CCDDEF")
        val cWhite        = Color.WHITE

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // ── Header ───────────────────────────────────────────────
        paint.color = cPrimaryDeep
        canvas.drawRect(0f, 0f, W.toFloat(), headerH, paint)

        paint.color     = cWhite
        paint.textAlign = Paint.Align.CENTER
        paint.typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize  = 44f
        canvas.drawText("BON DE COMMANDE", W / 2f, headerH / 2f - 12f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 27f
        canvas.drawText("Milos  •  $formattedDate", W / 2f, headerH / 2f + 28f, paint)

        // ── Supplier band ────────────────────────────────────────
        var y = headerH
        paint.color = cPrimaryLight
        canvas.drawRect(0f, y, W.toFloat(), y + supplierH, paint)

        paint.color    = cPrimary
        paint.textAlign = Paint.Align.LEFT
        paint.typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize  = 31f
        canvas.drawText(supplier.name, PAD, y + supplierH / 2f - 6f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 22f
        paint.color    = cTextSub
        canvas.drawText(supplier.deliveryInfo, PAD, y + supplierH / 2f + 24f, paint)

        y += supplierH

        // ── Table header ─────────────────────────────────────────
        paint.color = cTableHeader
        canvas.drawRect(0f, y, W.toFloat(), y + tableHeaderH, paint)

        paint.color    = cTextSub
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 21f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("PRODUIT", PAD, y + tableHeaderH / 2f + 8f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("QTÉ", W - PAD, y + tableHeaderH / 2f + 8f, paint)

        y += tableHeaderH

        // ── Product rows ─────────────────────────────────────────
        products.forEachIndexed { idx, product ->
            val rowY = y + idx * rowH

            if (idx % 2 == 1) {
                paint.color = cRowAlt
                canvas.drawRect(0f, rowY, W.toFloat(), rowY + rowH, paint)
            }

            paint.color    = cText
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 26f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                truncate(paint, product.nameFr, W - PAD * 3 - 80f),
                PAD, rowY + rowH / 2f - 6f, paint,
            )

            paint.color    = cTextSub
            paint.textSize = 20f
            canvas.drawText(
                truncate(paint, product.nameAr, W - PAD * 3 - 80f),
                PAD, rowY + rowH / 2f + 20f, paint,
            )

            paint.color    = cPrimary
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 28f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("× ${product.quantity}", W - PAD, rowY + rowH / 2f + 10f, paint)

            paint.color       = cDivider
            paint.strokeWidth = 1f
            canvas.drawLine(PAD, rowY + rowH, W - PAD, rowY + rowH, paint)
        }

        y += products.size * rowH

        // ── Footer ───────────────────────────────────────────────
        paint.color       = cDivider
        paint.strokeWidth = 2f
        canvas.drawLine(0f, y, W.toFloat(), y, paint)

        paint.color    = cTextSub
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Total : ${products.size} article(s)   •   Milos Achats",
            W / 2f, y + footerH / 2f + 9f, paint,
        )

        return bmp
    }

    private fun truncate(paint: Paint, text: String, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var result = text
        while (result.isNotEmpty() && paint.measureText("$result…") > maxWidth) {
            result = result.dropLast(1)
        }
        return "$result…"
    }

    // ── Clear folder ────────────────────────────────────────────────────────────

    fun clearFolder(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver  = context.contentResolver
            val uri       = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val cursor    = resolver.query(
                uri,
                arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?",
                arrayOf("%Milos Achats%"),
                null,
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    resolver.delete(ContentUris.withAppendedId(uri, id), null, null)
                }
            }
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Milos Achats",
            )
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    // ── Save to gallery ──────────────────────────────────────────────────────────

    private fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Milos Achats")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri      = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore insert failed")
            resolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Milos Achats",
            )
            dir.mkdirs()
            val file = File(dir, "$fileName.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
        }
    }
}
