package com.example.myapplication.data.remote.dto

data class DosukuResponse(
    val newboard: NewBoardDto?
)
data class NewBoardDto(
    val grids: List<GridDto>?,
    val results: Int?,
    val message: String?
)
data class GridDto(
    val value: List<List<Int>>?,
    val solution: List<List<Int>>?,
    val difficulty: String?
)
