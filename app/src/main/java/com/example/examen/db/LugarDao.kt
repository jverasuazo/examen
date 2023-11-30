package com.example.examen.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LugarDao {

    //Seleccionar todo y ordenar ascendente por lugar visitado 0 o 1
    @Query("SELECT * FROM lugar ORDER BY visitado ASC")
    fun findAll(): List<Lugar>

    @Query("SELECT COUNT(*) FROM lugar")
    fun contar(): Int

    @Insert
    fun insertar(lugar:Lugar):Long

    @Update
    fun actualizar(lugar:Lugar)

    @Delete
    fun eliminar(lugar:Lugar)

}