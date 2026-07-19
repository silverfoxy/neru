package com.y3owk1n.neru.android

import android.graphics.RectF

/**
 * Neru-style coordinate grid state.
 *
 * Two keys choose a labelled main-grid cell (row, then column). A third key
 * chooses one of nine subcells. Return can then commit the selected target.
 */
class GridNavigator(private val screenBounds: RectF) {
    companion object {
        const val PRIMARY_KEYS = "asdfghjkl"
        const val SUBGRID_KEYS = "asdfghjkl"
        private const val MAIN_COLUMNS = 9
        private const val MAIN_ROWS = 9
        private const val SUBGRID_COLUMNS = 3
        private const val SUBGRID_ROWS = 3
    }

    enum class Stage {
        MAIN_GRID,
        SUBGRID,
        READY,
    }

    var stage: Stage = Stage.MAIN_GRID
        private set
    var input: String = ""
        private set
    var mainCellBounds: RectF? = null
        private set
    var targetBounds: RectF? = null
        private set

    fun accept(key: Char): Boolean = when (stage) {
        Stage.MAIN_GRID -> acceptMainGridKey(key)
        Stage.SUBGRID -> acceptSubgridKey(key)
        Stage.READY -> false
    }

    fun back(): Boolean = when (stage) {
        Stage.READY -> {
            stage = Stage.SUBGRID
            targetBounds = null
            true
        }
        Stage.SUBGRID -> {
            stage = Stage.MAIN_GRID
            input = input.take(1)
            mainCellBounds = null
            true
        }
        Stage.MAIN_GRID -> {
            if (input.isEmpty()) false else {
                input = ""
                true
            }
        }
    }

    fun mainCell(row: Int, column: Int): RectF = subdivide(
        screenBounds,
        row,
        column,
        MAIN_ROWS,
        MAIN_COLUMNS,
    )

    fun subCell(index: Int): RectF {
        val main = requireNotNull(mainCellBounds)
        return subdivide(
            main,
            index / SUBGRID_COLUMNS,
            index % SUBGRID_COLUMNS,
            SUBGRID_ROWS,
            SUBGRID_COLUMNS,
        )
    }

    private fun acceptMainGridKey(key: Char): Boolean {
        val normalized = key.lowercaseChar()
        if (normalized !in PRIMARY_KEYS || input.length >= 2) return false

        input += normalized
        if (input.length == 2) {
            val row = PRIMARY_KEYS.indexOf(input[0])
            val column = PRIMARY_KEYS.indexOf(input[1])
            mainCellBounds = mainCell(row, column)
            stage = Stage.SUBGRID
        }
        return true
    }

    private fun acceptSubgridKey(key: Char): Boolean {
        val index = SUBGRID_KEYS.indexOf(key.lowercaseChar())
        if (index < 0) return false

        targetBounds = subCell(index)
        stage = Stage.READY
        return true
    }

    private fun subdivide(
        bounds: RectF,
        row: Int,
        column: Int,
        rows: Int,
        columns: Int,
    ): RectF {
        val width = bounds.width() / columns
        val height = bounds.height() / rows
        return RectF(
            bounds.left + column * width,
            bounds.top + row * height,
            bounds.left + (column + 1) * width,
            bounds.top + (row + 1) * height,
        )
    }
}
