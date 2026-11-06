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
                displayTurnStatus();
                controller.beginTurn();
                displayArrivalUpdates();
                promptHappyHour();
                if (controller.hasHost()) {
                    promptHostAction();
                    controller.finishHostPhase();
                }
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
        displayQueueAndTables();
        if (controller.hasHost()) { System.out.println("Host: seat the next queued customer or wait (one task)."); }
        System.out.println("Waiter: seat/order/serve or wait (one task). Chef: cook or wait (one unit).");
        if (controller.hasPolicyManager()) {
            System.out.println("Happy Hour: " + controller.getHappyHourRemainingTurns() + " active turns remaining | "
                    + (controller.hasHappyHourBeenUsed() ? "activation used" : "activation available"));
        }
    }

    private void displayArrivalUpdates() {
        if (controller.getTurnEvents().isEmpty()) { return; }
        System.out.println("Turn opening updates:");
        for (String event : controller.getTurnEvents()) { System.out.println("  " + event); }
        displayQueueAndTables();
    }

    private void displayQueueAndTables() {
        System.out.println("Queue:");
        if (controller.getWaitingQueue().isEmpty()) { System.out.println("  empty"); }
        for (Customer c : controller.getWaitingQueue().getCustomers()) { System.out.println("  " + describe(c)); }
        for (Table t : controller.getTables()) {
            System.out.println("Table " + t.getTableId() + ": "
                    + (t.isOccupied() ? describe(t.getCurrentCustomer()) : "free"));
        }
    }

    public void promptHostAction() {
        while (true) {
            System.out.print("Host [seat | wait]: ");
            String[] parts = readParts();
            try {
                requireLength(parts, 1);
                if (parts[0].equals("wait")) { return; }
                if (!parts[0].equals("seat")) { throw new IllegalArgumentException("Choose seat or wait."); }
                controller.hostSeat();
                System.out.println(controller.getTurnEvents().get(controller.getTurnEvents().size() - 1));
                displayQueueAndTables();
                return;
            } catch (InvalidActionException | IllegalArgumentException e) { displayError(e.getMessage()); }
        }
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

    public void promptHappyHour() {
        if (!controller.hasPolicyManager() || controller.hasHappyHourBeenUsed()) { return; }
        while (true) {
            System.out.print("Activate Happy Hour? [yes/no]: ");
            String answer = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            if (answer.equals("no") || answer.equals("n")) { return; }
            if (answer.equals("yes") || answer.equals("y")) {
                try {
                    controller.activateHappyHour();
                    System.out.println("Happy Hour active: " + controller.getHappyHourRemainingTurns() + " turns remaining.");
                    return;
                } catch (InvalidActionException e) { displayError(e.getMessage()); }
            } else { displayError("Enter yes or no."); }
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
