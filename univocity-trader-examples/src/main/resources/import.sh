#!\bin\sh

java -Djava.library.path=lib -cp "*:lib/*" com.univocity.trader.Main --exchange=Binance --backfill --config=config/univocity-trader.properties
 