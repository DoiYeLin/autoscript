package com.yaoyaoing.autoscript.config.easyexcel

import com.alibaba.excel.metadata.CellData
import com.alibaba.excel.metadata.Head
import com.alibaba.excel.write.handler.CellWriteHandler
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder
import com.alibaba.excel.write.metadata.holder.WriteTableHolder
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

class SmCellWriteHandler : CellWriteHandler {
    override fun beforeCellCreate(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, row: Row?, head: Head?, columnIndex: Int?, relativeRowIndex: Int?, isHead: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun afterCellCreate(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun afterCellDispose(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cellDataList: MutableList<CellData<Any>>?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun afterCellDataConverted(writeSheetHolder: WriteSheetHolder?, writeTableHolder: WriteTableHolder?, cellData: CellData<*>?, cell: Cell?, head: Head?, relativeRowIndex: Int?, isHead: Boolean?) {
    }

}