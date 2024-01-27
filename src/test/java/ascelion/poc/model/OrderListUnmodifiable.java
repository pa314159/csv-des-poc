package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

//CSV module defaults to alphabetic ordering so this is important
@JsonPropertyOrder({ "summary", "items", "billing", "billingAddress", "deliveryAddress" })
@Getter
@Setter
@ToString(includeFieldNames = false)
public class OrderListUnmodifiable {
	private OrderSummary summary;
	private Billing billing;
	private Address deliveryAddress;
	private Address billingAddress;
	private final List<OrderItem> items = new ArrayList<>();
}
