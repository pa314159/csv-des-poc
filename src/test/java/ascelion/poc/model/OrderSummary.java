package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

import lombok.*;

@JsonPropertyOrder({ "postage", "promotion", "total", "vat" })
@Getter
@Setter
@ToString(includeFieldNames = false)
public class OrderSummary {
	private BigDecimal total;
	private BigDecimal postage;
	private BigDecimal vat;
	private BigDecimal promotion;
}
