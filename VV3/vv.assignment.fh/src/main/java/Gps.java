import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class Gps {

    double latitude, longitude;

    public Gps(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public enum Direction {
        NORDEN, OSTEN, SUEDEN, WESTEN;

        private static final List<Direction> VALUES =
                Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = VALUES.size();
        private static final Random RANDOM = new Random();


        public static Direction randomDirection() {
            return VALUES.get(RANDOM.nextInt(SIZE));
        }


        public static Direction weightedRandomDirection(Direction preferredDirection) {
            boolean gotPreferredDirection = false;
            int i = 0;
            do {
                if (randomDirection() == preferredDirection) return preferredDirection;

            } while (i < 5);
            return randomDirection();
        }
    }

    ;


    public void driveToTheNorthLong() {
        this.latitude += 0.01;
    }


    public void driveToTheEastLong() {
        this.longitude += 0.01;
    }


    public void driveToTheSouthLong() {
        this.latitude -= 0.01;
    }

    public void driveToTheWestLong() {
        this.longitude -= 0.01;
    }

    public void driveToTheNorthShort() {
        this.latitude += 0.001;
    }

    public void driveToTheEastShort() {
        this.longitude += 0.001;
    }

    public void driveToTheSouthShort() {
        this.latitude -= 0.001;
    }

    public void driveToTheWestShort() {
        this.longitude -= 0.001;
    }

    @Override
    public String toString() {
        return "Gps{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}