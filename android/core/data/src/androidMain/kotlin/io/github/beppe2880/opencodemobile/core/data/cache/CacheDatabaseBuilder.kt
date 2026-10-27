package io.github.beppe2880.opencodemobile.core.data.cache

import android.content.Context
import androidx.room.Room

/** Uses the framework SQLite, as before the database code became multiplatform. */
internal fun cacheDatabase(context: Context): CacheDatabase =
    CacheDatabase.build(Room.databaseBuilder<CacheDatabase>(context, context.getDatabasePath(CacheDatabase.FILE_NAME).absolutePath))
