package com.example.accounting.util

import android.content.Context
import android.os.Environment
import com.example.accounting.data.db.entity.Transaction
import com.example.accounting.data.db.entity.TransactionType
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    /**
     * 导出账单为CSV文件
     * @return 文件路径
     */
    fun export(
        context: Context,
        transactions: List<Transaction>,
        fileName: String = "AI记账_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())}.csv"
    ): Result<String> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            FileWriter(file).use { writer ->
                // 写入BOM（解决Excel中文乱码）
                writer.write("﻿")

                // 写入表头
                writer.write("日期,类型,分类,金额,备注,来源\n")

                // 写入数据
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                transactions.forEach { transaction ->
                    val date = dateFormat.format(Date(transaction.date))
                    val type = if (transaction.type == TransactionType.EXPENSE) "支出" else "收入"
                    val category = transaction.categoryName.ifEmpty { "未分类" }
                    val amount = String.format("%.2f", transaction.amount)
                    val note = transaction.note.replace(",", "，").replace("\n", " ")
                    val source = when (transaction.source) {
                        com.example.accounting.data.db.entity.InputSource.MANUAL -> "手动"
                        com.example.accounting.data.db.entity.InputSource.OCR -> "拍照"
                        com.example.accounting.data.db.entity.InputSource.VOICE -> "语音"
                        com.example.accounting.data.db.entity.InputSource.TEXT_AI -> "文字AI"
                    }

                    writer.write("$date,$type,$category,$amount,$note,$source\n")
                }
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
