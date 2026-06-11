package com.example.videocallapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a single call history record.
 */
@Entity(tableName = "call_history")
public class CallRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String userName;    // remote user / self name
    public String roomId;      // call room/session ID
    public long   timestamp;   // epoch millis when call started
    public long   durationSec; // call duration in seconds (0 if missed)
    public String callType;    // "OUTGOING", "INCOMING", "MISSED"

    // Convenience constructor
    public CallRecord(String userName, String roomId, long timestamp,
                      long durationSec, String callType) {
        this.userName    = userName;
        this.roomId      = roomId;
        this.timestamp   = timestamp;
        this.durationSec = durationSec;
        this.callType    = callType;
    }
}
