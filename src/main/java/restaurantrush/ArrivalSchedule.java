package restaurantrush;

import java.util.HashMap;
import java.util.Map;

/** Deterministic arrivals; the factory is the only Stage 2 registration change. */
public class ArrivalSchedule {
    private final Map<Integer, Customer> arrivals;

    public ArrivalSchedule(Map<Integer, Customer> arrivals) {
        this.arrivals = Map.copyOf(arrivals);
        for (int turn : this.arrivals.keySet()) {
            if (turn < 1 || turn > 18) {
                throw new IllegalArgumentException("Arrival turn must be in 1..18");
            }
        }
    }

    public static ArrivalSchedule createBaseSchedule() {
        Map<Integer, Customer> arrivals = new HashMap<>();
        for (int id = 1; id <= 6; id++) {
            arrivals.put(1 + (id - 1) * 3, new RegularCustomer(id));
        }
        arrivals.put(4, new VIPCustomer(2));
        arrivals.put(10, new CriticCustomer(4));
        return new ArrivalSchedule(arrivals);
    }

    public Customer getArrivalFor(int turn) { return arrivals.get(turn); }
}
