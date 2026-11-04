package restaurantrush;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Real-object turn scenarios and CLI replays. No network or external test libraries. */
public final class IntegrationChecks {
    private static int checks;
    static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    interface Action { void run() throws Exception; }
    static void rejects(Action action, String message) throws Exception {
        try { action.run(); } catch (InvalidActionException | IllegalStateException e) { checks++; return; }
        throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        pastaTiming(); abandonment(); phases(); thresholds(); cliInvalidInput();
        String victory = play(Main.createGame(), true);
        String defeat = play(Main.createGame(), false);
        replay(victory, true); replay(defeat, false);
        if (args.length == 1 && args[0].equals("--write-examples")) {
            Files.createDirectories(Path.of("examples"));
            Files.writeString(Path.of("examples/victory.txt"), victory);
            Files.writeString(Path.of("examples/defeat.txt"), defeat);
        }
        System.out.println("Integration checks passed: " + checks);
    }

    static GameController custom(Menu menu, Customer customer) {
        return new GameController(List.of(new Table(1), new Table(2)), new WaitingQueue(),
                new Waiter("W"), new Chef("C"), new Cashier("K"), menu,
                new ArrivalSchedule(Map.of(1, customer)));
    }
    static void end(GameController game) throws Exception { game.finishWaiterPhase(); game.finishTurn(); }
    static void pastaTiming() throws Exception {
        Menu menu = new Menu(); menu.addItem(new MenuItem("Pasta", 1800, 2));
        Customer c = new RegularCustomer(1);
        GameController game = custom(menu, c);
        game.beginTurn(); game.waiterSeat(1, 1); end(game);
        check(c.getSatisfaction() == 92, "Turn 1 loss");
        game.beginTurn(); game.waiterTakeOrder(1); game.finishWaiterPhase(); game.chefAdvance(1); game.finishTurn();
        check(c.getSatisfaction() == 84, "Turn 2 loss");
        game.beginTurn(); rejects(() -> game.waiterServe(1), "Premature serve");
        check(!game.hasWaiterActedThisTurn(), "Rejected action preserves flag");
        game.finishWaiterPhase(); game.chefAdvance(1);
        rejects(() -> game.waiterServe(1), "Waiter cannot serve after Chef"); game.finishTurn();
        check(c.getSatisfaction() == 76 && c.getOrder().isReady(), "Turn 3 ready");
        game.beginTurn(); game.waiterServe(1); end(game);
        check(c.getState() == CustomerState.EATING && game.getPaidCustomerCount() == 0, "No same-turn eating/payment");
        game.beginTurn(); end(game);
        check(c.getState() == CustomerState.READY_TO_PAY && game.getRevenueInCents() == 0, "No newly-ready payment");
        game.beginTurn(); end(game);
        check(c.getState() == CustomerState.PAID && game.getRevenueInCents() == 1800, "Pay on turn 6");
        check(game.getAverageSatisfaction() == 76 && !game.getTables().get(0).isOccupied(), "Record satisfaction and free table");
        game.beginTurn(); end(game);
        check(game.getPaidCustomerCount() == 1, "No duplicate payments");
    }
    static void abandonment() throws Exception {
        Menu menu = new Menu(); menu.addItem(new MenuItem("Pasta", 1800, 2));
        Customer c = new RegularCustomer(1); GameController game = custom(menu, c);
        game.beginTurn(); game.waiterSeat(1, 1); end(game);
        game.beginTurn(); game.waiterTakeOrder(1); end(game);
        while (game.getCurrentTurn() <= 13) { game.beginTurn(); end(game); }
        check(c.getState() == CustomerState.ABANDONED && c.getOrder().getStatus() == OrderStatus.CANCELLED,
                "Cancel unpaid abandoned order");
        check(game.getAbandonedCount() == 1 && game.getRevenueInCents() == 0 && !game.getTables().get(0).isOccupied(),
                "Abandonment cleanup");
        game.beginTurn(); end(game); check(game.getAbandonedCount() == 1, "Count departure once");
    }
    static void phases() throws Exception {
        GameController game = Main.createGame();
        rejects(() -> game.waiterSeat(1, 1), "No action before begin");
        game.beginTurn(); rejects(game::beginTurn, "No duplicate arrivals");
        rejects(() -> game.chefAdvance(1), "Waiter first");
        rejects(() -> game.waiterSeat(1, 99), "Invalid table");
        check(game.getWaitingQueue().getQueueSize() == 1 && !game.hasWaiterActedThisTurn(), "Invalid seating atomic");
        game.waiterSeat(1, 1); rejects(() -> game.waiterTakeOrder(1), "One Waiter task"); end(game);
        rejects(game::finishTurn, "No duplicate finish");
    }
    static void thresholds() {
        check(GameController.meetsTargets(4, 4500, 240), "Exact victory boundary");
        check(!GameController.meetsTargets(3, 4500, 240), "Paid threshold");
        check(!GameController.meetsTargets(4, 4499, 240), "Revenue threshold");
        check(!GameController.meetsTargets(4, 4500, 239), "Average threshold unrounded");
    }
    static String play(GameController game, boolean efficient) throws Exception {
        List<String> commands = new ArrayList<>();
        while (!game.isGameOver()) {
            game.beginTurn();
            String command = "wait";
            if (efficient) {
                Table target = game.getTables().stream().filter(t -> t.isOccupied()
                        && t.getCurrentCustomer().getOrder() != null && t.getCurrentCustomer().getOrder().isReady())
                        .findFirst().orElse(null);
                if (target != null) { game.waiterServe(target.getTableId()); command = "serve " + target.getTableId(); }
                else {
                    target = game.getTables().stream().filter(t -> t.isOccupied()
                            && t.getCurrentCustomer().getState() == CustomerState.SEATED
                            && t.getCurrentCustomer().getOrder() == null).findFirst().orElse(null);
                    if (target != null) { game.waiterTakeOrder(target.getTableId()); command = "order " + target.getTableId(); }
                    else {
                        target = game.getTables().stream().filter(t -> !t.isOccupied()).findFirst().orElse(null);
                        Customer next = game.getWaitingQueue().getNextCustomer();
                        if (target != null && next != null) {
                            game.waiterSeat(next.getCustomerId(), target.getTableId());
                            command = "seat " + next.getCustomerId() + " " + target.getTableId();
                        }
                    }
                }
            }
            commands.add(command); game.finishWaiterPhase(); command = "wait";
            if (efficient) {
                for (Table t : game.getTables()) {
                    Order o = t.isOccupied() ? t.getCurrentCustomer().getOrder() : null;
                    if (o != null && o.getStatus() == OrderStatus.IN_PROGRESS) {
                        game.chefAdvance(t.getTableId()); command = "cook " + t.getTableId(); break;
                    }
                }
            }
            commands.add(command); game.finishTurn();
            check(!game.isVictory() || game.isGameOver(), "No early result");
        }
        System.out.printf("%s scenario: paid=%d cents=%d avg=%.2f turns=%d%n",
                efficient ? "Efficient" : "Wait", game.getPaidCustomerCount(), game.getRevenueInCents(),
                game.getAverageSatisfaction(), game.getTurnsUsed());
        check(game.getTurnsUsed() == 18 && game.isVictory() == efficient, "Full reproducible result");
        check(game.getPaidCustomerCount() <= 5, "Late arrival cannot pay in base timing");
        rejects(game::beginTurn, "No turn 19");
        return String.join("\n", commands) + "\n";
    }
    static void replay(String input, boolean victory) throws Exception {
        GameController game = Main.createGame(); String output = cli(game, input);
        check(game.isGameOver() && game.isVictory() == victory, "CLI replay result");
        check(output.contains(victory ? "VICTORY" : "DEFEAT") && output.contains("turns=18"), "CLI final output");
    }
    static void cliInvalidInput() throws Exception {
        GameController game = Main.createGame();
        String output = cli(game, "nonsense\nseat abc 1\nseat 1 99\nseat 1 1\ncook 1\nwait\n");
        check(output.contains("Invalid choice") && output.contains("Session interrupted"), "Retry and EOF");
        check(game.getTurnsUsed() == 1 && game.getTables().get(0).isOccupied(), "Retry preserves successful action");
        check(!output.contains("VICTORY") && !output.contains("DEFEAT"), "EOF not a final result");
    }
    static String cli(GameController game, String input) throws Exception {
        PrintStream original = System.out; ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capture); new GameCLI(game, new Scanner(input)).run();
        } finally { System.setOut(original); }
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
