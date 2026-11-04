package restaurantrush;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Stage 1 domain checks. No framework: run main(); exit code 1 if any check fails. */
public class DomainChecks {
    private static int passed = 0;
    private static int failed = 0;

    interface Action { void run() throws Exception; }

    static void check(String name, boolean condition) {
        if (condition) { passed++; } else { failed++; System.out.println("FAIL: " + name); }
    }

    /** The action must throw InvalidActionException. */
    static void expectInvalid(String name, Action a) {
        try { a.run(); failed++; System.out.println("FAIL (no exception): " + name); }
        catch (InvalidActionException e) {
            if (e.getMessage() == null || e.getMessage().isEmpty()) { failed++; System.out.println("FAIL (no reason): " + name); }
            else { passed++; }
        } catch (Exception e) { failed++; System.out.println("FAIL (wrong exception " + e + "): " + name); }
    }

    static void expectThrows(String name, Class<? extends Exception> type, Action a) {
        try { a.run(); failed++; System.out.println("FAIL (no exception): " + name); }
        catch (Exception e) {
            if (type.isInstance(e)) { passed++; }
            else { failed++; System.out.println("FAIL (wrong exception " + e + "): " + name); }
        }
    }

    static Menu baseMenu() {
        Menu m = new Menu();
        m.addItem(new MenuItem("Salad", 900, 1));
        m.addItem(new MenuItem("Burger", 1500, 1));
        m.addItem(new MenuItem("Pasta", 1800, 2));
        return m;
    }

    static List<Table> twoTables() {
        List<Table> t = new ArrayList<>();
        t.add(new Table(1));
        t.add(new Table(2));
        return t;
    }

    /** Queue a new customer and seat them at the table using the given waiter. */
    static Customer seated(int id, WaitingQueue q, Table t, Waiter w) throws Exception {
        Customer c = new RegularCustomer(id);
        q.addCustomer(c);
        w.seatCustomer(q, c, t);
        w.resetForNewTurn();
        return c;
    }

    public static void main(String[] args) throws Exception {
        menuChecks();
        orderChecks();
        customerChecks();
        queueAndTableChecks();
        waiterChecks();
        chefChecks();
        cashierChecks();
        lifecycleCheck();
        System.out.println("Checks passed: " + passed + ", failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void menuChecks() {
        Menu m = baseMenu();
        check("menu keeps insertion order", m.getItems().get(0).getName().equals("Salad") && m.getItems().get(2).getName().equals("Pasta"));
        check("findItemByName case-insensitive", m.findItemByName("bUrGeR").getPriceInCents() == 1500);
        check("findItemByName missing -> null", m.findItemByName("Pizza") == null);
        expectThrows("menu items list is unmodifiable", UnsupportedOperationException.class,
                () -> m.getItems().add(new MenuItem("X", 1, 1)));
        expectThrows("duplicate menu name rejected", IllegalArgumentException.class, () -> m.addItem(new MenuItem("salad", 1, 1)));
        expectThrows("negative price rejected", IllegalArgumentException.class, () -> new MenuItem("X", -1, 1));
        expectThrows("zero prep units rejected", IllegalArgumentException.class, () -> new MenuItem("X", 1, 0));
        expectThrows("null item rejected", IllegalArgumentException.class, () -> m.addItem(null));
    }

    static void orderChecks() throws Exception {
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order o = new Order(pasta, 1800);
        check("new order IN_PROGRESS with item units", o.getStatus() == OrderStatus.IN_PROGRESS && o.getRemainingPrepUnits() == 2);
        check("order keeps locked bill", o.getBillInCents() == 1800);
        expectInvalid("cannot serve before ready", o::markServed);
        expectInvalid("cannot pay before served", o::markPaid);
        o.decrementPrepUnit();
        check("one unit left, still IN_PROGRESS", o.getRemainingPrepUnits() == 1 && o.getStatus() == OrderStatus.IN_PROGRESS && !o.isReady());
        o.decrementPrepUnit();
        check("zero units -> READY automatically", o.getStatus() == OrderStatus.READY && o.isReady());
        expectInvalid("prep never goes negative", o::decrementPrepUnit);
        check("remaining stays 0", o.getRemainingPrepUnits() == 0);
        o.markServed();
        check("READY -> SERVED", o.getStatus() == OrderStatus.SERVED);
        expectInvalid("served order cannot be cancelled", o::cancel);
        expectInvalid("served order cannot be served twice", o::markServed);
        o.markPaid();
        check("SERVED -> PAID", o.getStatus() == OrderStatus.PAID);
        expectInvalid("paid order cannot be paid twice", o::markPaid);
        expectInvalid("paid order cannot be cooked", o::decrementPrepUnit);

        Order c1 = new Order(pasta, 1800);
        c1.cancel();
        check("IN_PROGRESS -> CANCELLED", c1.getStatus() == OrderStatus.CANCELLED);
        expectInvalid("cancelled order cannot be cooked", c1::decrementPrepUnit);
        expectInvalid("cancelled order cannot be cancelled again", c1::cancel);
        Order c2 = new Order(new MenuItem("Salad", 900, 1), 900);
        c2.decrementPrepUnit();
        c2.cancel();
        check("READY -> CANCELLED", c2.getStatus() == OrderStatus.CANCELLED);
        expectThrows("null item rejected", IllegalArgumentException.class, () -> new Order(null, 1));
    }

    static void customerChecks() throws Exception {
        Customer c = new RegularCustomer(1);
        check("starts QUEUED, 100, no order, turns -1",
                c.getState() == CustomerState.QUEUED && c.getSatisfaction() == 100 && c.getOrder() == null
                        && c.getServedTurn() == -1 && c.getReadyToPayTurn() == -1);
        check("regular label/loss/bill", c.getTypeLabel().equals("Regular") && c.getSatisfactionLossRate() == 8 && c.calculateBill(1500) == 1500);
        for (int i = 0; i < 13; i++) c.loseSatisfaction();
        check("13 losses of 8 clamp at 0 (not negative)", c.getSatisfaction() == 0);
        c.loseSatisfaction();
        check("stays 0", c.getSatisfaction() == 0);
        Customer d = new RegularCustomer(2);
        d.loseSatisfaction();
        check("one loss = 92", d.getSatisfaction() == 92);

        // food rule on base menu: customers 1..6 -> Salad, Burger, Pasta, Salad, Burger, Pasta
        Menu m = baseMenu();
        String[] expected = {"Salad", "Burger", "Pasta", "Salad", "Burger", "Pasta"};
        boolean ok = true;
        for (int id = 1; id <= 6; id++) ok &= new RegularCustomer(id).chooseItem(m).getName().equals(expected[id - 1]);
        check("food rule (id-1) mod size for ids 1..6", ok);
        expectThrows("empty menu is invalid configuration", IllegalArgumentException.class, () -> c.chooseItem(new Menu()));
        expectThrows("customer id 0 rejected", IllegalArgumentException.class, () -> new RegularCustomer(0));

        // isWaiting per state
        Customer w = new RegularCustomer(3);
        check("QUEUED is waiting", w.isWaiting());
        w.setState(CustomerState.SEATED);
        check("SEATED is waiting", w.isWaiting());
        Order o = new Order(new MenuItem("Salad", 900, 1), 900);
        w.setOrder(o);
        o.decrementPrepUnit();
        check("SEATED with READY dish is still waiting", w.isWaiting() && o.isReady());
        o.markServed();
        w.markServed(4);
        check("EATING is not waiting, servedTurn set", !w.isWaiting() && w.getState() == CustomerState.EATING && w.getServedTurn() == 4);
        expectThrows("same-turn ready to pay rejected", IllegalStateException.class, () -> w.markReadyToPay(4));
        w.markReadyToPay(5);
        check("READY_TO_PAY is not waiting, turn stamped", !w.isWaiting() && w.getReadyToPayTurn() == 5);
        o.markPaid();
        w.setState(CustomerState.PAID);
        check("PAID is not waiting", !w.isWaiting());
        Customer a = new RegularCustomer(4);
        a.setState(CustomerState.ABANDONED);
        check("ABANDONED is not waiting", !a.isWaiting());

        // guards
        expectThrows("ABANDONED -> SEATED illegal", IllegalStateException.class, () -> a.setState(CustomerState.SEATED));
        expectThrows("QUEUED -> EATING via setState illegal", IllegalStateException.class, () -> new RegularCustomer(5).setState(CustomerState.EATING));
        expectThrows("QUEUED -> PAID illegal", IllegalStateException.class, () -> new RegularCustomer(5).setState(CustomerState.PAID));
        expectThrows("null state rejected", IllegalArgumentException.class, () -> new RegularCustomer(5).setState(null));
        expectThrows("setOrder while queued rejected", IllegalStateException.class,
                () -> new RegularCustomer(5).setOrder(new Order(new MenuItem("S", 1, 1), 1)));
        expectThrows("second order rejected", IllegalStateException.class, () -> w.setOrder(new Order(new MenuItem("S", 1, 1), 1)));
        expectThrows("markServed when queued rejected", IllegalStateException.class, () -> new RegularCustomer(6).markServed(1));
        Customer s = new RegularCustomer(6);
        s.setState(CustomerState.SEATED);
        s.setOrder(new Order(new MenuItem("S", 1, 1), 1));
        expectThrows("markServed before order SERVED rejected", IllegalStateException.class, () -> s.markServed(1));
    }

    static void queueAndTableChecks() throws Exception {
        WaitingQueue q = new WaitingQueue();
        check("new queue empty, next is null", q.isEmpty() && q.getNextCustomer() == null && q.getQueueSize() == 0);
        Customer c1 = new RegularCustomer(1), c2 = new RegularCustomer(2);
        q.addCustomer(c1);
        q.addCustomer(c2);
        check("FIFO peek does not remove", q.getNextCustomer() == c1 && q.getQueueSize() == 2);
        expectThrows("duplicate add rejected", IllegalStateException.class, () -> q.addCustomer(c1));
        Customer seatedOne = new RegularCustomer(3);
        seatedOne.setState(CustomerState.SEATED);
        expectThrows("non-QUEUED add rejected", IllegalStateException.class, () -> q.addCustomer(seatedOne));
        expectThrows("queue view unmodifiable", UnsupportedOperationException.class, () -> q.getCustomers().remove(0));
        check("removeCustomer true then false", q.removeCustomer(c1) && !q.removeCustomer(c1));
        check("after removal head is c2", q.getNextCustomer() == c2);

        Table t = new Table(1);
        check("new table free", !t.isOccupied() && t.getCurrentCustomer() == null);
        t.assignCustomer(c2);
        check("assign -> occupied", t.isOccupied() && t.getCurrentCustomer() == c2);
        expectInvalid("assign to occupied table rejected", () -> t.assignCustomer(new RegularCustomer(9)));
        t.freeTable();
        check("freeTable -> not occupied", !t.isOccupied());
        expectInvalid("assign non-queued customer rejected", () -> t.assignCustomer(seatedOne));
        check("failed assign leaves table free", !t.isOccupied());
    }

    static void waiterChecks() throws Exception {
        Menu menu = baseMenu();
        WaitingQueue q = new WaitingQueue();
        List<Table> tables = twoTables();
        Waiter w = new Waiter("Wendy");
        Customer c1 = new RegularCustomer(1), c2 = new RegularCustomer(2), c3 = new RegularCustomer(3);
        q.addCustomer(c1); q.addCustomer(c2); q.addCustomer(c3);

        // failed actions leave everything unchanged
        expectInvalid("seat customer not in queue", () -> w.seatCustomer(q, new RegularCustomer(7), tables.get(0)));
        check("failed seat leaves waiter unused", !w.hasActedThisTurn());
        expectInvalid("order for unseated customer", () -> w.takeOrder(c1, menu));
        check("failed order: no order, flag unchanged", c1.getOrder() == null && !w.hasActedThisTurn());
        expectInvalid("serve queued customer", () -> w.serveOrder(c1, 1));

        // any queued customer can be seated, not only the head
        w.seatCustomer(q, c2, tables.get(0));
        check("seat non-head customer works", c2.getState() == CustomerState.SEATED && tables.get(0).getCurrentCustomer() == c2
                && !q.getCustomers().contains(c2) && q.getQueueSize() == 2 && w.hasActedThisTurn());
        expectInvalid("second action same turn rejected", () -> w.seatCustomer(q, c1, tables.get(1)));
        check("rejected action changed nothing", c1.getState() == CustomerState.QUEUED && !tables.get(1).isOccupied() && q.getQueueSize() == 2);
        w.resetForNewTurn();
        check("reset clears flag", !w.hasActedThisTurn());
        expectInvalid("seat at occupied table rejected", () -> w.seatCustomer(q, c1, tables.get(0)));
        check("failed seat keeps c1 queued and flag false", q.getCustomers().contains(c1) && !w.hasActedThisTurn());

        // takeOrder: customer chooses (c2 -> Burger)
        Order o = w.takeOrder(c2, menu);
        check("customer chooses food (id 2 -> Burger)", o.getMenuItem().getName().equals("Burger") && c2.getOrder() == o && o.getBillInCents() == 1500);
        w.resetForNewTurn();
        expectInvalid("duplicate order rejected", () -> w.takeOrder(c2, menu));
        check("duplicate order keeps original", c2.getOrder() == o && !w.hasActedThisTurn());
        expectInvalid("premature serve rejected", () -> w.serveOrder(c2, 2));
        check("premature serve leaves order IN_PROGRESS, customer SEATED, flag false",
                o.getStatus() == OrderStatus.IN_PROGRESS && c2.getState() == CustomerState.SEATED && !w.hasActedThisTurn());
        o.decrementPrepUnit();
        w.serveOrder(c2, 3);
        check("serve ready order", o.getStatus() == OrderStatus.SERVED && c2.getState() == CustomerState.EATING
                && c2.getServedTurn() == 3 && w.hasActedThisTurn());
        w.resetForNewTurn();
        expectInvalid("serve eating customer rejected", () -> w.serveOrder(c2, 4));
    }

    static void chefChecks() throws Exception {
        Chef chef = new Chef("Chuck");
        Order o = new Order(new MenuItem("Pasta", 1800, 2), 1800);
        chef.advanceOrder(o);
        check("advance one unit and mark acted", o.getRemainingPrepUnits() == 1 && chef.hasActedThisTurn());
        expectInvalid("second chef action rejected", () -> chef.advanceOrder(o));
        check("rejected chef action did not cook", o.getRemainingPrepUnits() == 1);
        chef.resetForNewTurn();
        chef.advanceOrder(o);
        check("order READY after 2 units", o.isReady());
        chef.resetForNewTurn();
        expectInvalid("cannot cook a READY order", () -> chef.advanceOrder(o));
        check("failed cook leaves chef unused", !chef.hasActedThisTurn());
        expectInvalid("null order rejected with reason", () -> chef.advanceOrder(null));
    }

    /** Put a customer at the table in EATING with a served order, then make them ready to pay on readyTurn. */
    static Customer readyToPay(int id, Table table, int servedTurn, int readyTurn) throws Exception {
        Customer c = new RegularCustomer(id);
        table.assignCustomer(c);
        c.setState(CustomerState.SEATED);
        Order o = new Order(new MenuItem("Salad", 900, 1), 900);
        c.setOrder(o);
        o.decrementPrepUnit();
        o.markServed();
        c.markServed(servedTurn);
        c.markReadyToPay(readyTurn);
        return c;
    }

    static void cashierChecks() throws Exception {
        Cashier cashier = new Cashier("Cathy");
        List<Table> tables = twoTables();
        check("no customers -> null and not acted", cashier.collectPayment(tables, 5) == null && !cashier.hasActedThisTurn());

        // newly ready (same turn) must not be paid
        Customer a = readyToPay(1, tables.get(0), 3, 4);
        check("customer ready this turn is excluded", cashier.collectPayment(tables, 4) == null && !cashier.hasActedThisTurn()
                && a.getState() == CustomerState.READY_TO_PAY);
        Customer paid = cashier.collectPayment(tables, 5);
        check("paid next turn: customer PAID, order PAID, table freed",
                paid == a && a.getState() == CustomerState.PAID && a.getOrder().getStatus() == OrderStatus.PAID
                        && !tables.get(0).isOccupied() && cashier.hasActedThisTurn());
        check("paid customer keeps order and satisfaction", a.getOrder() != null && a.getSatisfaction() == 100);
        check("second payment same turn -> null", cashier.collectPayment(tables, 6) == null);

        // oldest first
        cashier.resetForNewTurn();
        Customer later = readyToPay(2, tables.get(0), 5, 6);
        Customer earlier = readyToPay(3, tables.get(1), 4, 5);
        check("earliest readyToPayTurn paid first", cashier.collectPayment(tables, 7) == earlier);
        cashier.resetForNewTurn();
        check("then the later one", cashier.collectPayment(tables, 7) == later);

        // tie on timestamp -> lower id
        cashier.resetForNewTurn();
        Customer hi = readyToPay(6, tables.get(0), 4, 5);
        Customer lo = readyToPay(4, tables.get(1), 4, 5);
        check("tie broken by lower customerId", cashier.collectPayment(tables, 6) == lo);
        cashier.resetForNewTurn();
        check("higher id paid after", cashier.collectPayment(tables, 6) == hi);
        cashier.resetForNewTurn();
        check("nothing left -> null", cashier.collectPayment(tables, 9) == null);
    }

    /** Controller-style walk of the plan's Pasta scenario using only domain objects. Satisfaction at payment = 76. */
    static void lifecycleCheck() throws Exception {
        Menu menu = baseMenu();
        WaitingQueue q = new WaitingQueue();
        List<Table> tables = twoTables();
        Waiter waiter = new Waiter("W"); Chef chef = new Chef("C"); Cashier cashier = new Cashier("K");
        Customer c = new RegularCustomer(3);       // customer 3 chooses Pasta (2 units)
        q.addCustomer(c);
        Customer paid = null;
        for (int turn = 1; turn <= 6; turn++) {
            waiter.resetForNewTurn(); chef.resetForNewTurn(); cashier.resetForNewTurn();
            try {
                if (turn == 1) waiter.seatCustomer(q, c, tables.get(0));
                if (turn == 2) waiter.takeOrder(c, menu);
                if (turn == 4) waiter.serveOrder(c, turn);
                if (turn == 2 || turn == 3) chef.advanceOrder(c.getOrder());
            } catch (InvalidActionException e) { check("unexpected failure on turn " + turn + ": " + e.getMessage(), false); }
            if (c.getState() == CustomerState.EATING && c.getServedTurn() < turn) c.markReadyToPay(turn);
            Customer p = cashier.collectPayment(tables, turn);
            if (p != null) paid = p;
            if (c.isWaiting()) c.loseSatisfaction();
            if (turn == 3) check("order READY at end of turn 3", c.getOrder().isReady());
            if (turn == 5) check("READY_TO_PAY on turn 5", c.getState() == CustomerState.READY_TO_PAY);
        }
        check("paid on turn 6", paid == c && c.getState() == CustomerState.PAID);
        check("satisfaction at payment = 76 (waiting at end of turns 1,2,3)", c.getSatisfaction() == 76);
        check("table freed after payment", !tables.get(0).isOccupied());
        check("bill 1800", c.getOrder().getBillInCents() == 1800);
    }
}
