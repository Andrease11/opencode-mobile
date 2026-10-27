package io.github.beppe2880.opencodemobile.core.data.cache

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/** Lives in Caches: the system may purge it, and history is then fetched from the server again. */
internal fun cacheDatabase(): CacheDatabase {
    val caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String
    return CacheDatabase.build(
        Room.databaseBuilder<CacheDatabase>(name = "$caches/${CacheDatabase.FILE_NAME}")
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO),
    )
}
