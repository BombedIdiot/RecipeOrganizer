package software.blowtorch.recipeorganizer;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import static software.blowtorch.recipeorganizer.DBHelper.*;


public class Directions {
    private int directionID;
    private String direction;

    protected Directions(String direction) {
        this.direction = direction;
        this.directionID = DBHelper.doesXExist(DIRECTIONS_TABLE, DIRECTION_ID, DIRECTION_NAME, direction);
        if (this.directionID == 0) {
            this.directionID = DBHelper.writeOneValueToDB(DIRECTIONS_TABLE, DIRECTION_NAME, direction);
        }
    }

    protected int getDirectionID() { return this.directionID; }
    protected String getDirection() { return this.direction; }

    protected void setDirection(String d) { this.direction = d; }

    protected static ArrayList<Directions> getDirectionsByRecipeID(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Directions> dir = new ArrayList<>();
        try {
            conn = DBHelper.connectDB();
            String sql = "SELECT " + DIRECTION_NAME + " FROM " + DIRECTIONS_TABLE + "," + DIRECTIONS_RECIPE_TABLE +
                    " WHERE " + DIRECTION_ID + "=" + DIRECTIONS_FK + " AND " + RECIPE_FK_DIRECTION + "=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next() != false) {
                do {
                    Directions d = new Directions(rs.getString(DIRECTION_NAME));
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
