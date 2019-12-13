package com.univocity.trader.datasource;

import javax.sql.DataSource;

/**
 * @author tom@khubla.com
 */
public interface DataSourceFactory {
   DataSource getDataSource();
}
