package com.example.team13

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DB(context: Context):SQLiteOpenHelper(context,DATABASE_NAME, null,DATABASE_VAR) {
    companion object{
        private val DATABASE_VAR = 1
        private val DATABASE_NAME = "EDMTDB.db"
        private val TABLE_NAME = "Marker"
        private val LA = "latitude"
        private val LO = "longitude"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        val Creat_Marker : String =  ("Create table $TABLE_NAME  ($LA,  $LO)")
        db!!.execSQL(Creat_Marker)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("Drop TABLE IF EXISTS $TABLE_NAME")
        onCreate(db!!)
    }

    val allMarker:List<Marker>
        get() {
        val lastMarkers = ArrayList<Marker>()
        val selectQuery = "Select * FROM $TABLE_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
             do {
                 val marker= Marker(Double.NaN, Double.NaN)
                 marker.latitude = cursor.getDouble(cursor.getColumnIndex(LA))
                 marker.longitude = cursor.getDouble(cursor.getColumnIndex(LO))
                 lastMarkers.add(marker)
             }while(cursor.moveToNext())
         }
            db.close()
        return lastMarkers
    }
    fun addMarker(marker:Marker)
    {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LA, marker.latitude)
        values.put(LO, marker.longitude)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun deleteMarker(marker:Marker)
    {
        val db = this.writableDatabase

        db.delete(TABLE_NAME,"$LA", arrayOf(marker.latitude.toString()))
        db.close()
    }

}