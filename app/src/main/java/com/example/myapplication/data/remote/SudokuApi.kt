package com.example.myaplication.data.remote

import com.example.myapplication.data.remote.dto.DosukuResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SudokuApi {
    @GET("api/dosuku")
    suspend fun getNewBoard(
        @Query("query") query: String = "{newboard(limit:1){grids{value,solution,difficulty},results,message}}"
    ): DosukuResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://sudoku-api.vercel.app/"
    val api: SudokuApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SudokuApi::class.java)
    }
}