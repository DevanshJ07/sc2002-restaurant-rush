package restaurantrush;

import java.util.List;

/** Stage 3 checks: Host, ComboMeal, PolicyManager, Customer.recoverSatisfaction, Order.applyDiscount. */
public class Stage3Checks {
    static Menu comboMenu() {
        Menu m = DomainChecks.baseMenu();
        m.addItem(new ComboMeal("Salad + Pasta Combo", m.findItemByName("Salad"), m.findItemByName("Pasta")));
        return m;
    }

    public static void main(String[] args) throws Exception {
        comboChecks();
        discountChecks();
        policyChecks();
        recoveryChecks();
        hostChecks();
        System.out.println("Stage 3 checks done.");
        DomainChecks.summaryAndExit();
    }

    static void comboChecks() {
        Menu m = comboMenu();
        MenuItem combo = m.getItems().get(3);
        DomainChecks.check("combo price 2430 cents, 3 units", combo.getPriceInCents() == 2430 && combo.getPreparationUnits() == 3);
        DomainChecks.check("combo is a MenuItem with components", combo instanceof ComboMeal
                && ((ComboMeal) combo).getComponentA().getName().equals("Salad")
                && ((ComboMeal) combo).getComponentB().getName().equals("Pasta"));
        MenuItem odd = new ComboMeal("Odd", new MenuItem("A", 1005, 1), new MenuItem("B", 0, 1));
        DomainChecks.check("combo rounds half up: 1005 * 0.9 = 904.5 -> 905", odd.getPriceInCents() == 905);
        DomainChecks.expectThrows("combo with null component rejected", IllegalArgumentException.class,
                () -> new ComboMeal("X", null, new MenuItem("B", 1, 1)));
        DomainChecks.check("Stage 3 menu: customer 4 (index rule) orders the combo", new RegularCustomer(4).chooseItem(m) == combo);
        DomainChecks.check("Stage 3 menu: Critic picks the combo as most expensive", new CriticCustomer(4).chooseItem(m) == combo);
        DomainChecks.check("customers 1-3 unchanged (Salad, Burger, Pasta)",
                new RegularCustomer(1).chooseItem(m).getName().equals("Salad")
                        && new RegularCustomer(2).chooseItem(m).getName().equals("Burger")
                        && new RegularCustomer(3).chooseItem(m).getName().equals("Pasta"));
        DomainChecks.check("customers 5,6 wrap to Salad, Burger", new RegularCustomer(5).chooseItem(m).getName().equals("Salad")
                && new RegularCustomer(6).chooseItem(m).getName().equals("Burger"));
    }

    static void discountChecks() throws Exception {
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        Order o = new Order(burger, 1350);
        o.applyDiscount(20);
        DomainChecks.check("VIP Burger 1350 + Happy Hour 20% = 1080", o.getBillInCents() == 1080);
        Order half = new Order(burger, 1005);
        half.applyDiscount(10);
        DomainChecks.check("half-cent rounds up (1005 -10% -> 905)", half.getBillInCents() == 905);
        Order up = new Order(burger, 1301);
        up.applyDiscount(10);
        DomainChecks.check("1301 -10% = 1170.9 -> 1171", up.getBillInCents() == 1171);
        Order full = new Order(burger, 1500);
        full.applyDiscount(0);
        DomainChecks.check("0% leaves bill alone", full.getBillInCents() == 1500);
        DomainChecks.expectInvalid("percent above 100 rejected", () -> new Order(burger, 100).applyDiscount(101));
        DomainChecks.expectInvalid("negative percent rejected", () -> new Order(burger, 100).applyDiscount(-1));
        Order ready = new Order(burger, 1500);
        ready.decrementPrepUnit();
        DomainChecks.expectInvalid("cannot reprice a READY order", () -> ready.applyDiscount(20));
        DomainChecks.check("failed discount left bill unchanged", ready.getBillInCents() == 1500);
        // VIP first, then Happy Hour, each rounded
        Customer vip = new VIPCustomer(2);
        Order vo = new Order(burger, vip.calculateBill(1500));
        vo.applyDiscount(20);
        DomainChecks.check("VIP then Happy Hour through objects = 1080", vo.getBillInCents() == 1080);
    }

    static void policyChecks() throws Exception {
        PolicyManager p = new PolicyManager();
        DomainChecks.check("initially inactive, 0 turns", !p.isHappyHourActive() && p.getRemainingTurns() == 0);
        DomainChecks.check("recovery is 15", p.getSatisfactionRecovery() == 15);
        Order before = new Order(new MenuItem("Burger", 1500, 1), 1500);
        p.applyDiscount(before);
        DomainChecks.check("inactive: applyDiscount does nothing", before.getBillInCents() == 1500);
        p.activateHappyHour();
        DomainChecks.check("activate -> active, 2 turns", p.isHappyHourActive() && p.getRemainingTurns() == 2);
        Order during = new Order(new MenuItem("Burger", 1500, 1), 1500);
        p.applyDiscount(during);
        DomainChecks.check("active: new order gets 20% off", during.getBillInCents() == 1200);
        DomainChecks.check("earlier order keeps locked bill", before.getBillInCents() == 1500);
        DomainChecks.expectInvalid("reactivation while active rejected", p::activateHappyHour);
        DomainChecks.check("failed reactivation did not change timer", p.getRemainingTurns() == 2);
        p.decrementHappyHourTimer();
        DomainChecks.check("2 -> 1, still active", p.getRemainingTurns() == 1 && p.isHappyHourActive());
        p.decrementHappyHourTimer();
        DomainChecks.check("1 -> 0, inactive", p.getRemainingTurns() == 0 && !p.isHappyHourActive());
        p.decrementHappyHourTimer();
        DomainChecks.check("timer never below 0", p.getRemainingTurns() == 0);
        DomainChecks.expectInvalid("reactivation after expiry rejected", p::activateHappyHour);
        Order after = new Order(new MenuItem("Burger", 1500, 1), 1500);
        p.applyDiscount(after);
        DomainChecks.check("after expiry no discount", after.getBillInCents() == 1500);
    }

    static void recoveryChecks() {
        Customer c = new RegularCustomer(1);
        c.loseSatisfaction();
        c.loseSatisfaction();
        c.recoverSatisfaction(15);
        DomainChecks.check("84 + 15 = 99", c.getSatisfaction() == 99);
        c.recoverSatisfaction(15);
        DomainChecks.check("cap at 100", c.getSatisfaction() == 100);
        DomainChecks.expectThrows("negative recovery rejected", IllegalArgumentException.class, () -> c.recoverSatisfaction(-1));
        Customer low = new CriticCustomer(4);
        for (int i = 0; i < 9; i++) low.loseSatisfaction();
        low.recoverSatisfaction(15);
        DomainChecks.check("recovery from 0 -> 15", low.getSatisfaction() == 15);
    }

    static void hostChecks() throws Exception {
        WaitingQueue q = new WaitingQueue();
        List<Table> tables = DomainChecks.twoTables();
        Host host = new Host("Hannah");
        Waiter waiter = new Waiter("Wendy");
        DomainChecks.check("empty queue -> null, not acted", host.seatNextCustomer(q, tables) == null && !host.hasActedThisTurn());
        Customer c1 = new RegularCustomer(1), c2 = new RegularCustomer(2), c3 = new RegularCustomer(3);
        q.addCustomer(c1); q.addCustomer(c2); q.addCustomer(c3);
        Customer seated = host.seatNextCustomer(q, tables);
        DomainChecks.check("Host seats queue head at lowest-ID table",
                seated == c1 && tables.get(0).getCurrentCustomer() == c1 && c1.getState() == CustomerState.SEATED
                        && q.getQueueSize() == 2 && host.hasActedThisTurn());
        DomainChecks.check("Host seats at most one per turn", host.seatNextCustomer(q, tables) == null && q.getQueueSize() == 2);
        DomainChecks.check("Host did not use the Waiter's action", !waiter.hasActedThisTurn());
        waiter.seatCustomer(q, c3, tables.get(1));
        DomainChecks.check("Waiter independently seats another customer", c3.getState() == CustomerState.SEATED && waiter.hasActedThisTurn());
        host.resetForNewTurn();
        DomainChecks.check("no free table -> null, Host stays unused", host.seatNextCustomer(q, tables) == null && !host.hasActedThisTurn()
                && q.getNextCustomer() == c2);
        // lowest-ID free table when table 1 is busy and table 2 is free
        WaitingQueue q2 = new WaitingQueue();
        List<Table> t2 = DomainChecks.twoTables();
        Customer busy = new RegularCustomer(5);
        t2.get(0).assignCustomer(busy);
        Customer next = new RegularCustomer(6);
        q2.addCustomer(next);
        Host h2 = new Host("H2");
        h2.seatNextCustomer(q2, t2);
        DomainChecks.check("seats at the free table when table 1 is busy", t2.get(1).getCurrentCustomer() == next);
    }
}
