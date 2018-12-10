package jackwtat.simplembta.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    public Favorite[] loadAllFavorites();

    @Query("SELECT * FROM favorites " +
            "WHERE route_id = :routeId AND stop_id = :stopId AND direction_id = :directionId")
    public Favorite[] loadFavorite(String routeId, String stopId, int directionId);

    @Insert
    public void insertFavorite(Favorite favorite);

    @Delete
    public void deleteFavorite(Favorite favorite);
}
