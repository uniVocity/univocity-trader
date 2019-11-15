CREATE TABLE candle
(
	symbol     VARCHAR(32)     NOT NULL,
	open_time  BIGINT          NOT NULL,
	close_time BIGINT          NOT NULL,
	open       DECIMAL(20, 10) NOT NULL,
	high       DECIMAL(20, 10) NOT NULL,
	low        DECIMAL(20, 10) NOT NULL,
	close      DECIMAL(20, 10) NOT NULL,
	volume     DECIMAL(20, 10) NOT NULL,

	CONSTRAINT candle_symbol_time UNIQUE (symbol, open_time, close_time)
)
	PARTITION BY KEY (symbol) PARTITIONS 1000
;

CREATE INDEX candle_symbol_idx ON candle (symbol) USING HASH;
CREATE INDEX candle_symbol_open_idx ON candle (symbol, open_time) USING BTREE;
CREATE INDEX candle_symbol_close_idx ON candle (symbol, close_time) USING BTREE;
CREATE INDEX candle_open_close_idx ON candle (symbol, open_time, close_time) USING BTREE;

# Once table has data, you can run this to keep all rows sorted in the database.
ALTER TABLE candle ORDER BY symbol, open_time, close_time;
