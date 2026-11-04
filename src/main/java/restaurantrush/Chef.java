package restaurantrush;

/** Chef: does one unit of cooking on one order per turn. */
public class Chef extends Staff {
    public Chef(String name) {
        super(name);
    }

    public void advanceOrder(Order order) throws InvalidActionException {
        requireNotActed();
        if (order == null) {
            throw new InvalidActionException("There is no order to cook.");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new InvalidActionException("That order cannot be cooked (it is "
                    + order.getStatus().name().toLowerCase().replace('_', ' ') + ").");
        }
        order.decrementPrepUnit();
        markActed();
    }
}
