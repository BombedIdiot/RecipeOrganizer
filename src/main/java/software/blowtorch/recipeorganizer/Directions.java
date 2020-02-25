package software.blowtorch.recipeorganizer;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import static software.blowtorch.recipeorganizer.DBHelper.*;


public class Directions {
    private int directionID;
    private int directionDatabaseID;
    private String direction;

    protected Directions(String direction) {
        this.direction = direction;
        this.directionID = DBHelper.doesXExist(DIRECTIONS_TABLE, DIRECTION_ID, DIRECTION_NAME, direction);
        if (this.directionID == 0) {
            this.directionID = DBHelper.writeOneValueToDB(DIRECTIONS_TABLE, DIRECTION_NAME, direction);
        }
    }

    protected int    getDirectionID()          { return this.directionID; }
    protected int    getDirectionDatabaseID()  { return this.directionDatabaseID; }
    protected String getDirection()            { return this.direction; }

    protected void setDirection(String d)           { this.direction = d; }
    protected void setDirectionDatabaseID(int sdd)  { this.directionDatabaseID = sdd; }


    protected static ArrayList<Directions> getDirectionsByRecipeID(int id) {
        ArrayList<Directions> dir = new ArrayList<>();
        String sql = "SELECT " + DIRECTION_NAME + ", "+DIRECTIONS_RECIPE_ID+ " FROM " + DIRECTIONS_TABLE + "," + DIRECTIONS_RECIPE_TABLE +
                     " WHERE " + DIRECTION_ID + "=" + DIRECTIONS_FK + " AND " + RECIPE_FK_DIRECTION + "=?";
        try (Connection conn = DBHelper.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery();) {
                if (rs.next() != false) {
                    do {
                        Directions d = new Directions(rs.getString(DIRECTION_NAME));
                        d.setDirectionDatabaseID(rs.getInt(DIRECTIONS_RECIPE_ID));
                        dir.add(d);
                    } while (rs.next());
                    return dir;
                }
                return dir;
            }
        } catch (SQLException ex) {
            System.err.println("Exception :" + ex.getMessage() + " in getDirectionsByRecipeID");
            return dir;
        }
    }
}
