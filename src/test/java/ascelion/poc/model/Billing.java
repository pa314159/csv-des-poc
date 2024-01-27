package ascelion.poc.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.*;

@JsonPropertyOrder({ "column", "row" })
@Getter
@Setter
@ToString(includeFieldNames = false)
public class Billing {
	private String name;
	private String company;
	private String vatId;
}
