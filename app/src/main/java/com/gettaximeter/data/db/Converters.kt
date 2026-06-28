package com.gettaximeter.data.db

import androidx.room.TypeConverter
import com.gettaximeter.data.model.TripStatus
import com.gettaximeter.data.model.TripType

class Converters {
    @TypeConverter
    fun fromTripType(value: TripType): String = value.name

    @TypeConverter
    fun toTripType(value: String): TripType = TripType.valueOf(value)

    @TypeConverter
    fun fromTripStatus(value: TripStatus): String = value.name

    @TypeConverter
    fun toTripStatus(value: String): TripStatus = TripStatus.valueOf(value)
}
