package restaurantrush;

public final class Stage2IntegrationChecks {
    public static void main(String[] args) throws Exception {
        ArrivalSchedule schedule = ArrivalSchedule.createBaseSchedule();
        int count = 0;
        for (int turn = 1; turn <= 18; turn++) {
            Customer c = schedule.getArrivalFor(turn);
            if (c != null) { count++; }
            if (turn == 4) {
                IntegrationChecks.check(c != null && c.getTypeLabel().equals("VIP") && c.getCustomerId() == 2,
                        "VIP registration");
            }
            if (turn == 10) {
                IntegrationChecks.check(c != null && c.getTypeLabel().equals("Critic") && c.getCustomerId() == 4,
                        "Critic registration");
            }
        }
        IntegrationChecks.check(count == 6, "Six arrivals retained");
        Menu menu = new Menu(); menu.addItem(new MenuItem("Salad", 900, 1));
        menu.addItem(new MenuItem("Burger", 1500, 1)); menu.addItem(new MenuItem("Pasta", 1800, 2));
        Customer vip = new VIPCustomer(2);
        GameController g = IntegrationChecks.custom(menu, vip);
        g.beginTurn(); g.waiterSeat(2, 1); IntegrationChecks.end(g);
        g.beginTurn(); g.waiterTakeOrder(1);
        IntegrationChecks.check(vip.getOrder().getBillInCents() == 1350, "Controller uses polymorphic VIP bill");
        IntegrationChecks.check(vip.getSatisfaction() == 95, "VIP loss through controller");
        g.finishWaiterPhase(); g.chefAdvance(1); g.finishTurn();
        Customer critic = new CriticCustomer(1); g = IntegrationChecks.custom(menu, critic);
        g.beginTurn(); g.waiterSeat(1, 1); IntegrationChecks.end(g);
        g.beginTurn(); g.waiterTakeOrder(1);
        IntegrationChecks.check(critic.getOrder().getMenuItem().getName().equals("Pasta"), "Critic chooses most expensive");
        IntegrationChecks.check(critic.getSatisfaction() == 88, "Critic loss through controller");
        System.out.println("Stage 2 integration checks passed");
    }
}
