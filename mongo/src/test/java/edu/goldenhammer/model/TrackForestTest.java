package edu.goldenhammer.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by devonkinghorn on 3/27/17.
 */

public class TrackForestTest {
    City city1 = new City(0,0,"city1");
    City city2 = new City(0,0,"city2");
    City city3 = new City(0,0,"city3");
    City city4 = new City(0,0,"city4");
    City city5 = new City(0,0,"city5");
    @Test
    public void testLongest() {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(city1,city2,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,2,0,0,0,0,0,false));
        tracks.add(new Track(city2,city4,2,Color.BLACK,3,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,5,Color.BLACK,4,0,0,0,0,0,false));
        tracks.add(new Track(city1,city2,2,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city2,city4,2,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,5,Color.BLACK,0,0,0,0,0,0,false));

        TrackForest forest = new TrackForest(tracks);
        assert(forest.getPlayerWithLongestTrack().contains(0));
        assert(!forest.getPlayerWithLongestTrack().contains(1));
    }

    @Test
    public void testTie() {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(city1,city2,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city4,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,5,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city2,2,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city2,city4,2,Color.BLACK,0,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,5,Color.BLACK,0,0,0,0,0,0,false));

        TrackForest forest = new TrackForest(tracks);
        assert(forest.getPlayerWithLongestTrack().contains(0));
        assert(forest.getPlayerWithLongestTrack().contains(1));
        assert(!forest.getPlayerWithLongestTrack().contains(2));
    }

    @Test
    public void testLongestPathForTree() {

        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(city1,city2,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city4,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,5,Color.BLACK,1,0,0,0,0,0,false));
        TrackForest forest = new TrackForest(tracks);
        assert(forest.getLongestPathForTree(forest.trees.get(0)) == 10);
    }

    @Test
    public void testCycle() {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(city1,city2,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city3,2,Color.BLACK,1,0,0,0,0,0,false));
        TrackForest forest = new TrackForest(tracks);
        assert(forest.getLongestPathForTree(forest.trees.get(0)) == 7);
    }

    @Test
    public void testCycleWhenCycleIsntLongest() {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(city1,city2,7,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city3,3,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city3,2,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city1,city4,7,Color.BLACK,1,0,0,0,0,0,false));
        tracks.add(new Track(city2,city5,7,Color.BLACK,1,0,0,0,0,0,false));
        TrackForest forest = new TrackForest(tracks);
        assert(forest.getLongestPathForTree(forest.trees.get(0)) == 21);
    }
}
