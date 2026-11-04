package restaurantrush;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Text boundary: parse decisions, call the controller and display results. */
public class GameCLI {
    private final GameController controller;
    private final Scanner scanner;

    public GameCLI(GameController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("RESTAURANT RUSH | 18 turns | 4 paid, $45 revenue, 60 average satisfaction");
        System.out.println("Customers choose their own food. Waiter acts before Chef; Cashier is automatic.");
        for (MenuItem item : controller.getMenuItems()) {
            System.out.println(item.getName() + " " + money(item.getPriceInCents())
                    + " | preparation units: " + item.getPreparationUnits());
        }
        try {
            while (!controller.isGameOver()) {
                controller.beginTurn();
                displayTurnStatus();
                promptWaiterAction();
                controller.finishWaiterPhase();
                promptChefAction();
                controller.finishTurn();
                displayTurnResults();
            }
            displayFinalResults();
        } catch (NoSuchElementException e) {
            System.out.println("Input ended. Session interrupted; no final result declared.");
        } catch (InvalidActionException e) {
            throw new IllegalStateException("Internal turn coordination failed", e);
        }
    }

    public void displayTurnStatus() {
        System.out.printf(Locale.ROOT, "%nTURN %d / 18 | revenue %s | paid %d | average %s%n",
                controller.getCurrentTurn(), money(controller.getRevenueInCents()),
                controller.getPaidCustomerCount(), average());
        System.out.println("Queue:");
        if (controller.getWaitingQueue().isEmpty()) { System.out.println("  empty"); }
        for (Customer c : controller.getWaitingQueue().getCustomers()) { System.out.println("  " + describe(c)); }
        for (Table t : controller.getTables()) {
            System.out.println("Table " + t.getTableId() + ": "
                    + (t.isOccupied() ? describe(t.getCurrentCustomer()) : "free"));
        }
        System.out.println("Waiter: seat/order/serve or wait (one task). Chef: cook or wait (one unit).");
        for (String event : controller.getTurnEvents()) { System.out.println("  " + event); }
    }

    public void promptWaiterAction() {
        while (true) {
            System.out.print("Waiter [seat <customerId> <tableId> | order <tableId> | serve <tableId> | wait]: ");
            String[] parts = readParts();
            try {
                switch (parts[0]) {
                    case "wait": requireLength(parts, 1); return;
                    case "seat": requireLength(parts, 3);
                        controller.waiterSeat(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])); break;
                    case "order": requireLength(parts, 2);
                        controller.waiterTakeOrder(Integer.parseInt(parts[1])); break;
                    case "serve": requireLength(parts, 2);
                        controller.waiterServe(Integer.parseInt(parts[1])); break;
                    default: throw new IllegalArgumentException("Choose seat, order, serve or wait.");
                }
                System.out.println(controller.getTurnEvents().get(controller.getTurnEvents().size() - 1));
                return;
            } catch (InvalidActionException | IllegalArgumentException e) { displayError(e.getMessage()); }
        }
    }

    public void promptChefAction() {
        while (true) {
            System.out.print("Chef [cook <tableId> | wait]: ");
            String[] parts = readParts();
            try {
                if (parts[0].equals("wait")) { requireLength(parts, 1); return; }
                if (!parts[0].equals("cook")) { throw new IllegalArgumentException("Choose cook or wait."); }
                requireLength(parts, 2);
                controller.chefAdvance(Integer.parseInt(parts[1]));
                return;
            } catch (InvalidActionException | IllegalArgumentException e) { displayError(e.getMessage()); }
        }
    }

    private String[] readParts() { return scanner.nextLine().trim().toLowerCase(Locale.ROOT).split("\\s+"); }
    private void requireLength(String[] parts, int length) {
        if (parts.length != length) { throw new IllegalArgumentException("Wrong number of arguments; follow the prompt."); }
    }
    public void displayError(String message) { System.out.println("Invalid choice: " + message + " Try again."); }
    public void displayTurnResults() {
        System.out.println("Events:");
        for (String event : controller.getTurnEvents()) { System.out.println("  " + event); }
        System.out.printf(Locale.ROOT, "End turn %d | served %d | unhappy departures %d | paid %d | revenue %s | average %s | turns used %d%n",
                controller.getTurnsUsed(), controller.getServedCount(), controller.getAbandonedCount(),
                controller.getPaidCustomerCount(), money(controller.getRevenueInCents()), average(), controller.getTurnsUsed());
    }
    public void displayFinalResults() {
        System.out.println(controller.isVictory() ? "VICTORY" : "DEFEAT");
        System.out.printf(Locale.ROOT, "Final: paid=%d revenue=%s average=%s turns=%d%n",
                controller.getPaidCustomerCount(), money(controller.getRevenueInCents()), average(), controller.getTurnsUsed());
    }
    private String average() {
        return controller.getPaidCustomerCount() == 0 ? "N/A"
                : String.format(Locale.ROOT, "%.2f", controller.getAverageSatisfaction());
    }
    private String describe(Customer c) {
        Order o = c.getOrder();
        return c + " satisfaction=" + c.getSatisfaction() + " state=" + c.getState()
                + (o == null ? " | no order" : " | " + o.getMenuItem().getName() + " " + o.getStatus()
                + " prep=" + o.getRemainingPrepUnits() + " bill=" + money(o.getBillInCents()));
    }
    private static String money(int cents) { return String.format(Locale.ROOT, "$%.2f", cents / 100.0); }
}
