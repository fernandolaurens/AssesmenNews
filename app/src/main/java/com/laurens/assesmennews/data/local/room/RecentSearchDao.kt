package com.laurens.assesmennews.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecentSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearch: RecentSearchEntity)

    @Query("SELECT * FROM recent_search ORDER BY id DESC LIMIT 10")
    fun getRecentSearches(): LiveData<List<RecentSearchEntity>>
}