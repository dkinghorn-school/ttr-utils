package edu.goldenhammer.database.postgresql.data_types;

import java.io.Serializable;

/**
 * Created by seanjib on 2/19/2017.
 */
public class SQLRoute implements Serializable {
    public static final int ROUTE_COUNT = 101;
    public static final String ROUTE_NUMBER = "route_number";
    public static final String CITY_1 = "city_1";
    public static final String CITY_2 = "city_2";
    public static final String ROUTE_COLOR = "route_color";
    public static final String ROUTE_LENGTH = "route_length";
    public static final String TABLE_NAME = "route";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "    %2$s INTEGER NOT NULL,\n" +
                    "    %3$s INTEGER NOT NULL,\n" +
                    "    %4$s INTEGER NOT NULL,\n" +
                    "    %5$s VARCHAR(10) NOT NULL,\n" +
                    "    %6$s INTEGER NOT NULL,\n" +
                    "    PRIMARY KEY(%2$s),\n" +
                    "    FOREIGN KEY(%3$s)" +
                    "       REFERENCES %7$s" +
                    "       ON DELETE CASCADE,\n" +
                    "    FOREIGN KEY(%4$s)" +
                    "       REFERENCES %7$s" +
                    "       ON DELETE CASCADE\n" +
                    ");",
            TABLE_NAME,
            ROUTE_NUMBER,
            CITY_1,
            CITY_2,
            ROUTE_COLOR,
            ROUTE_LENGTH,
            SQLCity.TABLE_NAME);
    public static final String INSERT_STMT = String.format(
            "INSERT INTO %1$s(%2$s, %3$s, %4$s, %5$s, %6$s) VALUES %7$s",
            TABLE_NAME,
            ROUTE_NUMBER,
            CITY_1,
            CITY_2,
            ROUTE_COLOR,
            ROUTE_LENGTH,
            getAllRoutes());

    public SQLRoute(String id, int city1, int city2, String routeColor, int routeLength) {
        this.id = id;
        this.city1 = city1;
        this.city2 = city2;
        this.routeColor = routeColor;
        this.routeLength = routeLength;
    }

    
    public String getID() {
        return id;
    }

    
    public int getCity1() {
        return city1;
    }

    
    public int getCity2() {
        return city2;
    }

    
    public String getRouteColor() {
        return routeColor;
    }

    
    public int getRouteLength() {
        return routeLength;
    }

    private static String getAllRoutes() {
        String formattedRoute = "";
        for(int i = 0; i < ROUTE_COUNT; i++) {
            formattedRoute += getFormattedRoute();
        }
        return formattedRoute.substring(0, formattedRoute.length() - 2) + ";"; //replaces the final comma with a semicolon
    }

    private static String getFormattedRoute() {
        return String.format("(?, %1$s, %2$s, ?, ?),\n",
                String.format("(SELECT %1$s FROM %2$s WHERE %3$s = ?)\n",
                        SQLCity.ID,
                        SQLCity.TABLE_NAME,
                        SQLCity.NAME),
                String.format("(SELECT %1$s FROM %2$s WHERE %3$s = ?)\n",
                        SQLCity.ID,
                        SQLCity.TABLE_NAME,
                        SQLCity.NAME)
        );
    }

    public static String columnNames() {
        return String.join(",",ROUTE_NUMBER,CITY_1,CITY_2,ROUTE_COLOR,ROUTE_LENGTH);
    }

    String id;
    int city1;
    int city2;
    String routeColor;
    int routeLength;
}
