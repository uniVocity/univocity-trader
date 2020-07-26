package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.fasterxml.jackson.annotation.*;
import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;
import java.util.stream.*;

/**
 * Response returned when placing a new one-cancels-other order on the system.
 *
 * @see NewOCOOrderResponse for the request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewOCOOrderResponse implements OrderDetails {


	List<NewOrderResponse> orderReports;

	public void setOrderReports(List<NewOrderResponse> orderReports) {
		this.orderReports = orderReports;
	}

	@JsonIgnore
	private NewOrderResponse limitMakerOrderResponse;
	
	private NewOrderResponse getLimitMaker(){
		if (limitMakerOrderResponse == null){
			this.limitMakerOrderResponse = orderReports.stream().filter(p -> p.getType() == OrderType.LIMIT_MAKER).findFirst().get();
		}
		
		return this.limitMakerOrderResponse;
		
	}
	public String getSymbol() {
		return getLimitMaker().getSymbol();
	}


	public Long getOrderId() {
		return
				getLimitMaker().getOrderId();
	}

	public String getClientOrderId() {

		return getLimitMaker().getClientOrderId();
	}

	public Long getTransactTime() {
		return
				getLimitMaker().getTransactTime();
	}


	public String getPrice() {
		return
				getLimitMaker().getPrice();
	}

	public double getPriceAmount() {
		return  Double.parseDouble(getLimitMaker().getPrice());
	}

	public String getOrigQty() {
		return getLimitMaker().getOrigQty();
	}

	public double getOrigQtyAmount() {
		return Double.parseDouble(getLimitMaker().getOrigQty());
	}

	public String getExecutedQty() {
		return
				getLimitMaker().getExecutedQty();
	}

	public String getCummulativeQuoteQty() {
		return getLimitMaker().getCummulativeQuoteQty();
	}

	public OrderStatus getStatus() {
		return getLimitMaker().getStatus();
	}


	public TimeInForce getTimeInForce() {
		return getLimitMaker().getTimeInForce();
	}

	public OrderType getType() {
		return getLimitMaker().getType();
	}

	public OrderSide getSide() {
		return getLimitMaker().getSide();
	}


	public List<Trade> getFills() {
		return getLimitMaker().getFills();
	}

	@Override
	public Long getTime() {
		return getLimitMaker().getTransactTime();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("orders", orderReports)
				.toString();
	}

	public double getUnitPrice() {
		return orderReports.iterator().next().getUnitPrice();
	}


	@Override
	public List<OrderDetails> getAttachments() {
		List<OrderDetails> orderDetails = new LinkedList<>(orderReports);
		return orderDetails;
	}
}
