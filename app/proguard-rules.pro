# Room generated database implementations.
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase_Impl
-keep class * implements androidx.room.RoomDatabase$Callback

# Keep entities and DAOs referenced through annotation processing.
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep kotlinx serialization generated serializers.
-if @kotlinx.serialization.Serializable class **
-keep class <1>$$serializer { *; }
-keepclassmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WorkManager workers are instantiated reflectively.
-keep class * extends androidx.work.ListenableWorker
