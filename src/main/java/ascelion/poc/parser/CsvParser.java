package ascelion.poc.parser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvParser<T> {
	interface Mutator<E> {
		void set(E target, Object value) throws IOException;
	}

	static class ReadAheadLineReader implements Closeable {
		private final BufferedReader input;
		private final Deque<String> ahead = new LinkedList<>();
		private boolean tail;

		ReadAheadLineReader(URL input, int skip, int size) throws IOException {
			this.input = new BufferedReader(new InputStreamReader(input.openStream(), StandardCharsets.UTF_8));

			String line = null;

			while (skip-- > 0 && (line = this.input.readLine()) != null) {

			}

			// read ahead one more to catch the EOF
			size++;

			while (size-- > 0 && (line = this.input.readLine()) != null) {
				this.ahead.add(line);
			}

			this.tail = line == null;
		}

		String nextLine() throws IOException {
			if (this.tail) {
				if (this.ahead.isEmpty()) {
					return null;
				} else {
					return this.ahead.poll();
				}
			} else {
				final var line = this.input.readLine();

				if (line == null) {
					this.tail = true;
				} else {
					this.ahead.add(line);
				}

				return this.ahead.poll();
			}
		}

		@Override
		public void close() throws IOException {
			this.input.close();
		}
	}

	private final CsvMapper mapper = new CsvMapper();

	private final BeanDescription beanDescription;
	private final JavaType javaType;

	private final Map<BeanDescription, Mutator<T>> header = new LinkedHashMap<>();
	private final Map.Entry<BeanDescription, Mutator<T>> content;
	private final Map<BeanDescription, Mutator<T>> footer = new LinkedHashMap<>();

	private final Map<JavaType, ObjectReader> readers = new HashMap<>();

	public CsvParser(Class<T> type) {
		this.mapper.findAndRegisterModules();

		final var deserializationConfig = this.mapper.getDeserializationConfig();

		this.mapper.setConfig(deserializationConfig);

		this.javaType = this.mapper.getTypeFactory().constructType(type);
		this.beanDescription = deserializationConfig.introspect(this.javaType);

		Map.Entry<BeanDescription, Mutator<T>> content = null;

		for (final BeanPropertyDefinition bpd : this.beanDescription.findProperties()) {
			var bd = deserializationConfig.introspect(bpd.getPrimaryType());

			if (content != null) {
				this.footer.put(bd, determineSetter(bpd));

				LOG.info("+footer {}: {}", bpd.getName(), bd.getBeanClass().getName());
			} else if (bd.getType().isTypeOrSubTypeOf(List.class)) {
				bd = deserializationConfig.introspect(bd.getType().getContentType());

				content = Map.entry(bd, determineAdder(bpd));

				LOG.info("+content {}: {}", bpd.getName(), bd.getBeanClass().getName());
			} else {
				this.header.put(bd, determineSetter(bpd));

				LOG.info("+header {}: {}", bpd.getName(), bd.getBeanClass().getName());
			}
		}

		this.content = content;

		this.beanDescription.findProperties().forEach(bi -> {});
	}

	private Mutator<T> determineSetter(BeanPropertyDefinition bpd) {
		final var mutator = bpd.getMutator();

		if (mutator == null) {
			LOG.warn("No mutator for {}", bpd.getName());

			return (t, v) -> {};
		}

		mutator.fixAccess(true);

		return mutator::setValue;
	}

	private Mutator<T> determineAdder(BeanPropertyDefinition bpd) {
		final var accessor = bpd.getAccessor();

		if (accessor == null) {
			LOG.warn("No accessor for {}", bpd.getName());

			return (t, v) -> {};
		}

		accessor.fixAccess(true);

		final var mutator = determineSetter(bpd);

		return (t, v) -> {
			var list = (List<Object>) accessor.getValue(t);

			if (list == null) {
				list = new ArrayList<>();

				mutator.set(t, list);
			}

			list.add(v);
		};
	}

	public T resolve(URL input, int skip) throws IOException {
		try (var ard = new ReadAheadLineReader(input, skip, this.footer.size())) {
			final var target = (T) this.beanDescription.instantiateBean(true);

			readHeader(target, ard);
			readContent(target, ard);
			readFooter(target, ard);

			return target;
		}
	}

	private void readHeader(T target, ReadAheadLineReader brd) throws IOException {
		for (final var ent : this.header.entrySet()) {
			readObject("header", brd, target, ent);
		}
	}

	private void readContent(T target, ReadAheadLineReader brd) throws IOException {
		while (!brd.tail) {
			readObject("content", brd, target, this.content);
		}
	}

	private void readFooter(T target, ReadAheadLineReader brd) throws IOException {
		for (final var ent : this.footer.entrySet()) {
			readObject("footer", brd, target, ent);
		}
	}

	private void readObject(String what, ReadAheadLineReader brd, T target, Map.Entry<BeanDescription, Mutator<T>> ent)
			throws IOException {
		final var object = readObject(what, ent.getKey(), brd);

		if (object != null) {
			ent.getValue().set(target, object);
		}
	}

	private Object readObject(String what, BeanDescription element, ReadAheadLineReader brd) throws IOException {
		final var line = brd.nextLine();

		if (line == null) {
			return null;
		}

		final var object = readerFor(element.getType()).readValue(line);

		LOG.debug("[{}]: {} / {}", what, line, object);

		return object;
	}

	private final ObjectReader readerFor(JavaType type) {
		return this.readers.computeIfAbsent(type, t -> this.mapper
				.readerFor(t)
				.with(this.mapper.schemaFor(t)));
	}
}
