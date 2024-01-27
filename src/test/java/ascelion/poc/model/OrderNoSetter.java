package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

//CSV module defaults to alphabetic ordering so this is important
@JsonPropertyOrder({ "summary", "items", "billing", "billingAddress", "deliveryAddress" })
@Getter
@ToString(includeFieldNames = false)
public class OrderNoSetter {
	private OrderSummary summary;
	private Billing billing;
	private Address deliveryAddress;
	private Address billingAddress;
	private List<OrderItem> items;
}
