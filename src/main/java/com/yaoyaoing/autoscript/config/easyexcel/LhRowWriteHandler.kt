package com.yaoyaoing.autoscript.config.easyexcel

import com.alibaba.excel.write.handler.AbstractRowWriteHandler
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder
import com.alibaba.excel.write.metadata.holder.WriteTableHolder
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.slf4j.LoggerFactory
import java.awt.Color

class LhRowWriteHandler : AbstractRowWriteHandler() {

    companion object {
        private val log = LoggerFactory.getLogger(LhRowWriteHandler::class.java)
    }

    override fun afterRowDispose(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, row: Row?, relativeRowIndex: Int?, isHead: Boolean?) {
        val cells = row!!.cellIterator()
        for ((inx, cell) in cells.withIndex()) {
            if (inx > 1) {
                val workbook: SXSSFWorkbook = writeSheetHolder!!.sheet.workbook as SXSSFWorkbook
                val cellStyle: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
                var font: XSSFFont = cellStyle.font
                font.bold = true
                font.setColor(XSSFColor(Color.blue))
                cellStyle.setFont(font)
                cell.cellStyle = cellStyle
            }
        }
    }
}