package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

import lombok.*;

@JsonPropertyOrder({ "code", "name", "quantity", "price" })
@Getter
@Setter
@ToString(includeFieldNames = false)
public class OrderItem {
	private String name;
	private String code;
	private BigDecimal price;
	private BigDecimal quantity;
}
