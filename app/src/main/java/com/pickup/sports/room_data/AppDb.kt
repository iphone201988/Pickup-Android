package com.pickup.sports.room_data

import androidx.room.Database
import androidx.room.RoomDatabase

//Add the all table in this format As show in the blow line in comment
@Database(
    entities = [(ModelForDataSave::class)
        /*

, (ModelForDataSave::class),(ModelForDataSave::class)
*/
    ], version = 1
)
abstract class AppDb : RoomDatabase() {

    abstract fun connectAbstractClassConnection(): RoomDataBaseQueryPage
    //Add the all Abstract class and connect to the query class  in this format As show in the blow line in comment

    /*  abstract fun userDao(): UserDao*/

}