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
        System.out.println("Going in to check "+direction);
        this.directionID = DBHelper.doesXExist(DIRECTIONS_TABLE, DIRECTION_ID, DIRECTION_NAME, direction);
        System.out.println("Checking "+this.directionID);
        if (this.directionID == 0) {
            this.directionID = DBHelper.writeOneValueToDB(DIRECTIONS_TABLE, DIRECTION_NAME, direction);
            System.out.println("Set new direction "+this.directionID);
        }
    }

    protected int getDirectionID() { return this.directionID; }
    protected int getDirectionDatabaseID() { return this.directionDatabaseID; }
    protected String getDirection() { return this.direction; }

    protected void setDirection(String d) { this.direction = d; }
    protected void setDirectionDatabaseID(int sdd) { this.directionDatabaseID = sdd; }


    protected static ArrayList<Directions> getDirectionsByRecipeID(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Directions> dir = new ArrayList<>();
        try {
            conn = DBHelper.connectDB();
            String sql = "SELECT " + DIRECTION_NAME + ", "+DIRECTIONS_RECIPE_ID+ " FROM " + DIRECTIONS_TABLE + "," + DIRECTIONS_RECIPE_TABLE +
                    " WHERE " + DIRECTION_ID + "=" + DIRECTIONS_FK + " AND " + RECIPE_FK_DIRECTION + "=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next() != false) {
                do {
                    Directions d = new Directions(rs.getString(DIRECTION_NAME));
                    d.setDirectionDatabaseID(rs.getInt(DIRECTIONS_RECIPE_ID));
                    dir.add(d);
                } while (rs.next());
                return dir;
            }
            return dir;
        } catch (SQLException ex) {
            System.err.println("Exception :" + ex.getMessage() + " in getDirectionsByRecipeID");
            return dir;
        } finally {
            try {
                if (rs != null)rs.close();
                if (stmt != null)stmt.close();
                if (conn != null)conn.close();
            } catch (SQLException ex) {
                System.err.println("Exception :" + ex.getMessage() + " in getDirectionsByRecipeID");
            }
        }
    }
}
