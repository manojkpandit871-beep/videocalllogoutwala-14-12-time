package com.example.videocallapp.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.videocallapp.model.CallRecord;

import java.util.List;

@Dao
public interface CallDao {

    @Insert
    void insert(CallRecord record);

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    LiveData<List<CallRecord>> getAllCalls();

    @Query("SELECT * FROM call_history ORDER BY timestamp DESC LIMIT 50")
    List<CallRecord> getRecentCalls();

    @Query("DELETE FROM call_history")
    void deleteAll();

    @Query("DELETE FROM call_history WHERE id = :id")
    void deleteById(int id);
}
