package com.example.examen.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Lugar(
    @PrimaryKey(autoGenerate = true) val id:Int,
    var lugar:String,
    var visitado:Boolean
)