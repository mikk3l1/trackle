package dk.mikkel.trackle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter

@Database(entities = [WorkEvent::class], version = 1)
abstract class WorkDatabase : RoomDatabase() {

    abstract fun workEventDao(): WorkEventDao

    class EventTypeConverter {
        @TypeConverter fun toDb(value: EventType): String = value.name
        @TypeConverter fun fromDb(value: String): EventType = EventType.valueOf(value)
    }

    companion object {
        @Volatile
        private var instance: WorkDatabase? = null

        fun get(context: Context): WorkDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkDatabase::class.java,
                    "worktracker"
                )
                    .addTypeConverter(EventTypeConverter())
                    .build()
                    .also { instance = it }
            }
    }
}
