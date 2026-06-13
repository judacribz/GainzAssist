package ca.gainzassist.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.gainzassist.domain.model.Session

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: Session): Long

    @Query("SELECT * from  sessions")
    fun getAll(): LiveData<List<Session>>

    @Update
    fun update(vararg sessions: Session)

    @Delete
    fun delete(vararg sessions: Session)
}
