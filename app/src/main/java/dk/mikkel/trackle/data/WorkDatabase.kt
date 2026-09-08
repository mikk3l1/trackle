package dk.mikkel.trackle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Room handles enum columns (like [EventType]) natively. */
@Database(entities = [WorkEvent::class], version = 1)
abstract class WorkDatabase : RoomDatabase() {

    abstract fun workEventDao(): WorkEventDao

    companion object {
        @Volatile
        private var instance: WorkDatabase? = null

        fun get(context: Context): WorkDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkDatabase::class.java,
                    "worktracker"
                ).build().also { instance = it }
            }
    }
}
