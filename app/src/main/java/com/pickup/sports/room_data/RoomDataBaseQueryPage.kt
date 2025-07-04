package com.pickup.sports.room_data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RoomDataBaseQueryPage
{
    @Insert
    fun saveData(book: ModelForDataSave)
    @Query(value = "Select * from ModelForDataSave")
    fun getAllData() : List<ModelForDataSave>
}