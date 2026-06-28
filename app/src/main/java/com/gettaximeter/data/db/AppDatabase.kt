package com.gettaximeter.data.db

import android.content.Context
import androidx.room.*
import com.gettaximeter.data.model.Driver
import com.gettaximeter.data.model.GPSLog
import com.gettaximeter.data.model.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY lastUpdated DESC")
    fun getAllTripsFlow(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): Trip?

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getTripByIdFlow(tripId: String): Flow<Trip?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE isSynced = 0")
    suspend fun getUnsyncedTrips(): List<Trip>

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers")
    fun getAllDriversFlow(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE id = :driverId")
    suspend fun getDriverById(driverId: String): Driver?

    @Query("SELECT * FROM drivers")
    suspend fun getAllDriversList(): List<Driver>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: Driver)

    @Query("DELETE FROM drivers")
    suspend fun deleteAllDrivers()
}

@Dao
interface GPSLogDao {
    @Query("SELECT * FROM gps_logs WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getLogsForTrip(tripId: String): List<GPSLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGPSLog(log: GPSLog)

    @Query("DELETE FROM gps_logs WHERE tripId = :tripId")
    suspend fun deleteLogsForTrip(tripId: String)
}

@Database(entities = [Trip::class, Driver::class, GPSLog::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun driverDao(): DriverDao
    abstract fun gpsLogDao(): GPSLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "get_taxi_meter_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
