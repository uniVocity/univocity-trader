Welcome to univocity-trader
============================

univocity-trader is an open-source trading framework built to enable anyone with basic programming skills to efficiently
create and test trading algorithms for buying/selling stocks, cryptocurrencies or any other type of instrument.

With this framework you can pull historical and live trade details from any exchange or trading platform that provides a public API, test your
algorithm against historical data, optimize your algorithm, and run a robot to trade automatically for you using one or more accounts at the same time.

Differently from most other platforms, this framework lets you create strategies that look into different chart intervals at the same time, 
You can combine the signals of multiple strategies that work at multiple intervals to generate a "final" signal, 
or even mix indicators at different times, e.g. something like a moving average crossover where one moving average works with 33 minute candles and the other with 5 minute candles.

This framework was built for speed and minimal memory consumption (unless necessary). 
You should be able to run backtests on a million candlesticks using 500mb of RAM or even less.

## BEWARE

 * This is a personal project that is still under development. It's been refactored from some quick and dirty solution I built for myself
 and there are things to test and do to make it more convenient and general purpose.  
 
 * Trading is inherently risky and you can lose your money rather quickly in case of errors. Use this software at your own risk.
You are solely responsible for any financial loss incurred from using this software.   

# Project structure

The meat-and-bones of this framework is inside the [univocity-trader-core](./univocity-trader-core) project folder, it defines the basic interfaces
used to implement your strategies, support for backtesting, live trading, and integration with live exchanges.

Support for any exchange relies in implementing two interfaces:

 * [Exchange](./univocity-trader-core/src/main/java/com/univocity/trader/Exchange.java) 
 for everything that's available to the public in general, i.e. latest price of a symbol, available symbols, etc.
 
 * [ClientAccount](./univocity-trader-core/src/main/java/com/univocity/trader/ClientAccount.java)
 for account-specific operations: create an order, update account balance, etc. 

Everything else is handled by the framework.

Right now we provide an implementation that supports [Binance](https://www.binance.com/en/register?ref=36767892) for trading cryptocurrencies
in class [BinanceExchange](./univocity-trader-binance/src/main/java/com/univocity/trader/exchange/binance/BinanceExchange.java).
As you can see it's not a lot of work especially if the exchange already provides a Java library for their API.
 
The full integration code with Binance is in the [univocity-trader-binance](./univocity-trader-binance) project folder, and you can
quickly adapt the sample code under [univocity-trader-binance-example](./univocity-trader-binance-example) to start trading live.

The following guide will show you how to get started using [Binance](https://www.binance.com/en/register?ref=36767892) to populate then
update a database of historical data and start trading.

## Setting up

Make sure you have the following installed:

 * Java 11
 
 * MySQL or MariaDB (you can change this to something else, just adapt the database setup instructions below to your needs)
 
 * Apache maven (if your IDE doesn't come with it) 

With that ready you can clone this repository and open it in your favorite IDE, with all sub-projects.

### Database setup

Create a database in `MySQL` to store candles (and statistics generated from your backtests):

```sql
CREATE DATABASE trading;

USE trading;
```

Then create the `candle` table as defined in this [script](./univocity-trader-core/src/main/resources/db/mysql/candle.sql).

Let's quickly look at the [MarketHistoryLoader](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/MarketHistoryLoader.java)
class, which will connect to [Binance](https://www.binance.com/en/register?ref=36767892) and pull the market history of Bitcoin of the last 6 months in 1 minute candles.

```java
public static void main(String... args) {
 
 //TODO: configure your database connection here.
 SingleConnectionDataSource ds = new SingleConnectionDataSource();
 ... 
 
 //CandleRepository manages everything for us.
 CandleRepository.setDataSource(ds);
 
 //Instantiate the exchange API implementation you need
 BinanceExchange exchange = new BinanceExchange();
 
 //Gets all candes from the past 6 months
 final Instant start = LocalDate.now().minus(6, ChronoUnit.MONTHS).atStartOfDay().toInstant(ZoneOffset.UTC);
 
 //Only pair BTCUSDT is enabled in ALL_PAIRS, modify that to your liking
 for (String[] pair : ALL_PAIRS) {
  String symbol = pair[0] + pair[1];
  
  //Runs over stored candles backwards and tries to fill any gaps until the start date is reached.
  CandleRepository.fillHistoryGaps(exchange, symbol, start, TimeInterval.minutes(1)); // pulls one minute candles
 }
}

```

Once the database connection is configured with your particular details, you can execute the `main` method. The 
logs should print something like this:

```
[main] INFO  (CandleRepository.java:268) - Looking for gaps in history of BTCUSDT from 2019 May 13 09:30
[BTCUSDT candle reader] DEBUG (CandleRepository.java:153) - Executing SQL query: [SELECT open_time, close_time, open, high, low, close, volume FROM candle WHERE symbol = 'BTCUSDT' AND open_time >= 1557705600000 AND close_time <= 1573633279655 ORDER BY open_time]
[main] WARN  (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 264819 minutes between 2019 May 13 09:30 and 2019 Nov. 13 08:09
[main] WARN  (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 141 minutes between 2019 Nov. 13 12:29 and 2019 Nov. 13 14:50
[main] WARN  (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 4 minutes between 2019 Nov. 13 15:59 and 2019 Nov. 13 16:03
[main] INFO  (CandleRepository.java:319) - Filling 267 gaps in history of BTCUSDT
```

This will take a long while. To help you and to avoid abusing Binance's servers, 
the historical data I collected for all symbols listed in `ALL_PAIRS` are available [here](https://drive.google.com/file/d/13jzICy3rCSCUUwiIaum88-ldA_gAxlFm/view?usp=sharing).
 You can download this file, unpack it (2.2 GB of disk space required) then restore the backup with:

```
mysql -u <user> -p trading < candle.sql
```   

> NOTE: if you get "too many open files" errors later on, restart mysql after restoring the database.

After the backup is restored, the  [MarketHistoryLoader](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/MarketHistoryLoader.java)
should finish rather quickly. Re-execute this class whenever you want to update your local database
with the latest data from Binance.

Let's finally get started with the coding bit:

## Creating a Strategy

The [Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java) interface
is the center piece of this framework as its implementation must return a 
[Signal](./univocity-trader-core/src/main/java/com/univocity/trader/indicators/Signal.java)
for every [Candle](./univocity-trader-core/src/main/java/com/univocity/trader/candles/Candle.java) that is received.

Yet, it is simply defined as:

```java
public interface Strategy {
 Signal getSignal(Candle candle);
}
```

During simulations or live trading, every single candle received will be sent to your  
[Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java)'s `getSignal` method. 
Once your [Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java) returns
`BUY`, an [Order](./univocity-trader-core/src/main/java/com/univocity/trader/account/Order.java) will be made to buy the instrument at the given price.
If `SELL` is returned, any open [Order](./univocity-trader-core/src/main/java/com/univocity/trader/account/Order.java) will be sold at the current price.
Anything else will be ignored.

> NOTE: at this stage shorting is not supported. This will be added soon.  

You can use the [Indicator](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Indicator.java) implementations
provided in the [com.univocity.trader.indicators](./univocity-trader-core/src/main/java/com/univocity/trader/indicators) package to
use technical indicators to compose a trading strategy. Any strategy built with them should be a subclass 
of [IndicatorStrategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/IndicatorStrategy.java), which has the following
contract:

```java
public class ExampleStrategy extends IndicatorStrategy {
 protected Set<Indicator> getAllIndicators(){
  //return all indicators used by the strategy
 }
   
 public Signal getSignal(Candle candle){
  //produce a Signal
 }
}
```

Let's create the [ExampleStrategy](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/ExampleStrategy.java) 
to combine two [BollingerBands](./univocity-trader-core/src/main/java/com/univocity/trader/indicators/BollingerBand.java) in two different time frames:

```java
public class ExampleStrategy extends IndicatorStrategy {
 
 private final Set<Indicator> indicators = new HashSet<>();
 
 private final BollingerBand boll5m;
 private final BollingerBand boll1h;
 
 public ExampleStrategy() {
  indicators.add(boll5m = new BollingerBand(TimeInterval.minutes(5)));
  indicators.add(boll1h = new BollingerBand(TimeInterval.hours(1)));
 }
 
 @Override
 protected Set<Indicator> getAllIndicators() {
  return indicators;
 }
 
 @Override
 public Signal getSignal(Candle candle) {   
  //price jumped below lower band on the 1 hour time frame
  if (candle.high < boll1h.getLowerBand()) {    
   //on the 5 minute time frame, the lowest price of the candle is above the lower band.
   if (candle.low > boll5m.getLowerBand()) {           
    //still on the 5 minute time frame, the close price of the candle is under the middle band
    if (candle.close < boll5m.getMiddleBand()) {
     // if the slope of the 5 minute bollinger band is starting to point up, BUY 
     if (boll5m.movingUp()) { 
      return Signal.BUY;
     }
    }
   }
  }
                                             
  //candle hitting the upper band on the 1 hour time frame
  if (candle.high > boll1h.getUpperBand()) {  
   //on the 5 minute time frame, the lowest price of the candle is under the middle band
   if (candle.low < boll5m.getMiddleBand()) {
    //if the slope of the 5 minute bollinger band is starting to point down, SELL 
    if (boll5m.movingDown()) {
     return Signal.SELL;
    }
   }
  }
  return Signal.NEUTRAL;
 }
}

``` 

Very simple, but is it effective? Time to run a simulation to find out. 

## Using the MarketSimulator

The [MarketSimulator](./univocity-trader-core/src/main/java/com/univocity/trader/simulation/MarketSimulator.java) class
loads the data from each symbol in your database and executes them in the same order you'd receive from an exchange's live stream.

Create new instance with:

```java
//USDT here is the reference currency. Your account balance will be calculated using this symbol.
MarketSimulator simulation = new MarketSimulator("USDT");

//Trade with the following symbols:
simulation.tradeWith("BTC", "ADA", "LTC", "XRP", "ETH");
```

Each one of the symbols given in `tradeWith` will be paired against the reference currency, i.e. the 
[MarketSimulator](./univocity-trader-core/src/main/java/com/univocity/trader/simulation/MarketSimulator.java)
will trade pairs BTCUSDT, ADAUSDT, LTCUSDT, XRPUSDT and ETHUSDT.

You can also define additional trading pairs such as ADABTC by adding the line:

```java
simulation.tradeWithPair("ADA", "BTC");
```

Add our [ExampleStrategy](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/ExampleStrategy.java)
to the simulation. Note that the framework **forces** you to provide functions/suppliers that create new instances
as required.

```java
simulation.strategies().add(ExampleStrategy::new);
```

Set the initial funds of your account with:

```java
simulation.account().setAmount("USDT", 1000.0);
```

NEVER forget trading fees. Each buy/sell on an exchange will typically cost you a fraction
of the position at stake. Here we use what [Binance](https://www.binance.com/en/register?ref=36767892) charges
new accounts: **0.1%** for any type of trade. 

```java
simulation.setTradingFees(SimpleTradingFees.percentage(0.1));
```

If you want to see the action, log trades made by the simulator with:

```java
simulation.listeners().add(new OrderExecutionToLog())
```

Finally define the time period of the simulation:
```java
simulation.setSimulationStart(LocalDate.of(2018, 7, 1).atStartOfDay());
simulation.setSimulationEnd(LocalDate.of(2019, 7, 1).atStartOfDay());
```

Here we test with data from July 2018 to July 2019.

You can finally execute to see what happens as the [OrderExecutionToLog](./univocity-trader-core/src/main/java/com/univocity/trader/notification/OrderExecutionToLog.java) will 
print each trade made to the log.

The last line will show something like this:

```

- 2019 May 09 15:50 XRPUSDT BUY      
0.29808 @ $0.29808000 (670.22282608 units. 
Total spent: $199.78001999)

- 2019 May 10 14:40 XRPUSDT SELL   
0.29801 @ $0.29801 (-0.02%) sell signal >> 1370 ticks 
>> [Min: $0.29112999 (-2.33%) - Max: $0.30133999 (1.09%)]	 
Holdings ~$496.11674953 USDT

...
Approximate holdings: $447.66581896401414 USDT
```

Notice at the very end of the last line: `Approximate holdings ~$447.66581896401414 USDT` - we'd lose half of our money in one year with that great strategy!

### Refining to the simulation

Before we give up on our initial strategy, it's a good idea to look to closer to see
what went wrong. How many trades were positive? How many negative? We can answer that by adding another
listener to the simulation, the [SimpleStrategyStatistics](./univocity-trader-core/src/main/java/com/univocity/trader/notification/SimpleStrategyStatistics.java):

```java
SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
simulation.listeners().add(stats);

simulation.run();

stats.printTradeStats();
```

If you run the simulation again, you should see a bit more of information at the end:

```
===[  results using parameters: {} ]===
Negative: 18 trades, avg. loss: -14.26%
Positive: 55 trades, avg. gain: +2.91%
Returns : -55.19%
```

So most of the trades were positive, the problem with our strategy is that when it loses, it's really really bad.

Now, how many of these trades are relevant? Look back at the logs and you'll see some that make no sense, for example:

```
2018 Jul. 02 00:14 XRPUSDT BUY      0.45266 @ $0.45266000 (0.22288083 units. Total spent: $0.10088923)
```

No exchange in the world allows you to spend 10 cents on a trade. They establish a minimum investment amount and 
we should include that to simulation to prevent adding noise to our results.

The following configures how many assets minimum a trade can have (i.e. the strategy must buy at least 100 ADA, 0.001 BTC, and so on): 

```java
simulation.symbolInformation("ADAUSDT")
 .minimumAssetsPerOrder(100.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);

simulation.symbolInformation("BTCUSDT")
 .minimumAssetsPerOrder(0.001).priceDecimalPlaces(8).quantityDecimalPlaces(8);

simulation.symbolInformation("LTCUSDT")
 .minimumAssetsPerOrder(0.1).priceDecimalPlaces(8).quantityDecimalPlaces(8);

simulation.symbolInformation("XRPUSDT")
 .minimumAssetsPerOrder(50.0).priceDecimalPlaces(8).quantityDecimalPlaces(2);

simulation.symbolInformation("ETHUSDT")
 .minimumAssetsPerOrder(0.01).priceDecimalPlaces(8).quantityDecimalPlaces(8); 
```

With this we try to prevent buying anything for under 10 dollars (roughly). Now the log should print something like the following at the end:

```
Approximate holdings: $538.4896701047184 USDT
===[  results using parameters: {} ]===
Negative: 6 trades, avg. loss: -18.07%
Positive: 29 trades, avg. gain: +2.69%
Returns : -46.10%
```

Which means half of the trades shown earlier were simply unrealistic and just added noise.

> Never get lazy when testing a trading strategy. If you are careless 
> you will get unrealistic results very quickly.

Maybe adding some sort of stop loss here would help to prevent letting a trade lose too much money.
We use a [StrategyMonitor](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/StrategyMonitor.java) 
to monitor open orders and to control what should happen on every tick 
after the algorithm opens a position. In our simple example, we can override the `handleStop` method.
It receives the current `Signal` generated from a given `Strategy` for the current candle.
If your implementation decides it's time to exit the position, return a `String`  with a message explaining the reason
(that can be later on be logged or e-mailed so you can know why a trade was stopped) 

```java
public abstract class StrategyMonitor extends IndicatorGroup {

 protected Trader trader;
 
 public String handleStop(Signal signal, Strategy strategy) {
  return null;
 }
 
 public boolean discardBuy(Strategy strategy) {
  return null;
 }
 
 ... //other methods that can also be overridden
}
```

We can also use indicators with a [StrategyMonitor](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/StrategyMonitor.java)
to help determine your actions. 
[Here](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/ExampleStrategyMonitor.java) 
is a simple implementation:

```java
public class ExampleStrategyMonitor extends StrategyMonitor {

 private final Set<Indicator> indicators = new HashSet<>();
 
 //use the InstantaneousTrendline indicator to hit us about the current trend
 private final InstantaneousTrendline trend;
 
 //if we lose money on a trade, will wait for the trend to give a BUY signal before buying again.
 private boolean waitForUptrend = false;
 
 public ExampleStrategyMonitor() {
  //check the trend on the 25 minute time scale
  indicators.add(trend = new InstantaneousTrendline(TimeInterval.minutes(25)));
 }
 
 @Override
 protected Set<Indicator> getAllIndicators() {
  return indicators;
 }
 
 @Override
 public String handleStop(Signal signal, Strategy strategy) {
  //current profit or loss %
  double currentReturns = trader.getChange();
  //best profit % (can only be 0% or more)
  double bestReturns = trader.getMaxChange(); 
                                             
  //if we are down 2% from the best ever profit generated
  if (currentReturns - bestReturns < -2.0) { 
   //if we are losing money
   if (currentReturns < 0.0) { 
    waitForUptrend = true;
    return "stop loss";
   }
   return "exit with some profit"; //else we made money (though we let 2% slip)
  }
  return null; //hold on to the trade
 }
 
 @Override
 public boolean discardBuy(Strategy strategy) {
  //we need at least two 25 minute candles on the uptrend
  if(trend.getSignal(trader.getCandle()) == Signal.BUY) {
   waitForUptrend = false;
  }

  return waitForUptrend; //ignore buy signals until we are on an uptrend
 }
}
```

Let's add the monitor to our simulation to see what happens:

```java
simulation.monitors().add(ExampleStrategyMonitor::new);
```

And we can finally see some improvement (and profits):

```
Approximate holdings: $1181.8492949136973 USDT
===[  results using parameters: {} ]===
Negative: 40 trades, avg. loss: -1.59%
Positive: 36 trades, avg. gain: +2.30%
Returns : 18.30%
```

# Optimizing 

Now that we found a strategy that kinda looks like it can make money, we can try to optimize it
to see if we can squeeze more profits out of it.

One basic method of optimization is to update the parameters used by the strategy. You can
build lists of [Parameters](./univocity-trader-core/src/main/java/com/univocity/trader/simulation/Parameters.java)
to try and see how returns would change. For example, in the 
[ExampleStrategy](/univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/ExampleStrategy.java),
change the constructor to:

```java
public ExampleStrategy(Parameters params) {
 int length = 12; //default bollinger length
 int interval = 5; //default interval   
 //if we receive parameters as an array of integers
 if (params instanceof IntParameters) { 
  int[] p = ((IntParameters) params).params;
  length = p[0];
  interval = p[1];
 }
 
 indicators.add(boll5m = new BollingerBand(length, TimeInterval.minutes(interval))); //instantiate using the given parameters
 indicators.add(boll1h = new BollingerBand(TimeInterval.hours(1)));
}
```

We can now change the [MarketSimulation](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/MarketSimulation.java)
code to generate a set of parameters to see how better (or worse) the results will be:


```java
// comment out this this line
// simulation.strategies().add(ExampleStrategy::new);
// and replace with:
 
simulation.strategies().add((symbol, params) -> new ExampleStrategy(params));

// Also remove this as we are not interested in seeing each and every trade
// simulation.listeners().add(new OrderExecutionToLog());

// Load all candles in memory so the simulation won't have to query the database so often 
simulation.setCachingEnabled(true);

// Remove this line
// simulation.run();

// Run the simulation on a loop that generates different parameters for
// the time interval and the length of the bollinger band in the ExampleStrategy

// testing from 1 minute to 15 minute time frames                                                     
for (int interval = 1; interval <= 15; interval++) {
 // with length varying from 5 to 25 bars 
 for (int length = 5; length <= 25; length++) {
  
  simulation.listeners().clear(); //remove previous stats
  
  SimpleStrategyStatistics stats = new SimpleStrategyStatistics();
  simulation.listeners().add(stats);
  
  //reset to acount balance to 1000.00 on every loop
  simulation.account().resetBalances().setAmount("USDT", 1000.0);
  
  //execute simulation with  
  simulation.run(new IntParameters(length, interval));
  
  stats.printTradeStats();
 }
}
```


The simulation will run for some time, and you should find some combination of parameters
that apparently produce better returns than the original ones we had, namely:

```java
===[  results using parameters: [15, 4] ]===
Negative: 44 trades, avg. loss: -1.60%
Positive: 39 trades, avg. gain: +2.44%
Returns : 24.65%
Real time trading simulation from 2018-07-01T00:00 to 2019-07-01T00:00
XRP = $0.00
USDT = $1222.86
```

You can now repeat the process to test other parameters in the strategy or the strategy monitor.

Remember that this is an introductory example. You should ideally generate statistics for your
particular with the help your own custom [OrderEventListener](./univocity-trader-core/src/main/java/com/univocity/trader/notification/OrderEventListener.java)
implementations.

# Improving backtesting performance

We built this code to enable running backtests and optimizations very efficiently using all cores of your CPU
which is ideal for machine learning or simple brute force parameter testing. We built a separate small library that makes use of that
capability which will not be open-sourced, as we intend to sell it to help us continue developing this framework.
 
If you are familiar with core java you should be able to implement faster backtests for yourself, but if
you don't want to waste time on it, or just are not familiar enough with concurrent programming in Java,
you can buy our backtesting solution for only US$ 79.00. Just send an e-mail directly to jbax@univocity.com and 
I'll help you out.

# Trading live

Once you are satisfied with your strategy you might decide to start trading. All you need to do 
is to create an instance of a supported exchange (i.e. anything that implements interface [Exchange](/home/jbax/dev/repository/univocity-trader/univocity-trader-core/src/main/java/com/univocity/trader/Exchange.java)).

At this moment, we have built-in support [Binance](https://www.binance.com/en/register?ref=36767892). We
suggest you to create a new trading account using the link above, and only add funds dedicated for your strategy.

I had issues buying crypto directly on Binance, so I suggest using [Coinmama](https://go.coinmama.com/visit/?bta=56730&brand=coinmama)
to buy your crypto quickly and without major hassles. 

Class [LiveBinanceTrader](./univocity-trader-binance-example/src/main/java/com/univocity/trader/exchange/binance/example/LiveBinanceTrader.java) 
has code you'd be using to trade with the example strategy shown earlier:

```java
public static void main(String... args) {
       
 BinanceTrader binance = new BinanceTrader(TimeInterval.minutes(1), getEmailConfig()); //gets ticks every 1 minute
 
 String apiKey = "<YOUR BINANCE API KEY>"; //your binance account API credentials
 String secret = "<YOUR BINANCE API SECRET>";
 
 //set an e-mail and timezone here to get notifications to your e-mail every time a BUY or SELL happens.
 //the timezone is required if you want to host this in a server outside of your local timezone
 //so the time a trade happens will come to you in your local time and not the server time 
 Client client = binance.addClient("<YOUR E-MAIL>", ZoneId.systemDefault(), "USDT", apiKey, secret);
 
 
 client.tradeWith("BTC", "ETH", "XRP", "ADA");
 
 client.strategies().add(ExampleStrategy::new);
 client.monitors().add(ExampleStrategyMonitor::new);
 
 //limit 20 dollars per trade here
 
 client.account().maximumInvestmentAmountPerAsset(20);
 
 //you set an OrderManager to manipulate an order before it is sent to the exchange for execution
 //the code below will change the price of the order so it won't be filled (in case you want to see how the program behaves) 
 client.account().setOrderManager(new DefaultOrderManager() {
  @Override
  public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
   switch (order.getSide()) {
    case BUY:
     order.setPrice(order.getPrice().multiply(new BigDecimal("0.9"))); //10% less
     break;
    case SELL:
     order.setPrice(order.getPrice().multiply(new BigDecimal("1.1"))); //10% more
   }
  }
 });
 
 //let's also log every trade.
 client.listeners().add(new OrderExecutionToLog());
 binance.run();
}

```

Before you execute this class, we suggest you to enable the `trace` log level 
in the [logback.xml](./univocity-trader-binance-example/src/main/resources/logback.xml) file:

```xml
<configuration>
 ....
 </appender>
 
 <root level="trace">
  <appender-ref ref="STDOUT"/>
  <!--<appender-ref ref="FILE" />-->
 </root>
</configuration>
```

This will show more useful details on the logs for the live environment.

Don't forget to pass along the your e-mail server details so you can receive e-mails (gmail works great for that)

```java
private static final MailSenderConfig getEmailConfig() {
 MailSenderConfig out = new MailSenderConfig();
 
 out.setReplyToAddress("dev@univocity.com");
 out.setSmtpHost("smtp.gmail.com");
 out.setSmtpTlsSsl(true);
 out.setSmtpPort(587);
 out.setSmtpUsername("<YOU>@gmail.com");
 out.setSmtpPassword("<YOUR SMTP PASSWORD>".toCharArray());
 out.setSmtpSender("<YOU>>@gmail.com");
 
 return out;
} 
```

That's it for now, I hope you have fun and become rich soon. 

Please consider <a class="github-button" href="https://github.com/sponsors/jbax" data-icon="octicon-heart" aria-label="Sponsor @jbax on GitHub">sponsoring</a> univocity-trader if 
you find it useful, any contribution will help me a lot to continue working on the
improvement of this project.

Thank you!

# We build custom strategies for you

If you are a trader looking for a coder to implement your strategy, send an e-mail to jbax@univocity.com
and I'll gladly work with you.

## More to come (a.k.a. ROADMAP)
 
This is my personal TODO list of what is going to come to this library (in order):
 
 * improve tutorial.
 
 * add javadocs - right now there is no documentation anywhere. Every class/method/interface here
 NEEDS to have proper javadocs.
 
 * simulate order filling on backtesting. Right now it just assumes and immediate fill when a buy or sell is made.
 
 * release the first stable version to maven central. 
 
 * support additional functions such as: placing and managing stops, shorting, margin, etc. Right now the library only buys then sells. 
 
 * add more indicators - we are trying to migrate everything already implemented by [ta4j](https://github.com/ta4j/ta4j), and will add more.
 
 * support more exchanges - we are planning to have many other sub-projects like [univocity-trader-binance](./univocity-trader-binance) here.
 
## Special thanks to

 * [ta4j](https://github.com/ta4j/ta4j) from where I adapted some indicators (and tests) from.
 
 * [binance-java-api](https://github.com/binance-exchange/binance-java-api) to integrate with [Binance](https://www.binance.com/en/register?ref=36767892). 
   I [copied the code over](./univocity-trader-binance/src/main/java/com/univocity/trader/exchange/binance/api/client) 
   and made some changes for improved network stability and ease of use for myself.

 
## Bugs, contributions & support

If you find a bug, please report it on github or send us an email on dev@univocity.com.

We try out best to eliminate all bugs as soon as possible and we do our best to answer all questions. Enhancements/suggestions are implemented on a best effort basis.

Fell free to submit your contribution via pull requests. Any little bit is appreciated.

If you need support or are looking for someone who can code your trading strategy, please contact me directly on jbax@univocity.com.

# Thank you for using our project!

Please consider <a class="github-button" href="https://github.com/sponsors/jbax" data-icon="octicon-heart" aria-label="Sponsor @jbax on GitHub">sponsoring</a> univocity-trader or [![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=JKH3JNHLL4Y42&source=url) any amount via PayPal, or Bitcoin on the following address:

 * 3BcmUPTPfLDuYWWSBxGKkChkq5WMzC94J6

Thank you!

The univocity team.