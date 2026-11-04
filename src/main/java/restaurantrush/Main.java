package restaurantrush;

import java.util.List;
import java.util.Scanner;

public final class Main {
    private Main() { }

    public static GameController createGame() {
        Menu menu = new Menu();
        menu.addItem(new MenuItem("Salad", 900, 1));
        menu.addItem(new MenuItem("Burger", 1500, 1));
        menu.addItem(new MenuItem("Pasta", 1800, 2));
        menu.addItem(new ComboMeal("Salad + Pasta Combo", menu.findItemByName("Salad"), menu.findItemByName("Pasta")));
        return new GameController(List.of(new Table(1), new Table(2)), new WaitingQueue(),
                new Waiter("Waiter"), new Chef("Chef"), new Cashier("Cashier"),
                menu, ArrivalSchedule.createBaseSchedule(), new Host("Host"), new PolicyManager());
    }

    public static void main(String[] args) {
        new GameCLI(createGame(), new Scanner(System.in)).run();
    }
}
