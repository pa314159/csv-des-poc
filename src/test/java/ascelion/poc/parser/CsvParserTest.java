package ascelion.poc.parser;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import ascelion.poc.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CsvParserTest {
	private static final String ORDER_CSV = "order.csv";

	private URL input;

	@BeforeEach
	void beforeEach() {
		this.input = currentThread().getContextClassLoader().getResource(ORDER_CSV);

		assertThat(this.input)
				.as(ORDER_CSV)
				.isNotNull();
	}

	@ParameterizedTest
	@ValueSource(classes = { Order.class, OrderNoSetter.class, OrderListUnmodifiable.class })
	void run(Class<?> type) throws IOException {
		final var parser = new CsvParser<>(type);
		final Object object = parser.resolve(this.input, 2);

		assertAll(
				() -> assertThat(object)
						.isInstanceOf(type),

				() -> assertThat(object)
						.usingRecursiveAssertion()
						.ignoringFieldsMatchingRegexes(".+\\.extra")
						.allFieldsSatisfy(Objects::nonNull),

				() -> assertThat(object)
						.usingRecursiveAssertion()
						.ignoringFieldsMatchingRegexes("^.+(?<!\\.extra)$")
						.allFieldsSatisfy(Predicate.not(Objects::nonNull)),

				() -> assertThat(object)
						.extracting("items", as(InstanceOfAssertFactories.LIST))
						.hasSize(3),

				() -> {});
	}
}
