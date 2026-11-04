package restaurantrush;

import java.util.List;
import java.util.Map;

/** Happy Hour boundaries and independent Host/Waiter integration using real objects. */
public final class Stage3IntegrationChecks {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++; if (!condition) { throw new AssertionError(message); }
    }
    private static Menu menu() {
        Menu menu = new Menu(); menu.addItem(new MenuItem("Burger", 1500, 1)); return menu;
    }
    private static GameController game(Customer customer, Host host) {
        return new GameController(List.of(new Table(1), new Table(2)), new WaitingQueue(),
                new Waiter("W"), new Chef("C"), new Cashier("K"), menu(),
                new ArrivalSchedule(Map.of(1, customer)), host, new PolicyManager());
    }
    public static void main(String[] args) throws Exception {
        recoveryAndPricing(); oldOrder(); hostIndependence(); lateActivation();
        check(Main.createGame().getMenuItems().get(3).getPriceInCents() == 2430, "Combo registration");
        String output = IntegrationChecks.cli(Main.createGame(), "maybe\nyes\norder 1\ncook 1\n");
        check(output.contains("Enter yes or no") && output.contains("2 turns remaining")
                && output.contains("1 active turns remaining"), "Activation input and duration display");
        System.out.println("Stage 3 integration checks passed: " + checks);
    }
    private static void recoveryAndPricing() throws Exception {
        Customer vip = new VIPCustomer(2); GameController g = game(vip, new Host("H"));
        IntegrationChecks.rejects(g::activateHappyHour, "No activation before turn");
        g.beginTurn(); check(vip.getState() == CustomerState.SEATED && !g.hasWaiterActedThisTurn(), "Host independent");
        IntegrationChecks.end(g); check(vip.getSatisfaction() == 95, "Unserved VIP loss");
        g.beginTurn(); IntegrationChecks.end(g); check(vip.getSatisfaction() == 90, "Two waiting turns");
        g.beginTurn(); g.activateHappyHour(); check(vip.getSatisfaction() == 100, "Recovery caps");
        IntegrationChecks.rejects(g::activateHappyHour, "No repeated activation/recovery");
        check(vip.getSatisfaction() == 100 && g.getHappyHourRemainingTurns() == 2, "Failed activation atomic");
        g.waiterTakeOrder(1); check(vip.getOrder().getBillInCents() == 1080, "VIP then Happy Hour");
        g.finishWaiterPhase(); g.finishTurn(); check(vip.getSatisfaction() == 95, "Loss still occurs at end");
        g.beginTurn(); check(vip.getSatisfaction() == 100 && g.getHappyHourRemainingTurns() == 1, "Next active turn recovery");
        g.finishWaiterPhase(); g.chefAdvance(1); g.finishTurn();
        check(g.getHappyHourRemainingTurns() == 0 && vip.getSatisfaction() == 95, "Expiry after two turns");
        g.beginTurn(); check(vip.getSatisfaction() == 95, "No third recovery");
        g.waiterServe(1); IntegrationChecks.end(g);
        g.beginTurn(); IntegrationChecks.end(g); g.beginTurn(); IntegrationChecks.end(g);
        check(vip.getState() == CustomerState.PAID && g.getRevenueInCents() == 1080
                && g.getAverageSatisfaction() == 95, "Discounted real payment and recorded satisfaction");
        g.beginTurn(); IntegrationChecks.rejects(g::activateHappyHour, "Cannot reactivate after expiry");

        Customer regular = new RegularCustomer(1); g = game(regular, null);
        for (int turn = 1; turn <= 3; turn++) { g.beginTurn(); IntegrationChecks.end(g); }
        check(regular.getSatisfaction() == 76, "Queued waiting before recovery");
        g.beginTurn(); g.activateHappyHour(); check(regular.getSatisfaction() == 91, "Exactly 15 recovery on activation");
        IntegrationChecks.end(g); check(regular.getSatisfaction() == 83, "End loss after recovery");
        g.beginTurn(); check(regular.getSatisfaction() == 98, "Exactly 15 on next turn");
    }
    private static void oldOrder() throws Exception {
        Customer c = new RegularCustomer(1); GameController g = game(c, new Host("H"));
        g.beginTurn(); g.waiterTakeOrder(1);
        IntegrationChecks.rejects(g::activateHappyHour, "Cannot activate after staff action");
        IntegrationChecks.end(g); g.beginTurn(); g.activateHappyHour();
        check(c.getOrder().getBillInCents() == 1500, "Old IN_PROGRESS bill not repriced");
        g.finishWaiterPhase(); g.chefAdvance(1); g.finishTurn();
        g.beginTurn(); g.waiterServe(1); IntegrationChecks.end(g);
        check(c.getOrder().getBillInCents() == 1500, "Old bill stays locked through serving");
        Customer eating = new RegularCustomer(1); g = game(eating, new Host("H"));
        g.beginTurn(); g.waiterTakeOrder(1); g.finishWaiterPhase(); g.chefAdvance(1); g.finishTurn();
        g.beginTurn(); g.waiterServe(1); IntegrationChecks.end(g); int satisfaction = eating.getSatisfaction();
        g.beginTurn(); g.activateHappyHour(); check(eating.getSatisfaction() == satisfaction, "No EATING recovery");
        IntegrationChecks.end(g); g.beginTurn(); check(eating.getSatisfaction() == satisfaction, "No READY_TO_PAY recovery");
        IntegrationChecks.end(g); check(eating.getSatisfaction() == satisfaction, "No paid recovery");
    }
    private static void hostIndependence() throws Exception {
        WaitingQueue q = new WaitingQueue(); q.addCustomer(new RegularCustomer(1)); q.addCustomer(new RegularCustomer(2));
        Table t1 = new Table(1), t2 = new Table(2);
        GameController g = new GameController(List.of(t2, t1), q, new Waiter("W"), new Chef("C"), new Cashier("K"),
                menu(), new ArrivalSchedule(Map.of()), new Host("H"), new PolicyManager());
        g.beginTurn(); check(t1.isOccupied() && !t2.isOccupied() && q.getQueueSize() == 1, "Host chooses lowest ID, seats only one");
        g.waiterSeat(2, 2); check(t2.isOccupied() && q.isEmpty(), "Waiter seats another independently");
    }
    private static void lateActivation() throws Exception {
        Customer c = new RegularCustomer(1); GameController g = game(c, null);
        for (int turn = 1; turn < 18; turn++) { g.beginTurn(); IntegrationChecks.end(g); }
        check(c.getState() == CustomerState.ABANDONED, "Customer already departed");
        g.beginTurn(); g.activateHappyHour(); check(c.getSatisfaction() == 0, "No recovery for departed customer");
        IntegrationChecks.end(g);
        check(g.isGameOver() && g.getTurnsUsed() == 18 && g.getHappyHourRemainingTurns() == 1, "Activation on final turn does not extend game");
    }
}
