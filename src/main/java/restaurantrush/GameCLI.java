package restaurantrush;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Text boundary: reads the manager's decisions, passes them to the controller and displays the results.
 * It contains no game rules; every rule check happens in the controller and domain objects.
 */
public class GameCLI {
    private final GameController controller;
    private final Scanner scanner;

    public GameCLI(GameController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    public void run() {
        System.out.printf(Locale.ROOT,
                "RESTAURANT RUSH | %d turns | 4 paid, $45 revenue, 60 average satisfaction%n",
                GameController.MAX_TURNS);
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
            // The CLI always calls the phases in order, so reaching this means a programming error.
            throw new IllegalStateException("Internal turn coordination failed", e);
        }
    }

    public void displayTurnStatus() {
        System.out.printf(Locale.ROOT, "%nTURN %d / %d | revenue %s | paid %d | average %s%n",
                controller.getCurrentTurn(), GameController.MAX_TURNS, money(controller.getRevenueInCents()),
                controller.getPaidCustomerCount(), average());
        displayQueueAndTables();
        if (controller.hasHost()) {
            System.out.println("Host: seat the next queued customer or wait (one task).");
        }
        System.out.println("Waiter: seat/order/serve or wait (one task). Chef: cook or wait (one unit).");
        if (controller.hasPolicyManager()) {
            String activation = controller.hasHappyHourBeenUsed() ? "activation used" : "activation available";
            System.out.println("Happy Hour: " + controller.getHappyHourRemainingTurns()
                    + " active turns remaining | " + activation);
        }
    }

    private void displayArrivalUpdates() {
        if (controller.getTurnEvents().isEmpty()) {
            return;
        }
        System.out.println("Turn opening updates:");
        printEvents();
        displayQueueAndTables();
    }

    private void displayQueueAndTables() {
        System.out.println("Queue:");
        if (controller.getWaitingQueue().isEmpty()) {
            System.out.println("  empty");
        }
        for (Customer customer : controller.getWaitingQueue().getCustomers()) {
            System.out.println("  " + describe(customer));
        }
        for (Table table : controller.getTables()) {
            String occupant = table.isOccupied() ? describe(table.getCurrentCustomer()) : "free";
            System.out.println("Table " + table.getTableId() + ": " + occupant);
        }
    }

    public void promptHostAction() {
        while (true) {
            System.out.print("Host [seat | wait]: ");
            String[] parts = readParts();
            try {
                requireLength(parts, 1);
                if (parts[0].equals("wait")) {
                    return;
                }
                if (!parts[0].equals("seat")) {
                    throw new IllegalArgumentException("Choose seat or wait.");
                }
                controller.hostSeat();
                printLatestEvent();
                displayQueueAndTables();
                return;
            } catch (InvalidActionException | IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public void promptWaiterAction() {
        while (true) {
            System.out.print("Waiter [seat <customerId> <tableId> | order <tableId> | serve <tableId> | wait]: ");
            String[] parts = readParts();
            try {
                switch (parts[0]) {
                    case "wait":
                        requireLength(parts, 1);
                        return;
                    case "seat":
                        requireLength(parts, 3);
                        controller.waiterSeat(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        break;
                    case "order":
                        requireLength(parts, 2);
                        controller.waiterTakeOrder(Integer.parseInt(parts[1]));
                        break;
                    case "serve":
                        requireLength(parts, 2);
                        controller.waiterServe(Integer.parseInt(parts[1]));
                        break;
                    default:
                        throw new IllegalArgumentException("Choose seat, order, serve or wait.");
                }
                printLatestEvent();
                return;
            } catch (InvalidActionException | IllegalArgumentException e) {
                // NumberFormatException is an IllegalArgumentException, so a typo like "seat a 1" lands here too.
                displayError(e.getMessage());
            }
        }
    }

    public void promptHappyHour() {
        if (!controller.hasPolicyManager() || controller.hasHappyHourBeenUsed()) {
            return;
        }
        while (true) {
            System.out.print("Activate Happy Hour? [yes/no]: ");
            String answer = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            if (answer.equals("no") || answer.equals("n")) {
                return;
            }
            if (!answer.equals("yes") && !answer.equals("y")) {
                displayError("Enter yes or no.");
                continue;
            }
            try {
                controller.activateHappyHour();
                System.out.println("Happy Hour active: " + controller.getHappyHourRemainingTurns()
                        + " turns remaining.");
                return;
            } catch (InvalidActionException e) {
                displayError(e.getMessage());
            }
        }
    }

    public void promptChefAction() {
        while (true) {
            System.out.print("Chef [cook <tableId> | wait]: ");
            String[] parts = readParts();
            try {
                if (parts[0].equals("wait")) {
                    requireLength(parts, 1);
                    return;
                }
                if (!parts[0].equals("cook")) {
                    throw new IllegalArgumentException("Choose cook or wait.");
                }
                requireLength(parts, 2);
                controller.chefAdvance(Integer.parseInt(parts[1]));
                return;
            } catch (InvalidActionException | IllegalArgumentException e) {
                displayError(e.getMessage());
            }
        }
    }

    public void displayError(String message) {
        System.out.println("Invalid choice: " + message + " Try again.");
    }

    public void displayTurnResults() {
        System.out.println("Events:");
        printEvents();
        System.out.printf(Locale.ROOT,
                "End turn %d | served %d | unhappy departures %d | paid %d | revenue %s | average %s"
                        + " | turns used %d%n",
                controller.getTurnsUsed(), controller.getServedCount(), controller.getAbandonedCount(),
                controller.getPaidCustomerCount(), money(controller.getRevenueInCents()), average(),
                controller.getTurnsUsed());
    }

    public void displayFinalResults() {
        System.out.println(controller.isVictory() ? "VICTORY" : "DEFEAT");
        System.out.printf(Locale.ROOT, "Final: paid=%d revenue=%s average=%s turns=%d%n",
                controller.getPaidCustomerCount(), money(controller.getRevenueInCents()), average(),
                controller.getTurnsUsed());
    }

    private String[] readParts() {
        return scanner.nextLine().trim().toLowerCase(Locale.ROOT).split("\\s+");
    }

    private void requireLength(String[] parts, int length) {
        if (parts.length != length) {
            throw new IllegalArgumentException("Wrong number of arguments; follow the prompt.");
        }
    }

    private void printEvents() {
        for (String event : controller.getTurnEvents()) {
            System.out.println("  " + event);
        }
    }

    /** Shows the result of the action the manager just made (the controller records it as the newest event). */
    private void printLatestEvent() {
        List<String> events = controller.getTurnEvents();
        System.out.println(events.get(events.size() - 1));
    }

    private String average() {
        if (controller.getPaidCustomerCount() == 0) {
            return "N/A";
        }
        return String.format(Locale.ROOT, "%.2f", controller.getAverageSatisfaction());
    }

    private String describe(Customer customer) {
        Order order = customer.getOrder();
        String orderText = order == null
                ? " | no order"
                : " | " + order.getMenuItem().getName() + " " + order.getStatus()
                        + " prep=" + order.getRemainingPrepUnits() + " bill=" + money(order.getBillInCents());
        return customer + " satisfaction=" + customer.getSatisfaction() + " state=" + customer.getState() + orderText;
    }

    private static String money(int cents) {
        return String.format(Locale.ROOT, "$%.2f", cents / 100.0);
    }
}
