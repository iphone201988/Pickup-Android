package com.pickup.sports.room_data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ModelForDataSave {
    @PrimaryKey(autoGenerate = true)
    var Id: Int =0
    @ColumnInfo (name ="BookName")
    var name:  String =""
}