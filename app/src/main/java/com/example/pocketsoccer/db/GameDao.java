package com.example.pocketsoccer.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameDao {

    @Query("SELECT * FROM game")
    List<Game> getAll();

    @Query("SELECT * FROM game WHERE uid IN (:gameIds)")
    List<Game> loadAllByIds(int[] gameIds);

    @Query("SELECT * FROM game WHERE player1Name = :player1 AND " +
            "player2Name = :player2")
    List<Game> find(String player1, String player2);

    @Query("DELETE FROM game WHERE player1Name = :player1 AND " +
            "player2Name = :player2")
    void delete(String player1, String player2);

    @Query("DELETE FROM game")
    void deleteAll();

    @Insert
    void insertAll(Game... games);

    @Delete
    void delete(Game game);

}
