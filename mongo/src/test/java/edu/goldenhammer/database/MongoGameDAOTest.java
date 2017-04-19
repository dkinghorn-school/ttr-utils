package edu.goldenhammer.database;

/**
 * Created by devonkinghorn on 4/18/17.
 */
import org.junit.Test;

public class MongoGameDAOTest {
    @Test
    public void testStartGame() {
        MongoGameDAO dao = new MongoGameDAO();
        dao.playGame("jjjj");
    }
}
