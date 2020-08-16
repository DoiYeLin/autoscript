package com.yaoyaoing.autoscript.config.easyexcel

import com.alibaba.excel.metadata.CellData
import com.alibaba.excel.metadata.Head
import com.alibaba.excel.write.handler.CellWriteHandler
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder
import com.alibaba.excel.write.metadata.holder.WriteTableHolder
import org.apache.poi.common.usermodel.HyperlinkType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Hyperlink
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.slf4j.LoggerFactory
import java.awt.Color


class LuCellWriteHandler : CellWriteHandler {

    companion object {
        private val log = LoggerFactory.getLogger(LuCellWriteHandler::class.java)
    }

    override fun beforeCellCreate(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, row: Row?, head: Head?, columnIndex: Int?, relativeRowIndex: Int?, isHead: Boolean?) {
    }

    override fun afterCellCreate(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
    }

    override fun afterCellDispose(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cellDataList: MutableList<CellData<Any>>?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
    }

    override fun afterCellDataConverted(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cellData: CellData<Any>?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
        if (cell!!.columnIndex < 2) {
            return
        }




        if (cellData!!.stringValue.equals("龙")) {
            val workbook: SXSSFWorkbook = writeSheetHolder!!.sheet.workbook as SXSSFWorkbook
            val cellStyle: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
            var font: XSSFFont = cellStyle.font
            font.bold = true
            font.setColor(XSSFColor(Color.blue))
            cellStyle.setFont(font)
            cell.cellStyle = cellStyle
        }
//        else if (cellData!!.stringValue.equals("虎")) {
//            var font: XSSFFont = cellStyle.font
//            font.bold = true
//            font.setColor(XSSFColor(Color.red))
//            cellStyle.setFont(font)
//        } else if (cellData!!.stringValue.equals("和")) {
//            var font: XSSFFont = cellStyle.font
//            font.bold = true
//            font.setColor(XSSFColor(Color.green))
//            cellStyle.setFont(font)
//        }

    }
}