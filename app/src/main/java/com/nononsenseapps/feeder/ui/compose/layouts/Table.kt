package com.nononsenseapps.feeder.ui.compose.layouts

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
fun Table(
    tableData: TableData,
    modifier: Modifier = Modifier,
    allowHorizontalScroll: Boolean = true,
    caption: @Composable (() -> Unit)? = null,
    content: @Composable (row: Int, column: Int) -> Unit,
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }
    val rowHeights = remember { mutableStateMapOf<Int, Int>() }

    val horizontalScrollState: ScrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Layout(
            modifier =
                Modifier
                    .run {
                        if (allowHorizontalScroll) {
                            horizontalScroll(horizontalScrollState)
                        } else {
                            this
                        }
                    },
            content = {
                for (tableCell in tableData.cells) {
                    if (tableCell.rowSpan > 0 && tableCell.colSpan > 0) {
                        content(tableCell.row, tableCell.column)
                    }
                }
            },
        ) { measurables, constraints ->
            val placeables =
                measurables.mapIndexed { index, measurable ->
                    val tableCell = tableData.cells[index]

                    val minWidth = (0 until tableCell.colSpan).sumOf { columnWidths.getOrDefault(tableCell.column + it, 0) }
                    val minHeight = (0 until tableCell.rowSpan).sumOf { rowHeights.getOrDefault(tableCell.row + it, 0) }

                    measurable.measure(
                        Constraints(
                            minWidth = minWidth,
                            maxWidth = Constraints.Infinity,
                            minHeight = minHeight,
                            maxHeight = Constraints.Infinity,
                        ),
                    )
                }

            // Calculate max column width and max row height
            // This depends on the fact that the items are sorted non-spanning items first
            placeables.forEachIndexed { index, placeable ->
                val tableCell = tableData.cells[index]

                val widthPerColumn = placeable.width / tableCell.colSpan
                for (col in tableCell.column until tableCell.column + tableCell.colSpan) {
                    columnWidths[col] = maxOf(columnWidths[col] ?: 0, widthPerColumn)
                }

                val heightPerRow = placeable.height / tableCell.rowSpan
                for (row in tableCell.row until tableCell.row + tableCell.rowSpan) {
                    rowHeights[row] = maxOf(rowHeights[row] ?: 0, heightPerRow)
                }
            }

            // Calculate total width and height
            val totalWidth = columnWidths.values.sumOf { it }
            val totalHeight = rowHeights.values.sumOf { it }

            layout(width = totalWidth, height = totalHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val tableCell = tableData.cells[index]

                    val x = (0 until tableCell.column).sumOf { columnWidths.getOrDefault(it, 0) }
                    val y = (0 until tableCell.row).sumOf { rowHeights.getOrDefault(it, 0) }

                    placeable.place(x, y)
                }
            }
        }
    }
}

@Preview
@Composable
private fun TableFixedPreview() {
    Table(tableData = TableData(3, 3)) { row, column ->
        Box(
            modifier =
                Modifier
                    .size(25.dp)
                    .background(if ((row + column) % 2 == 0) Color.Gray else Color.White),
        )
    }
}

@Preview
@Composable
private fun TableDifferentColumnsPreview() {
    Table(
        tableData = TableData(3, 3),
        modifier =
            Modifier
                .widthIn(max = 150.dp)
                .border(1.dp, Color.Red)
                .padding(24.dp),
    ) { row, column ->
        Box(
            modifier =
                Modifier
                    .background(if ((row + column) % 2 == 0) Color.Gray else Color.White),
        ) {
            Row {
                for (i in 0..row) {
                    Text(text = "Row $row Column $column")
                }
            }
        }
    }
}

@Preview
@Composable
private fun TableWithPaddingPreview() {
    Table(
        tableData = TableData(3, 3),
        modifier =
            Modifier
                .border(1.dp, Color.Red)
                .padding(24.dp),
    ) { row, column ->
        Box(
            modifier =
                Modifier
                    .size(25.dp)
                    .background(if ((row + column) % 2 == 0) Color.Gray else Color.White),
        )
    }
}

@Preview
@Composable
private fun TableCaptionPreview() {
    Surface {
        Table(tableData = TableData(3, 3), caption = {
            Text("Table caption")
        }) { row, column ->
            Box(
                modifier =
                    Modifier
                        .size(25.dp)
                        .background(if ((row + column) % 2 == 0) Color.Gray else Color.White),
            )
        }
    }
}

data class TableData(
    val cells: List<TableCell>,
) {
    constructor(row: Int, column: Int) : this(
        List(row * column) { index ->
            TableCell(
                row = index / column,
                rowSpan = 1,
                column = index % column,
                colSpan = 1,
            )
        },
    )

    init {
        var lastRowSpan = 0
        var lastColSpan = 0
        for (cell in cells) {
            check(cell.rowSpan >= lastRowSpan) {
                "Cells must be sorted in order of increasing spans"
            }
            check(cell.colSpan >= lastColSpan) {
                "Cells must be sorted in order of increasing spans"
            }
            lastRowSpan = cell.rowSpan
            lastColSpan = cell.colSpan
        }
    }

    val rows: Int = cells.maxOf { it.row + it.rowSpan }
    val columns: Int = cells.maxOf { it.column + it.colSpan }
}

data class TableCell(
    val row: Int,
    val rowSpan: Int,
    val column: Int,
    val colSpan: Int,
)
