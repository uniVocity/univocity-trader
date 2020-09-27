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
quickly adapt the sample code under [univocity-trader-examples](./univocity-trader-examples) to start trading live.

The following guide will show you how to get started using [Binance](https://www.binance.com/en/register?ref=36767892) to populate then
update a database of historical data and start trading.

## Setting up

Make sure you have the following installed:

 * Java 14
 
 * MySQL or MariaDB (you can change this to something else, just adapt the database setup instructions below to your needs)
 
 * Apache maven (if your IDE doesn't come with it) 

With that ready you can clone this repository and open it in your favorite IDE, with all sub-projects.

### Database setup

Create a database in `MySQL` to store candles (and statistics generated from your backtests):

```sql
CREATE DATABASE trading;

USE trading;
```

Then create the [candle](./univocity-trader-core/src/main/resources/db/mysql/candle.sql)
and [gap](./univocity-trader-core/src/main/resources/db/mysql/gap.sql) tables 
defined [here](./univocity-trader-core/src/main/resources/db/mysql).

Let's quickly look at the [MarketHistoryLoader](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/MarketHistoryLoader.java)
class, which will connect to [Binance](https://www.binance.com/en/register?ref=36767892) and pull the market history of Bitcoin of the last 6 months in 1 minute candles.

```java
public static void main(String... args) {
  Binance.Simulator simulator = Binance.simulator();
  Simulation simulation = simulator.configure().simulation();

//configure to update historical data in database going back 2 years from today.
  simulation.backfillYears(2);

// pulls any missing candlesticks from the exchange 
// and store them in our local database.
// This runs over stored candles backwards and will try to fill
// any gaps until the date 2 years ago from today is reached.
// Pulls one-minute candles by default.
  simulator.backfillHistory("BTCUSDT", "ADAUSDT");
}

```

Once the database connection is configured with your particular details, you can execute the `main` method.

The logs should print something like this:

```
[main] INFO (CandleRepository.java:268) - Looking for gaps in history of BTCUSDT from 2019 May 13 09:30
[BTCUSDT candle reader] DEBUG (CandleRepository.java:153) - Executing SQL query: [SELECT open_time, close_time, open, high, low, close, volume FROM candle WHERE symbol = 'BTCUSDT' AND open_time >= 1557705600000 AND close_time <= 1573633279655 ORDER BY open_time]
[main] WARN (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 264819 minutes between 2019 May 13 09:30 and 2019 Nov. 13 08:09
[main] WARN (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 141 minutes between 2019 Nov. 13 12:29 and 2019 Nov. 13 14:50
[main] WARN (CandleRepository.java:307) - Historical data of BTCUSDT has a gap of 4 minutes between 2019 Nov. 13 15:59 and 2019 Nov. 13 16:03
[main] INFO (CandleRepository.java:319) - Filling 267 gaps in history of BTCUSDT
```

This will take a long while. To help you and to avoid abusing Binance's servers, 
the historical data I collected for all symbols listed in `ALL_PAIRS` are available [here](https://drive.google.com/file/d/1bdg064knXHOgO8w-emp1tDYYD_upAmq2/view?usp=sharing).
 You can download this file, unpack it (2.5 GB of disk space required) then restore the backup with:

```
mysql -u <user> -p trading < db.sql
```  

> NOTE: if you get "too many open files" errors later on, restart mysql after restoring the database.

After the backup is restored, the [MarketHistoryLoader](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/MarketHistoryLoader.java)
should finish rather quickly. Re-execute this class whenever you want to update your local database
with the latest data from Binance.

> NOTE: if you get database authentication errors after restoring the backup, execute the following commands on MySQL console:
> 
> ```
> GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost'; 
> FLUSH PRIVILEGES;
> ```

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
 
  default Trade.Side tradeSide() {
  	return null;
  }
}
```

A  [Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java) that only generates 
signals relevant for shorting has to return `Trade.Side.SHORT` when `tradeSide()` is called by the framework.

For signals relevant only for regular (long) trades, `tradeSide()` must return `Trade.Side.LONG`. If the same strategy
can work for both long and short trades, simply let the default implementation do its job and return `null`.

> NOTE: Shorting is disabled by default, to enable it, invoke `enableShorting()` from your  
> [AccountConfiguration](./univocity-trader-core/src/main/java/com/univocity/trader/config/AccountConfiguration.java).
> You may also want to adjust the margin reserve percentage with `account.marginReservePercentage(<percentage>)`, which
> defaults to 150%. This means that if you short 50 EUR on pair EURUSD, the proceeds of the short will be locked away,
> along with 50% of the value of that short. So the USD required to buy 25 EUR at the time this short fills will
> be taken out of the free USD amount of your account and locked away until the short position is covered.

During simulations or live trading, every single candle received will be sent to your 
[Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java)'s `getSignal` method. 
Once your [Strategy](./univocity-trader-core/src/main/java/com/univocity/trader/strategy/Strategy.java) returns
`BUY`, an [Order](./univocity-trader-core/src/main/java/com/univocity/trader/account/Order.java) will be made to buy the instrument at the given price.
If `SELL` is returned, any open [Order](./univocity-trader-core/src/main/java/com/univocity/trader/account/Order.java) will be sold at the current price.
Anything else will be ignored.

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

Let's create the [ExampleStrategy](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/ExampleStrategy.java) 
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
  // on the 5 minute time frame, the lowest price 
  // of the candle is above the lower band.
  if (candle.low > boll5m.getLowerBand()) {      
  // still on the 5 minute time frame, the close 
  // price of the candle is under the middle band
  if (candle.close < boll5m.getMiddleBand()) {
   // if the slope of the 5 minute bollinger band
   // is starting to point up, BUY 
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

It's an abstract class and your each exchange should provide you the concrete implementation like
demonstrated below using class [Binance](./univocity-trader-binance/src/main/java/com/univocity/trader/exchange/binance/Binance.java):

```java
Binance.Simulator simulator = Binance.simulator();

//you can test with one or more accounts at the same time
Account account = simulator.configure().account();

account
  .referenceCurrency("USDT") //Balances will be calculated using the reference currency.
  .tradeWith("BTC", "ADA", "LTC", "XRP", "ETH")
  .minimumInvestmentAmountPerTrade(10.0)

Simulation simulation = simulator.configure().simulation();
simulation.initialFunds(1000.0)
  .tradingFees(SimpleTradingFees.percentage(0.1))
  .emulateSlippage() //try to replicate a real trading environment
  .simulateFrom(LocalDate.of(2018, 7, 1).atStartOfDay())
  .simulateTo(LocalDate.of(2019, 7, 1).atStartOfDay());

...
```

Each one of the symbols given in `tradeWith` will be paired against the reference currency, i.e. the 
[MarketSimulator](./univocity-trader-core/src/main/java/com/univocity/trader/simulation/MarketSimulator.java)
will trade pairs BTCUSDT, ADAUSDT, LTCUSDT, XRPUSDT and ETHUSDT.

You can also define additional trading pairs such as ADABTC by adding the line:

```java
account.tradeWithPair("ADA", "BTC");
```

NEVER forget trading fees. Each buy/sell on an exchange will typically cost you a fraction
of the position at stake. In the above we use what [Binance](https://www.binance.com/en/register?ref=36767892) charges
new accounts: **0.1%** for any type of trade. 

```java
simulation
  .tradingFees(SimpleTradingFees.percentage(0.1))
```

Add our [ExampleStrategy](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/ExampleStrategy.java)
to the simulation. Note that the framework **forces** you to provide functions/suppliers that create new instances
as required.

```java
account.strategies()
  .add(ExampleStrategy::new);
```

If you want to see the action, log trades made by the simulator with:

```java
account.listeners()
  .add(new OrderExecutionToLog())
```

Finally we can execute the simulation with:
```java
simulator.run();
```

Here we test with data from July 2018 to July 2019, and you should see each trade when they happen as the
[OrderExecutionToLog](./univocity-trader-core/src/main/java/com/univocity/trader/notification/OrderExecutionToLog.java) 
will print each trade made to the log, you may also log to a Google Sheet via [OrderExecutionToGoogleSheet](./univocity-trader-core/src/main/java/com/univocity/trader/notification/OrderExecutionToGoogleSheet.java).

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
account.listeners()
  .add(new OrderExecutionToLog())
  .add((symbol)->new SimpleStrategyStatistics(symbol))
;

simulation.run();
```

If you run the simulation again, you should see a bit more of information at the end:

```
===[ results using parameters: {} ]===
Negative: 18 trades, avg. loss: -14.26%
Positive: 55 trades, avg. gain: +2.91%
Returns : -55.19%
```

So most of the trades were positive, the problem with our strategy is that when it loses, it's really really bad.

Now, how many of these trades are relevant? Look back at the logs and you'll see some that make no sense, for example:

```
2018 Jul. 02 00:14 XRPUSDT BUY   0.45266 @ $0.45266000 (0.22288083 units. Total spent: $0.10088923)
```

No exchange in the world allows you to spend 10 cents on a trade. They establish a minimum investment amount and 
we should include that to simulation to prevent adding noise to our results.

So let's add this line:

```
account
    .minimumInvestmentAmountPerTrade(10.0);
```

With this we try to prevent buying anything for under 10 dollars. Now the log should print something like the following at the end:

```
Approximate holdings: $538.4896701047184 USDT
===[ results using parameters: {} ]===
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
If your implementation decides it's time to exit the position, return a `String` with a message explaining the reason
(that can be later on be logged or e-mailed so you can know why a trade was stopped) 

```java
public abstract class StrategyMonitor extends IndicatorGroup {

 protected Trader trader;
 
 public String handleStop(Trade trade, Signal signal, Strategy strategy) {
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
[Here](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/ExampleStrategyMonitor.java) 
is a simple implementation:

```java
public class ExampleStrategyMonitor extends StrategyMonitor {

 private final Set<Indicator> indicators = new HashSet<>();
 
 // use the InstantaneousTrendline indicator to 
 // hint us about the current trend
 private final InstantaneousTrendline trend;
 
 // if we lose money on a trade, will wait for the 
 // trend to give a BUY signal before buying again.
 private boolean waitForUptrend = false;
 
 public ExampleStrategyMonitor() {
 // check the trend on the 25 minute time scale
 indicators.add(trend = new InstantaneousTrendline(TimeInterval.minutes(25)));
 }
 
 @Override
 protected Set<Indicator> getAllIndicators() {
 return indicators;
 }
 
 @Override
 public String handleStop(Trade trade, Signal signal, Strategy strategy) {
 // current profit or loss %
 double currentReturns = trade.priceChangePct();
 // best profit % (can only be 0% or more)
 double bestReturns = trade.getMaxChange(); 
                       
 // if we are down 2% from the best ever profit generated
 if (currentReturns - bestReturns < -2.0) { 
  // if we are losing money
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
account.monitors()
  .add(ExampleStrategyMonitor::new);
```

And we can finally see some improvement (and profits):

```
Approximate holdings: $1181.8492949136973 USDT
===[ results using parameters: {} ]===
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
[ExampleStrategy](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/ExampleStrategy.java),
change the constructor to:

```java
public ExampleStrategy(Parameters params) {
 int length = 12; //default bollinger length
 int interval = 5; //default interval  
 //if we receive parameters as an array of integers
 if (params instanceof LongParameters lp) { 
 int[] p = lp.params;
 length = p[0];
 interval = p[1];
 }
 
 // instantiate using the given parameters
 indicators.add(boll5m = new BollingerBand(length, TimeInterval.minutes(interval)));
 indicators.add(boll1h = new BollingerBand(TimeInterval.hours(1)));
}
```

We can now change the [MarketSimulation](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/MarketSimulation.java)
code to generate a set of parameters to see how better (or worse) the results will be:


```java
// comment out this this line
// simulation.strategies().add(ExampleStrategy::new);

// and replace with:
account.strategies()
  .add((symbol, params) -> new ExampleStrategy(params));

// Also remove the OrderExecutionToLog as we are 
// not interested in seeing each and every trade
account.listeners()
//  .add(new OrderExecutionToLog())
    .add((symbol)->new SimpleStrategyStatistics(symbol))
;

// keeps all candles in memory so the simulation won't
// have to query the database to test each parameter set
simulation.cacheCandles(true);

// testing from 1 minute to 15 minute time frames
for (int interval = 1; interval <= 15; interval++) {
  // with length varying from 5 to 25 bars
  for (int length = 5; length <= 25; length++) {
    simulation.addParameters(new LongParameters(length, interval));
  }
}

//execute simulation
simulator.run();

}
```


The simulation will run for some time, and you should find some combination of parameters
that apparently produce better returns than the original ones we had, namely:

```java
===[ results using parameters: [15, 4] ]===
Negative: 44 trades, avg. loss: -1.60%
Positive: 39 trades, avg. gain: +2.44%
Returns : 24.65%
Real time trading simulation from 2018-07-01T00:00 to 2019-07-01T00:00
XRP = $0.00
USDT = $1222.86
```

You can now repeat the process to test other parameters in the strategy or the strategy monitor.

Remember that this is an introductory example. You should ideally generate statistics for your
particular with the help your own custom [OrderListener](./univocity-trader-core/src/main/java/com/univocity/trader/notification/OrderListener.java)
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
is to create an instance of a supported exchange (i.e. anything that implements interface [Exchange](./univocity-trader-core/src/main/java/com/univocity/trader/Exchange.java)).

At this moment, we have built-in support [Binance](https://www.binance.com/en/register?ref=36767892). We
suggest you to create a new trading account using the link above, and only add funds dedicated for your strategy.

I had issues buying crypto directly on Binance, so I suggest using [Coinmama](https://go.coinmama.com/visit/?bta=56730&brand=coinmama)
to buy your crypto quickly and without major hassles. 

Class [LiveBinanceTrader](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/LiveBinanceTrader.java) 
has code you'd be using to trade with the example strategy shown earlier:

```java
public static void main(String... args) {
  Binance.Trader trader = Binance.trader();

// If you want to receive e-mail notifications each time an order
// is submitted to the exchange, configure your e-mail sender
  trader.configure().mailSender()...

Account account = trader.configure().account()
  .email("<YOUR E-MAIL")
  .timeZone("system")
  .referenceCurrency("USDT")
  .apiKey("<YOUR BINANCE API KEY>")
  .secret("<YOUR BINANCE API SECRET>");

account.strategies().add(ExampleStrategy::new);
account.monitors().add(ExampleStrategyMonitor::new);
account.listeners().add(new OrderExecutionToLog());

// never invest more than 20 USDT on anything
account
  .tradeWith("BTC", "ETH", "XRP", "ADA")
  .maximumInvestmentAmountPerAsset(20)
;

// overrides the default order manager submit orders that 
// likely won't be filled so you can see what the program does.
account.orderManager(new DefaultOrderManager() {
  @Override
  public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
    switch (order.getSide()) {
      case BUY: //10% less
        order.setPrice(order.getPrice().multiply(new BigDecimal("0.9"))); 
        break;
      case SELL: //10% more
        order.setPrice(order.getPrice().multiply(new BigDecimal("1.1"))); 
    }
  }
});

//begin trading
trader.run();
}
```

Before you execute this class, we suggest you to enable the `trace` log level 
in the [logback.xml](./univocity-trader-examples/src/main/resources/logback.xml) file:

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

You can now run the [LiveBinanceTrader](./univocity-trader-examples/src/main/java/com/univocity/trader/examples/LiveBinanceTrader.java).

# Running from the command line

Instead of writing code to setup your simulation/live trading environment, you can provide a `properties`
file. This can be useful to execute multiple simulations at the same time on different computers.

On the [resources](./univocity-trader-examples/src/main/resources) folder of the `univocity-trader-examples`
you will find the [binance.properties](./univocity-trader-examples/src/main/resources/binance.properties), which
looks like this:

```properties
tick.interval=1m
#
# Database properties:
#
database.jdbc.driver=com.mysql.jdbc.Driver
database.jdbc.url=jdbc:mysql://localhost:3306/trading?autoReconnect=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull&useSSL=false
database.user=root
database.password=
#
# E-mail properties:
#
mail.reply.to=dev@univocity.com
mail.smtp.host=smtp.gmail.com
mail.smtp.ssl=true
mail.smtp.port=587
mail.smtp.username=<YOU>@gmail.com
mail.smtp.password=<YOUR SMTP PASSWORD>
mail.smtp.sender=<YOU>>@gmail.com
#
# Simulation properties:
#
simulation.start=2018-07-01
simulation.end=2019-07-01
simulation.cache.candles=false
simulation.initial.funds=[USDT]1000.0,[ADA;ETH]0.01
simulation.trade.fees=0.1%
simulation.parameters.file=
simulation.parameters.class=MyParametersClass
simulation.history.backfill=30d
simulation.order.fill=slippage

default.pairs=\
  ETH/USDT, ADA/USDT,\
  BNB/USDT

#
# Client-specific properties:
#
accounts=jbax,tom
jbax.email=jbax@univocity.com
jbax.timezone=America/New_York
jbax.reference.currency=USDT
jbax.strategies=ExampleStrategy
jbax.monitors=ExampleStrategyMonitor
jbax.listeners=OrderExecutionToLog,SimpleStrategyStatistics
jbax.api.key=abcd
jbax.api.secret=cdef
jbax.order.manager=[ADA;XRP]DefaultOrderManager
jbax.trade.minimum.amount=[ADA;XRP]10.5, [BTC]15, 10
jbax.trade.maximum.amount=
jbax.trade.maximum.percentage=
jbax.asset.maximum.amount=
jbax.asset.maximum.percentage=
jbax.asset.symbols=BTC,ADA,LTC,XRP,ETH
jbax.trade.pairs=${default.pairs}

tom.email=tom@univocity.com
tom.timezone=America/Edmonton
tom.reference.currency=USDT
tom.strategies=ExampleStrategy
tom.monitors=ExampleStrategyMonitor
tom.listeners=OrderExecutionToLog,SimpleStrategyStatistics
tom.api.key=12345
tom.api.secret=67890
tom.asset.symbols=BTC,ADA
tom.trade.pairs=${default.pairs}
```

Every configuration option available via code has a counterpart in the properties file. Once you build 
the [univocity-trader-examples](./univocity-trader-examples) sub-project, the `univocity-trader-examples.zip`
file will be generated. You can unpack this file and execute the 
[run.bat](./univocity-trader-examples/src/main/resources/run.bat) or 
[run.sh](./univocity-trader-examples/src/main/resources/run.sh) scripts
from where you unpacked the zip file, or simply `cd` into the `target` folder generated by maven, and
execute the scripts from there, for example:

>```
>cd univocity-trader-examples/target
>sh run.sh --exchange=Binance --simulate
>```

These script will invoke the [Main](./univocity-trader-core/src/main/java/com/univocity/trader/Main.java)
and load the configuration provided in the properties files.

You can also load a pre-configured properties file in code and change the values loaded, using:

```java
Binance.Trader trader = Binance.trader();
trader.configure().loadConfigurationFromProperties();

//configure further
...

trader.run();
``` 

That's it for now, I hope you have fun and become rich soon. 

Please consider <a class="github-button" href="https://github.com/sponsors/jbax" data-icon="octicon-heart" aria-label="Sponsor @jbax on GitHub">sponsoring</a> univocity-trader if 
you find it useful, any contribution will help me a lot to continue working on the
improvement of this project.

Thank you!

## More to come (a.k.a. ROADMAP)
 
This is my personal TODO list of what is going to come to this library (in order):

 * Implement enhancements listed in the [issues](https://github.com/uniVocity/univocity-trader/issues) page.
 I've marked many with `help wanted` and `good first issue` if you interested in contributing to the project
 and don't know where to look.  
 
 * add javadocs - right now there is not a lot of documentation. Every class/method/interface here
 NEEDS to have proper javadocs.
 
 * release the first stable version to maven central. 
 
 * support additional functions such as: placing and managing stops,  margin, etc. 
 
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

Fell free to submit your contribution via pull requests. Any little bit is appreciated. Any github issues marked as **help wanted**
are things we want to do but don't have time/resources yet. If you can take on that work by all means go for it!

If you need support or are looking for someone who can code your trading strategy, please contact me directly on jbax@univocity.com.

# Thank you for using our project!

Please consider <a class="github-button" href="https://github.com/sponsors/jbax" data-icon="octicon-heart" aria-label="Sponsor @jbax on GitHub">sponsoring</a> univocity-trader or [![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=JKH3JNHLL4Y42&source=url) any amount via PayPal, or Bitcoin on the following address:

 * 3BcmUPTPfLDuYWWSBxGKkChkq5WMzC94J6

Thank you!

The univocity team.