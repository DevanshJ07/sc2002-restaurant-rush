package restaurantrush;

import java.util.List;

/** Stage 2 checks: VIPCustomer, CriticCustomer, Menu.getMostExpensiveItem. */
public class Stage2Checks {
    public static void main(String[] args) throws Exception {
        Menu menu = DomainChecks.baseMenu();

        // VIP
        Customer vip = new VIPCustomer(2);
        DomainChecks.check("VIP label and loss rate 5", vip.getTypeLabel().equals("VIP") && vip.getSatisfactionLossRate() == 5);
        DomainChecks.check("VIP Burger bill = 1350", vip.calculateBill(1500) == 1350);
        DomainChecks.check("VIP Salad = 810, Pasta = 1620", vip.calculateBill(900) == 810 && vip.calculateBill(1800) == 1620);
        DomainChecks.check("VIP half-cent rounds UP (1305 -> 1175)", vip.calculateBill(1305) == 1175);
        DomainChecks.check("VIP below half rounds down (1301 -> 1171)", vip.calculateBill(1301) == 1171);
        vip.loseSatisfaction();
        DomainChecks.check("VIP loses 5 per turn", vip.getSatisfaction() == 95);
        DomainChecks.check("VIP uses same food rule (id 2 -> Burger)", vip.chooseItem(menu).getName().equals("Burger"));

        // Critic
        Customer critic = new CriticCustomer(4);
        DomainChecks.check("Critic label and loss rate 12", critic.getTypeLabel().equals("Critic") && critic.getSatisfactionLossRate() == 12);
        DomainChecks.check("Critic picks Pasta on base menu (not index rule, which gives Salad)", critic.chooseItem(menu).getName().equals("Pasta"));
        DomainChecks.check("Critic pays full price", critic.calculateBill(1800) == 1800);
        for (int i = 0; i < 9; i++) critic.loseSatisfaction();
        DomainChecks.check("Critic clamps at 0 (9 x 12 > 100)", critic.getSatisfaction() == 0);

        // Menu.getMostExpensiveItem
        DomainChecks.check("most expensive on base menu = Pasta", menu.getMostExpensiveItem().getName().equals("Pasta"));
        Menu tie = new Menu();
        tie.addItem(new MenuItem("A", 1000, 1));
        tie.addItem(new MenuItem("B", 1000, 1));
        DomainChecks.check("first item wins a price tie", tie.getMostExpensiveItem().getName().equals("A"));
        DomainChecks.expectThrows("empty menu rejected", IllegalArgumentException.class, () -> new Menu().getMostExpensiveItem());

        // polymorphism through the Waiter, with no type checks
        WaitingQueue q = new WaitingQueue();
        List<Table> tables = DomainChecks.twoTables();
        Waiter w = new Waiter("W");
        q.addCustomer(vip);
        q.addCustomer(critic);
        w.seatCustomer(q, vip, tables.get(0));
        w.resetForNewTurn();
        Order vo = w.takeOrder(vip, menu);
        DomainChecks.check("Waiter order for VIP: Burger locked at 1350", vo.getMenuItem().getName().equals("Burger") && vo.getBillInCents() == 1350);
        w.resetForNewTurn();
        w.seatCustomer(q, critic, tables.get(1));
        w.resetForNewTurn();
        Order co = w.takeOrder(critic, menu);
        DomainChecks.check("Waiter order for Critic: Pasta 1800, 2 units", co.getMenuItem().getName().equals("Pasta") && co.getBillInCents() == 1800 && co.getRemainingPrepUnits() == 2);

        // all types share one queue
        WaitingQueue mixed = new WaitingQueue();
        mixed.addCustomer(new RegularCustomer(1));
        mixed.addCustomer(new VIPCustomer(2));
        mixed.addCustomer(new CriticCustomer(3));
        DomainChecks.check("mixed customer types share one queue", mixed.getQueueSize() == 3);

        System.out.println("Stage 2 checks done. ");
        DomainChecks.summaryAndExit();
    }
}
