package StockTrading;import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * StockTradingApp
 * ----------------
 * A console-based simulated stock trading environment demonstrating OOP design.
 *
 * Features:
 * - Market data display (simulated real-time prices)
 * - Buy / Sell operations with basic validation
 * - Portfolio tracking (holdings + cash)
 * - Portfolio performance snapshots over time
 * - Simple file persistence (save/load portfolio and trade history)
 *
 * Single-file implementation for learning and easy compilation.
 * Compile: javac StockTradingApp.java
 * Run:     java StockTradingApp
 */
public class StockTradingApp {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}

/* ==========================
   Domain classes
   ========================== */

class Stock implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String ticker;
    private final String name;
    private double price;

    public Stock(String ticker, String name, double initialPrice) {
        this.ticker = ticker.toUpperCase();
        this.name = name;
        this.price = initialPrice;
    }

    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return String.format("%s (%s) - ₹%.2f", name, ticker, price);
    }
}

class Trade implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { BUY, SELL }

    private final Type type;
    private final String ticker;
    private final int quantity;
    private final double pricePerShare;
    private final LocalDateTime timestamp;

    public Trade(Type type, String ticker, int quantity, double pricePerShare) {
        this.type = type;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.timestamp = LocalDateTime.now();
    }

    public Type getType() { return type; }
    public String getTicker() { return ticker; }
    public int getQuantity() { return quantity; }
    public double getPricePerShare() { return pricePerShare; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s %d × %s @ ₹%.2f",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                type, quantity, ticker, pricePerShare);
    }
}

class Holding implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String ticker;
    private int qty;
    private double avgPrice; // weighted average

    public Holding(String ticker, int qty, double avgPrice) {
        this.ticker = ticker.toUpperCase();
        this.qty = qty;
        this.avgPrice = avgPrice;
    }

    public String getTicker() { return ticker; }
    public int getQty() { return qty; }
    public double getAvgPrice() { return avgPrice; }

    public void addShares(int q, double price) {
        double totalCost = this.avgPrice * this.qty + price * q;
        this.qty += q;
        if (this.qty > 0) this.avgPrice = totalCost / this.qty;
        else this.avgPrice = 0;
    }

    public void removeShares(int q) {
        if (q > qty) throw new IllegalArgumentException("Not enough shares to remove");
        qty -= q;
        if (qty == 0) avgPrice = 0;
    }

    @Override
    public String toString() {
        return String.format("%s : %d shares @ avg ₹%.2f", ticker, qty, avgPrice);
    }
}

/* ==========================
   Portfolio + Persistence
   ========================== */

class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    private double cash;
    private final Map<String, Holding> holdings = new HashMap<>();
    private final List<Trade> trades = new ArrayList<>();

    // snapshots: timestamp -> totalValue
    private final List<PortfolioSnapshot> snapshots = new ArrayList<>();

    public Portfolio(double startingCash) {
        this.cash = startingCash;
        takeSnapshot();
    }

    public double getCash() { return cash; }
    public Map<String, Holding> getHoldings() { return holdings; }
    public List<Trade> getTrades() { return trades; }
    public List<PortfolioSnapshot> getSnapshots() { return snapshots; }

    public double totalMarketValue(Market market) {
        double total = cash;
        for (Holding h : holdings.values()) {
            Stock s = market.getStock(h.getTicker());
            double price = (s != null) ? s.getPrice() : 0.0;
            total += price * h.getQty();
        }
        return total;
    }

    public void buy(Market market, String ticker, int qty) throws IllegalArgumentException {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Stock s = market.getStock(ticker);
        if (s == null) throw new IllegalArgumentException("Unknown ticker: " + ticker);
        double cost = s.getPrice() * qty;
        if (cost > cash) throw new IllegalArgumentException("Insufficient cash: need ₹" + cost + " but have ₹" + cash);

        cash -= cost;
        holdings.compute(ticker.toUpperCase(), (t, h) -> {
            if (h == null) return new Holding(ticker, qty, s.getPrice());
            h.addShares(qty, s.getPrice());
            return h;
        });
        trades.add(new Trade(Trade.Type.BUY, ticker, qty, s.getPrice()));
        takeSnapshot();
    }

    public void sell(Market market, String ticker, int qty) throws IllegalArgumentException {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Holding h = holdings.get(ticker.toUpperCase());
        if (h == null || h.getQty() < qty) throw new IllegalArgumentException("Not enough shares to sell");
        Stock s = market.getStock(ticker);
        if (s == null) throw new IllegalArgumentException("Unknown ticker: " + ticker);

        double proceeds = s.getPrice() * qty;
        h.removeShares(qty);
        if (h.getQty() == 0) holdings.remove(ticker.toUpperCase());
        cash += proceeds;
        trades.add(new Trade(Trade.Type.SELL, ticker, qty, s.getPrice()));
        takeSnapshot();
    }

    public void takeSnapshot() {
        snapshots.add(new PortfolioSnapshot(LocalDateTime.now(), cash, new HashMap<>(holdings)));
    }

    public void saveToFile(String path) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        }
    }

    public static Portfolio loadFromFile(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof Portfolio) return (Portfolio) obj;
            else throw new IOException("File does not contain a Portfolio object");
        }
    }
}

class PortfolioSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDateTime time;
    private final double cash;
    private final Map<String, Holding> holdings;

    public PortfolioSnapshot(LocalDateTime time, double cash, Map<String, Holding> holdings) {
        this.time = time;
        this.cash = cash;
        this.holdings = holdings;
    }

    public LocalDateTime getTime() { return time; }
    public double getCash() { return cash; }
    public Map<String, Holding> getHoldings() { return holdings; }
}

/* ==========================
   Market simulator
   ========================== */

class Market {
    private final Map<String, Stock> stocks = new HashMap<>();
    private final Random rng = new Random();

    public Market() {
        // seed with some example stocks
        addStock(new Stock("TCS", "Tata Consultancy Services", 3500.00));
        addStock(new Stock("INFY", "Infosys", 1450.00));
        addStock(new Stock("RELI", "Reliance Industries", 2450.00));
        addStock(new Stock("HDFC", "HDFC Bank", 1700.00));
        addStock(new Stock("LT", "Larsen & Toubro", 2200.00));
    }

    public void addStock(Stock s) { stocks.put(s.getTicker(), s); }
    public Stock getStock(String ticker) { return stocks.get(ticker.toUpperCase()); }
    public Collection<Stock> allStocks() { return stocks.values(); }

    /**
     * Randomly move prices to simulate market.
     * percentageRange e.g. 0.03 means ±3% drift per tick
     */
    public void tick(double percentageRange) {
        for (Stock s : stocks.values()) {
            double changePct = (rng.nextDouble() * 2 - 1) * percentageRange;
            double newPrice = Math.max(1.0, s.getPrice() * (1 + changePct));
            s.setPrice(Math.round(newPrice * 100.0) / 100.0);
        }
    }
}

/* ==========================
   Console UI
   ========================== */

class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final Market market = new Market();
    private Portfolio portfolio;
    private final String SAVE_FILE = "portfolio.dat";

    public ConsoleUI() {
        // Try to load saved portfolio, otherwise create a new one
        try {
            portfolio = Portfolio.loadFromFile(SAVE_FILE);
            System.out.println("Loaded saved portfolio from " + SAVE_FILE);
        } catch (Exception e) {
            portfolio = new Portfolio(100000.00); // starting cash ₹100,000
            System.out.println("Starting new portfolio with ₹100,000.00 cash.");
        }
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String cmd = scanner.nextLine().trim().toLowerCase();
            switch (cmd) {
                case "1": case "market": showMarket(); break;
                case "2": case "buy": handleBuy(); break;
                case "3": case "sell": handleSell(); break;
                case "4": case "portfolio": showPortfolio(); break;
                case "5": case "history": showTrades(); break;
                case "6": case "snapshots": showSnapshots(); break;
                case "7": case "tick": manualTick(); break;
                case "8": case "save": save(); break;
                case "q": case "quit": running = false; saveOnExit(); break;
                default: System.out.println("Unknown command. Type the number or keyword (e.g. 'buy').");
            }
        }
        System.out.println("Goodbye — portfolio saved.");
    }

    private void printMainMenu() {
        System.out.println("\n=== Simple Stock Trading Simulator ===");
        System.out.println("1) Market data");
        System.out.println("2) Buy stock");
        System.out.println("3) Sell stock");
        System.out.println("4) View portfolio");
        System.out.println("5) Trade history");
        System.out.println("6) Portfolio snapshots (performance over time)");
        System.out.println("7) Advance market tick (simulate price change)");
        System.out.println("8) Save portfolio to disk");
        System.out.println("Q) Quit");
        System.out.print("Enter choice: ");
    }

    private void showMarket() {
        System.out.println("\n--- Market Data ---");
        System.out.printf("%-8s %-30s %10s\n", "Ticker", "Name", "Price (₹)");
        System.out.println("-----------------------------------------------------------");
        for (Stock s : market.allStocks()) {
            System.out.printf("%-8s %-30s %10.2f\n", s.getTicker(), s.getName(), s.getPrice());
        }
    }

    private void handleBuy() {
        try {
            System.out.print("Enter ticker to BUY: ");
            String ticker = scanner.nextLine().trim().toUpperCase();
            Stock s = market.getStock(ticker);
            if (s == null) { System.out.println("Unknown ticker."); return; }
            System.out.println(s);
            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            portfolio.buy(market, ticker, qty);
            System.out.println("Bought " + qty + " shares of " + ticker + " at ₹" + s.getPrice());
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException iae) {
            System.out.println("Trade failed: " + iae.getMessage());
        }
    }

    private void handleSell() {
        try {
            System.out.print("Enter ticker to SELL: ");
            String ticker = scanner.nextLine().trim().toUpperCase();
            Holding h = portfolio.getHoldings().get(ticker);
            if (h == null) { System.out.println("You have no holdings of " + ticker); return; }
            System.out.println(h + " current market price: " + market.getStock(ticker).getPrice());
            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            portfolio.sell(market, ticker, qty);
            System.out.println("Sold " + qty + " shares of " + ticker);
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException iae) {
            System.out.println("Trade failed: " + iae.getMessage());
        }
    }

    private void showPortfolio() {
        System.out.println("\n--- Portfolio ---");
        System.out.printf("Cash: ₹%.2f\n", portfolio.getCash());
        System.out.println("Holdings:");
        if (portfolio.getHoldings().isEmpty()) System.out.println("  (none)");
        else {
            System.out.printf("%-8s %8s %12s %12s\n", "Ticker", "Qty", "Avg Price", "Market Value");
            for (Holding h : portfolio.getHoldings().values()) {
                Stock s = market.getStock(h.getTicker());
                double mval = (s != null) ? s.getPrice() * h.getQty() : 0.0;
                System.out.printf("%-8s %8d %12.2f %12.2f\n", h.getTicker(), h.getQty(), h.getAvgPrice(), mval);
            }
        }
        System.out.printf("Total portfolio value: ₹%.2f\n", portfolio.totalMarketValue(market));
    }

    private void showTrades() {
        System.out.println("\n--- Trade History ---");
        if (portfolio.getTrades().isEmpty()) System.out.println("(no trades yet)");
        else portfolio.getTrades().forEach(t -> System.out.println("  " + t));
    }

    private void showSnapshots() {
        System.out.println("\n--- Portfolio Snapshots (time -> total value) ---");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int i = 0;
        for (PortfolioSnapshot snap : portfolio.getSnapshots()) {
            double total = snap.getCash();
            for (Holding h : snap.getHoldings().values()) {
                Stock s = market.getStock(h.getTicker());
                double price = (s != null) ? s.getPrice() : 0.0;
                total += price * h.getQty();
            }
            System.out.printf("%2d) %s -> ₹%.2f\n", ++i, snap.getTime().format(f), total);
        }
        System.out.println("(Snapshots are taken automatically after each trade and at startup)");
    }

    private void manualTick() {
        System.out.print("Advance market by one tick? Enter volatility pct (e.g. 0.03 for ±3%): ");
        try {
            double pct = Double.parseDouble(scanner.nextLine().trim());
            if (pct < 0 || pct > 1) { System.out.println("Enter a sensible percentage (0 - 1)"); return; }
            market.tick(pct);
            System.out.println("Market advanced. Use 'market' to view prices.");
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number entered.");
        }
    }

    private void save() {
        try {
            portfolio.saveToFile(SAVE_FILE);
            System.out.println("Saved portfolio to " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void saveOnExit() {
        try { portfolio.saveToFile(SAVE_FILE); }
        catch (IOException e) { System.out.println("Warning: failed to save on exit: " + e.getMessage()); }
    }
}
