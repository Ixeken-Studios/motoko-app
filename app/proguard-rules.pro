# Reglas de optimización y ofuscación de Room Database
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class * { *; }
-keep interface * extends androidx.room.Dao { *; }

# Mantener los modelos de dominio por seguridad en serializaciones
-keep class com.ixeken.motoko.domain.model.** { *; }
-keep class com.ixeken.motoko.data.model.** { *; }

# Reglas generales para Dagger Hilt
-keep class * extends java.lang.annotation.Annotation { *; }
